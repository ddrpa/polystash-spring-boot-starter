package cc.ddrpa.dorian.polystash.utils.filesystem;

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
    String USER_DEFINED_ATTRIBUTE_PREFIX = "polystash.user.";

    /**
     * 元数据属性的前缀标识符。
     * <p>
     * 所有系统元数据属性都以此外缀开头，用于区分用户属性和系统属性。
     */
    String METADATA_ATTRIBUTE_PREFIX = "polystash.meta.";

    /**
     * ETag 属性名称。
     * <p>
     * 用于存储文件的 ETag 值，用于缓存验证和并发控制。
     */
    String ATTR_ETAG = "etag";

    /**
     * 内容类型属性名称。
     * <p>
     * 用于存储文件的 MIME 类型信息。
     */
    String ATTR_CONTENT_TYPE = "content-type";

    /**
     * 可读文件名属性名称。
     * <p>
     * 用于存储人类可读的文件名，通常与系统生成的文件名不同。
     */
    String ATTR_READABLE_FILENAME = "readable-filename";

    /**
     * 校验和属性名称。
     * <p>
     * 用于存储文件的校验和值，用于数据完整性验证。
     */
    String ATTR_CHECKSUM = "checksum";

    /**
     * 校验和算法属性名称。
     * <p>
     * 用于存储计算校验和时使用的算法标识符。
     */
    String ATTR_CHECKSUM_ALGORITHM = "checksum-algorithm";

    /**
     * 读取文件的元数据属性。
     * <p>
     * 从指定文件中读取所有系统元数据属性，返回属性名和值的映射。
     * 元数据包括 ETag、内容类型、可读文件名、校验和等系统信息。
     *
     * @param filePath 要读取元数据的文件路径
     * @return 包含元数据键值对的映射，如果没有元数据则返回空映射
     */
    Map<String, String> metadataAttributes(Path filePath);

    /**
     * 设置文件的元数据属性。
     * <p>
     * 将指定的元数据属性写入到文件中。如果属性已存在，将覆盖原值。
     * 元数据属性用于存储系统级别的文件信息。
     *
     * @param filePath 要设置元数据的文件路径
     * @param metadata 要设置的元数据键值对映射
     */
    void metadataAttributes(Path filePath, Map<String, String> metadata);

    /**
     * 设置文件的用户自定义属性。
     * <p>
     * 将用户定义的属性写入到文件中。这些属性用于存储业务相关的信息，
     * 如标签、分类、权限等，不会影响文件的系统行为。
     *
     * @param filePath              要设置用户自定义属性的文件路径
     * @param userDefinedAttributes 要设置的用户自定义属性键值对映射
     */
    void userDefinedAttributes(Path filePath, Map<String, String> userDefinedAttributes);

    /**
     * 读取文件的用户自定义属性。
     * <p>
     * 从指定文件中读取所有用户自定义属性，返回属性名和值的映射。
     * 这些属性通常由应用程序设置，用于存储业务逻辑相关的信息。
     *
     * @param filePath 要读取用户自定义属性的文件路径
     * @return 包含用户自定义属性键值对的映射，如果没有属性则返回空映射
     */
    Map<String, String> userDefinedAttributes(Path filePath);

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