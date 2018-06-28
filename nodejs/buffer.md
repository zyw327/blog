### 类方法: Buffer.alloc(size[, fill[, encoding]])
#### History
| 版本 | 变化 |
| - | - | 
| v8.9.3 | 指定无效的字符串填充将会被替换为0来填充缓冲区。|
| v5.10.0 | 新增于: v5.10.0 |
#### 参数
| 参数字段 | 类型 | 说明 |
| - | :-: | -: |
| size | integer | 指定新创建的Buffer大小 |
| fill | string \| Buffer \| integer| 指定填充Buffer的值。 Default: 0 |
| encoding | \<string> | 如果填充的是字符串，这个参数指定的就是字符串的编码。 默认: 'utf8'|

分配一个指定字节大小的新Buffer。如果填充未定义，则Buffer将被零填充。

```js
const buf = Buffer.alloc(5);

// Prints: <Buffer 00 00 00 00 00>
console.log(buf);
```

### buf.toString([encoding[, start[, end]]])

新增于: v0.1.90
- encoding \<string\> 要解码的字符编码。 默认: 'utf8'
- start \<integer\> 开始解码的字节偏移量。 默认: 0
- end \<integer\> 在（不包括）停止解码的字节偏移量. 默认: buf.length
- Returns: \<string\> 根据编码中指定的字符编码将BUF解码为字符串。可以通过开始和结束仅解码BUF的子集

字符串实例的最大长度（在UTF-16编码单元中）可以作为Buffer-Struts.Max String长度来使用。

```js
const buf2 = Buffer.alloc(11, "你好12345", 'utf8');
// 以下输出: <Buffer e4 bd a0 e5 a5 bd 31 32 33 34 35>
console.log(buf2);
// 以下输出: 你好12345
console.log(buf2.toString('utf8'));

const buf3 = Buffer.alloc(11, "你好123456", 'utf8');
// 以下输出: <Buffer e4 bd a0 e5 a5 bd 31 32 33 34 35>
console.log(buf2);
// 以下输出: 你好12345 => buf3长度11，utf-8编码中一个汉字占三个字节，你好123456共12个字节，所以丢失一个字节
console.log(buf2.toString('utf8'));
```
