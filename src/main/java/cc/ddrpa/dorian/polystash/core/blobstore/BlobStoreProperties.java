package cc.ddrpa.dorian.polystash.core.blobstore;

/**
 * BlobStore 属性接口，定义存储实例的基本配置属性。
 * <p>
 * 该接口提供了 BlobStore 实例的基本配置信息，包括是否为主存储、
 * 限定符等。这些属性用于在多个存储实例之间进行区分和选择。
 * <p>
 * 实现类应该提供这些基本属性的具体值，以支持存储实例的管理
 * 和配置。
 */
public interface BlobStoreProperties {

    /**
     * 检查当前 BlobStore 是否为主存储实例。
     * <p>
     * 主存储实例通常用于默认的存储操作，当应用程序没有明确指定
     * 使用哪个存储实例时，会使用主存储实例。在配置中应该只有一个
     * 存储实例被标记为主存储。
     *
     * @return true 如果当前实例是主存储，false 如果不是
     */
    boolean isPrimary();

    /**
     * 获取 BlobStore 的限定符。
     * <p>
     * 限定符用于在多个存储实例之间进行区分，特别是在 Spring 容器中。
     * 限定符通常是一个描述性的字符串，如 "local"、"s3"、"backup" 等。
     * 当需要注入特定的存储实例时，可以使用限定符进行选择。
     *
     * @return 存储实例的限定符标识符
     */
    String getQualifier();
}