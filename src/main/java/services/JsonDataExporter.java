package services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import de.chrgroth.smartcron.api.Smartcron;
import de.chrgroth.smartcron.api.SmartcronExecutionContext;

public class JsonDataExporter implements Smartcron {
    private static final Logger LOG = LoggerFactory.getLogger(JsonDataExporter.class);

    private static final int HTTP_OK = 200;
    private static final String HTTP_HEADER_CONTENT_TYPE = "content-type";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    public JsonDataExporter(String url, String data, int retryCount, int retryDelaySeconds) {
        this.url = url;
        this.data = data;
        retries = 0;
        this.retryCount = retryCount;
        this.retryDelaySeconds = retryDelaySeconds;
    }

    private String url;
    private String data;
    private int retries;
    private final int retryCount;
    private int retryDelaySeconds;

    @Override
    public boolean executionHistory() {
        return false;
    }

    @Override
    public LocalDateTime run(SmartcronExecutionContext context) {

        // send data
        boolean sent = false;
        try {
            LOG.info("sending data to : " + url);
            HttpResponse<String> response = Unirest.post(url).header(HTTP_HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON).body(data).asString();
            if (response.getStatus() == HTTP_OK) {
                sent = true;
            } else {
                LOG.error("POST response to '" + url + "' was not successful " + response.getStatus() + ": " + response.getStatusText());
            }
        } catch (Exception e) {
            LOG.error("unable to execute POST to '" + url + "': " + e.getMessage());
        }

        // check if sent
        if (sent) {
            LOG.info("data was sent successfully");
            return abort();
        }

        // check retry
        retries++;
        if (retries >= retryCount) {
            final int retrySeconds = retries * retryDelaySeconds;
            LOG.error(url + "sending data failed, retrying in " + retrySeconds + " seconds.");
            return delay(retrySeconds, ChronoUnit.SECONDS);
        } else {
            LOG.error(url + "sending data failed in all attempty, giving up!!");
            return abort();
        }
    }
}
