## cluster
Node.js的单个实例在单个线程中运行。 要利用多核系统，用户有时会想启动一个Node.js进程集群来处理负载。
cluster模块可以轻松创建全部共享服务器端口的子进程。
```js
const cluster = require('cluster');
const http = require('http');
const numCPUs = require('os').cpus().length;

if (cluster.isMaster) {
  console.log(`Master ${process.pid} is running`);

  // Fork workers.
  for (let i = 0; i < numCPUs; i++) {
    cluster.fork();
  }

  cluster.on('exit', (worker, code, signal) => {
    console.log(`worker ${worker.process.pid} died`);
  });
} else {
  // Workers can share any TCP connection
  // In this case it is an HTTP server
  http.createServer((req, res) => {
    res.writeHead(200);
    res.end('hello world\n');
  }).listen(8000);

  console.log(`Worker ${process.pid} started`);
}
```
运行Node.js，所有的工作进程将共享8000端口
```bash
$ node server.js
Master 3596 is running
Worker 4324 started
Worker 4520 started
Worker 6056 started
Worker 5644 started
```
请注意，在Windows上，尚不可能在工作进程中设置命名管道服务。
### cluster如何工作
使用child_process.fork（）方法产生工作进程，以便它们可以通过IPC与父进程通信并来回传递服务器句柄。

cluster模块支持两种分配传入连接的方法。

第一种方法（除Windows以外的所有平台上的默认方法）都是循环法，即主进程在端口上进行侦听，接受新连接并以循环方式在工作进程之间进行分配，其中一些已构建智能避免过载工作进程。

第二种方法是主进程创建侦听套接字并将其发送给感兴趣的工作进程。工作进程然后直接接受传入连接。

理论上，第二种方法应该是最好的。然而，在实践中，由于操作系统调度器的变幻莫测，分配趋于非常不平衡。已经观察到负载超过70％的连接只有两个过程，总共有八个。

因为server.listen（）将大部分工作交给了主进程，所以在正常的Node.js进程和集群工作进程之间的行为有三种情况：

1. server.listen（{fd：7}）因为消息被传递给主进程，父进程中的文件描述符7将被监听，并且该句柄传递给工作进程，而不是监听工作进程的文件描述符7的引用。
2. server.listen（句柄）明确地监听句柄会导致工作进程使用提供的句柄，而不是与主进程通信。
3. server.listen（0）通常，这将导致服务器侦听随机端口。但是，在集群中，每个工作人员每次收听（0）时都会收到相同的“随机”端口。实质上，这个端口在第一时间是随机的，但此后可以预见。要监听唯一端口，请根据群集工作器ID生成端口号。

Node.js不提供路由逻辑。因此，设计一个应用程序非常重要，因为它不会过度依赖内存数据对象，如会话和登录等。

因为工作进程都是独立的进程，所以他们可以根据程序的需要被杀死或重新生成，而不会影响其他工作进程。只要有一些工作进程还活着，服务器将继续接受连接。如果没有工作进程活着，现有的连接将被丢弃，新的连接将被拒绝。但是，Node.js不会自动管理工作进程的数量。根据自己的需要管理工作进程池是应用程序的责任。

虽然cluster模块的主要用例是联网，但它也可以用于需要工作进程的其他用例。

