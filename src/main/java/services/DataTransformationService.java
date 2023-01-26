package services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.ConfigPropertyData;
import configuration.OpenligaDbConfig;
import configuration.SystemConfig;
import configuration.api.Config;
import model.match.Goal;
import model.match.Match;
import model.match.Match.Status;
import model.match.Team;
import model.openligadb.MatchData;
import model.openligadb.MatchGoalData;
import model.openligadb.MatchResultData;
import model.openligadb.TeamData;
import model.user.BulibotSourceData;
import model.user.BulibotVersion;
import model.user.User;
import model.user.UserData;
import model.user.UserNamesData;
import ninja.Route;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
import util.DateUtils;

// TODO get rid of this, use some kind of data transformation / mapping framework
@Singleton
public class DataTransformationService {
    private static final Logger LOG = LoggerFactory.getLogger(DataTransformationService.class);

    public static final Comparator<MatchGoalData> MATCH_GOAL_MINUTE_ASC = (o1, o2) -> new Integer(o1.getMatchMinute()).compareTo(new Integer(o2.getMatchMinute()));

    private final Map<String, String> teamShortNames = new HashMap<>();

    @Inject
    private NinjaProperties ninjaProperties;

    @Start
    public void startup() {
        LOG.info("starting data transformation service...");

        // convert team shot names
        String[] shortNamesRaw = ninjaProperties.getStringArray(OpenligaDbConfig.TEAM_SHORT_NAMES.getKey());
        if (shortNamesRaw != null && shortNamesRaw.length > 1) {
            for (String definition : shortNamesRaw) {
                String[] parts = definition.split(":");
                if (parts != null && parts.length == 2) {
                    teamShortNames.put(parts[0], parts[1]);
                } else {
                    LOG.error("Unable to load team short name definition: '" + definition + "'!!");
                }
            }
            LOG.info("using team short names: " + teamShortNames);
        }

        LOG.info("started data transformation service.");
    }

    public Set<Team> teams(List<MatchData> matchData) {

        // null guard
        Set<Team> result = new HashSet<>();
        if (matchData == null) {
            return result;
        }

        // convert
        for (MatchData m : matchData) {
            result.add(team(m.getTeamOne()));
            result.add(team(m.getTeamTwo()));
        }

        // done
        return result;
    }

    private Team team(TeamData teamData) {

        // copy data
        Team team = new Team();
        team.setId(teamData.getId());
        team.setName(teamData.getName());
        if (teamShortNames.containsKey(team.getName())) {
            team.setShortName(teamShortNames.get(team.getName()));
        } else if (StringUtils.isNotBlank(teamData.getShortName())) {
            LOG.warn("using openligadb team short name for " + team.getName());
            team.setShortName(teamData.getShortName());
        } else {
            LOG.error("no team short name found for " + team.getName() + "!!");
        }
        team.setIconUrl(teamData.getIconUrl());

        // done
        return team;
    }

    public Set<Match> matches(List<MatchData> matchData) {

        // null guard
        Set<Match> result = new HashSet<>();
        if (matchData == null) {
            return result;
        }

        // convert
        for (MatchData m : matchData) {
            result.add(match(m));
        }

        // done
        return result;
    }

