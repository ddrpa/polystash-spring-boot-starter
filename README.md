# PolyStash Spring Boot Starter

PolyStash 是一个为 Spring Boot 项目提供统一存储抽象层的库，支持文件系统和 S3 兼容的存储系统（MinIO、AWS S3、阿里云 OSS 等）。

## 设计目标

PolyStash 解决以下场景的需求：

- **环境隔离**：开发环境使用本地文件系统，生产环境切换到 S3 存储，无需修改业务代码
- **多存储管理**：不同业务模块的文件存储到不同位置（如用户头像存本地、附件存 S3）
- **统一接口**：屏蔽底层存储差异，提供一致的 API

## 核心概念

### 架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                      Application                            │
├─────────────────────────────────────────────────────────────┤
│                       BlobStore                             │
│              (统一的存储操作抽象接口)                          │
├──────────────────────────┬──────────────────────────────────┤
│   FileSystemBlobStore    │         S3BlobStore              │
│   (本地文件系统实现)        │    (S3 兼容存储实现)              │
└──────────────────────────┴──────────────────────────────────┘
```

### 核心组件

| 组件 | 职责 |
|------|------|
| `BlobStore` | 存储操作抽象类，定义 put/get/list/remove 等方法 |
| `Blob` | 存储对象的元数据和内容载体 |
| `Payload` | 数据载荷抽象，封装不同类型的输入数据 |
| `BlobStoreHolder` | 管理多个 BlobStore 实例的容器 |
| `BlobStoreBuilder` | 构建器接口，用于创建 BlobStore 实例 |

### 对象命名

存储对象使用 `objectName` 作为唯一标识：

- **S3 存储**：对应 S3 对象的 Key
- **文件系统**：相对于 `baseDir` 的路径

调用 `put()` 方法时，系统会在指定的 `prefix` 下自动生成 UUID 作为 `objectName`。如需指定名称，使用 `putOrReplace()` 方法。

## 安装

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>cc.ddrpa.dorian.polystash</groupId>
    <artifactId>polystash-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

要求：

- Java 17+
- Spring Boot 3.x

## 配置

### 配置属性

在 `application.yml` 中配置存储实例：

```yaml
polystash:
  # 指定主存储实例，未指定时使用配置中的第一个
  primary: avatar
  # 是否将 BlobStore 注册为 Spring Bean，默认 true
  bean-registration: true
  # 存储实例配置
  blobstore:
    # 实例名称，用于依赖注入时的 @Qualifier
    avatar:
      builder: fs
      baseDir: /var/www/public/avatar
    attachments:
      builder: s3
      endpoint: https://minio.example.com:9000
      bucket: my-bucket
      accessKey: ${MINIO_ACCESS_KEY}
      secretKey: ${MINIO_SECRET_KEY}
```

### 文件系统存储配置

| 属性 | 必填 | 说明 |
|------|------|------|
| `builder` | 是 | 固定值 `fs` 或 `filesystem` |
| `baseDir` | 是 | 存储根目录路径，支持相对路径和绝对路径 |

### S3 存储配置

| 属性 | 必填 | 说明 |
|------|------|------|
| `builder` | 是 | 固定值 `s3` |
| `endpoint` | 是 | 服务端点 URL，如 `https://s3.amazonaws.com` |
| `bucket` | 是 | 存储桶名称 |
| `accessKey` | 是 | 访问密钥 ID |
| `secretKey` | 是 | 访问密钥 |
| `region` | 否 | 区域，默认 `us-east-1` |

### 默认配置

未配置任何 BlobStore 时，系统自动创建一个文件系统存储，路径为工作目录下的 `blobstore` 目录。

## 使用

### 注入 BlobStore

通过 `@Qualifier` 注解指定要注入的存储实例：

```java
@Service
public class FileService {
    
    private final BlobStore avatarStore;
    private final BlobStore attachmentStore;
    
    public FileService(
            @Qualifier("avatar") BlobStore avatarStore,
            @Qualifier("attachments") BlobStore attachmentStore) {
        this.avatarStore = avatarStore;
        this.attachmentStore = attachmentStore;
    }
}
```

