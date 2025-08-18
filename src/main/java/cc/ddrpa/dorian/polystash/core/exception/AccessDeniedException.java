package cc.ddrpa.dorian.polystash.core.exception;

/**
 * 访问被拒绝异常，表示当前操作没有足够的权限访问请求的资源。
 * <p>
 * 当尝试访问超出权限范围的文件、目录或存储资源时，会抛出此异常。
 * 常见的情况包括：
 * <ul>
 *   <li>访问超出基础目录范围的文件路径</li>
 *   <li>没有读取或写入特定文件的权限</li>
 *   <li>存储账户权限不足</li>
 *   <li>访问被策略或配置限制的资源</li>
 * </ul>
 * <p>
 * 此异常通常表示配置问题或权限设置问题，而不是系统错误。
 * 调用方应该检查访问路径和权限配置。
 */
public class AccessDeniedException extends GeneralPolyStashException {

    /**
     * 构造函数，使用指定的错误消息创建访问被拒绝异常。
     *
     * @param message 描述访问被拒绝原因的详细错误消息
     */
    public AccessDeniedException(String message) {
        super(message);
    }

    /**
     * 构造函数，使用指定的错误消息和原因异常创建访问被拒绝异常。
     *
     * @param message 描述访问被拒绝原因的详细错误消息
     * @param cause   导致此异常的根本原因异常
     */
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}