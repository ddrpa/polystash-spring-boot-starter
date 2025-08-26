# PolyStash Spring Boot Starter

PolyStash 是一个试图为 Spring Boot 项目提供统一存储抽象层的项目，目前支持文件系统和 S3 兼容的存储系统（如 MinIO、AWS S3、阿里云
OSS）。

该项目一开始只是试图实现一种通过
`application.properties` [批量创建 OSS Client 的方案](https://github.com/ddrpa/forvariz-spring-boot-starter)
，不过后来意识到，可以在此基础上实现「本地测试时使用文件系统存储，生产环境使用 S3」这种需求，
或者是「不同功能产生的文件放到不同的位置」，
然后就逐渐演变成了一个存储抽象层。

## 主要特性

- 提供统一的 API 接口，支持多种存储后端
- 多存储后端支持:
    - 文件系统存储
    - S3 兼容存储（MinIO、AWS S3、阿里云 OSS 等）
- Spring Boot 自动配置
- 多存储实例管理: 支持配置多个存储实例，可指定主存储

## 快速开始

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>cc.ddrpa.dorian.polystash</groupId>
    <artifactId>polystash-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

在 `application.yml` 中配置：

```yaml
polystash:
  primary: avatar
  blobstore:
    avatar:
      builder: fs
      baseDir: /var/www/public/avatar
    attachments:
      builder: s3
      endpoint: https://minio.example.com:9000
      bucket: my-bucket
      accessKey: ${MINIO_ACCESS_KEY}
      secretKey: ${MINIO_SECRET_KEY}
    archive-datalog:
      builder: s3
      endpoint: https://s3.amazonaws.com
      bucket: my-s3-bucket
      accessKey: ${AWS_ACCESS_KEY_ID}
      secretKey: ${AWS_SECRET_ACCESS_KEY}
      region: us-west-2
```

### 使用存储服务

注入 BlobStore 实例

```java
// SomeUserService.java
@Qualifier("avatar")
private final BlobStore blobStore;
````

保存文件

```java
public String saveAvatar(String username, MultipartFile file) {
    Blob blob = blobStore.put("uploads/", fileName,
            new MultipartFilePayload(file),
            Collections.emptyMap(),
            file.contentType());
    return blob.objectName();
}
```

读取文件

````java
public InputStream getAvatar(String objectName) {
    return blobStore.get(objectName).payload().getStream();
}
````

## 概念与 API 参考

### BlobStore

操纵文件，主要方法包括

- `Blob put(String prefix, String readableName, Payload<?> payload, Map<String, String> userDefinedAttributes, String contentType)` -
存储文件
- `Iterable<BlobResult> list(String prefix, ListOptions listOptions)` - 列出文件
- `boolean exist(String objectName)` - 检查文件是否存在
- `Blob stat(String objectName)` - 获取文件元数据
- `Blob get(String objectName)` - 获取文件
- `void remove(String objectName, boolean silent)` - 删除文件

### Blob

文件对象，包含以下属性

- `objectName` - 对象名称，S3 存储中表一个对象的 Key，文件系统中为相对路径
- `readableName` - 可读文件名 / 原始文件名
- `length` - 文件大小
- `contentType` - 内容类型
- `lastModified` - 最后修改时间
- `etag` - ETag 值
- `checksum` - 校验和，仅文件系统存储支持，使用 xxHash64 计算并以十六进制字符串表示
- `checksumAlgorithm` - 校验和算法
- `isRepeatable` - 是否为可重复读取负载，如 ByteArrayPayload 或 FilePayload 支持重复读取，而 MultipartFilePayload 和
  InputStreamPayload 不支持

### Payload 类型

- `ByteArrayPayload` - 字节数组
- `FilePayload` - 文件
- `InputStreamPayload` - 输入流
- `MultipartFilePayload` - Spring MVC MultipartFile

## 路线图

- [ ] 支持 Spring Boot Actuator 健康检查
- [ ] 添加存储监控和指标
- [ ] 添加存储策略和生命周期管理
- [ ] 支持业务层存储加密
- [ ] 添加存储迁移工具
