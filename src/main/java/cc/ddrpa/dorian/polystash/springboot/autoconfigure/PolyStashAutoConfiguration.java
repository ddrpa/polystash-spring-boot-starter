package cc.ddrpa.dorian.polystash.springboot.autoconfigure;

import cc.ddrpa.dorian.polystash.core.blobstore.BlobStore;
import cc.ddrpa.dorian.polystash.core.blobstore.BlobStoreBuilder;
import cc.ddrpa.dorian.polystash.core.exception.GeneralPolyStashException;
import cc.ddrpa.dorian.polystash.provider.filesystem.FileSystemBlobStoreBuilder;
import cc.ddrpa.dorian.polystash.provider.s3.S3BlobStoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.SortedMap;

/**
 * PolyStash 自动配置类，负责根据配置文件自动创建和配置 BlobStore 实例。
 * <p>
 * 该类是 Spring Boot 自动配置的核心，在应用启动时自动扫描配置并创建
 * 相应的 BlobStore 实例。支持多种存储后端，包括文件系统和 S3 兼容的存储服务。
 * </p>
 * <p>
 * 主要功能包括：
 * <ul>
 *   <li>自动注册内置的 BlobStoreBuilder 实现</li>
 *   <li>解析配置文件中的存储配置</li>
 *   <li>创建和配置 BlobStore 实例</li>
 *   <li>管理主存储和辅助存储</li>
 *   <li>提供默认配置支持</li>
 * </ul>
 * <p>
 * 配置通过 PolyStashProperties 类进行管理，支持 YAML 和 properties 格式的配置文件。
 *
 * @see PolyStashProperties
 * @see BlobStoreHolder
 */
