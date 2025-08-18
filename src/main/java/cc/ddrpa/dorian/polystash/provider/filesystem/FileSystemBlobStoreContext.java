package cc.ddrpa.dorian.polystash.provider.filesystem;

import cc.ddrpa.dorian.polystash.core.blobstore.BlobStoreContext;

import java.nio.file.Path;
import java.util.Objects;

public class FileSystemBlobStoreContext implements BlobStoreContext {

    private String blobStoreName;
    private Path baseDir;

    public FileSystemBlobStoreContext(String blobStoreName, Path baseDir) {
        this.blobStoreName = blobStoreName;
        this.baseDir = baseDir;
    }

    public String getBlobStoreName() {
        return blobStoreName;
    }

    public void setBlobStoreName(String blobStoreName) {
        this.blobStoreName = blobStoreName;
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSystemBlobStoreContext that = (FileSystemBlobStoreContext) o;
        return Objects.equals(blobStoreName, that.blobStoreName) && Objects.equals(baseDir, that.baseDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blobStoreName, baseDir);
    }

    @Override
    public String toString() {
        return "FileSystemBlobStoreContext{" +
                "blobStoreName='" + blobStoreName + '\'' +
                ", baseDir=" + baseDir +
                '}';
    }
}