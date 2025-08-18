package cc.ddrpa.dorian.polystash.core.blobstore;

/**
 * BlobStore 上下文接口，提供存储实例的上下文信息。
 * <p>
 * 该接口封装了 BlobStore 实例运行所需的上下文信息，包括存储名称、
 * 配置参数等。通过上下文对象，BlobStore 可以访问其运行环境的相关信息。
 * <p>
 * 实现类应该提供必要的上下文信息，以支持 BlobStore 的正常运行
 * 和配置管理。
 */
public interface BlobStoreContext {

    /**
     * 获取 BlobStore 的名称。
     * <p>
     * 存储名称是 BlobStore 实例的唯一标识符，用于区分不同的存储实例。
     * 在日志记录、监控、配置管理和错误处理中起到重要作用。
     *
     * @return BlobStore 的名称标识符
     */
    String getBlobStoreName();
}