主存储实例可直接注入，无需 `@Qualifier`：

```java
@Autowired
private BlobStore primaryStore;
```

### 通过 BlobStoreHolder 获取

```java
@Autowired
private BlobStoreHolder holder;

public void example() {
    BlobStore store = holder.getBlobStore("avatar");
}
```

### 存储文件

```java
// 使用 MultipartFile
public String saveUpload(MultipartFile file) throws GeneralPolyStashException {
    Blob blob = blobStore.put(
        "uploads/",                              // 前缀路径
        file.getOriginalFilename(),              // 可读文件名
        new MultipartFilePayload(file),          // 数据载荷
        Map.of("uploader", "user123"),           // 自定义属性
        file.getContentType()                    // MIME 类型
    );
    return blob.getObjectName();
}

// 使用字节数组
public String saveBytes(byte[] data, String filename) throws GeneralPolyStashException {
    Blob blob = blobStore.put(
        "data/",
        filename,
        new ByteArrayPayload(data),
        Collections.emptyMap(),
        "application/octet-stream"
    );
    return blob.getObjectName();
}
```

### 指定对象名称存储

```java
public void saveWithName(String objectName, byte[] data) throws GeneralPolyStashException {
    blobStore.putOrReplace(
        objectName,                              // 指定的对象名称
        "config.json",
        new ByteArrayPayload(data),
        Collections.emptyMap(),
        "application/json"
    );
}
```

### 读取文件

```java
public InputStream getFile(String objectName) throws GeneralPolyStashException {
    Blob blob = blobStore.get(objectName);
    return blob.getPayload().stream();
}

// 获取元数据（不读取内容）
public Blob getMetadata(String objectName) throws GeneralPolyStashException {
    return blobStore.stat(objectName);
}
```

### 列出文件

```java
public List<String> listFiles(String prefix) throws GeneralPolyStashException {
    List<String> names = new ArrayList<>();
    for (BlobResult result : blobStore.list(prefix, ListOptions.withDefault())) {
        if (result.isSuccess()) {
            names.add(result.blob().getObjectName());
        }
    }
    return names;
}
```

### 删除文件

```java
// 文件不存在时抛出异常
public void deleteFile(String objectName) throws GeneralPolyStashException {
    blobStore.remove(objectName, false);
}

// 静默删除，文件不存在时不抛异常
public void deleteFileSilently(String objectName) throws GeneralPolyStashException {
    blobStore.remove(objectName, true);
}
```

### 检查文件是否存在

```java
public boolean fileExists(String objectName) throws GeneralPolyStashException {
    return blobStore.exist(objectName);
}
```

## API 参考

### BlobStore 方法

| 方法 | 说明 |
|------|------|
| `put(prefix, readableName, payload, attributes, contentType)` | 存储文件，自动生成对象名称 |
| `putOrReplace(objectName, readableName, payload, attributes, contentType)` | 存储文件，使用指定的对象名称 |
| `get(objectName)` | 获取文件内容和元数据 |
| `stat(objectName)` | 仅获取元数据 |
| `list(prefix, listOptions)` | 列出指定前缀下的文件 |
| `exist(objectName)` | 检查文件是否存在 |
| `remove(objectName, silent)` | 删除文件 |

### Blob 属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `objectName` | `String` | 对象唯一标识 |
| `readableName` | `String` | 可读文件名 |
| `length` | `long` | 文件大小（字节） |
| `contentType` | `String` | MIME 类型 |
| `lastModified` | `Instant` | 最后修改时间 |
| `etag` | `String` | ETag 值 |
| `checksum` | `String` | 校验和（仅文件系统存储） |
| `checksumAlgorithm` | `String` | 校验和算法 |
| `userDefinedAttributes` | `Map<String, String>` | 用户自定义属性 |
| `payload` | `Payload` | 数据载荷 |

### Payload 类型

| 类型 | 可重复读取 | 适用场景 |
|------|------------|----------|
| `ByteArrayPayload` | 是 | 内存中的字节数据 |
| `FilePayload` | 是 | 本地文件 |
| `InputStreamPayload` | 否 | 流式数据 |
| `MultipartFilePayload` | 否 | Spring MVC 文件上传 |

