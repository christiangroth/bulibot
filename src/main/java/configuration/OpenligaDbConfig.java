package configuration;

import configuration.api.Config;

/**
 * Enumeration regarding bulibot and script execution configuration.
 *
 * @author Christian Groth
 */
public enum OpenligaDbConfig implements Config {

    URL_BASE("openligadb.url.base", true),
    URL_MATCHDATA_POSTFIX("openligadb.url.matchdata", true),
    TEAM_SHORT_NAMES("openligadb.team.shortNames", true);

    private final String key;
    private final boolean configureable;

    private OpenligaDbConfig(String key, boolean configureable) {
        this.key = key;
        this.configureable = configureable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getGroup() {
        return "OpenligaDb";
    }

    @Override
    public boolean isConfigureable() {
        return configureable;
    }
}
