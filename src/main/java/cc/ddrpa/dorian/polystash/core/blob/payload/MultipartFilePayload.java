package cc.ddrpa.dorian.polystash.core.blob.payload;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * MultipartFile 载荷
 */
public class MultipartFilePayload extends Payload<MultipartFile> {

    public MultipartFilePayload(MultipartFile content) {
        super(content);
    }

    public String filename() {
        return this.content.getName();
    }

    public String originalFilename() {
        return this.content.getOriginalFilename();
    }

    public long length() {
        return this.content.getSize();
    }

    public String contentType() {
        return this.content.getContentType();
    }

    @Override
    public InputStream stream() throws IOException {
        return content.getInputStream();
    }
}