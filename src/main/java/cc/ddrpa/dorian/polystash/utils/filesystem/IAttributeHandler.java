package cc.ddrpa.dorian.polystash.utils.filesystem;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * 文件属性处理器接口，用于管理文件的扩展属性和元数据。
 * <p>
 * 该接口定义了如何读取和设置文件的扩展属性，包括用户自定义属性和系统元数据。
 * 不同的实现类支持不同的文件系统特性，如用户定义属性视图、XATTR 等。
 * </p>
 * <p>
 * 当文件存储支持用户定义属性视图时（即 fileStore.supportsFileAttributeView(UserDefinedFileAttributeView.class)
 * 返回 true），应该使用支持该特性的实现类。
 * <p>
 * 该接口提供了统一的属性管理 API，隐藏了不同文件系统的实现差异。
 */
public interface IAttributeHandler {

    /**
     * 用户自定义属性的前缀标识符。
     * <p>
     * 所有用户自定义属性都以此外缀开头，用于区分系统属性和用户属性。
     */
    String USER_DEFINED_ATTRIBUTE_PREFIX = "forvariz.user.";

    /**
     * 元数据属性的前缀标识符。
     * <p>
     * 所有系统元数据属性都以此外缀开头，用于区分用户属性和系统属性。
     */
    String METADATA_ATTRIBUTE_PREFIX = "forvariz.meta.";

    /**
     * 文件的 ETag 值，用于缓存验证和并发控制
     */
    String ATTR_ETAG = "etag";

    /**
     * 文件的 MIME 类型信息。
     */
    String ATTR_CONTENT_TYPE = "content-type";

    /**
     * 可读文件名
     */
    String ATTR_READABLE_FILENAME = "readable-filename";
    String ATTR_LEGACY_READABLE_FILENAME = "original-filename"; // 兼容旧版本

    /**
     * 存储文件的校验和值，用于数据完整性验证
     */
    String ATTR_CHECKSUM = "checksum";
    /**
     * 计算校验和时使用的算法
     */
    String ATTR_CHECKSUM_ALGORITHM = "checksum-algorithm";

    static Optional<String> parseETag(Map<String, String> metadata) {
        return Optional.ofNullable(metadata.get(ATTR_ETAG));
    }

    static Optional<String> parseContentType(Map<String, String> metadata) {
        return Optional.ofNullable(metadata.get(ATTR_CONTENT_TYPE));
    }

    static Optional<String> parseReadableFilename(Map<String, String> metadata) {
        String nullableReadableFilename = metadata.get(ATTR_READABLE_FILENAME);
        if (StringUtils.isBlank(nullableReadableFilename)) {
            return Optional.ofNullable(metadata.get(ATTR_LEGACY_READABLE_FILENAME));
        } else {
            return Optional.of(nullableReadableFilename);
        }
    }

    static Optional<String> parseChecksum(Map<String, String> metadata) {
        return Optional.ofNullable(metadata.get(ATTR_CHECKSUM));
    }

    static Optional<String> parseChecksumAlgorithm(Map<String, String> metadata) {
        return Optional.ofNullable(metadata.get(ATTR_CHECKSUM_ALGORITHM));
    }

    /**
     * 读取文件的元数据属性。
     * <p>
     * 从指定文件中读取所有系统元数据属性，返回属性名和值的映射。
     * 元数据包括 ETag、内容类型、可读文件名、校验和等系统信息。
     *
     * @param filePath 要读取元数据的文件路径
     * @return 包含元数据键值对的映射，如果没有元数据则返回空映射
     */
    Map<String, String> readMetadataAttributes(Path filePath);

    /**
     * 设置文件的元数据属性。
     * <p>
     * 将指定的元数据属性写入到文件中。如果属性已存在，将覆盖原值。
     * 元数据属性用于存储系统级别的文件信息。
     *
     * @param filePath 要设置元数据的文件路径
     * @param metadata 要设置的元数据键值对映射
     */
    void writeMetadataAttributes(Path filePath, Map<String, String> metadata);

    /**
     * 设置文件的用户自定义属性。
     * <p>
     * 将用户定义的属性写入到文件中。这些属性用于存储业务相关的信息，
     * 如标签、分类、权限等，不会影响文件的系统行为。
     *
     * @param filePath              要设置用户自定义属性的文件路径
     * @param userDefinedAttributes 要设置的用户自定义属性键值对映射
     */
    void writeUserDefinedAttributes(Path filePath, Map<String, String> userDefinedAttributes);

    /**
     * 读取文件的用户自定义属性。
     * <p>
     * 从指定文件中读取所有用户自定义属性，返回属性名和值的映射。
     * 这些属性通常由应用程序设置，用于存储业务逻辑相关的信息。
     *
     * @param filePath 要读取用户自定义属性的文件路径
     * @return 包含用户自定义属性键值对的映射，如果没有属性则返回空映射
     */
    Map<String, String> readUserDefinedAttributes(Path filePath);

    /**
     * 读取文件的原始属性值。
     * <p>
     * 从指定文件中读取指定名称的原始属性值。该方法提供了对文件系统
     * 原生属性的直接访问，适用于需要访问特定文件系统特性的场景。
     *
     * @param filePath         要读取属性的文件路径
     * @param rawAttributeName 要读取的属性名称
     * @return 包含属性值的 Optional，如果属性不存在则返回空的 Optional
     */
    Optional<String> readRawAttribute(Path filePath, String rawAttributeName);
}