### 异常类型

| 异常 | 说明 |
|------|------|
| `GeneralPolyStashException` | 所有异常的基类 |
| `BlobNotFoundException` | 对象不存在 |
| `AccessDeniedException` | 访问被拒绝（如路径越界） |
| `IOErrorOccursException` | IO 操作失败 |
| `OperationNotSupportedException` | 操作不支持 |

## S3 扩展功能

`S3BlobStore` 提供额外的 S3 特有方法：

### 预签名 URL

```java
S3BlobStore s3Store = (S3BlobStore) blobStore;

// 生成下载 URL，默认 7 天有效
String downloadUrl = s3Store.presignObjectUrlToGet("path/to/file", null);

// 指定有效期
String downloadUrl = s3Store.presignObjectUrlToGet(
    "path/to/file", 
    Duration.ofHours(1)
);

// 生成上传 URL
String uploadUrl = s3Store.presignObjectUrlToPut(
    "path/to/new-file",
    Duration.ofMinutes(30)
);
```

### POST 表单上传

```java
// 生成 POST 表单数据，用于浏览器直传
Map<String, String> formData = s3Store.getPresignedPostFormData(
    "uploads/image.jpg",           // 对象名称
    Duration.ofHours(1),           // 有效期
    "image/",                      // Content-Type 前缀限制
    1024L,                         // 最小文件大小
    10 * 1024 * 1024L              // 最大文件大小
);
```

### 访问底层客户端

```java
MinioClient minioClient = (MinioClient) s3Store._raw();
```

## 技术细节

### 文件系统存储

- **路径安全**：所有路径操作都会验证是否在 `baseDir` 范围内，防止路径遍历攻击
- **目录创建**：存储文件时自动创建必要的目录结构
- **校验和**：使用 xxHash64 算法计算文件校验和，存储在文件扩展属性中
- **元数据存储**：通过文件系统扩展属性（xattr）存储元数据，支持 `UserDefinedFileAttributeView` 和 `xattr` 命令

### S3 存储

- **客户端**：基于 MinIO Java SDK 实现，兼容所有 S3 协议的存储服务
- **分片上传**：自动处理大文件分片，默认分片大小 5MB
- **Content-Disposition**：自动设置 `Content-Disposition` 头，保留原始文件名

### 自动配置

- 应用启动时自动扫描配置并创建 BlobStore 实例
- 支持将 BlobStore 注册为 Spring Bean，可通过 `@Qualifier` 注入
- 未配置时提供默认的文件系统存储

## 扩展

### 自定义 BlobStoreBuilder

实现 `BlobStoreBuilder` 接口以支持其他存储后端：

```java
public class CustomBlobStoreBuilder implements BlobStoreBuilder {
    
    public static final String TYPE = "custom";
    
    private String blobStoreName;
    private FullBlobStoreProperties properties;
    
    @Override
    public BlobStoreBuilder name(String blobStoreName) {
        this.blobStoreName = blobStoreName;
        return this;
    }
    
    @Override
    public BlobStoreBuilder properties(FullBlobStoreProperties properties) {
        this.properties = properties;
        return this;
    }
    
    @Override
    public void validate(FullBlobStoreProperties properties) {
        // 验证配置
    }
    
    @Override
    public BlobStore build() throws GeneralPolyStashException {
        // 创建 BlobStore 实例
        return new CustomBlobStore(blobStoreName, properties);
    }
}
```

在配置中使用完整类名：

```yaml
polystash:
  blobstore:
    custom-store:
      builder: com.example.CustomBlobStoreBuilder
      # 自定义配置通过 extra 传递
      extra:
        customKey: customValue
```

## 路线图

- [ ] 支持 Spring Boot Actuator 健康检查
- [ ] 添加存储监控和指标
- [ ] 添加存储策略和生命周期管理
- [ ] 支持业务层存储加密
- [ ] 添加存储迁移工具

## 许可证

Apache License 2.0