@Configuration
@EnableConfigurationProperties(PolyStashProperties.class)
public class PolyStashAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PolyStashAutoConfiguration.class);

    /**
     * 创建并配置 BlobStoreHolder Bean，管理所有 BlobStore 实例。
     * <p>
     * 该方法在应用启动时自动执行，负责：
     * <ul>
     *   <li>注册内置的 BlobStoreBuilder 实现（S3 和文件系统）</li>
     *   <li>解析配置文件中的存储配置</li>
     *   <li>创建和配置各个 BlobStore 实例</li>
     *   <li>设置主存储实例</li>
     *   <li>提供默认配置（如果未配置任何存储）</li>
     * </ul>
     * <p>
     * 如果配置文件中没有指定任何 BlobStore 配置，将自动创建一个默认的
     * 文件系统存储，路径为 '${WORK_DIR}/blobstore'。
     *
     * @param properties PolyStash 的配置属性
     * @return 配置完成的 BlobStoreHolder 实例
     */
    @Bean
    public BlobStoreHolder blobStoreProvider(PolyStashProperties properties) {
        logger.info("开始初始化 PolyStash BlobStore 配置");
        BlobStoreHolder provider = new BlobStoreHolder();
        // 注册内置的 BlobStoreBuilder
        provider.registerBlobStoreBuilder("s3", S3BlobStoreBuilder.class);
        logger.info("已注册内置 BlobStoreBuilder: S3BlobStoreBuilder.class: s3");
        provider.registerBlobStoreBuilder("filesystem", FileSystemBlobStoreBuilder.class);
        logger.info("已注册内置 BlobStoreBuilder: FileSystemBlobStoreBuilder.class: filesystem");

        SortedMap<String, FullBlobStoreProperties> blobstoreConfigs = properties.getBlobstore();
        if (blobstoreConfigs.isEmpty()) {
            logger.warn("未配置任何 BlobStore，将使用默认配置，文件将存储在本地磁盘的 '${WORK_DIR}/blobstore' 目录下");
            blobstoreConfigs.put("default", new FullBlobStoreProperties().setBuilder("filesystem").setBaseDir("blobstore"));
        }
        String primaryBlobStoreQualifier = properties.getPrimary();
        if (!blobstoreConfigs.containsKey(primaryBlobStoreQualifier)) {
            primaryBlobStoreQualifier = blobstoreConfigs.firstKey();
        }
        logger.info("使用的主 BlobStore 配置: {}", primaryBlobStoreQualifier);

        // 解析配置文件，添加 BlobStore
        for (Map.Entry<String, FullBlobStoreProperties> entry : blobstoreConfigs.entrySet()) {
            String blobStoreQualifier = entry.getKey();
            FullBlobStoreProperties blobStoreProperties = entry.getValue();
            try {
                processBlobStoreConfiguration(provider, blobStoreQualifier, blobStoreProperties, primaryBlobStoreQualifier.equalsIgnoreCase(blobStoreQualifier));
            } catch (Exception e) {
                logger.error("处理 BlobStore 配置 '{}' 时发生错误: {}", blobStoreQualifier, e.getMessage(), e);
                throw new RuntimeException(
                        String.format("Failed to process BlobStore configuration '%s'", blobStoreQualifier), e);
            }
        }

        logger.info("PolyStash BlobStore 配置初始化完成，共配置 {} 个 BlobStore", blobstoreConfigs.size());
        return provider;
    }

    /**
     * 处理单个 BlobStore 配置，创建和配置 BlobStore 实例。
     * <p>
     * 该方法负责处理单个存储配置，包括：
     * <ul>
     *   <li>验证配置的完整性</li>
     *   <li>创建相应的 BlobStoreBuilder</li>
     *   <li>验证配置参数</li>
     *   <li>构建 BlobStore 实例</li>
     *   <li>注册到 BlobStoreHolder</li>
     * </ul>
     *
     * @param provider            BlobStore 持有者，用于注册 BlobStore 实例
     * @param blobStoreQualifier  BlobStore 的限定符标识符
     * @param blobStoreProperties BlobStore 的配置属性
     * @param isPrimary           是否为主存储实例
     * @throws GeneralPolyStashException 当配置处理失败时抛出
     */
    private void processBlobStoreConfiguration(BlobStoreHolder provider,
                                               String blobStoreQualifier,
                                               FullBlobStoreProperties blobStoreProperties,
                                               boolean isPrimary) throws GeneralPolyStashException {

        logger.debug("处理 BlobStore 配置: {}", blobStoreQualifier);
        String builderType = blobStoreProperties.getBuilder();
        if (!StringUtils.hasText(builderType)) {
            throw new IllegalArgumentException(
                    String.format("BlobStore '%s' 缺少必需的 'builder' 配置", blobStoreQualifier));
        }
        BlobStoreBuilder blobStoreBuilder = createBlobStoreBuilder(builderType, provider);
        // 验证必需字段
        blobStoreBuilder.validate(blobStoreProperties);

        // 初始化 BlobStore 实例
        BlobStore blobStore = blobStoreBuilder
                .name(blobStoreQualifier)
                .properties(blobStoreProperties)
                .build();
        // 注册
        provider.registerBlobStore(blobStore, isPrimary);
    }

    /**
     * 创建 BlobStoreBuilder 实例。
     * <p>
     * 首先尝试从已注册的构建器中获取，如果不存在则尝试通过反射创建。
     * 支持内置的构建器类型和用户自定义的构建器实现。
     *
     * @param builder  构建器类型标识符
     * @param provider BlobStore 持有者，包含已注册的构建器
     * @return 对应的 BlobStoreBuilder 实例
     */
    private BlobStoreBuilder createBlobStoreBuilder(String builder, BlobStoreHolder provider) {
        // 首先尝试从已注册的构建器中获取
        BlobStoreBuilder blobStoreBuilder = provider.getBlobStoreBuilder(builder);
        if (blobStoreBuilder != null) {
            return blobStoreBuilder;
        }
        // 如果未找到，尝试通过类名创建
        try {
            Class<?> builderClass = Class.forName(builder);
            if (BlobStoreBuilder.class.isAssignableFrom(builderClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends BlobStoreBuilder> typedClass = (Class<? extends BlobStoreBuilder>) builderClass;
                provider.registerBlobStoreBuilder(typedClass);
                return typedClass.getDeclaredConstructor().newInstance();
            } else {
                throw new IllegalArgumentException(
                        String.format("类 '%s' 不是 BlobStoreBuilder 的实现", builder));
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    String.format("未找到 BlobStoreBuilder 类: '%s'", builder), e);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("创建 BlobStoreBuilder 实例失败: '%s'", builder), e);
        }
    }
}