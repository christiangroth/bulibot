package services.exception;

public class ConfigPropertyMandatoryException extends Exception {
    private static final long serialVersionUID = 3075412902729420492L;

    public ConfigPropertyMandatoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigPropertyMandatoryException(String message) {
        super(message);
    }

    public ConfigPropertyMandatoryException(Throwable cause) {
        super(cause);
    }
}