### 类：Worker
新增于v0.7.0
Worker对象包含有关工作进程的所有公共信息和方法。 在主进程中可以使用cluster.workers获得。 在工作进程中，可以使用cluster.worker获得。
### Event: 'disconnect'
新增于v0.7.7
类似于cluster.on（'disconnect'）事件，但是特定于此worker。
```js
cluster.fork().on('disconnect', () => {
  // Worker has disconnected
});
```
### Event: 'error'
新增于v0.7.3
此事件与child_process.fork（）提供的事件相同。
在工作进程中，process.on（'error'）也可以被使用。
### Event: 'exit'
新增于v0.11.2
- code &lt;number&gt; 退出代码，如果退出正常。
- signal &lt;string&gt; 导致进程被终止的信号名称（例如'SIGHUP'）。
类似于cluster.on('exit')，但是只针对此worker
```js
const worker = cluster.fork();
worker.on('exit', (code, signal) => {
  if (signal) {
    console.log(`worker was killed by signal: ${signal}`);
  } else if (code !== 0) {
    console.log(`worker exited with error code: ${code}`);
  } else {
    console.log('worker success!');
  }
});
```
### Event: 'listening'
新增于v0.7.0
- address &lt;object&gt;
类似于 cluster.on('listening') 事件，但是只针对此 worker.
```js
cluster.fork().on('listening', (address) => {
  // Worker is listening
});
```
它不会在工作进程之间分发。
### Event: 'message'
新增于 v0.7.0
- message &lt;Object&gt;
- handle &lt;undefined&gt; | &lt;Objec&gt;
类似于cluster的'message'事件，但是只针对此 worker.
在worker中，process.on（'message'）也可以使用。
作为一个例子，下面是一个集群，它使用消息系统保持主进程中的请求数量：
```js
const cluster = require('cluster');
const http = require('http');

if (cluster.isMaster) {

  // 跟踪http请求
  let numReqs = 0;
  setInterval(() => {
    console.log(`numReqs = ${numReqs}`);
  }, 1000);

  // 计数请求数目
  function messageHandler(msg) {
    if (msg.cmd && msg.cmd === 'notifyRequest') {
      numReqs += 1;
    }
  }

  // 启动工作进程并且监听通知的请求消息。
  const numCPUs = require('os').cpus().length;
  for (let i = 0; i < numCPUs; i++) {
    cluster.fork();
  }

  for (const id in cluster.workers) {
    cluster.workers[id].on('message', messageHandler);
  }

} else {

  // 工作进程中创建http Server服务
  http.Server((req, res) => {
    res.writeHead(200);
    res.end('hello world\n');

    // 通知主进程关于这个请求
    process.send({ cmd: 'notifyRequest' });
  }).listen(8000);
}
```
### Event: 'online'
新增于 v0.7.0
类似于cluster.on('online')事件，但是只针对此 worker.
```js
cluster.fork().on('online', () => {
  // Worker is online
});
```
不会在其他工作进程中传递
### worker.disconnect()
<!-- YAML
added: v0.7.7
changes:
  - version: v7.3.0
    pr-url: https://github.com/nodejs/node/pull/10019
    description: This method now returns a reference to `worker`.
-->
| Version| Changes|
|-|-|
| v7.3.0| 这个方法现在会返回一个工作进程的引用|
|v0.7.7|新增于: v0.7.7 |

* 返回值: {cluster.Worker} 引用于 `worker`.

在一个工作进程中, 这个函数将关闭所有的服务, 这些服务将等待 `'close'` 事件,随后断开IPC频道。

在主进程中, 一个内部消息被发送到这个工作进程中将引起这个工作进程自己调用
`.disconnect()` 。

原因是 `.exitedAfterDisconnect`将被设置.

注意，一个服务关闭后它将不再接受新的连接，但是连接会被其他正在监听的工作进程接受。现有的连接照常允许关闭. 当没有进程存在时,
详见 [`server.close()`][], 通往该进程的IPC频道将被关闭。优雅的退出。

以上情况仅适用服务端连接，客户端的连接不会被工作进程自动关闭。并且断开连接不会等待客户端退出后才关闭。

注意 这些工作进程中，还有一个 `process.disconnect` 方法存在，但是工作进程中的disconnect方法不是这个方法。

