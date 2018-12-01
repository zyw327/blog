## Docker 1.8的部署安装(centos7)
### docker yum的安装方式
```bash
cat >/etc/yum.repos.d/docker.repo <<-EOF

[dockerrepo]
name=Docker Repository
baseurl=https://yum.dockerproject.org/repo/main/centos/7
enabled=1
gpgcheck=1
gpgkey=https://yum.dockerproject.org/gpg
EOF

yum install docker-engine
```
### 设置开机启动Docker Daemon进程
```bash
systemctl start docker.service
# 设置开机启动
systemctl enable docker.service
# systemctl grep docker 查看 docker 进程的状态
# 保证docker正常运行，禁用firewalld
systemctl disable firewalld

yum -y install iptables-services
systemctl enable iptables
systemctl start iptables
```

### 查看docker版本
```bash
docker version
```
### docker配置文件
- 位置：/ect/sysconfig/docker
- 重要参数解释

    OPTIONS 用来控制Docker Deamon进程参数
    -H表示Docker Daemon绑定的地址，-H=unix:///var/run/docker.sock -H=tcp://0.0.0.0:225
    --registry-mirror表示（本地）私有Docker Registry的地址，--insecure-registry ${prvateRegistyHost}:5000
    --selinux-enabled是否开启SElinux，默认开启 --selinux=true
    --bip 表示网桥docker0使用指定CIDR网络地址，--bip=172.17.42.1
    -b 表示采用已经创建好的网桥，-b=xxx
    OPTIONS=-H=unix:///var/run/docker.sock -H=tcp://0.0.0.0:2375 -registry-mirror=http://4bc5abed.m.daoclud.io --selinux=true
- 配置文件
```bash
vim /usr/lib/systemd/system/docker.service
# 内容
ExecStart=/usr/bin/docker daemon -H fd:// -H=unix:///var/run/docker.sock -H=tcp://0.0.0.0:2375 --registry-mirror=http://4bc5abed.m.daocloud.io --selinux-enabled=true
[Service]
Environment="HTTP_PROXY=http://xxxx.com:8080"
Environment="HTTPS_PROXY=http://xxxx.com:8080"
Type=notify
ExecStart=/usr/bin/docker daemon
# 重启服务
systemctl restart docker.service
```

### docker代理配置
    http_proxy=xxxx:8080
    https_proxy=xxxx:8080
### docker日志
- 位置 /var/log/message
### Docker基础命令
- docker search 
    docker search java 查找镜像
- docker pull
    docker pull java 下载镜像
- docker images
    查看本地下载的镜像
- docker run
    - -it 表示交互模式
    - docker run -it java java -version
    - docker run -it java ps 查看java进程
    - docker run -it java uname
    - docker run 里面的命令结束，container（容器）就结束了。
    - docker run [OPTIONS] IMAGE[:TAG] [COMMAND] [ARGS]
    决定容器的运行方式，前台执行还是后台执行
    docker run 后面追加-d=true或者-d,那么容器将会运行在后台模式。
    docker exec 执行进入到容器中，或者attach重新连接容器的会话
    - 进行交互操作（例如Shell脚本），那么我们必须使用 -i -t参数同容器进行数据交互
    docker run 时没有指定 --name，那么deamon会自动生成一个随机字符串UUID
    docher时有自动化需求，你可以将containerId输出到指定的文件中（PIDfile）: --cidfile=""
    docher的容器是没有特权的，例如不能在容器中再启动一个容器。这是因为默认情况下容器是不能访问任何其他设备的，但是通过“privileged”,容器就拥有了访问其他设备的权限。
- docker create/start/stop/pause/unpause
    - docker create -it --name=myjava java java version
    - docker ps -a
    - docker start myjava
    - docker ps
    - docker create --name mysqlsrv1 -e MYSQL_ROOT_PASSWORD=123456 -p 3306:3006 mysql
    - docker start mysqlsrv1
    - docker ps
    - docker exec -it mysqlsrv1 /bin/bash
    - docker exec mysqlsrv1 env
### 镜像制作
- 将容器变成镜像，buildfile语法和案例，镜像制作中常见的问题。
- docker commit <container> [repo:tag] 当我们在制作自己的镜像的时候，会在container中安装一些工具、修改配置，如果不做commit保存起来，那么container停止后再启动，这些更改就消失了。（错误）
- docker commit d5c8 myjava(最方便，最快速，不规范， 无法自动化)
- docker images
- docker run -it myjava ls
- vim Dockerfile
- docker build -t leader/java .

