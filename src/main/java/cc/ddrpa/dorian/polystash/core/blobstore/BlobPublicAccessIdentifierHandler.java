package cc.ddrpa.dorian.polystash.core.blobstore;

/**
 * Blob 公共访问标识符处理器接口，用于生成对象的公共访问链接。
 * <p>
 * 该接口定义了如何为存储对象生成公共访问标识符（通常是 URL）。
 * 不同的存储后端可能需要不同的访问策略，如预签名 URL、
 * 公共访问链接、CDN 链接等。
 * <p>
 * 实现类应该根据具体的存储后端和访问策略，生成相应的访问标识符。
 * 该接口被标记为函数式接口，可以使用 Lambda 表达式或方法引用来实现。
 */
@FunctionalInterface
public interface BlobPublicAccessIdentifierHandler {

    /**
     * 获取指定对象的公共访问标识符。
     * <p>
     * 根据存储上下文和对象名称，生成可用于公共访问的标识符。
     * 返回的标识符通常是完整的 URL，可以直接用于访问对象内容。
     * <p>
     * 实现应该考虑访问权限、过期时间、认证要求等因素，
     * 确保生成的标识符既安全又实用。
     *
     * @param context    BlobStore 的上下文信息，包含存储配置等
     * @param objectName 要生成公共访问标识符的对象名称
     * @return 对象的公共访问标识符（通常是 URL）
     */
    String get(BlobStoreContext context, String objectName);
}