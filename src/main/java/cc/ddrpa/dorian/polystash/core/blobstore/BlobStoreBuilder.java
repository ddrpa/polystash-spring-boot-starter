package cc.ddrpa.dorian.polystash.core.blobstore;

import cc.ddrpa.dorian.polystash.core.exception.GeneralPolyStashException;
import cc.ddrpa.dorian.polystash.springboot.autoconfigure.FullBlobStoreProperties;

/**
 * BlobStore 构建器接口，用于创建和配置 BlobStore 实例。
 * <p>
 * 该接口遵循构建器模式，提供了流式 API 来配置 BlobStore 的各种属性。
 * 通过链式调用可以设置存储名称、配置属性等，最后调用 build() 方法
 * 创建配置完成的 BlobStore 实例。
 * </p>
 * <p>
 * 实现类需要提供具体的构建逻辑，确保所有必要的配置都已设置，
 * 并在构建过程中进行适当的验证。
 * </p>
 */
public interface BlobStoreBuilder {
    /**
     * 设置 BlobStore 的名称。
     * <p>
     * 存储名称用于标识不同的存储实例，在日志记录、监控和配置管理中
     * 起到重要作用。名称应该是唯一的，便于区分不同的存储后端。
     *
     * @param blobStoreName BlobStore 的名称
     * @return 当前的 BlobStoreBuilder 实例，支持链式调用
     */
    BlobStoreBuilder name(String blobStoreName);

    /**
     * 通过 FullBlobStoreProperties 设置 BlobStore 的属性。
     * <p>
     * 使用完整的配置属性对象来配置 BlobStore，包括连接信息、认证凭据、
     * 存储路径等所有必要的配置参数。
     *
     * @param properties 包含所有配置信息的属性对象
     * @return 当前的 BlobStoreBuilder 实例，支持链式调用
     */
    BlobStoreBuilder properties(FullBlobStoreProperties properties);

    /**
     * 构建并返回配置完成的 BlobStore 实例。
     * <p>
     * 在调用此方法之前，必须确保所有必要的配置都已设置。
     * 构建过程会验证配置的有效性，如果配置不完整或无效，
     * 将抛出相应的异常。
     *
     * @return 配置完成的 BlobStore 实例
     * @throws GeneralPolyStashException 当配置无效或构建失败时抛出
     */
    BlobStore build() throws GeneralPolyStashException;

    /**
     * 验证配置的有效性。
     * <p>
     * 检查提供的配置属性是否满足构建 BlobStore 的要求。
     * 验证包括必填字段检查、格式验证、连接测试等。
     * 如果验证失败，应该抛出相应的异常并提供详细的错误信息。
     *
     * @param properties 要验证的配置属性
     * @throws IllegalArgumentException 当配置无效时抛出
     */
    void validate(FullBlobStoreProperties properties);
}
