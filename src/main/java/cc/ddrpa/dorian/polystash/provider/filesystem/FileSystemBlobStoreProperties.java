package cc.ddrpa.dorian.polystash.provider.filesystem;

import cc.ddrpa.dorian.polystash.core.blobstore.BlobStoreProperties;
import cc.ddrpa.dorian.polystash.springboot.autoconfigure.FullBlobStoreProperties;

import java.util.Objects;

public class FileSystemBlobStoreProperties implements BlobStoreProperties {

    private static final String BUILDER = "fs";
    private boolean primary = false;
    /**
     * Qualifier of bucket service，used for distinguishing while injecting
     */
    private String qualifier;
    private String baseDir;

    /**
     * NEED_CHECK 配置检查
     *
     * @param fullProperties
     * @return
     */
    public static FileSystemBlobStoreProperties validate(String qualifier,
                                                         FullBlobStoreProperties fullProperties) {
        if (!fullProperties.getBuilder().equalsIgnoreCase(BUILDER)) {
            throw new RuntimeException(
                    String.format("Configuration type mismatch: expected FileSystemBlobStoreProperties (builder='%s') but got builder='%s'",
                            BUILDER, fullProperties.getBuilder()));
        }
        FileSystemBlobStoreProperties properties = new FileSystemBlobStoreProperties();
        properties.setBaseDir(fullProperties.getBaseDir());
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

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSystemBlobStoreProperties that = (FileSystemBlobStoreProperties) o;
        return primary == that.primary && Objects.equals(qualifier, that.qualifier) && Objects.equals(baseDir, that.baseDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, qualifier, baseDir);
    }

    @Override
    public String toString() {
        return "FileSystemBlobStoreProperties{" +
                "primary=" + primary +
                ", qualifier='" + qualifier + '\'' +
                ", baseDir='" + baseDir + '\'' +
                '}';
    }
}