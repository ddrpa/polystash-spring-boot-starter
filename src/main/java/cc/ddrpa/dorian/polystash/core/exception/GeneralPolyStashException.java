package cc.ddrpa.dorian.polystash.core.exception;

/**
 * PolyStash 通用异常基类，为所有 PolyStash 相关异常提供基础结构。
 * <p>
 * 该类是所有 PolyStash 特定异常的父类，提供了统一的异常处理机制。
 * 继承自 Exception 类，支持异常消息和原因异常的传递。
 * <p>
 * 具体的异常类型应该继承此类，并提供特定的异常处理逻辑和错误信息。
 * 通过统一的异常层次结构，调用方可以更容易地捕获和处理不同类型的异常。
 */
public abstract class GeneralPolyStashException extends Exception {

    /**
     * 构造函数，使用指定的错误消息创建异常实例。
     * <p>
     * 创建一个包含错误描述信息的异常，不包含原因异常。
     *
     * @param message 描述异常情况的错误消息
     */
    public GeneralPolyStashException(String message) {
        super(message);
    }

    /**
     * 构造函数，使用指定的错误消息和原因异常创建异常实例。
     * <p>
     * 创建一个包含错误描述信息和原因异常的异常，便于异常链的追踪和调试。
     *
     * @param message 描述异常情况的错误消息
     * @param cause   导致此异常的原因异常
     */
    public GeneralPolyStashException(String message, Throwable cause) {
        super(message, cause);
    }
}