因为上时间的服务端连接可能导致工作进程的disconnect方法阻塞，通过发送消息通知的方式也许可以，所以可能会采取特定于应用程序的操作
关闭它们。 通过设置超时来关闭工作进程, 有时`'disconnect'`事件触发后的一段时间仍然不能关闭工作进程，通过超时关闭工作进程。
```js
if (cluster.isMaster) {
  const worker = cluster.fork();
  let timeout;

  worker.on('listening', (address) => {
    worker.send('shutdown');
    worker.disconnect();
    timeout = setTimeout(() => {
      worker.kill();
    }, 2000);
  });

  worker.on('disconnect', () => {
    clearTimeout(timeout);
  });

} else if (cluster.isWorker) {
  const net = require('net');
  const server = net.createServer((socket) => {
    // 连接没有结束
  });

  server.listen(8000);

  process.on('message', (msg) => {
    if (msg === 'shutdown') {
      // 启动与服务器的任何连接的完美关闭
    }
  });
}
```

### worker.exitedAfterDisconnect
<!-- YAML
added: v6.0.0
-->

* {boolean}

通过调用 `.kill()` 或 `.disconnect()`触发， 在调用之前它一直是`undefined`.

这个布尔值[`worker.exitedAfterDisconnect`][] 可以用于区分是自发退出还是被动退出。主进程可以根据这个值决定是否衍生新的工作进程。

```js
cluster.on('exit', (worker, code, signal) => {
  if (worker.exitedAfterDisconnect === true) {
    console.log('Oh, it was just voluntary – no need to worry');
  }
});

// 杀死工作进程
worker.kill();
```

### worker.id
<!-- YAML
added: v0.8.0
-->

* {number}

每个新的工作进程都会被分配一个唯一的id,并存储与这个
`id`。

当工作进程还在是，这是cluster.workers里面的一个索引。

### worker.isConnected()
<!-- YAML
added: v0.11.14
-->

如果工作进程是通过IPC通道连接到主进程，则这个函数会返回`true`，
否则返回`false`，一个工作进程建立连接后会自动连接到主进程。在触发`'disconnect'`事件后断开连接。

### worker.isDead()
<!-- YAML
added: v0.11.14
-->

如果工作进程被终止这个函数会返回`true`(包括通过信号中止和退出)否则返回`false`。

### worker.kill([signal='SIGTERM'])
<!-- YAML
added: v0.9.12
-->

* `signal` {string} 发送给工作进程的中止信号的名称。
这个函数将关闭这个工作进程，在主进程中，他通过断开连接工作进程`worker.process`来完成 ，并且一旦断开连接，通过信号关闭工作进程，在工作进程中，他通过断开频道来完成,然后以代码0退出进程。

设置 `.exitedAfterDisconnect`属性。

为了向后兼容，此方法被命名为`worker.destroy（）`。

注意，在工作进程中 `process.kill()` 是存在的，但是不同于这个函数。
他是 [`kill`][].

### worker.process
<!-- YAML
added: v0.7.0
-->

* {ChildProcess}

所有工作进程都是使用child_process.fork（）创建的，这个方法返回的对象被存储为.process。 在一个工作进程中，process进程
被储存了。

看: [Child Process module][]

注意 工作进程将调用 `process.exit(0)` 如果 `'disconnect'` 时间发生
在 `process` 上并且 `.exitedAfterDisconnect` 不是 `true`。这样可以防止连接意外断开

### worker.send(message[, sendHandle][, callback])
<!-- YAML
added: v0.7.0
changes:
  - version: v4.0.0
    pr-url: https://github.com/nodejs/node/pull/2620
    description: The `callback` parameter is supported now.
-->

* `message` {Object}
* `sendHandle` {Handle}
* `callback` {Function}
* Returns: {boolean}

发送消息给工作进程或主进程，可选句柄。
在主进程中发送消息给指定的工作进程。等同于[`ChildProcess.send()`][].
工作进程发送消息给主进程等同于`process.send()`。
这个例子将输出所有来自主进程的信息。

```js
if (cluster.isMaster) {
  const worker = cluster.fork();
  worker.send('hi there');

} else if (cluster.isWorker) {
  process.on('message', (msg) => {
    process.send(msg);
  });
}
```

