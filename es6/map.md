## Map
### 语法
```js
    new Map([iterable])
```
### 参数
`iterable`

    Iterable 可以是一个数组或者其他 iterable 对象，其元素或为键值对，或为两个元素的数组。 每个键值对都会添加到新的 Map。null 会被当做 undefined。
## 描述
一个Map对象以插入顺序迭代其元素 — 一个  for...of 循环为每次迭代返回一个[key，value]数组。

### 键的相等(Key equality)
键的比较是基于 "SameValueZero" 算法：`NaN` 是与 `NaN` 相同的（虽然 `NaN` !== `NaN`），剩下所有其它的值是根据 === 运算符的结果判断是否相等。在目前的ECMAScript规范中，`-0`和`+0`被认为是相等的，尽管这在早期的草案中并不是这样。有关详细信息，请参阅浏览器兼容性 表中的“value equality for -0 and 0”。
### Objects 和 maps 的比较
`Object` 和 `Map` 类似的是，它们都允许你按键存取一个值、删除键、检测一个键是否绑定了值。因此（并且也没有其他内建的替代方式了）过去我们一直都把对象当成 `Map` 使用。不过 `Map` 和 `Object` 有一些重要的区别，在下列情况里 `Map` 会是更好的选择：

- 一个对象的键只能是字符串或者 Symbols，但一个 `Map` 的键可以是任意值，包括函数、对象、基本类型。
- 你可以通过 size 属性直接获取一个 `Map` 的键值对个数，而 `Object` 的键值对个数只能手动计算。
- `Map` 是可迭代的，而 `Object` 的迭代需要先获取它的键数组然后再进行迭代。
- `Object` 都有自己的原型，所以原型链上的键名有可能和对象上的键名产生冲突。虽然 ES5 开始可以用 `map = Object.create(null)` 来创建一个没有原型的对象，但是这种用法不太常见。
- `Map` 在涉及频繁增删键值对的场景下会有些性能优势。
## 属性
- `Map.length`

    属性 length 的值为 0 。
- `get Map[@@species]`

    本构造函数用于创建派生对象。
- `Map.prototype`

    表示 `Map` 构造器的原型。 允许添加属性从而应用于所有的 `Map` 对象。
## Map 实例

所有的 `Map` 对象实例都会继承 `Map.prototype`。

### 属性

- `Map.prototype.constructor`
    
    返回一个函数，它创建了实例的原型。默认是`Map`函数。
- `Map.prototype.size`

    返回`Map`对象的键/值对的数量。
### 方法

- `Map.prototype.clear()`

    移除Map对象的所有键/值对 。
- `Map.prototype.delete(key)`
    
    移除任何与键相关联的值，并且返回该值，该值在之前会被`Map.prototype.has(key)`返回为`true`。之后再调用`Map.prototype.has(key)`会返回`false`。
- `Map.prototype.entries()`

    返回一个新的 `Iterator` 对象，它按插入顺序包含了`Map`对象中每个元素的 `[key, value]` 数组。
- `Map.prototype.forEach(callbackFn[, thisArg])`

    按插入顺序，为 `Map`对象里的每一键值对调用一次`callbackFn`函数。如果为`forEach`提供了`thisArg`，它将在每次回调中作为`this`值。
- `Map.prototype.get(key)`
    
    返回键对应的值，如果不存在，则返回`undefined`。
- `Map.prototype.has(key)`
    
    返回一个布尔值，表示`Map`实例是否包含键对应的值。
- `Map.prototype.keys()`
    
    返回一个新的 `Iterator`对象， 它按插入顺序包含了`Map`对象中每个元素的键 。
- `Map.prototype.set(key, value)`
    
    设置`Map`对象中键的值。返回该`Map`对象。
- `Map.prototype.values()`
    
    返回一个新的`Iterator`对象，它按插入顺序包含了`Map`对象中每个元素的值 。
- `Map.prototype[@@iterator]()`
    
    返回一个新的`Iterator`对象，它按插入顺序包含了`Map`对象中每个元素的 `[key, value]` 数组。

## 示例
### 使用映射对象
```js
var myMap = new Map();
 
var keyObj = {},
    keyFunc = function () {},
    keyString = "a string";
 
// 添加键
myMap.set(keyString, "和键'a string'关联的值");
myMap.set(keyObj, "和键keyObj关联的值");
myMap.set(keyFunc, "和键keyFunc关联的值");
 
myMap.size; // 3
 
// 读取值
myMap.get(keyString);    // "和键'a string'关联的值"
myMap.get(keyObj);       // "和键keyObj关联的值"
myMap.get(keyFunc);      // "和键keyFunc关联的值"
 
myMap.get("a string");   // "和键'a string'关联的值"
                         // 因为keyString === 'a string'
myMap.get({});           // undefined, 因为keyObj !== {}
myMap.get(function() {}) // undefined, 因为keyFunc !== function () {}
```
### 将NaN作为映射的键
NaN 也可以作为Map对象的键. 虽然 NaN 和任何值甚至和自己都不相等(NaN !== NaN 返回true), 但下面的例子表明, 两个NaN作为Map的键来说是没有区别的:
```js
var myMap = new Map();
myMap.set(NaN, "not a number");

myMap.get(NaN); // "not a number"

var otherNaN = Number("foo");
myMap.get(otherNaN); // "not a number"
```
### 使用for..of方法迭代映射
映射也可以使用for..of循环来实现迭代：
```js
var myMap = new Map();
myMap.set(0, "zero");
myMap.set(1, "one");
for (var [key, value] of myMap) {
  console.log(key + " = " + value);
}
// 将会显示两个log。一个是"0 = zero"另一个是"1 = one"

for (var key of myMap.keys()) {
  console.log(key);
}
// 将会显示两个log。 一个是 "0" 另一个是 "1"

for (var value of myMap.values()) {
  console.log(value);
}
// 将会显示两个log。 一个是 "zero" 另一个是 "one"

for (var [key, value] of myMap.entries()) {
  console.log(key + " = " + value);
}
// 将会显示两个log。 一个是 "0 = zero" 另一个是 "1 = one"
```
### 使用forEach()方法迭代映射
映射也可以通过forEach()方法迭代：
```js
myMap.forEach(function(value, key) {
  console.log(key + " = " + value);
}, myMap)
// 将会显示两个logs。 一个是 "0 = zero" 另一个是 "1 = one"
```
### 映射与数组对象的关系
```js
var kvArray = [["key1", "value1"], ["key2", "value2"]];

// 使用映射对象常规的构造函数将一个二维键值对数组对象转换成一个映射关系
var myMap = new Map(kvArray);

myMap.get("key1"); // 返回值为 "value1"

// 使用展开运算符将一个映射关系转换成一个二维键值对数组对象
console.log(uneval([...myMap])); // 将会向您显示和kvArray相同的数组

// 或者使用展开运算符作用在键或者值的迭代器上，进而得到只含有键或者值得数组
console.log(uneval([...myMap.keys()])); // 输出 ["key1", "key2"]
```