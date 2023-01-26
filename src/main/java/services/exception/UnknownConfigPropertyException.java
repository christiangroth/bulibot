package services.exception;

public class UnknownConfigPropertyException extends Exception {
    private static final long serialVersionUID = 3075412902729420492L;

    public UnknownConfigPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownConfigPropertyException(String message) {
        super(message);
    }

    public UnknownConfigPropertyException(Throwable cause) {
        super(cause);
    }
}
