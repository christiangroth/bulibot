package services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import configuration.BulibotConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import configuration.AuthConfig;
import configuration.BuildConfig;
import configuration.SmartcronConfig;
import configuration.api.Config;
import de.chrgroth.smartcron.api.Smartcron;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
import ninja.utils.NinjaPropertiesImpl;
import services.exception.ConfigPropertyInvalidValueException;
import services.exception.ConfigPropertyMandatoryException;
import services.exception.UnknownConfigGroupException;
import services.exception.UnknownConfigPropertyException;

@Singleton
public class InfoService {
    private static final Logger LOG = LoggerFactory.getLogger(InfoService.class);

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private Injector injector;

    @Inject
    private SmartcronService smartcronService;

    private LocalDateTime started;

    private String buildVersion;

    private Map<String, Class<? extends Config>> groupToConfig = new HashMap<>();

    @Start
    public void init() {

        // set startup time
        LOG.info("initializing info service...");
        started = LocalDateTime.now();

        // build version
        buildVersion = ninjaProperties.get(BuildConfig.BUILD_VERSION.getKey());

        // configs cache
        for (Class<? extends Config> configClass : Config.CONFIGS) {
            for (Config config : configClass.getEnumConstants()) {
                groupToConfig.put(config.getGroup(), configClass);
                continue;
            }
        }

        LOG.info("started info service.");
    }

    // TODO add to UI
    public LocalDateTime getStarted() {
        return started;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public Map<String, Object> getBulibotConfig() {
        return ImmutableMap.of(
                "executionsMatchThreshold", ninjaProperties.getInteger(SmartcronConfig.EXECUTIONS_MATCH_THRESHOLD.getKey()),
                "maxRuntime", ninjaProperties.getInteger(BulibotConfig.SCRIPT_TIMEOUT.getKey())
        );
    }

    public Map<String, Object> getAuthConfig() {
        return ImmutableMap.of("registrationEnabled", isRegistrationEnabled());
    }

    public boolean isRegistrationEnabled() {
        return ninjaProperties.getBooleanWithDefault(AuthConfig.AUTH_CONFIG_REGISTRATION_ENABLED_VALUE.getKey(), Boolean.FALSE).booleanValue();
    }

    public void changeProperty(String group, String key, String value)
            throws UnknownConfigGroupException, UnknownConfigPropertyException, ConfigPropertyMandatoryException, ConfigPropertyInvalidValueException {

        // check config group definition
        Class<? extends Config> configClass = groupToConfig.get(group);
        if (configClass == null) {
            throw new UnknownConfigGroupException("configuration group " + group + " not found!!");
        }

        // check config property definition
        Config config = Arrays.stream(configClass.getEnumConstants()).filter(e -> StringUtils.equals(key, e.getKey())).findFirst().orElse(null);
        if (config == null) {
            throw new UnknownConfigPropertyException("configuration property " + key + " not found in group " + group + "!!");
        }

        // mandatory check
        if (config.isMandatory() && StringUtils.isBlank(value)) {
            throw new ConfigPropertyMandatoryException("configuration property " + key + " in group " + group + " is mandatory!!");
        }

        // convert value to correct type
        if (!config.validate(value)) {
            throw new ConfigPropertyInvalidValueException("configuration property " + key + " in group " + group + " is mandatory!!");
        }

        // set the new value
        ((NinjaPropertiesImpl) ninjaProperties).setProperty(key, value);

        // execute change callback if needed
        Class<? extends Smartcron> changeCallbackType = config.getChangeCallback();
        if (changeCallbackType != null) {
            smartcronService.schedule(injector.getInstance(changeCallbackType));
        }
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped info service.");
    }
}
