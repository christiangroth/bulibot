package model;

public class AjaxResult {

    public static AjaxResult ok() {
        return ok(null, null);
    }

    public static AjaxResult ok(Object payload) {
        return ok(null, payload);
    }

    public static AjaxResult ok(String message) {
        return ok(message, null);
    }

    public static AjaxResult ok(String message, Object payload) {
        return new AjaxResult(true, message, payload);
    }

    public static AjaxResult error(String message) {
        return error(message, null);
    }

    public static AjaxResult error(String message, Object payload) {
        return new AjaxResult(false, message, payload);
    }

    private boolean valid;
    private String message;
    private Object payload;

    public AjaxResult() {
        this(true, null, null);
    }

    public AjaxResult(boolean valid, String message, Object payload) {
        this.valid = valid;
        this.message = message;
        this.payload = payload;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
