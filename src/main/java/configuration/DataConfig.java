package configuration;

import configuration.api.Config;

/**
 * Enumeration regarding data and persistence configuration.
 *
 * @author Christian Groth
 */
public enum DataConfig implements Config {

    DATA_FIRST_SEASON("bulibot.data.firstSeason", false),
    MEDIA_DIR("bulibot.media.dir", false),

    STORAGE_DIR("bulibot.storage.dir", false),
    STORAGE_PRETTY_PRINT("bulibot.storage.prettyPrint", false),

    EVENT_RAW_THRESHOLD("bulibot.event.raw.threshold.days", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    };

    private final String key;
    private final boolean configureable;

    private DataConfig(String key, boolean configureable) {
        this.key = key;
        this.configureable = configureable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getGroup() {
        return "Data";
    }

    @Override
    public boolean isConfigureable() {
        return configureable;
    }
}
