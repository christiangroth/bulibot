package services.model;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// TODO add (and save) execution statistics
public class ScriptServiceResult {
    private boolean executed;
    private final Writer stdout = new StringWriter();
    private final Map<String, String> state = new TreeMap<>();
    private boolean success = true;
    private Throwable error;
    private final Map<String, Object> returnValues = new HashMap<>();
    private long duration;

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public Writer getStdout() {
        return stdout;
    }

    public Map<String, String> getState() {
        return state;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Map<String, Object> getReturnValues() {
        return returnValues;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
