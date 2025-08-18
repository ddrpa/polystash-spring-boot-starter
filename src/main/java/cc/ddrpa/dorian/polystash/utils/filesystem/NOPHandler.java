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
    public Map<String, String> metadataAttributes(Path filePath) {
        return Collections.emptyMap();
    }

    @Override
    public void metadataAttributes(Path filePath, Map<String, String> metadata) {
    }

    @Override
    public void userDefinedAttributes(Path targetPath,
                                      Map<String, String> userDefinedAttributes) {
    }

    @Override
    public Map<String, String> userDefinedAttributes(Path targetPath) {
        return Collections.emptyMap();
    }

    @Override
    public Optional<String> readRawAttribute(Path filePath, String rawAttributeName) {
        return Optional.empty();
    }
}