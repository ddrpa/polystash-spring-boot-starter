package cc.ddrpa.dorian.polystash.core.exception;

/**
 * 数据在处理过程中被破坏或不完整
 */
public class DataCorruptionException extends GeneralPolyStashException {
    public DataCorruptionException(String message) {
        super(message);
    }

    public DataCorruptionException(String message, Throwable cause) {
        super(message, cause);
    }
}