package model.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.chrgroth.jsonstore.store.VersionMigrationHandler;
import flexjson.JSON;

public class User {

    public static final Integer VERSION = 1;
    public static final VersionMigrationHandler[] HANDLERS = new VersionMigrationHandler[] {};

    private long id;
    private String name;
    private String email;
    private String password;
    private LocalDateTime since;
    private String verificationPhrase;
    private boolean verified;
    private LocalDateTime verifiedSince;

    private boolean generated;
    private boolean locked;
    private boolean admin;
    private long failedLogins;

    private String bulibotName;
    @JSON
    private List<BulibotVersion> bulibotVersions = new ArrayList<>();

    private boolean jsonExportBulibotResults;
    private String jsonExportBulibotResultsUrl;

    private boolean slackExportBulibotResults;
    private String slackExportBulibotResultsUrl;

    @JsonIgnore
    @JSON(include = false)
    public BulibotVersion getBulibotLiveVersion() {
        return bulibotVersions.stream().filter(bv -> bv.isLive()).findFirst().orElse(null);
    }

    @JsonIgnore
    @JSON(include = false)
    public List<BulibotVersion> getBulibotUserVersions() {
        return bulibotVersions.stream().filter(bv -> !bv.isSystemTag()).collect(Collectors.toList());
    }

    @JsonIgnore
    @JSON(include = false)

    public BulibotVersion getBulibotUserVersionByName(String name) {
        return bulibotVersions.stream().filter(bv -> !bv.isSystemTag() && StringUtils.equals(name, bv.getName())).findFirst().orElse(null);
    }

    @JsonIgnore
    @JSON(include = false)
    public BulibotVersion getBulibotVersionBySystemTag(String systemTag) {
        return bulibotVersions.stream().filter(bv -> bv.isSystemTag() && StringUtils.equals(systemTag, bv.getName())).findFirst().orElse(null);
    }

    @JsonIgnore
    @JSON(include = false)
    public BulibotVersion getSystemTaggedBulibotVersionBySource(String source) {
        return bulibotVersions.stream().filter(bv -> bv.isSystemTag() && StringUtils.equals(source, bv.getSource())).findFirst().orElse(null);
    }

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

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getSince() {
        return since;
    }

    public void setSince(LocalDateTime since) {
        this.since = since;
    }

    public String getVerificationPhrase() {
        return verificationPhrase;
    }

    public void setVerificationPhrase(String verificationPhrase) {
        this.verificationPhrase = verificationPhrase;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getVerifiedSince() {
        return verifiedSince;
    }

    public void setVerifiedSince(LocalDateTime verifiedSince) {
        this.verifiedSince = verifiedSince;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public long getFailedLogins() {
        return failedLogins;
    }

    public void setFailedLogins(long failedLogins) {
        this.failedLogins = failedLogins;
    }

    public String getBulibotName() {
        return bulibotName;
    }

    public void setBulibotName(String bulibotName) {
        this.bulibotName = bulibotName;
    }

    public List<BulibotVersion> getBulibotVersions() {
        return bulibotVersions;
    }

    public void setBulibotVersions(List<BulibotVersion> bulibotVersions) {
        this.bulibotVersions = bulibotVersions;
    }

    public boolean isJsonExportBulibotResults() {
        return jsonExportBulibotResults;
    }

    public void setJsonExportBulibotResults(boolean jsonExportBulibotResults) {
        this.jsonExportBulibotResults = jsonExportBulibotResults;
    }

    public String getJsonExportBulibotResultsUrl() {
        return jsonExportBulibotResultsUrl;
    }

    public void setJsonExportBulibotResultsUrl(String jsonExportBulibotResultsUrl) {
        this.jsonExportBulibotResultsUrl = jsonExportBulibotResultsUrl;
    }

    public boolean isSlackExportBulibotResults() {
        return slackExportBulibotResults;
    }

    public void setSlackExportBulibotResults(boolean slackExportBulibotResults) {
        this.slackExportBulibotResults = slackExportBulibotResults;
    }

    public String getSlackExportBulibotResultsUrl() {
        return slackExportBulibotResultsUrl;
    }

    public void setSlackExportBulibotResultsUrl(String slackExportBulibotResultsUrl) {
        this.slackExportBulibotResultsUrl = slackExportBulibotResultsUrl;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (email == null ? 0 : email.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "User [name=" + name + ", email=" + email + ", password=*****" + ", since=" + since + ", verificationPhrase=" + verificationPhrase + ", verified=" + verified
                + ", verifiedSince=" + verifiedSince + ", generated=" + generated + ", locked=" + locked + ", admin=" + admin + ", bulibotName=" + bulibotName
                + ", bulibotVersions=" + bulibotVersions + ", jsonExportBulibotResults=" + jsonExportBulibotResults + ", jsonExportBulibotResultsUrl=" + jsonExportBulibotResultsUrl
                + ", slackExportBulibotResults=" + slackExportBulibotResults + ", slackExportBulibotResultsUrl=" + slackExportBulibotResultsUrl + "]";
    }
}