## Event: 'disconnect'
<!-- YAML
added: v0.7.9
-->

* `worker` {cluster.Worker}

工作进程与IPC频道断开连接后触发。这可能发生在一个工作进程优雅的退出、关闭或手动断开连接，（例如调用worker.disconnect()）

`disconnect`和`exit`事件之间可能存在延迟。 这些事件可以用来检测process是否停留在清理过程中，或者是否存在是长连接。

```js
cluster.on('disconnect', (worker) => {
  console.log(`The worker #${worker.id} has disconnected`);
});
```

## Event: 'exit'
<!-- YAML
added: v0.7.9
-->

* `worker` {cluster.Worker}
* `code` {number} 退出的编码，如果是正常退出.
* `signal` {string} 引起进程关闭的信号名称 (e.g. `'SIGHUP'`) 

当任何工作进程死亡时，cluster模块将发出`exit`事件。

这可以通过再次调用`.fork（）`来重新启动工作进程。

```js
cluster.on('exit', (worker, code, signal) => {
  console.log('worker %d died (%s). restarting...',
              worker.process.pid, signal || code);
  cluster.fork();
});
```

See [child_process event: 'exit'][].

## Event: 'fork'
<!-- YAML
added: v0.7.0
-->

* `worker` {cluster.Worker}

当一个新的工作进程通过cluster forked，cluster模块将触发`'fork'`事件
这可以用来记录工作进程的活动，并且创建自定义超时。

```js
const timeouts = [];
function errorMsg() {
  console.error('Something must be wrong with the connection ...');
}

