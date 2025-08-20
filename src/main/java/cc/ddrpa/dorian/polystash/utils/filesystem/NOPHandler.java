package cc.ddrpa.dorian.polystash.utils.filesystem;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * do nothing
 */
public class NOPHandler implements IAttributeHandler {

    public static boolean support(Path path) {
        return true;
    }

    @Override
    public Map<String, String> readMetadataAttributes(Path filePath) {
        return Collections.emptyMap();
    }

    @Override
    public void writeMetadataAttributes(Path filePath, Map<String, String> metadata) {
        // do nothing
    }

    @Override
    public void writeUserDefinedAttributes(Path targetPath, Map<String, String> userDefinedAttributes) {
        // do nothing
    }

    @Override
    public Map<String, String> readUserDefinedAttributes(Path targetPath) {
        return Collections.emptyMap();
    }

    @Override
    public Optional<String> readRawAttribute(Path filePath, String rawAttributeName) {
        return Optional.empty();
    }
}