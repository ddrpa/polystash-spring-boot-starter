package cc.ddrpa.dorian.polystash.core.exception;

/**
 * 某个操作在当前上下文中不被支持
 */
public class OperationNotSupportedException extends GeneralPolyStashException {

    private final String operation;

    public OperationNotSupportedException(String operation) {
        super(String.format("Operation <%s> is not supported", operation));
        this.operation = operation;
    }

    public OperationNotSupportedException(String operation, Throwable cause) {
        super(String.format("Operation <%s> is not supported", operation), cause);
        this.operation = operation;

    }

    public String getOperation() {
        return operation;
    }
}