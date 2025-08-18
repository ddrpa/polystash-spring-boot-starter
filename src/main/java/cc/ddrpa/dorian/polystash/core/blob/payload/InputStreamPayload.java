package cc.ddrpa.dorian.polystash.core.blob.payload;

import java.io.InputStream;

/**
 * 输入流载荷
 */
public class InputStreamPayload extends Payload<InputStream> {

    public InputStreamPayload(InputStream content) {
        super(content);
    }

    @Override
    public InputStream stream() {
        return content;
    }

    @Override
    public void close() {
        try {
            content.close();
        } catch (Exception ignored) {
        }
    }
}