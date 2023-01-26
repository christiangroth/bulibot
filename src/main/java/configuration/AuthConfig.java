package configuration;

import configuration.api.Config;

/**
 * Enumeration regarding authorization and security configuration.
 *
 * @author Christian Groth
 */
public enum AuthConfig implements Config {

    ADMIN_VERIFIED_EMAIL("bulibot.admin.verifiedEmail", false),

    AUTH_CONFIG_REGISTRATION_ENABLED_VALUE("authConfig.registrationEnabled.value", true),
    AUTH_CONFIG_DELAY_LOGIN_ENABLED("authConfig.delayLogin.enabled", true),
    AUTH_CONFIG_DELAY_LOGIN_THRESHOLD("authConfig.delayLogin.threshold", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    AUTH_CONFIG_DELAY_LOGIN_SECONDS("authConfig.delayLogin.seconds", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    };

    private final String key;
    private final boolean configureable;

    private AuthConfig(String key, boolean configureable) {
        this.key = key;
        this.configureable = configureable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getGroup() {
        return "Authorization";
    }

    @Override
    public boolean isConfigureable() {
        return configureable;
    }
}
