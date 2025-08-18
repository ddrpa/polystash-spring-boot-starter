package cc.ddrpa.dorian.polystash.provider.filesystem;

import cc.ddrpa.dorian.polystash.core.blobstore.BlobStoreBuilder;
import cc.ddrpa.dorian.polystash.core.exception.GeneralPolyStashException;
import cc.ddrpa.dorian.polystash.core.exception.IOErrorOccursException;
import cc.ddrpa.dorian.polystash.core.exception.OperationNotSupportedException;
import cc.ddrpa.dorian.polystash.springboot.autoconfigure.FullBlobStoreProperties;
import cc.ddrpa.dorian.polystash.utils.filesystem.IAttributeHandler;
import cc.ddrpa.dorian.polystash.utils.filesystem.NOPHandler;
import cc.ddrpa.dorian.polystash.utils.filesystem.UserDefinedFileAttributeViewHandler;
import cc.ddrpa.dorian.polystash.utils.filesystem.XATTRHandler;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件系统 BlobStore 构建器，用于创建基于本地文件系统的存储实例。
 * <p>
 * 该构建器实现了 BlobStoreBuilder 接口，专门用于创建文件系统存储实例。
 * 支持自动创建基础目录，并根据系统能力选择合适的文件属性处理器。
 * <p>
 * 文件系统存储将 Blob 对象映射为本地文件系统中的文件，支持：
 * <ul>
 *   <li>自动目录创建和管理</li>
 *   <li>文件属性扩展支持（用户定义属性、XATTR 等）</li>
 *   <li>路径安全验证</li>
 *   <li>跨平台兼容性</li>
 * </ul>
 *
 * @see BlobStoreBuilder
 * @see FileSystemBlobStore
 */
public class FileSystemBlobStoreBuilder implements BlobStoreBuilder {

    /**
     * 文件系统存储类型标识符。
     * <p>
     * 在配置文件中使用此常量来指定使用文件系统存储后端。
     */
    public static final String TYPE = "filesystem";

    private String blobStoreName;
    private FileSystemBlobStoreProperties properties;

    /**
     * 设置 BlobStore 的名称。
     * <p>
     * 存储名称用于标识不同的存储实例，在日志记录、监控和配置管理中
     * 起到重要作用。
     *
     * @param blobStoreName BlobStore 的名称标识符
     * @return 当前构建器实例，支持链式调用
     */
    @Override
    public BlobStoreBuilder name(String blobStoreName) {
        this.blobStoreName = blobStoreName;
        return this;
    }

    /**
     * 设置 BlobStore 的配置属性。
     * <p>
     * 将通用的配置属性转换为文件系统特定的配置，并进行验证。
     * 确保配置的完整性和有效性。
     *
     * @param fullBlobStoreProperties 完整的 BlobStore 配置属性
     * @return 当前构建器实例，支持链式调用
     */
    @Override
    public BlobStoreBuilder properties(FullBlobStoreProperties fullBlobStoreProperties) {
        FileSystemBlobStoreProperties properties = FileSystemBlobStoreProperties.validate(blobStoreName,
                fullBlobStoreProperties);
        this.properties = properties;
        return this;
    }

    /**
     * 构建并返回配置完成的 FileSystemBlobStore 实例。
     * <p>
     * 构建过程包括：
     * <ul>
     *   <li>验证和创建基础目录</li>
     *   <li>选择合适的文件属性处理器</li>
     *   <li>创建 FileSystemBlobStore 实例</li>
     * </ul>
     * <p>
     * 如果基础目录不存在，将自动创建。如果基础目录路径指向文件而不是目录，
     * 将抛出 OperationNotSupportedException。
     *
     * @return 配置完成的 FileSystemBlobStore 实例
     * @throws GeneralPolyStashException      当构建过程失败时抛出
     * @throws IOErrorOccursException         当创建基础目录失败时抛出
     * @throws OperationNotSupportedException 当基础目录路径无效时抛出
     */
    @Override
    public FileSystemBlobStore build() throws GeneralPolyStashException {
        Path baseDir = Paths.get(properties.getBaseDir()).toAbsolutePath().normalize();
        if (!Files.exists(baseDir)) {
            try {
                Files.createDirectories(baseDir);
            } catch (IOException e) {
                throw new IOErrorOccursException(
                        String.format("Failed to create base directory '%s' for FileSystemBlobStore '%s'", baseDir, blobStoreName), e);
            }
        } else if (!Files.isDirectory(baseDir)) {
            throw new OperationNotSupportedException(
                    String.format("Base directory for FileSystemBlobStore '%s' must be a directory, but found file at: %s", blobStoreName, baseDir));
        }
        IAttributeHandler attributeHandler;
        if (UserDefinedFileAttributeViewHandler.support(baseDir)) {
            attributeHandler = new UserDefinedFileAttributeViewHandler();
        } else if (XATTRHandler.support(baseDir)) {
            attributeHandler = new XATTRHandler();
        } else {
            attributeHandler = new NOPHandler();
        }
        return new FileSystemBlobStore(blobStoreName, baseDir, attributeHandler);
    }

    /**
     * 验证配置属性的有效性。
     * <p>
     * 检查必需的配置字段是否已设置，确保构建过程能够正常进行。
     * 对于文件系统存储，必须指定 baseDir 配置。
     *
     * @param properties 要验证的配置属性
     * @throws IllegalArgumentException 当配置无效时抛出
     */
    @Override
    public void validate(FullBlobStoreProperties properties) {
        if (!StringUtils.hasText(properties.getBaseDir())) {
            throw new IllegalArgumentException(
                    String.format("文件系统 BlobStore '%s' 缺少必需的 'baseDir' 配置", blobStoreName));
        }
    }
}