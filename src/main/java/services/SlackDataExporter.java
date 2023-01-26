package services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

import de.chrgroth.smartcron.api.Smartcron;
import de.chrgroth.smartcron.api.SmartcronExecutionContext;

public class SlackDataExporter implements Smartcron {
    private static final Logger LOG = LoggerFactory.getLogger(SlackDataExporter.class);

    public SlackDataExporter(String url, String user, String data, int retryCount, int retryDelaySeconds, Provider<SlackService> slackServiceProvider) {
        this.url = url;
        this.user = user;
        this.data = data;
        retries = 0;
        this.retryCount = retryCount;
        this.retryDelaySeconds = retryDelaySeconds;
        this.slackServiceProvider = slackServiceProvider;
    }

    private String url;
    private String user;
    private String data;
    private int retries;
    private final int retryCount;
    private int retryDelaySeconds;
    private Provider<SlackService> slackServiceProvider;

    @Override
    public boolean executionHistory() {
        return false;
    }

    @Override
    public LocalDateTime run(SmartcronExecutionContext context) {

        // send data
        boolean sent = false;
        try {
            sent = slackServiceProvider.get().send(url, user, data);
        } catch (Exception e) {
            LOG.error("unable to execute POST to '" + url + "': " + e.getMessage());
        }

        // check if sent
        if (sent) {
            LOG.info(url + "data was sent successfully");
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