    public Match match(MatchData matchData) {

        // copy data
        Match match = new Match();
        match.setId(matchData.getId());
        match.setSeason(matchData.getSeason());
        match.setMatchday(matchData.getMatchday());
        match.setAssignedTime(DateUtils.parseOpenligaDbDate(matchData.getDateTimeString()));
        match.setLastUpdateTimeString(matchData.getLastUpdateString());
        if (matchData.getTeamOne() != null) {
            match.setTeamOneId(matchData.getTeamOne().getId());
            match.setTeamOneName(matchData.getTeamOne().getName());
            match.setTeamOneShortName(team(matchData.getTeamOne()).getShortName());
            match.setTeamOneIconUrl(matchData.getTeamOne().getIconUrl());
        }
        if (matchData.getTeamTwo() != null) {
            match.setTeamTwoId(matchData.getTeamTwo().getId());
            match.setTeamTwoName(matchData.getTeamTwo().getName());
            match.setTeamTwoShortName(team(matchData.getTeamTwo()).getShortName());
            match.setTeamTwoIconUrl(matchData.getTeamTwo().getIconUrl());
        }

        // set status
        boolean hasResults = matchData.getResults() != null && !matchData.getResults().isEmpty();
        if (matchData.isFinished()) {
            match.setStatus(Status.FINISHED);
        } else if (hasResults) {
            match.setStatus(Status.IN_PROGRESS);
        } else {

            // check if match might have been started
            boolean mightHaveStarted = match.getAssignedTime().isBefore(LocalDateTime.now());
            match.setStatus(mightHaveStarted ? Status.IN_PROGRESS : Status.WAITING);
        }

        // process results
        if (hasResults) {

            // convert half time result, also set for current full time result
            MatchResultData halfTimeResult = matchData.getResults().stream().filter(r -> r.isHalfTime()).findFirst().orElse(null);
            if (halfTimeResult != null) {
                match.setGoalsTeamOneHalfTime(halfTimeResult.getPointsTeamOne());
                match.setGoalsTeamTwoHalfTime(halfTimeResult.getPointsTeamTwo());
                match.setGoalsTeamOneFullTime(halfTimeResult.getPointsTeamOne());
                match.setGoalsTeamTwoFullTime(halfTimeResult.getPointsTeamTwo());
            }

            // convert full time result
            MatchResultData fullTimeResult = matchData.getResults().stream().filter(r -> r.isFullTime()).findFirst().orElse(null);
            if (fullTimeResult != null) {
                match.setGoalsTeamOneFullTime(fullTimeResult.getPointsTeamOne());
                match.setGoalsTeamTwoFullTime(fullTimeResult.getPointsTeamTwo());
            }
        }

        // convert goals
        List<MatchGoalData> goalDataList = matchData.getGoals();
        if (goalDataList != null) {

            // sort by minute ascending
            Collections.sort(goalDataList, MATCH_GOAL_MINUTE_ASC);
            MatchGoalData lastGoalData = null;
            for (int i = 0; i < goalDataList.size(); i++) {
                MatchGoalData goalData = goalDataList.get(i);

                // copy data
                Goal goal = new Goal();
                goal.setOrder(i);
                goal.setGoalsTeamOne(goalData.getPointsTeamOne());
                goal.setGoalsTeamTwo(goalData.getPointsTeamTwo());
                goal.setMinute(goalData.getMatchMinute());
                goal.setGoalGetterId(goalData.getGoalGetterId());
                goal.setGoalGetterName(goalData.getGoalGetterName());
                Team goalGetterTeam = team(goalGetterTeam(matchData, lastGoalData, goalData));
                goal.setGoalGetterTeamId(goalGetterTeam.getId());
                goal.setGoalGetterTeamName(goalGetterTeam.getName());
                goal.setGoalGetterTeamDisplayName(goalGetterTeam.getDisplayName());
                goal.setPenalty(goalData.isPenalty());
                goal.setOwnGoal(goalData.isOwnGoal());
                goal.setOvertime(goalData.isOvertime());

                // add
                match.getGoals().add(goal);

                // next
                lastGoalData = goalData;
            }
        }

        // done
        return match;
    }

    public TeamData goalGetterTeam(MatchData matchData, MatchGoalData lastGoalData, MatchGoalData goalData) {

        // check who scored
        boolean teamOneScored;
        if (lastGoalData == null) {

            // first goal
            teamOneScored = goalData.getPointsTeamOne() > goalData.getPointsTeamTwo();
        } else {

            // compare with previous goal
            teamOneScored = lastGoalData.getPointsTeamOne() < goalData.getPointsTeamOne();
        }

        // return correct team
        if (goalData.isOwnGoal()) {
            return teamOneScored ? matchData.getTeamTwo() : matchData.getTeamOne();
        } else {
            return teamOneScored ? matchData.getTeamOne() : matchData.getTeamTwo();
        }
    }

