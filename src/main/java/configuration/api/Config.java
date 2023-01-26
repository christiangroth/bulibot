package configuration.api;

import java.util.Arrays;
import java.util.List;

import configuration.AuthConfig;
import configuration.BuildConfig;
import configuration.BulibotConfig;
import configuration.DataConfig;
import configuration.NotificationConfig;
import configuration.NotificationEventConfig;
import configuration.OpenligaDbConfig;
import configuration.SmartcronConfig;
import configuration.SystemConfig;
import de.chrgroth.smartcron.api.Smartcron;

/**
 * Basic interface for all configuration entries.
 *
 * @author Christian Groth
 */
public interface Config {

    String GROUP_UNBOUND = "Unbound";
    List<Class<? extends Config>> CONFIGS = Arrays.asList(AuthConfig.class, BuildConfig.class, BulibotConfig.class, DataConfig.class, NotificationConfig.class,
            NotificationEventConfig.class, OpenligaDbConfig.class, SmartcronConfig.class, SystemConfig.class);

    /**
     * Returns the key used in application.conf file.
     *
     * @return config key
     */
    String getKey();

    /**
     * Returns a logical group for this configuration key.
     *
     * @return group
     */
    String getGroup();

    /**
     * Returns true if the property is configureable at runtime. Be sure to not cache those property values in your code.
     *
     * @return true if configureable during runtime, false otherwise
     */
    boolean isConfigureable();

    /**
     * Returns true if the property is mandatory. This is only checked if {@link #isConfigureable()} returns true.
     *
     * @return true if mandatory, false otherwise
     */
    default boolean isMandatory() {
        return true;
    }

    /**
     * Called to validate a new value that is set at runtime.
     *
     * @return true if valid, false otherwise
     */
    default boolean validate(String value) {
        return true;
    }

    /**
     * Helper method to check for no negative int value.
     *
     * @param value
     *            the value to validate
     * @return
     */
    default boolean validateNonNegativeInt(String value) {
        try {
            int intValue = Integer.parseInt(value);
            return intValue >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns a smartcron type to be executed after successful change at runtime.
     *
     * @return change callback or null for no action
     */
    default Class<? extends Smartcron> getChangeCallback() {
        return null;
    }
}
