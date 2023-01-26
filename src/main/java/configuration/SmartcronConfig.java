package configuration;

import configuration.api.Config;

/**
 * Enumeration regarding smartcron configurations.
 *
 * @author Christian Groth
 */
public enum SmartcronConfig implements Config {

    OPENLIGADB_DELAY_MATCH_IN_PROGRESS("bulibot.smartcron.openligadb.delay.matchInProgress", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    OPENLIGADB_DAILY_HOUR("bulibot.smartcron.openligadb.daily.hour", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    OPENLIGADB_DAILY_MINUTE("bulibot.smartcron.openligadb.daily.minute", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    OPENLIGADB_RECOVERY("bulibot.smartcron.openligadb.recoveryTime", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },

    CURRENTSEASONMATCHDAY_RECOVERY("bulibot.smartcron.currentSeasonMatchday.recoveryTime", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },

    PARROTEXECUTIONS_NO_SEASON("bulibot.smartcron.bulibotParrotExecutions.noSeason", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },

    EXECUTIONS_DAILY_HOUR("bulibot.smartcron.bulibotExecutions.daily.hour", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    EXECUTIONS_DAILY_MINUTE("bulibot.smartcron.bulibotExecutions.daily.minute", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    EXECUTIONS_MATCH_THRESHOLD("bulibot.smartcron.bulibotExecutions.matchThreshold", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    EXECUTIONS_RECOVERY("bulibot.smartcron.bulibotExecutions.recoveryTime", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },

    PERSIST_METRICS_DELAY("bulibot.smartcron.persistMetrics.delay", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },

    COMPACT_METRICS_DAILY_HOUR("bulibot.smartcron.compactMetrics.daily.hour", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    COMPACT_METRICS_DAILY_MINUTE("bulibot.smartcron.compactMetrics.daily.minute", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    RESULT_EXPORT_JSON_RETRY_COUNT("bulibot.smartcron.resultJsonExport.retry", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    RESULT_EXPORT_JSON_RETRY_DELAY_SECONDS("bulibot.smartcron.resultJsonExport.retry.seconds", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    RESULT_EXPORT_SLACK_RETRY_COUNT("bulibot.smartcron.resultSlackExport.retry", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    RESULT_EXPORT_SLACK_RETRY_DELAY_SECONDS("bulibot.smartcron.resultSlackExport.retry.seconds", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    RANKING_EXPORT_SLACK_RETRY_COUNT("bulibot.smartcron.rankingSlackExport.retry", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    },
    RANKING_EXPORT_SLACK_RETRY_DELAY_SECONDS("bulibot.smartcron.rankingSlackExport.retry.seconds", true) {

        @Override
        public boolean validate(String value) {
            return validateNonNegativeInt(value);
        }
    };

    private final String key;
    private final boolean configureable;

    private SmartcronConfig(String key, boolean configureable) {
        this.key = key;
        this.configureable = configureable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getGroup() {
        return "Smartcrons";
    }

    @Override
    public boolean isConfigureable() {
        return configureable;
    }
}