    public UserData user(User user) {
        UserData data = new UserData();
        data.setId(user.getId());
        data.setName(user.getName());
        data.setEmail(user.getEmail());
        data.setSince(user.getSince());
        data.setBulibotName(user.getBulibotName());
        data.setJsonExportBulibotResultsUrlEnabled(user.isJsonExportBulibotResults());
        data.setJsonExportBulibotResultsUrl(user.getJsonExportBulibotResultsUrl());
        data.setSlackExportBulibotResultsUrlEnabled(user.isSlackExportBulibotResults());
        data.setSlackExportBulibotResultsUrl(user.getSlackExportBulibotResultsUrl());
        return data;
    }

    public Map<Long, UserNamesData> userNamesData(List<User> users) {
        Map<Long, UserNamesData> data = new HashMap<>();

        // convert
        if (users != null) {
            users.forEach(u -> data.put(u.getId(), new UserNamesData(u)));
        }

        // done
        return data;
    }

    public List<BulibotSourceData> bulibotSources(List<BulibotVersion> bulibotVersions) {
        List<BulibotSourceData> data = new ArrayList<>();
        if (bulibotVersions != null) {
            bulibotVersions.forEach(b -> data.add(bulibotSource(b)));
        }
        return data;
    }

    public BulibotSourceData bulibotSource(BulibotVersion bulibotVersion) {
        BulibotSourceData data = new BulibotSourceData();
        if (bulibotVersion != null) {
            data.setName(bulibotVersion.getName());
            data.setSource(bulibotVersion.getSource());
            data.setLive(bulibotVersion.isLive());
        }
        return data;
    }

    public List<String> routes(List<Route> routes) {
        List<String> result = new ArrayList<>();

        // convert routes
        for (Route route : routes) {
            StringBuilder sb = new StringBuilder();
            sb.append(route.getHttpMethod());
            sb.append(" ");
            sb.append(route.getUri());
            result.add(sb.toString());
        }

        // done
        return result;
    }

    public Map<String, Map<String, ConfigPropertyData>> properties(List<Class<? extends Config>> configClasses, NinjaProperties ninjaProperties) {

        // get ignores
        List<String> ignores = Arrays.asList(ninjaProperties.getStringArray(SystemConfig.API_CONFIG_HIDE.getKey()));

        // get all properties
        Properties properties = ninjaProperties.getAllCurrentNinjaProperties();

        // create one group per config
        Map<String, Map<String, ConfigPropertyData>> result = new TreeMap<>();
        for (Class<? extends Config> configClass : configClasses) {
            Map<String, ConfigPropertyData> configResult = new TreeMap<>();

            String group = null;
            for (Config config : configClass.getEnumConstants()) {
                group = config.getGroup();

                // check ignore
                String key = config.getKey();
                if (ignores.contains(key)) {
                    continue;
                }

                // add value
                Object value = properties.remove(key);
                ConfigPropertyData data = new ConfigPropertyData();
                data.setKey(key);
                data.setValue(value);
                data.setEditable(config.isConfigureable());
                configResult.put(key, data);
            }

            // add group result
            result.put(group, configResult);
        }

        // add all others
        Map<String, ConfigPropertyData> unboundConfigResult = new TreeMap<>();
        for (Object key : properties.keySet()) {
            if (ignores.contains(key.toString())) {
                continue;
            }

            Object value = properties.get(key);
            ConfigPropertyData data = new ConfigPropertyData();
            data.setKey(key.toString());
            data.setValue(value);
            data.setEditable(false);
            unboundConfigResult.put(key.toString(), data);
        }
        result.put(Config.GROUP_UNBOUND, unboundConfigResult);

        // done
        return result;
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped data transformation service.");
    }
}
