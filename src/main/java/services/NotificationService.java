package services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.NotificationConfig;
import configuration.NotificationEventConfig;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;

// TODO maybe change to async processing like implemented in metric service
@Singleton
public class NotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private SlackService slackService;

    @Start
    public void start() {
        LOG.info("started notification service.");
    }

    public void send(NotificationEventConfig eventConfig, String... parameters) {

        // check if enabled
        boolean enabled = ninjaProperties.getBooleanWithDefault(NotificationConfig.ENABLED.getKey(), Boolean.FALSE);
        boolean eventEnabled = ninjaProperties.getBooleanWithDefault(eventConfig.getKey(), Boolean.FALSE);
        if (!enabled || !eventEnabled) {
            return;
        }

        // do slack post
        notifyViaSlack(eventConfig.getMessage(), parameters);
    }

    private void notifyViaSlack(String message, String[] parameters) {

        // check if slack enabled
        String slackUrl = ninjaProperties.get(NotificationConfig.SLACK_WEBHOOK.getKey());
        if (StringUtils.isBlank(slackUrl)) {
            return;
        }

        // check for slack username override
        String slackUsername = ninjaProperties.get(NotificationConfig.SLACK_USERNAME.getKey());
        // add parameters to message, if any
        String messageText = message;
        if (parameters != null && parameters.length > 0) {
            messageText = messageText + ": " + String.join(", ", parameters);
        }

        // send via slack
        slackService.send(slackUrl, slackUsername, messageText);
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped notification service.");
    }
}