cluster.on('fork', (worker) => {
  timeouts[worker.id] = setTimeout(errorMsg, 2000);
});
cluster.on('listening', (worker, address) => {
  clearTimeout(timeouts[worker.id]);
});
cluster.on('exit', (worker, code, signal) => {
  clearTimeout(timeouts[worker.id]);
  errorMsg();
});
```

## Event: 'listening'
<!-- YAML
added: v0.7.0
-->

* `worker` {cluster.Worker}
* `address` {Object}
在工作进程调用`listen()`后，这个`'listening'`事件就会被触发。
主线程的`cluster`的`'listening'`事件也会被触发。

这个事件回调包含两个参数，`worker`是一个工作进程对象，`address`对象包含以下连接属性`address`、 `port` 和 `addressType`，这对于监听超过一个ip地址是很有用的。

```js
cluster.on('listening', (worker, address) => {
  console.log(
    `A worker is now connected to ${address.address}:${address.port}`);
});
```

`addressType` 取值如下:

* `4` (TCPv4)
* `6` (TCPv6)
* `-1` (unix domain socket)
* `"udp4"` or `"udp6"` (UDP v4 or v6)

## Event: 'message'
<!-- YAML
added: v2.5.0
changes:
  - version: v6.0.0
    pr-url: https://github.com/nodejs/node/pull/5361
    description: The `worker` parameter is passed now; see below for details.
-->

* `worker` {cluster.Worker}
* `message` {Object}
* `handle` {undefined|Object}

当主线程集群从任何一个工作进程中接收到信息时触发。

详见 [child_process event: 'message'][].

在Node.js v6.0之前，这个事件触发没有worker对象，只有`message`和`handle`，与文档中的说明不同。

如果需要对旧版本的支持，但工作对象不是
必须，可以通过检查参数个数。

```js
cluster.on('message', (worker, message, handle) => {
  if (arguments.length === 2) {
    handle = message;
    message = worker;
    worker = undefined;
  }
  // ...
});
```

## Event: 'online'
<!-- YAML
added: v0.7.0
-->

* `worker` {cluster.Worker}

在分派新工作进程后，应通过online信息响应。当主进程接收到online消息时，它将发出此事件。
`'fork'` 和 `'online'`事件的不同在于前者是在主进程新建工作进程后触发，而后者是在工作进程运行的时候触发。

```js
cluster.on('online', (worker) => {
  console.log('Yay, the worker responded after it was forked');
});
```

## Event: 'setup'
<!-- YAML
added: v0.7.1
-->

* `settings` {Object}

Emitted every time `.setupMaster()` is called.

每当 `.setupMaster()` 被调用的时候触发。

`settings` 对象是 `setupMaster()` 被调用时的 `cluster.settings` 对象，并且只能查询，因为在一个 `tick` 内 `.setupMaster()` 可以被调用多次。

如果精确度十分重要，请使用 cluster.settings。

## cluster.disconnect([callback])
<!-- YAML
added: v0.7.7
-->

* `callback` {Function} 当所有工作进程都断开连接并且所有handle关闭的时候调用。
在`cluster.workers`的每个工作进程中调用 `.disconnect()`。

当所有工作进程断开连接后，所有内部handle将会关闭，这个时候如果没有等待事件的话，运行主进程优雅地关闭。

这个方法可以选择添加一个回调参数，当结束时会调用这个回调函数。

这个方法只能由主进程调用。

## cluster.fork([env])
<!-- YAML
added: v0.6.0
-->

* `env` {Object} 增加进程环境变量，以Key/value对的形式。
* Returns: {cluster.Worker}

衍生出一个新的工作进程。

只能通过主进程调用。

## cluster.isMaster
<!-- YAML
added: v0.8.1
-->

* {boolean}

当该进程是主进程时，返回 `true`。这是由`process.env.NODE_UNIQUE_ID`决定的，当`process.env.NODE_UNIQUE_ID`未定义时，`isMaster`为`true`。

## cluster.isWorker
<!-- YAML
added: v0.6.0
-->

* {boolean}

当进程不是主进程时，返回 `true`。（和`cluster.isMaster`刚好相反）

## cluster.schedulingPolicy
<!-- YAML
added: v0.11.2
-->

调度策略，包括循环计数的 `cluster.SCHED_RR`，以及由操作系统决定的`cluster.SCHED_NONE`。 这是一个全局设置，当第一个工作进程被衍生或者调动`cluster.setupMaster()`时，都将第一时间生效。

除Windows外的所有操作系统中，`SCHED_RR`都是默认设置。只要libuv可以有效地分发IOCP handle，而不会导致严重的性能冲击的话，Windows系统也会更改为`SCHED_RR`。

`cluster.schedulingPolicy` 可以通过设置`NODE_CLUSTER_SCHED_POLICY`环境变量来实现。这个环境变量的有效值包括"rr" 和 "`none`"。

## cluster.settings
<!-- YAML
added: v0.7.1
changes:
  - version: v9.5.0
    pr-url: https://github.com/nodejs/node/pull/18399
    description: The `cwd` option is supported now.
  - version: v9.4.0
    pr-url: https://github.com/nodejs/node/pull/17412
    description: The `windowsHide` option is supported now.
  - version: v8.2.0
    pr-url: https://github.com/nodejs/node/pull/14140
    description: The `inspectPort` option is supported now.
  - version: v6.4.0
    pr-url: https://github.com/nodejs/node/pull/7838
    description: The `stdio` option is supported now.
-->

* {Object}
  * `execArgv` {Array} 传递给Node.js可执行文件的参数列表。 **默认:** `process.execArgv`
  * `exec` {string} worker文件的路径. **默认:** `process.argv[1]`
  * `args` {Array} 传递给worker的参数。
    **默认:** `process.argv.slice(2)`
  * `cwd` {string} 当前工作进程的工作目录 **默认:**
    `undefined` (继承自父进程)
  * `silent` {boolean} 是否需要发送输出到父进程的stdio上。
    **默认:** `false`
  * `stdio` {Array} 配置fork进程的stdio。 由于cluster模块运行依赖于IPC，这个配置必须包含'ipc'。当提供了这个选项后，将撤销silent。
  * `uid` 设置进程的user标识符。 (见 setuid(2).)
  * `gid` 设置进程的group标识符。 (见 setgid(2).)
  * `inspectPort` {number|Function} 配置端口检查。
  这可以是一个数字或是不带参数的返回一个数字的函数。默认每个工作进程获取自己端口，从主线程的`process.debugPort`中递增。
  * `windowsHide` {boolean} 隐藏fork进程控制台窗口
通常是在Windows系统上创建的。 **默认:** `false`

在调用 `.setupMaster()` (或 `.fork()`)后，这个设置对象将包含这些设置。包含默认值。

此对象不能手动更改或设置。

## cluster.setupMaster([settings])
<!-- YAML
added: v0.7.1
changes:
  - version: v6.4.0
    pr-url: https://github.com/nodejs/node/pull/7838
    description: The `stdio` option is supported now.
-->

* `settings` {Object} 看 [`cluster.settings`][]

`setupMaster`常被用于改变默认的'fork'行为. 一旦调用,这些设置将传递到`cluster.settings`中。

注意这点:

* 任何设置的改变仅仅影响将来调用`.fork()`并且对运行中工作进程没有影响。
* 唯一无法通过`.setupMaster()`设置的属性是传递给`.fork()`的`env`属性
* 上述的默认值只在第一次调用时有效，当后续调用时，将采用cluster.setupMaster()调用时的当前值。

例如:

```js
const cluster = require('cluster');
cluster.setupMaster({
  exec: 'worker.js',
  args: ['--use', 'https'],
  silent: true
});
cluster.fork(); // https 工作进程
cluster.setupMaster({
  exec: 'worker.js',
  args: ['--use', 'http']
});
cluster.fork(); // http 工作进程
```

这个只能从主进程中调用。

## cluster.worker
<!-- YAML
added: v0.7.0
-->

* {Object}

对当前工作进程对象的引用。 在主进程中不可用。

```js
const cluster = require('cluster');

