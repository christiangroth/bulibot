package configuration;

import configuration.api.Config;

/**
 * Enumeration regarding general system configuration.
 *
 * @author Christian Groth
 */
public enum SystemConfig implements Config {

    HOST("bulibot.host", true),

    SMTP_USER("smtp.user", false),
    API_CONFIG_HIDE("bulibot.api.config.hide", false);

    private final String key;
    private final boolean configureable;

    private SystemConfig(String key, boolean configureable) {
        this.key = key;
        this.configureable = configureable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getGroup() {
        return "System";
    }

    @Override
    public boolean isConfigureable() {
        return configureable;
    }
}
