package configuration;

import configuration.api.Config;

/**
 * Enumeration regarding notification events configuration.
 *
 * @author Christian Groth
 */
public enum NotificationEventConfig implements Config {

    SYSTEM_STARTUP_RUNLEVEL("notifications.event.systemStartup.runlevel", "Entering runlevel"),
    SYSTEM_STARTUP("notifications.event.systemStartup", "Started."),
    SYSTEM_SHUTDOWN("notifications.event.systemShutdown", "Shut down."),

    USER_CREATED("notifications.event.userCreated", "User created"),
    USER_VERIFIED("notifications.event.userVerified", "User verified"),

    SEASON_CHANGED("notifications.event.seasonChanged", "Season changed"),
    MATCHDAY_CHANGED("notifications.event.matchdayChanged", "Matchday changed"),

    BULIBOTS_EXECUTED("notifications.event.bulibotsExecuted", "Bulibots executed"),

    MAIL_FAILURE("notifications.event.mailingFailure", "Mailing failure"),

    OPENLIGADB_MATCHDATA_FAILED("notifications.event.openligaDbMatchDataFailure", "OpenligaDB match data failure"),
    OPENLIGADB_TEAM_IMAGE_FAILED("notifications.event.openligaDbTeamImageFailure", "Team image download failure");

    private final String key;
    private final String message;

    private NotificationEventConfig(String key, String message) {
        this.key = key;
        this.message = message;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getGroup() {
        return "Notification Events";
    }

    @Override
    public boolean isConfigureable() {
        return true;
    }

    public String getMessage() {
        return message;
    }
}
