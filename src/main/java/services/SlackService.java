package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import de.chrgroth.jsonstore.json.FlexjsonHelper;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import services.model.SlackMessage;

@Singleton
public class SlackService {
    private static final Logger LOG = LoggerFactory.getLogger(SlackService.class);

    private static final int HTTP_OK = 200;

    private FlexjsonHelper flexjsonHelper;

    @Start
    public void start() {
        LOG.info("configuring slack service...");
        flexjsonHelper = FlexjsonHelper.builder().build();
        LOG.info("started slack service.");
    }

    public boolean send(String url, String username, String message) {

        // build slack message
        SlackMessage slackMessage = new SlackMessage(username, message);
        final String slackMessageJson = toJson(slackMessage);

        // do slack POST request
        try {
            HttpResponse<String> response = Unirest.post(url).body(slackMessageJson).asString();
            if (response.getStatus() != HTTP_OK) {
                LOG.error("slack notification unsuccessful: " + response.getStatus() + " " + response.getStatusText());
                return false;
            }

            // done
            return true;
        } catch (UnirestException e) {
            LOG.error("slack notification failed: " + e.getMessage());
            return false;
        }
    }

    public String toJson(Object exportData) {
        return flexjsonHelper.serializer(true).exclude("*.class").serialize(exportData);
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped slack service.");
    }
}
