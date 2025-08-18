package cc.ddrpa.dorian.polystash.blobstore.s3;

import cc.ddrpa.dorian.polystash.blobstore.AbstractBlobStoreTests;
import cc.ddrpa.dorian.polystash.core.blobstore.BlobStore;
import cc.ddrpa.dorian.polystash.provider.s3.S3BlobStoreBuilder;
import org.junit.jupiter.api.Assumptions;

import java.util.Objects;

class MinIOBlobStoreTests extends AbstractBlobStoreTests {

    private static final String ENDPOINT = System.getenv("MINIO_ENDPOINT");
    private static final String BUCKET = System.getenv("MINIO_BUCKET");
    private static final String ACCESS_KEY = System.getenv("MINIO_ACCESS_KEY");
    private static final String SECRET_KEY = System.getenv("MINIO_SECRET_KEY");
    private static BlobStore blobStore;

    @Override
    protected BlobStore getBlobStore() {
        Assumptions.assumeTrue(ENDPOINT != null && !ENDPOINT.isEmpty(), "MINIO_ENDPOINT is not set");
        Assumptions.assumeTrue(BUCKET != null && !BUCKET.isEmpty(), "MINIO_BUCKET is not set");
        Assumptions.assumeTrue(ACCESS_KEY != null && !ACCESS_KEY.isEmpty(), "MINIO_ACCESS_KEY is not set");
        Assumptions.assumeTrue(SECRET_KEY != null && !SECRET_KEY.isEmpty(), "MINIO_SECRET_KEY is not set");

        if (Objects.isNull(blobStore)) {
            blobStore = ((S3BlobStoreBuilder) new S3BlobStoreBuilder().name("minio"))
                    .minio()
                    .endpoint(ENDPOINT)
                    .bucket(BUCKET)
                    .credentials(ACCESS_KEY, SECRET_KEY)
                    .build();
        }
        return blobStore;
    }
}