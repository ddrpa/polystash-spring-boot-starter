package cc.ddrpa.dorian.polystash.springboot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * PolyStash 配置属性类
 * 支持配置多个 BlobStore 实例，并指定主实例
 */
@ConfigurationProperties(prefix = PolyStashProperties.PREFIX)
public class PolyStashProperties {

    public static final String PREFIX = "polystash";

    /**
     * 指定默认 BlobStore
     * <p>
     * 如果配置，将使用第一个配置的 BlobStore 作为主实例
     */
    private String primary;

    /**
     * BlobStore 配置映射
     * key: BlobStore 名称
     * value: BlobStore 配置属性
     */
    private SortedMap<String, FullBlobStoreProperties> blobstore = new TreeMap<>();

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public SortedMap<String, FullBlobStoreProperties> getBlobstore() {
        return blobstore;
    }

    public void setBlobstore(SortedMap<String, FullBlobStoreProperties> blobstore) {
        this.blobstore = blobstore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolyStashProperties that = (PolyStashProperties) o;
        return Objects.equals(primary, that.primary) && Objects.equals(blobstore, that.blobstore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, blobstore);
    }

    @Override
    public String toString() {
        return "PolyStashProperties{" +
                "primary='" + primary + '\'' +
                ", blobstore=" + blobstore +
                '}';
    }
}