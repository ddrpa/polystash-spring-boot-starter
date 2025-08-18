package cc.ddrpa.dorian.polystash.core.exception;

/**
 * 存储空间已满或超过限制
 */
public class StorageQuotaExceededException extends GeneralPolyStashException {
    public StorageQuotaExceededException(String message) {
        super(message);
    }

    public StorageQuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}