if (cluster.isMaster) {
  console.log('I am master');
  cluster.fork();
  cluster.fork();
} else if (cluster.isWorker) {
  console.log(`I am worker #${cluster.worker.id}`);
}
```

## cluster.workers
<!-- YAML
added: v0.7.0
-->

* {Object}

通过哈希存储这个活动的工作进程对象，键名`id`字段。
存储活动工作对象的哈希，由`id`字段标识。通过它很容易找到所有的工作进程。它只在主进程中可用。
一个工作进程从cluster.workers中移除后，这个工作进程已经断开连接并退出，但是这个两个事件的先后顺序是不确定的。
然而，可以确定的是工作进程从cluster.workers中移除是在`'disconnect'` 和 `'exit'`其中一个事件最后触发之前执行的。

```js
// Go through all workers
function eachWorker(callback) {
  for (const id in cluster.workers) {
    callback(cluster.workers[id]);
  }
}
eachWorker((worker) => {
  worker.send('big announcement to all workers');
});
```
使用工作进程的唯一id定位工作进程是最好的方法。

```js
socket.on('data', (id) => {
  const worker = cluster.workers[id];
});
```

[`ChildProcess.send()`]: child_process.html#child_process_subprocess_send_message_sendhandle_options_callback
[`child_process.fork()`]: child_process.html#child_process_child_process_fork_modulepath_args_options
[`disconnect`]: child_process.html#child_process_subprocess_disconnect
[`kill`]: process.html#process_process_kill_pid_signal
[`process` event: `'message'`]: process.html#process_event_message
[`server.close()`]: net.html#net_event_close
[`worker.exitedAfterDisconnect`]: #cluster_worker_exitedafterdisconnect
[Child Process module]: child_process.html#child_process_child_process_fork_modulepath_args_options
[child_process event: 'exit']: child_process.html#child_process_event_exit
[child_process event: 'message']: child_process.html#child_process_event_message
[`cluster.settings`]: #cluster_cluster_settings
