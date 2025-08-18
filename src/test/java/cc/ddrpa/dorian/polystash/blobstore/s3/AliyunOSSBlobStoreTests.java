package cc.ddrpa.dorian.polystash.blobstore.s3;

import cc.ddrpa.dorian.polystash.blobstore.AbstractBlobStoreTests;
import cc.ddrpa.dorian.polystash.core.blobstore.BlobStore;
import cc.ddrpa.dorian.polystash.core.exception.GeneralPolyStashException;
import cc.ddrpa.dorian.polystash.provider.s3.S3BlobStoreBuilder;
import cc.ddrpa.dorian.polystash.springboot.autoconfigure.FullBlobStoreProperties;
import org.junit.jupiter.api.Assumptions;

import java.util.Objects;

class AliyunOSSBlobStoreTests extends AbstractBlobStoreTests {

    private static final String ENDPOINT = System.getenv("ALIYUN_OSS_ENDPOINT");
    private static final String BUCKET = System.getenv("ALIYUN_OSS_BUCKET");
    private static final String ACCESS_KEY = System.getenv("ALIYUN_OSS_ACCESS_KEY");
    private static final String SECRET_KEY = System.getenv("ALIYUN_OSS_SECRET_KEY");
    private static final String REGION = System.getenv("ALIYUN_OSS_REGION");
    private static BlobStore blobStore;

    @Override
    protected BlobStore getBlobStore() throws GeneralPolyStashException {
        // Skip tests if Aliyun OSS environment variables are not set
        Assumptions.assumeTrue(ENDPOINT != null && !ENDPOINT.isEmpty(), "ALIYUN_OSS_ENDPOINT is not set");
        Assumptions.assumeTrue(BUCKET != null && !BUCKET.isEmpty(), "ALIYUN_OSS_BUCKET is not set");
        Assumptions.assumeTrue(ACCESS_KEY != null && !ACCESS_KEY.isEmpty(), "ALIYUN_OSS_ACCESS_KEY is not set");
        Assumptions.assumeTrue(SECRET_KEY != null && !SECRET_KEY.isEmpty(), "ALIYUN_OSS_SECRET_KEY is not set");

        if (Objects.isNull(blobStore)) {
            S3BlobStoreBuilder builder = new S3BlobStoreBuilder();
            FullBlobStoreProperties properties = new FullBlobStoreProperties("s3")
                    .setEndpoint(ENDPOINT)
                    .setBucket(BUCKET)
                    .setAccessKey(ACCESS_KEY)
                    .setSecretKey(SECRET_KEY)
                    .setRegion(REGION != null ? REGION : "oss-cn-hangzhou");
            blobStore = builder
                    .name("aliyun-oss")
                    .properties(properties)
                    .build();
        }
        return blobStore;
    }
}