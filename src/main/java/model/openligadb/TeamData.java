package model.openligadb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamData {

    @JsonProperty("TeamId")
    private long id;
    @JsonProperty("TeamName")
    private String name;
    @JsonProperty("ShortName")
    private String shortName;
    @JsonProperty("TeamIconUrl")
    private String iconUrl;

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
    public String toString() {
        return "TeamData [id=" + id + ", name=" + name + ", shortName=" + shortName + ", iconUrl=" + iconUrl + "]";
    }
}
