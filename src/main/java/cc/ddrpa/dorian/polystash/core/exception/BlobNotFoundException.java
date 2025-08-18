package cc.ddrpa.dorian.polystash.core.exception;

/**
 * Blob 对象未找到异常，表示无法在存储系统中找到指定的 Blob 对象。
 * <p>
 * 当尝试访问不存在的存储对象时，会抛出此异常。可能的原因包括：
 * <ul>
 *   <li>对象名称拼写错误或路径不正确</li>
 *   <li>对象已被删除或移动</li>
 *   <li>对象存储在不同的存储桶或目录中</li>
 *   <li>存储系统配置问题导致对象不可见</li>
 * </ul>
 * <p>
 * 此异常通常表示业务逻辑问题，调用方应该检查对象名称的正确性
 * 或确认对象是否仍然存在。
 */
public class BlobNotFoundException extends GeneralPolyStashException {

    /**
     * 构造函数，使用指定的错误消息创建 Blob 未找到异常。
     *
     * @param message 描述 Blob 未找到原因的详细错误消息
     */
    public BlobNotFoundException(String message) {
        super(message);
    }

    /**
     * 构造函数，使用指定的错误消息和原因异常创建 Blob 未找到异常。
     *
     * @param message 描述 Blob 未找到原因的详细错误消息
     * @param cause   导致此异常的根本原因异常
     */
    public BlobNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}