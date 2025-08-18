package cc.ddrpa.dorian.polystash.springboot.autoconfigure;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * BlobStore 完整配置属性类
 * 支持文件系统、S3兼容存储等多种存储类型的配置
 */
public class FullBlobStoreProperties {
    /**
     * BlobStore 限定符
     * 用于在依赖注入时区分不同的 BlobStore 实例
     */
    private String qualifier;

    /**
     * BlobStore 构建器类型
     * 支持的值：
     * - "fs": 文件系统存储
     * - "s3": S3 兼容存储
     * - 自定义实现类的全限定名
     */
    private String builder;

    /**
     * OSS
     * <p>
     * 服务端点 URL，例如 https://oss-cn-hangzhou.aliyuncs.com/，不要使用带 Bucket 的端点
     */
    private String endpoint;

    /**
     * OSS
     * <p>
     * 存储服务区域，默认值：us-east-1
     */
    private String region = "us-east-1";

    /**
     * OSS
     * <p>
     * 访问密钥 ID，如果使用 credentials 文件，则不需要此字段
     */
    private String accessKey;

    /**
     * OSS
     * <p>
     * 访问密钥，如果使用 credentials 文件，则不需要此字段
     */
    private String secretKey;

    /**
     * OSS
     * <p>
     * JSON 格式的凭证文件路径，如果提供此字段，则不需要 accessKey 和 secretKey
     */
    private String credentials;

    /**
     * OSS
     * <p>
     * 存储桶名称
     */
    private String bucket;

    /**
     * FileSystem
     * <p>
     * 基础目录路径，指定文件存储的根目录
     */
    private String baseDir;

    /**
     * 允许添加额外的配置供自定义实现访问
     * 用于扩展配置，支持特定存储类型的额外参数
     */
    private Map<String, String> extra = new LinkedHashMap<>();

    public FullBlobStoreProperties() {
    }

    public FullBlobStoreProperties(String builder) {
        this.builder = builder;
    }

    public String getQualifier() {
        return qualifier;
    }

    public FullBlobStoreProperties setQualifier(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public FullBlobStoreProperties setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public FullBlobStoreProperties setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public FullBlobStoreProperties setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public FullBlobStoreProperties setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getCredentials() {
        return credentials;
    }

    public FullBlobStoreProperties setCredentials(String credentials) {
        this.credentials = credentials;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public FullBlobStoreProperties setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public FullBlobStoreProperties setBaseDir(String baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    public String getBuilder() {
        return builder;
    }

    public FullBlobStoreProperties setBuilder(String builder) {
        this.builder = builder;
        return this;
    }

    public Map<String, String> getExtra() {
        return extra;
    }

    public FullBlobStoreProperties setExtra(Map<String, String> extra) {
        this.extra = extra;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullBlobStoreProperties that = (FullBlobStoreProperties) o;
        return Objects.equals(qualifier, that.qualifier) && Objects.equals(endpoint, that.endpoint) && Objects.equals(region, that.region) && Objects.equals(accessKey, that.accessKey) && Objects.equals(secretKey, that.secretKey) && Objects.equals(credentials, that.credentials) && Objects.equals(bucket, that.bucket) && Objects.equals(baseDir, that.baseDir) && Objects.equals(builder, that.builder) && Objects.equals(extra, that.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifier, endpoint, region, accessKey, secretKey, credentials, bucket, baseDir, builder, extra);
    }

    @Override
    public String toString() {
        return "FullBlobStoreProperties{" +
                "qualifier='" + qualifier + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", region='" + region + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", credentials='" + credentials + '\'' +
                ", bucket='" + bucket + '\'' +
                ", baseDir='" + baseDir + '\'' +
                ", builder='" + builder + '\'' +
                ", extra=" + extra +
                '}';
    }
}