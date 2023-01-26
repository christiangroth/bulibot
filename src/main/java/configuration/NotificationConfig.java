package configuration;

import configuration.api.Config;

/**
 * Enumeration regarding notification configuration.
 *
 * @author Christian Groth
 */
public enum NotificationConfig implements Config {

    ENABLED("notification.enabled", true), 
    SLACK_WEBHOOK("notification.slack.url", true), 
    SLACK_USERNAME("notification.slack.username", true);

    private final String key;
    private final boolean configureable;

    private NotificationConfig(String key, boolean configureable) {
        this.key = key;
        this.configureable = configureable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getGroup() {
        return "Notification";
    }

    @Override
    public boolean isConfigureable() {
        return configureable;
    }
}
