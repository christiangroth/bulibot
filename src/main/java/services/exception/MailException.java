package services.exception;

public class MailException extends Exception {
    private static final long serialVersionUID = 3075412902729420492L;

    public MailException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailException(String message) {
        super(message);
    }

    public MailException(Throwable cause) {
        super(cause);
    }
}
