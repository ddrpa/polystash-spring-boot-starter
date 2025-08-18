package cc.ddrpa.dorian.polystash.provider.s3;

import cc.ddrpa.dorian.polystash.core.blobstore.BlobStoreProperties;
import cc.ddrpa.dorian.polystash.springboot.autoconfigure.FullBlobStoreProperties;
import cc.ddrpa.dorian.polystash.utils.s3.ParseMinIOCredentialFromJSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.Objects;

public class S3BlobStoreProperties implements BlobStoreProperties {

    private static final String BUILDER = "s3";
    private boolean primary = false;
    /**
     * Qualifier of bucket service，used for distinguishing while injecting
     */
    private String qualifier;
    /**
     * Endpoint of bucket
     */
    private String endpoint;
    /**
     * Region of bucket
     */
    private String region;
    /**
     * Access key of bucket
     */
    private String accessKey;
    /**
     * Secret key of bucket
     */
    private String secretKey;
    /**
     * Bucket name of bucket
     */
    private String bucket;

    /**
     * NEED_CHECK 配置检查
     *
     * @param fullProperties
     * @return
     */
    public static S3BlobStoreProperties validate(String qualifier,
                                                 FullBlobStoreProperties fullProperties) {
        if (!fullProperties.getBuilder().equalsIgnoreCase(BUILDER)) {
            throw new RuntimeException(
                    String.format("Configuration type mismatch: expected S3BlobStoreProperties (builder='%s') but got builder='%s'",
                            BUILDER, fullProperties.getBuilder()));
        }
        S3BlobStoreProperties properties = new S3BlobStoreProperties();
        if (StringUtils.isNoneBlank(fullProperties.getCredentials())) {
            Pair<String, String> credentials = ParseMinIOCredentialFromJSON.parse(
                    new File(fullProperties.getCredentials())).get();
            properties.setAccessKey(credentials.getLeft());
            properties.setSecretKey(credentials.getRight());
        } else {
            properties.setAccessKey(fullProperties.getAccessKey());
            properties.setSecretKey(fullProperties.getSecretKey());
        }
        properties.setEndpoint(fullProperties.getEndpoint());
        if (StringUtils.isBlank(fullProperties.getRegion())) {
            properties.setRegion("us-east-1");
        } else {
            properties.setRegion(fullProperties.getRegion());
        }
        properties.setBucket(fullProperties.getBucket());
        properties.setQualifier(qualifier);
        return properties;
    }

    @Override
    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        S3BlobStoreProperties that = (S3BlobStoreProperties) o;
        return primary == that.primary && Objects.equals(qualifier, that.qualifier) && Objects.equals(endpoint, that.endpoint) && Objects.equals(region, that.region) && Objects.equals(accessKey, that.accessKey) && Objects.equals(secretKey, that.secretKey) && Objects.equals(bucket, that.bucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, qualifier, endpoint, region, accessKey, secretKey, bucket);
    }

    @Override
    public String toString() {
        return "S3BlobStoreProperties{" +
                "qualifier='" + qualifier + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", region='" + region + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", bucket='" + bucket + '\'' +
                '}';
    }
}