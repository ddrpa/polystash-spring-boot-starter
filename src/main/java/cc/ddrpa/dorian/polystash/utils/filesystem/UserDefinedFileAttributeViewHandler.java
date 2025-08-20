package cc.ddrpa.dorian.polystash.utils.filesystem;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserDefinedFileAttributeViewHandler implements IAttributeHandler {

    private static final Logger logger = LoggerFactory.getLogger(
            UserDefinedFileAttributeViewHandler.class);

    public static boolean support(Path baseDir) {
        try {
            FileStore fileStore = Files.getFileStore(baseDir);
            return fileStore.supportsFileAttributeView(UserDefinedFileAttributeView.class);
        } catch (IOException ignored) {
            return false;
        }
    }

    @Override
    public void writeMetadataAttributes(Path filePath, Map<String, String> metadata) {
        setAttributes(filePath, METADATA_ATTRIBUTE_PREFIX, metadata);
    }

    @Override
    public Map<String, String> readMetadataAttributes(Path filePath) {
        return getAttributes(filePath, METADATA_ATTRIBUTE_PREFIX);
    }

    @Override
    public void writeUserDefinedAttributes(Path filePath, Map<String, String> attributes) {
        setAttributes(filePath, USER_DEFINED_ATTRIBUTE_PREFIX, attributes);
    }

    @Override
    public Map<String, String> readUserDefinedAttributes(Path filePath) {
        return getAttributes(filePath, USER_DEFINED_ATTRIBUTE_PREFIX);
    }

    @Override
    public Optional<String> readRawAttribute(Path filePath, String rawAttributeName) {
        return getAttribute(filePath, rawAttributeName);
    }

    private void setAttributes(Path filePath, String attributePrefix, Map<String, String> attributes) {
        UserDefinedFileAttributeView view = Files.getFileAttributeView(filePath, UserDefinedFileAttributeView.class);
        if (Objects.isNull(view)) {
            logger.debug("UserDefinedFileAttributeView is not supported with path {}", filePath);
            return;
        }
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isBlank(value)) {
                continue;
            }
            String attributeKey = attributePrefix + key;
            try {
                view.write(attributeKey,
                        StandardCharsets.UTF_8.encode(value));
            } catch (IOException e) {
                logger.debug("Failed to write attribute {} with value {} to path {}",
                        attributeKey, value, filePath);
            }
        }
    }

    private Map<String, String> getAttributes(Path filePath, String attributePrefix) {
        UserDefinedFileAttributeView view = Files.getFileAttributeView(filePath, UserDefinedFileAttributeView.class);
        if (view == null) {
            return Collections.emptyMap();
        }
        int attributePrefixLength = attributePrefix.length();
        try {
            return view.list().stream()
                    .filter(attribute -> attribute.startsWith(attributePrefix))
                    .map(attribute -> {
                        try {
                            ByteBuffer buffer = ByteBuffer.allocate(view.size(attribute));
                            view.read(attribute, buffer);
                            buffer.flip();
                            return Map.entry(
                                    attribute.substring(attributePrefixLength),
                                    StandardCharsets.UTF_8.decode(buffer).toString());
                        } catch (IOException e) {
                            logger.debug("Failed to read attribute {} from path {}",
                                    attribute,
                                    filePath);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue));
        } catch (IOException ignored) {
            logger.debug("Failed to list user defined attributes from path {}", filePath);
            return Collections.emptyMap();
        }
    }

    private void setAttribute(Path filePath, String key, String value) {
        UserDefinedFileAttributeView view = Files.getFileAttributeView(filePath,
                UserDefinedFileAttributeView.class);
        if (view == null) {
            logger.debug("UserDefinedFileAttributeView is not supported with path {}", filePath);
            return;
        }
        if (StringUtils.isBlank(value)) {
            logger.debug("Value is blank, skip writing <{}> to {}", key, filePath);
        }
        try {
            view.write(key, StandardCharsets.UTF_8.encode(value));
        } catch (IOException e) {
            logger.debug("Failed writing <{}>:<{}> to {}", key, value, filePath);
        }
    }

    private Optional<String> getAttribute(Path filePath, String key) {
        UserDefinedFileAttributeView view = Files.getFileAttributeView(filePath,
                UserDefinedFileAttributeView.class);
        if (view == null) {
            logger.debug("UserDefinedFileAttributeView is not supported with path {}", filePath);
            return Optional.empty();
        }
        try {
            ByteBuffer buffer = ByteBuffer.allocate(view.size(key));
            view.read(key, buffer);
            buffer.flip();
            return Optional.of(StandardCharsets.UTF_8.decode(buffer).toString());
        } catch (IOException e) {
            logger.debug("Failed reading <{}> from {}", key, filePath);
            return Optional.empty();
        }
    }
}