## 封装
### es5封装
```js
function Person(name, sex, age) {
    if (!(this instanceof Person)) {
        return new Person(name, sex, age);
    }
    this.name = name;
    this.sex = sex || 'female';
    this.age = age || 0;

    this.walk = function() {
        if (this.age <= 2) {
            return console.log('我不会走路');
        }

        if (this.age >2 && this.age < 4) {
            return console.log('我会走路了');
        }

        return console.log('走路');
    }

    this.study = function (skill) {
        console.log('学习' + skill);
    }

    this.introduce = function () {
        console.log(`我是${this.name}，我是一个${this.sex === 'male' ? "男" : "女"}孩，今年${this.age}岁了。`);
    }
}

// 调用方式
// new关键字创建实例
var p = new Person('小名', 'male', 10);
// 直接调用创建
var p1 = Person('小红', 'female', 9);
p.walk(); // 走路
p1.study('游泳'); // 学习游泳
p.introduce();// 我是小名，我是一个男孩，今年10岁了。
p1.introduce();// 我是小红，我是一个女孩，今年9岁了。
```
### 原型链的方式
```js
function Person(name, sex, age) {
    if (!(this instanceof Person)) {
        return new Person(name, sex, age);
    }
    this.name = name;
    this.sex = sex;
    this.age = age;
}

Person.prototype.walk = function() {
    if (this.age <= 2) {
        return console.log('我不会走路');
    }

    if (this.age >2 && this.age < 4) {
        return console.log('我会走路了');
    }

    return console.log('走路');
}

Person.prototype.study = function(skill) {
    console.log('学习' + skill);
}

Person.prototype.introduce = function() {
    console.log(`我是${this.name}，我是一个${this.sex === 'male' ? "男" : "女"}孩，今年${this.age}岁了。`);
}

// 调用方式
// new关键字创建实例
var p = new Person('小名', 'male', 10);
// 直接调用创建
var p1 = Person('小红', 'female', 9);
p.walk(); // 走路
p1.study('游泳'); // 学习游泳
p.introduce();
p1.introduce(); 
```
### es6封装
```js
class Person {
    constructor(name, sex, age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }

    walk() {
        if (this.age <= 2) {
            return console.log('我不会走路');
        }
    
        if (this.age >2 && this.age < 4) {
            return console.log('我会走路了');
        }
    
        return console.log('走路');
    }

    study(skill) {
        console.log('学习' + skill);
    }

    introduce() {
        console.log(`我是${this.name}，我是一个${this.sex === 'male' ? "男" : "女"}孩，今年${this.age}岁了。`);
    }
}

// 调用方式
// new关键字创建实例
var p = new Person('小名', 'male', 10);
p.walk(); // 走路
p.introduce();
// 直接调用创建
// var p1 = Person('小红', 'female', 9); // TypeError: Class constructor Person cannot be invoked without 'new'
// class定义的不能直接调用，只能通过new关键字实例化后调用

console.log(typeof Person); // function

```
## 继承
### es5
- 原型链实现继承
```js
function Person(name, sex, age) {
    if (!(this instanceof Person)) {
        return new Person(name, sex, age);
    }
    this.name = name;
    this.sex = sex || 'female';
    this.age = age || 0;

    this.walk = function() {
        if (this.age <= 2) {
            return console.log('我不会走路');
        }

        if (this.age >2 && this.age < 4) {
            return console.log('我会走路了');
        }

        return console.log('走路');
    }

    this.study = function (skill) {
        console.log('学习' + skill);
    }

    this.introduce = function () {
        console.log(`我是${this.name}，我是一个${this.sex === 'male' ? "男" : "女"}孩，今年${this.age}岁了。`);
    }
}

function Boy(name, age) {
    this.name = name;
    this.age = age;
    this.sex = 'male';

    this.doHouseWork = function() {
        console.log('我在做家务');
    }
}

Boy.prototype = new Person();
Boy.prototype.constructor = Boy;

var boy = new Boy('汤姆', 12);
boy.introduce(); // 我是汤姆，我是一个男孩，今年12岁了。
boy.doHouseWork();// 我在做家务
console.log(boy instanceof Boy);// true
```
- Object.create
```js
function create(obj) {
    return Object.create(obj);
}

var person = {
    name: 'Tom',
    age: 20,
    sex: 'male',
    walk: function() {
        if (this.age <= 2) {
            return console.log('我不会走路');
        }

        if (this.age >2 && this.age < 4) {
            return console.log('我会走路了');
        }

        return console.log('走路');
    },
    study: function(skill) {
        console.log('学习' + skill);
    },
    introduce: function() {
        console.log(`我是${this.name}，我是一个${this.sex === 'male' ? "男" : "女"}孩，今年${this.age}岁了。`);
    }
};

var boy = create(person);
boy.age = 15,
boy.name = '晓东';
boy.sex = 'male';
boy.doHouseWork = function() {
    console.log('我在做家务');
}

boy.introduce(); // 我是晓东，我是一个男孩，今年15岁了
boy.doHouseWork();// 我在做家务

```
- call方法
```js
function Person(name, sex, age) {
    if (!(this instanceof Person)) {
        return new Person(name, sex, age);
    }
    this.name = name;
    this.sex = sex || 'female';
    this.age = age || 0;

    this.walk = function() {
        if (this.age <= 2) {
            return console.log('我不会走路');
        }

        if (this.age >2 && this.age < 4) {
            return console.log('我会走路了');
        }

        return console.log('走路');
    }

    this.study = function (skill) {
        console.log('学习' + skill);
    }

    this.introduce = function () {
        console.log(`我是${this.name}，我是一个${this.sex === 'male' ? "男" : "女"}孩，今年${this.age}岁了。`);
    }
}

function Boy(name, age) {
    var obj = Person.call(this, name, 'male', age);
    obj.doHouseWork = function() {
        console.log('我在做家务');
    }
    return obj
}

let boy = Boy('小米', 16);
boy.introduce(boy); // 我是小米，我是一个男孩，今年16岁了。
boy.doHouseWork();// 我在做家务
```
- apply方法
```js
function Person(name, sex, age) {
    if (!(this instanceof Person)) {
        return new Person(name, sex, age);
    }
    this.name = name;
    this.sex = sex || 'female';
    this.age = age || 0;

    this.walk = function() {
        if (this.age <= 2) {
            return console.log('我不会走路');
        }

        if (this.age >2 && this.age < 4) {
            return console.log('我会走路了');
        }

        return console.log('走路');
    }

    this.study = function (skill) {
        console.log('学习' + skill);
    }

    this.introduce = function () {
        console.log(`我是${this.name}，我是一个${this.sex === 'male' ? "男" : "女"}孩，今年${this.age}岁了。`);
    }
}

function Boy(name, age) {
    var obj = Person.apply(this, [name, 'male', age]);
    obj.doHouseWork = function() {
        console.log('我在做家务');
    }
    return obj
}

let boy = Boy('小米', 17);
boy.introduce(boy); // 我是小米，我是一个男孩，今年16岁了。
boy.doHouseWork();// 我在做家务
```
- es6 extends关键字
```js
class Person {
    constructor(name, sex, age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }

    walk() {
        if (this.age <= 2) {
            return console.log('我不会走路');
        }
    
        if (this.age >2 && this.age < 4) {
            return console.log('我会走路了');
        }
    
        return console.log('走路');
    }

    study(skill) {
        console.log('学习' + skill);
    }

    introduce() {
        console.log(`我是${this.name}，我是一个${this.sex === 'male' ? "男" : "女"}孩，今年${this.age}岁了。`);
    }
}

class Boy extends Person {
    constructor(name, age) {
        super(name, 'male', age);
    }

    doHouseWork() {
        console.log('我在做家务');
    }
}

var boy = new Boy('汤姆', 14);
boy.introduce(); // 我是汤姆，我是一个男孩，今年12岁了。
boy.doHouseWork();// 我在做家务
console.log(boy instanceof Boy);// true
```
## 多态
- 函数参数不定个数
```js
function Person(name, sex, age) {
    if (!(this instanceof Person)) {
        return new Person(name, sex, age);
    }
    this.name = name;
    this.sex = sex || 'female';
    this.age = age || 0;

    this.walk = function() {
        if (this.age <= 2) {
            return console.log('我不会走路');
        }

        if (this.age >2 && this.age < 4) {
            return console.log('我会走路了');
        }

        return console.log('走路');
    }

    this.study = function (skill) {
        console.log('学习' + skill);
    }

    this.introduce = function () {
        console.log(`我是${this.name}，我是一个${this.sex === 'male' ? "男" : "女"}孩，今年${this.age}岁了。`);
    }
}

function Mathematician(name, age) {
    this.sex = 'male';
    this.calc = function() {
        var argsLength = arguments.length;
        switch(argsLength) {
            case 1:
                console.log("这是" + arguments[0]);
                break;
            case 2:
                console.log(arguments[0] + '+' + arguments[1] + '=' + (arguments[0] + arguments[1]));
                break;
            default:
                console.log('太复杂的暂时不会');
        }
    }
}

Mathematician.prototype = new Person();
Mathematician.prototype.constructor = Mathematician;

var boy = new Mathematician();

boy.calc(); // 太复杂的暂时不会
boy.calc(1);// 这是1
boy.calc(1, 3); // 1+3=4
```