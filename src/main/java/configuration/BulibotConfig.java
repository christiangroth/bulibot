package configuration;

import configuration.api.Config;
import de.chrgroth.smartcron.api.Smartcron;

/**
 * Enumeration regarding bulibot and script execution configuration.
 *
 * @author Christian Groth
 */
public enum BulibotConfig implements Config {

    PARROT_ENABLED("bulibot.parrot.enabled", false),
    PARROT_NAME("bulibot.parrot.name", false),
    PARROT_BULIBOT_NAME("bulibot.parrot.bulibotName", false),
    PARROT_SOURCE("bulibot.parrot.source", false),

    BULIBOT_DEFAULT_VERSION_NAME("bulibot.default.versionName", false),
    BULIBOT_DEFAULT_SOURCE("bulibot.default.source", false),

    SCRIPT_CACHE_SIZE("bulibot.script.cache.size", false),
    SCRIPT_TIMEOUT("bulibot.script.timeout", false),

    POINTS_EXACT("bulibot.points.exact", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }

        @Override
        public Class<? extends Smartcron> getChangeCallback() {
            return UpdateRankingsPropertyChangeCallback.class;
        }
    },
    POINTS_RELATIVE("bulibot.points.relative", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }

        @Override
        public Class<? extends Smartcron> getChangeCallback() {
            return UpdateRankingsPropertyChangeCallback.class;
        }
    },
    POINTS_WINNER("bulibot.points.winner", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }

        @Override
        public Class<? extends Smartcron> getChangeCallback() {
            return UpdateRankingsPropertyChangeCallback.class;
        }
    },
    POINTS_NOTHING("bulibot.points.nothing", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }

        @Override
        public Class<? extends Smartcron> getChangeCallback() {
            return UpdateRankingsPropertyChangeCallback.class;
        }
    };

    private final String key;
    private final boolean configureable;

    private BulibotConfig(String key, boolean configureable) {
        this.key = key;
        this.configureable = configureable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getGroup() {
        return "Bulibot";
    }

    @Override
    public boolean isConfigureable() {
        return configureable;
    }
}
