package cc.ddrpa.dorian.polystash.springboot.autoconfigure;

import cc.ddrpa.dorian.polystash.core.blobstore.BlobStore;
import cc.ddrpa.dorian.polystash.core.blobstore.BlobStoreBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * BlobStore 持有者容器，用于管理 BlobStoreBuilder 和 BlobStore 实例的注册和获取。
 * <p>
 * 该类在项目启动时注册为 Spring Bean，负责管理所有存储相关的组件。
 * 提供了统一的注册和查找机制，支持按别名和类名进行组件管理。
 * <p>
 * 主要功能包括：
 * <ul>
 *   <li>注册和管理 BlobStoreBuilder 实例</li>
 *   <li>注册和管理 BlobStore 实例</li>
 *   <li>维护主存储实例的引用</li>
 *   <li>提供组件查找和实例化服务</li>
 * </ul>
 * <p>
 * 支持两种注册方式：按别名注册和按类名注册，便于灵活配置和管理。
 *
 * @see BlobStore
 * @see BlobStoreBuilder
 */
public class BlobStoreHolder {

    /**
     * 按别名记录的 BlobStoreBuilder 映射表。
     * <p>
     * 键为别名（如 "s3"、"filesystem"），值为对应的 BlobStoreBuilder 类。
     * 用于通过别名快速查找和创建 BlobStoreBuilder 实例。
     */
    private final Map<String, Class<? extends BlobStoreBuilder>> aliasBuilderMap = new HashMap<>();

    /**
     * 按类名记录的 BlobStoreBuilder 映射表。
     * <p>
     * 键为类的完全限定名，值为对应的 BlobStoreBuilder 类。
     * 用于通过类名查找和创建 BlobStoreBuilder 实例。
     */
    private final Map<String, Class<? extends BlobStoreBuilder>> classNameBuilderMap = new HashMap<>();

    /**
     * 按名称记录的 BlobStore 实例映射表。
     * <p>
     * 键为 BlobStore 的名称，值为对应的 BlobStore 实例。
     * 用于通过名称快速获取已创建的 BlobStore 实例。
     */
    private final Map<String, BlobStore> blobStoreMap = new HashMap<>();

    /**
     * 主 BlobStore 实例的引用。
     * <p>
     * 当应用程序没有明确指定使用哪个存储实例时，
     * 将使用此主存储实例进行默认操作。
     */
    private BlobStore primaryBlobStore;

    /**
     * 为指定的别名注册 BlobStoreBuilder。
     * <p>
     * 同时将构建器注册到别名映射表和类名映射表中，
     * 支持通过别名和类名两种方式进行查找。
     *
     * @param alias                 BlobStoreBuilder 的别名标识符
     * @param blobStoreBuilderClazz 要注册的 BlobStoreBuilder 类
     */
    public void registerBlobStoreBuilder(String alias,
                                         Class<? extends BlobStoreBuilder> blobStoreBuilderClazz) {
        aliasBuilderMap.put(alias, blobStoreBuilderClazz);
        classNameBuilderMap.put(blobStoreBuilderClazz.getName(), blobStoreBuilderClazz);
    }

    /**
     * 按类名注册 BlobStoreBuilder。
     * <p>
     * 将构建器注册到类名映射表中，支持通过类名进行查找。
     * 这种方式适用于不需要别名的场景。
     *
     * @param blobStoreBuilderClazz 要注册的 BlobStoreBuilder 类
     */
    public void registerBlobStoreBuilder(Class<? extends BlobStoreBuilder> blobStoreBuilderClazz) {
        classNameBuilderMap.put(blobStoreBuilderClazz.getName(), blobStoreBuilderClazz);
    }

    /**
     * 根据别名获取 BlobStoreBuilder 实例。
     * <p>
     * 通过别名查找对应的 BlobStoreBuilder 类，并创建新的实例。
     * 如果别名不存在，将抛出 IllegalArgumentException。
     *
     * @param alias BlobStoreBuilder 的别名标识符
     * @return 对应的 BlobStoreBuilder 实例
     * @throws IllegalArgumentException 当别名不存在时抛出
     * @throws RuntimeException         当实例化失败时抛出
     */
    public BlobStoreBuilder getBlobStoreBuilder(String alias) {
        Class<? extends BlobStoreBuilder> builderClazz = aliasBuilderMap.get(alias);
        if (builderClazz == null) {
            throw new IllegalArgumentException(
                    String.format("BlobStoreBuilder not found for alias '%s'. Available aliases: %s",
                            alias, String.join(", ", aliasBuilderMap.keySet())));
        }
        try {
            return builderClazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(
                    String.format("Failed to create BlobStoreBuilder instance for alias '%s' with class '%s'",
                            alias, builderClazz.getName()), e);
        }
    }

    /**
     * 根据类获取 BlobStoreBuilder 实例。
     * <p>
     * 通过类名查找对应的 BlobStoreBuilder 类，并创建新的实例。
     * 如果类未注册，将抛出 IllegalArgumentException。
     *
     * @param clazz 要查找的 BlobStoreBuilder 类
     * @return 对应的 BlobStoreBuilder 实例
     * @throws IllegalArgumentException 当类未注册时抛出
     * @throws RuntimeException         当实例化失败时抛出
     */
    public BlobStoreBuilder getBlobStoreBuilder(Class<? extends BlobStoreBuilder> clazz) {
        Class<? extends BlobStoreBuilder> builderClazz = classNameBuilderMap.get(clazz.getName());
        if (builderClazz == null) {
            throw new IllegalArgumentException(
                    String.format("BlobStoreBuilder not registered for class '%s'. Available classes: %s",
                            clazz.getName(), String.join(", ", classNameBuilderMap.keySet())));
        }
        try {
            return builderClazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(
                    String.format("Failed to instantiate BlobStoreBuilder class '%s'", builderClazz.getName()), e);
        }
    }

    /**
     * 注册 BlobStore 实例。
     * <p>
     * 将 BlobStore 实例注册到名称映射表中，如果指定为主存储，
     * 还将设置为主存储实例。
     *
     * @param blobStore 要注册的 BlobStore 实例
     * @param primary   是否设置为主存储实例
     */
    public void registerBlobStore(BlobStore blobStore, boolean primary) {
        blobStoreMap.put(blobStore.getBlobStoreName(), blobStore);
        if (primary) {
            primaryBlobStore = blobStore;
        }
    }

    /**
     * 根据名称获取 BlobStore 实例。
     * <p>
     * 通过名称查找已注册的 BlobStore 实例。如果名称不存在，
     * 将抛出 IllegalArgumentException。
     *
     * @param blobStoreName BlobStore 的名称标识符
     * @return 对应的 BlobStore 实例
     * @throws IllegalArgumentException 当名称不存在时抛出
     */
    public BlobStore getBlobStore(String blobStoreName) {
        return blobStoreMap.getOrDefault(blobStoreName, primaryBlobStore);
    }
}