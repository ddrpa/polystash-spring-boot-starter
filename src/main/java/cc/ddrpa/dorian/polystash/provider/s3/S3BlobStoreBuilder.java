package cc.ddrpa.dorian.polystash.provider.s3;

import cc.ddrpa.dorian.polystash.core.blobstore.BlobStore;
import cc.ddrpa.dorian.polystash.core.blobstore.BlobStoreBuilder;
import cc.ddrpa.dorian.polystash.springboot.autoconfigure.FullBlobStoreProperties;
import io.minio.MinioClient;
import org.springframework.util.StringUtils;

public class S3BlobStoreBuilder implements BlobStoreBuilder {

    public static final String TYPE = "s3";

    private String blobStoreName;
    private String endpoint;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String region;

    @Override
    public BlobStoreBuilder name(String blobStoreName) {
        this.blobStoreName = blobStoreName;
        return this;
    }

    @Override
    public BlobStoreBuilder properties(FullBlobStoreProperties fullBlobStoreProperties) {
        S3BlobStoreProperties properties = S3BlobStoreProperties.validate(blobStoreName,
                fullBlobStoreProperties);
        this.endpoint = properties.getEndpoint();
        this.bucket = properties.getBucket();
        this.accessKey = properties.getAccessKey();
        this.secretKey = properties.getSecretKey();
        this.region = properties.getRegion();
        return this;
    }

    @Override
    public BlobStore build() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .region(region)
                .credentials(accessKey, secretKey)
                .build();
        return new S3BlobStore(blobStoreName, endpoint, bucket, minioClient);
    }

    /**
     * Builder for creating an instance of S3BlobStore.
     */
    public MinIOBuilder minio() {
        MinIOBuilder builder = new MinIOBuilder(this.blobStoreName);
        return builder;
    }

    public void setBlobStoreName(String blobStoreName) {
        this.blobStoreName = blobStoreName;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public void validate(FullBlobStoreProperties properties) {
        // S3 存储验证
        if (!StringUtils.hasText(properties.getEndpoint())) {
            throw new IllegalArgumentException(
                    String.format("S3 BlobStore '%s' 缺少必需的 'endpoint' 配置", blobStoreName));
        }
        if (!StringUtils.hasText(properties.getBucket())) {
            throw new IllegalArgumentException(
                    String.format("S3 BlobStore '%s' 缺少必需的 'bucket' 配置", blobStoreName));
        }

        // 验证认证信息
        boolean hasCredentials = StringUtils.hasText(properties.getCredentials());
        boolean hasAccessKey = StringUtils.hasText(properties.getAccessKey());
        boolean hasSecretKey = StringUtils.hasText(properties.getSecretKey());

        if (!hasCredentials && (!hasAccessKey || !hasSecretKey)) {
            throw new IllegalArgumentException(
                    String.format("S3 BlobStore '%s' 缺少认证信息，需要提供 credentials 文件或 accessKey + secretKey", blobStoreName));
        }
    }

    public static class MinIOBuilder {
        private final String blobStoreName;
        private String endpoint;
        private String bucket;
        private String accessKey;
        private String secretKey;
        private String region;

        public MinIOBuilder(String blobStoreName) {
            this.blobStoreName = blobStoreName;
        }

        public MinIOBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public MinIOBuilder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public MinIOBuilder credentials(String accessKey, String secretKey) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            return this;
        }

        public MinIOBuilder region(String region) {
            this.region = region;
            return this;
        }

        public S3BlobStore build() {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .region(region)
                    .credentials(accessKey, secretKey)
                    .build();
            return new S3BlobStore(blobStoreName, endpoint, bucket, minioClient);
        }
    }
}