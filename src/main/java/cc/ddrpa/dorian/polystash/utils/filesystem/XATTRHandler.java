package cc.ddrpa.dorian.polystash.utils.filesystem;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * macOS 的 APFS 可以使用 xattr 来支持用户自定义属性
 */
public class XATTRHandler implements IAttributeHandler {

    private static final Logger logger = LoggerFactory.getLogger(XATTRHandler.class);

    private static final String XATTR_KV_DELIMITER = ": ";

    public static boolean support(Path path) {
        // 检查系统中是否存在 xattr 命令
        return Files.exists(Path.of("/usr/bin/xattr"));
    }

    @Override
    public Map<String, String> metadataAttributes(Path filePath) {
        return getAttributes(filePath, METADATA_ATTRIBUTE_PREFIX);
    }

    @Override
    public void metadataAttributes(Path filePath, Map<String, String> metadata) {
        setAttributes(filePath, METADATA_ATTRIBUTE_PREFIX, metadata);
    }

    @Override
    public void userDefinedAttributes(Path filePath, Map<String, String> userDefinedAttributes) {
        setAttributes(filePath, USER_DEFINED_ATTRIBUTE_PREFIX, userDefinedAttributes);
    }

    @Override
    public Map<String, String> userDefinedAttributes(Path filePath) {
        return getAttributes(filePath, USER_DEFINED_ATTRIBUTE_PREFIX);
    }

    @Override
    public Optional<String> readRawAttribute(Path filePath, String rawAttributeName) {
        return getAttribute(filePath, rawAttributeName);
    }

    private void setAttributes(Path filePath, String attributePrefix, Map<String, String> attributes) {
        // 执行 xattr -w user.key value example.txt 设置文件 example.txt 的扩展属性
        attributes.forEach((key, value) -> {
            if (StringUtils.isBlank(value)) {
                logger.debug("Value is blank, skip writing <{}> to {}", key, filePath);
            }
            try {
                Process process = new ProcessBuilder()
                        .command("/usr/bin/xattr", "-w", attributePrefix + key, value, filePath.toString())
                        .start();
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Interrupted while writing user defined attribute {} with value {} to path {}",
                        key, value, filePath);
            } catch (IOException e) {
                logger.debug("Failed to write user defined attribute {} with value {} to path {}",
                        key, value, filePath);
            }
        });
    }

    private Map<String, String> getAttributes(Path filePath, String attributePrefix) {
        Process process;
        try {
            // 执行 xattr -l example.txt 列出文件 example.txt 的所有扩展属性
            process = new ProcessBuilder()
                    .command("/usr/bin/xattr", "-l", filePath.toString())
                    .start();
        } catch (IOException e) {
            logger.debug("Unable to execute xattr -l {}", filePath);
            return Map.of();
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream ins = process.getInputStream();
             Reader reader = new BufferedReader(
                     new InputStreamReader(ins, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char) c);
            }
        } catch (IOException ignored) {
            logger.debug("Failed to read user defined attributes from path {}", filePath);
        }
        int attributePrefixLength = attributePrefix.length();
        return Arrays.stream(stringBuilder.toString().split("\n"))
                .filter(line -> line.startsWith(attributePrefix))
                .map(line -> {
                    String[] lineSplit = line.split(XATTR_KV_DELIMITER);
                    if (lineSplit.length != 2) {
                        return null;
                    }
                    try {
                        return Map.entry(
                                lineSplit[0].substring(attributePrefixLength),
                                lineSplit[1].trim());
                    } catch (Exception ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    private void setAttribute(Path path, String key, String value) {
        if (StringUtils.isBlank(value)) {
            logger.debug("Value is blank, skip writing <{}> to {}", key, path);
        }
        try {
            Process process = new ProcessBuilder()
                    .command("/usr/bin/xattr", "-w", key, value, path.toString())
                    .start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.debug("Failed writing <{}>:<{}> to {}", key, value, path);
        }
    }

    private Optional<String> getAttribute(Path path, String key) {
        Process process;
        try {
            process = new ProcessBuilder()
                    .command("/usr/bin/xattr", "-p", key, path.toString())
                    .start();
        } catch (IOException e) {
            logger.debug("Unable to execute xattr -p {} {}", key, path);
            return Optional.empty();
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream ins = process.getInputStream();
             Reader reader = new BufferedReader(
                     new InputStreamReader(ins, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char) c);
            }
        } catch (IOException ignored) {
            logger.debug("Failed reading <{}> from {}", key, path);
            return Optional.empty();
        }
        String processOutput = stringBuilder.toString();
        if (StringUtils.isBlank(processOutput)) {
            return Optional.empty();
        }
        return Optional.of(processOutput);
    }
}