package model.openligadb;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchResultData {

    public static final String NAME_HALF_TIME = "Halbzeitergebnis";
    public static final String NAME_FULL_TIME = "Endergebnis";

    @JsonProperty("ResultID")
    private long id;
    @JsonProperty("ResultOrderID")
    private int orderId;
    @JsonProperty("ResultName")
    private String name;
    @JsonProperty("ResultDescription")
    private String description;
    @JsonProperty("PointsTeam1")
    private int pointsTeamOne;
    @JsonProperty("PointsTeam2")
    private int pointsTeamTwo;

    public boolean isHalfTime() {
        return StringUtils.equals(NAME_HALF_TIME, name);
    }

    public boolean isFullTime() {
        return StringUtils.equals(NAME_FULL_TIME, name);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPointsTeamOne() {
        return pointsTeamOne;
    }

    public void setPointsTeamOne(int pointsTeamOne) {
        this.pointsTeamOne = pointsTeamOne;
    }

    public int getPointsTeamTwo() {
        return pointsTeamTwo;
    }

    public void setPointsTeamTwo(int pointsTeamTwo) {
        this.pointsTeamTwo = pointsTeamTwo;
    }

    @Override
    public String toString() {
        return "MatchResultData [id=" + id + ", orderId=" + orderId + ", name=" + name + ", description=" + description + ", pointsTeamOne=" + pointsTeamOne + ", pointsTeamTwo="
                + pointsTeamTwo + "]";
    }
}
