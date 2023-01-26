package services.exception;

public class ConfigPropertyInvalidValueException extends Exception {
    private static final long serialVersionUID = 3075412902729420492L;

    public ConfigPropertyInvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigPropertyInvalidValueException(String message) {
        super(message);
    }

    public ConfigPropertyInvalidValueException(Throwable cause) {
        super(cause);
    }
}
