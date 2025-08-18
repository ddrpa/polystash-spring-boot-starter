package cc.ddrpa.dorian.polystash.core.exception;

/**
 * IO 错误异常，表示文件读写操作失败。
 * <p>
 * 当存储操作过程中发生 IO 相关错误时，会抛出此异常。常见的 IO 错误包括：
 * <ul>
 *   <li>磁盘空间不足或磁盘损坏</li>
 *   <li>网络连接中断或超时</li>
 *   <li>文件系统权限问题</li>
 *   <li>硬件设备故障</li>
 *   <li>文件被其他进程锁定</li>
 * </ul>
 * <p>
 * 此异常通常表示系统级问题或硬件问题，调用方应该检查系统状态
 * 或重试操作。
 * </p>
 */
public class IOErrorOccursException extends GeneralPolyStashException {

    /**
     * 构造函数，使用指定的错误消息创建 IO 错误异常。
     *
     * @param message 描述 IO 错误原因的详细错误消息
     */
    public IOErrorOccursException(String message) {
        super(message);
    }

    /**
     * 构造函数，使用指定的错误消息和原因异常创建 IO 错误异常。
     *
     * @param message 描述 IO 错误原因的详细错误消息
     * @param cause   导致此异常的根本原因异常
     */
    public IOErrorOccursException(String message, Throwable cause) {
        super(message, cause);
    }
}