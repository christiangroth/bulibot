package configuration;

import configuration.api.Config;

/**
 * Enumeration regarding build information configuration.
 *
 * @author Christian Groth
 */
public enum BuildConfig implements Config {

    BUILD_OS_NAME("build.os.name"),
    BUILD_OS_VERSION("build.os.version"),
    BUILD_OS_ARCH("build.os.arch"),

    BUILD_JAVA_VENDOR("build.java.vendor"),
    BUILD_JAVA_VERSION("build.java.version"),

    BUILD_SCM_BRANCH("build.scm.branch"),
    BUILD_SCM_COMMIT_ID("build.scm.lastcommit.id"),
    BUILD_SCM_COMMIT_USER_NAME("build.scm.lastcommit.user.name"),
    BUILD_SCM_COMMIT_USER_EMAIL("build.scm.lastcommit.user.email"),
    BUILD_SCM_COMMIT_MESSAGE("build.scm.lastcommit.message"),
    BUILD_SCM_COMMIT_TIME("build.scm.lastcommit.time"),

    BUILD_HOST("build.host"),
    BUILD_TIME("build.time"),
    BUILD_VERSION("build.version");

    private final String key;

    private BuildConfig(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getGroup() {
        return "Build";
    }

    @Override
    public boolean isConfigureable() {
        return false;
    }
}
