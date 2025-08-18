package cc.ddrpa.dorian.polystash.provider.s3;

import cc.ddrpa.dorian.polystash.core.blobstore.BlobStoreContext;

import java.util.Objects;

public class S3BlobStoreContext implements BlobStoreContext {

    private String blobStoreName;
    private String endpoint;
    private String bucket;

    public S3BlobStoreContext(String blobStoreName, String endpoint, String bucket) {
        this.blobStoreName = blobStoreName;
        this.endpoint = endpoint;
        this.bucket = bucket;
    }

    public String getBlobStoreName() {
        return blobStoreName;
    }

    public void setBlobStoreName(String blobStoreName) {
        this.blobStoreName = blobStoreName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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
        S3BlobStoreContext that = (S3BlobStoreContext) o;
        return Objects.equals(blobStoreName, that.blobStoreName) && Objects.equals(endpoint, that.endpoint) && Objects.equals(bucket, that.bucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blobStoreName, endpoint, bucket);
    }

    @Override
    public String toString() {
        return "S3BlobStoreContext{" +
                "blobStoreName='" + blobStoreName + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", bucket='" + bucket + '\'' +
                '}';
    }
}