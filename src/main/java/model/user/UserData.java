package model.user;

import java.time.LocalDateTime;

public class UserData {
    private long id;
    private String name;
    private String email;
    private LocalDateTime since;
    private String bulibotName;
    private boolean jsonExportBulibotResultsUrlEnabled;
    private String jsonExportBulibotResultsUrl;
    private boolean slackExportBulibotResultsUrlEnabled;
    private String slackExportBulibotResultsUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getSince() {
        return since;
    }

    public void setSince(LocalDateTime since) {
        this.since = since;
    }

    public String getBulibotName() {
        return bulibotName;
    }

    public void setBulibotName(String bulibotName) {
        this.bulibotName = bulibotName;
    }

    public boolean isJsonExportBulibotResultsUrlEnabled() {
        return jsonExportBulibotResultsUrlEnabled;
    }

    public void setJsonExportBulibotResultsUrlEnabled(boolean jsonExportBulibotResultsUrlEnabled) {
        this.jsonExportBulibotResultsUrlEnabled = jsonExportBulibotResultsUrlEnabled;
    }

    public String getJsonExportBulibotResultsUrl() {
        return jsonExportBulibotResultsUrl;
    }

    public void setJsonExportBulibotResultsUrl(String jsonExportBulibotResultsUrl) {
        this.jsonExportBulibotResultsUrl = jsonExportBulibotResultsUrl;
    }

    public boolean isSlackExportBulibotResultsUrlEnabled() {
        return slackExportBulibotResultsUrlEnabled;
    }

    public void setSlackExportBulibotResultsUrlEnabled(boolean slackExportBulibotResultsUrlEnabled) {
        this.slackExportBulibotResultsUrlEnabled = slackExportBulibotResultsUrlEnabled;
    }

    public String getSlackExportBulibotResultsUrl() {
        return slackExportBulibotResultsUrl;
    }

    public void setSlackExportBulibotResultsUrl(String slackExportBulibotResultsUrl) {
        this.slackExportBulibotResultsUrl = slackExportBulibotResultsUrl;
    }
}
