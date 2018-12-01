### 使用RestTemplate下载文件
```java
String url = "需要下载的文件地址";
HttpHeaders header = new HttpHeaders();
List<MediaType> list = new ArrayList<MediaType>();
// 指定下载文件类型
list.add(MediaType.APPLICATION_OCTET_STREAM);
header.setAccept(list);

HttpEntity<byte[]> request = new HttpEntity<byte[]>(params, header);
ResponseEntity<byte[]> response = this.restTemplate.exchange(url, HttpMethod.POST, request, byte[].class);
// 取得文件字节
byte[] result = response.getBody();
```
### 使用RestTemplate上传文件
```java
String url = "接收文件的地址";
HttpHeaders headers = new HttpHeaders();
// 设置请求头
headers.setContentType(MediaType.MULTIPART_FORM_DATA);
// file为通过表单上传的文件，如果是服务器上的文件，直接通过new File创建即可。
byte[] bytes = file.getBytes();
File fileToSave = new File(file.getOriginalFilename());
// 将表单上传的文件写入新创建文件中
FileCopyUtils.copy(bytes, fileToSave);
// 建立需要上传的资源对象
FileSystemResource resource = new FileSystemResource(fileToSave); 
MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
// 指定上传文件所对应的字段及资源  
param.add("file", resource);
HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);
// Result为上传返回结果映射类
HttpEntity<Result> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Result.class);
// 上传文件返回的结果
responseEntity.getBody();
```
### 使用poi导出excel字节到浏览器
```java
HSSFWorkbook workbook = new HSSFWorkbook();
HSSFSheet sheet = workbook.createSheet("excel");
HttpHeaders header = new HttpHeaders();
// 指定文件类型
header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//指定导出的文件名称
header.set("Content-Disposition", "attachment;filename=excel.xls");
ByteArrayOutputStream os = new ByteArrayOutputStream();
try {
	workbook.write(os);
} catch (IOException e) {
	e.printStackTrace();
}
ResponseEntity<byte[]> excelModels = new ResponseEntity<>(os.toByteArray(), header, HttpStatus.OK);
```
