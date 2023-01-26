package services.exception;

public class UnknownConfigGroupException extends Exception {
    private static final long serialVersionUID = 3075412902729420492L;

    public UnknownConfigGroupException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownConfigGroupException(String message) {
        super(message);
    }

    public UnknownConfigGroupException(Throwable cause) {
        super(cause);
    }
}
