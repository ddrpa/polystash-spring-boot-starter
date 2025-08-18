package cc.ddrpa.dorian.polystash.core.blob.payload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;

/**
 * 文件载荷
 */
public class FilePayload extends Payload<File> {

    public FilePayload(File content) {
        super(content);
    }

    public String filename() {
        return this.content.getName();
    }

    public Instant lastModified() {
        return Instant.ofEpochSecond(this.content.lastModified());
    }

    public long length() {
        return this.content.length();
    }

    public String contentType() throws IOException {
        return Files.probeContentType(this.content.toPath());
    }

    public File file() {
        return this.content;
    }

    @Override
    public InputStream stream() throws IOException {
        return new FileInputStream(content);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }
}