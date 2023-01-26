package model.match;

import org.apache.commons.lang3.StringUtils;

import de.chrgroth.jsonstore.store.VersionMigrationHandler;
import flexjson.JSON;

public class Team {

    public static final Integer VERSION = 2;
    public static final VersionMigrationHandler[] HANDLERS = new VersionMigrationHandler[] {};

    private long id;
    private String name;
    private String shortName;
    private String iconUrl;

    @JSON(include = false)
    public String getDisplayName() {
        return StringUtils.isNotBlank(shortName) ? shortName : name;
    }

    public String getLocalFileName() {
        return id + iconUrl.substring(iconUrl.lastIndexOf("."));
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ id >>> 32);
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
        Team other = (Team) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Team [id=" + id + ", name=" + name + ", shortName=" + shortName + ", iconUrl=" + iconUrl + "]";
    }
}
