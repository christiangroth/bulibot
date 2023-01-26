package services;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import configuration.NotificationEventConfig;
import configuration.OpenligaDbConfig;
import model.match.Match;
import model.match.Team;
import model.openligadb.MatchData;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;

@Singleton
public class OpenligaDbService {
    private static final Logger LOG = LoggerFactory.getLogger(OpenligaDbService.class);

    private static final int HTTP_OK = 200;

    @Inject
    private SeasonService seasonService;

    @Inject
    private BulibotService bulibotService;

    @Inject
    private DataTransformationService dataTransformationService;

    @Inject
    private DataService dataService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private NinjaProperties ninjaProperties;

    @Start
    public void startup() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        LOG.info("initializing unirest ssl config service...");
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }

        } };

        SSLContext sslcontext = SSLContext.getInstance("SSL");
        sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        Unirest.setHttpClient(httpclient);
        LOG.info("started openligadb rest service.");
    }

    public void synchronize(Integer season) {

        // fetch match data
        List<MatchData> matchData = retrieveMatchData(Match.LEAGUE_BUNDESLIGA_FIRST, season);

        // update internal data
        update(matchData);
    }

    public List<MatchData> retrieveMatchData(String league, int season) {
        LOG.info("retrieving matchdata for " + league + " / " + season);
        List<MatchData> result = new ArrayList<>();

        // null guard
        if (StringUtils.isBlank(league)) {
            throw new IllegalStateException("league must be present!!");
        }

        // optimize number of requests, check if all matches are already present and then fetch all data at once
        final List<Match> matches = dataService.matches(season);
        LOG.info("local number of matches: " + matches.size());
        if (matches.size() == Match.NUMBER_OF_MATCHES_PER_SEASON) {

            // load data
            List<MatchData> matchData = loadMatchData(getMatchdataUrl() + league + "/" + season, false);

            // fix league and season values
            Set<String> mismatchedIds = new HashSet<>();
            for (MatchData md : matchData) {
                md.setLeague(league);
                md.setSeason(season);

                // fetch matchday from original match
                Match match = matches.stream().filter(m -> m.getId() == md.getId()).findFirst().orElse(null);
                if (match != null) {
                    md.setMatchday(match.getMatchday());
                } else {
                    mismatchedIds.add(Long.toString(md.getId()));
                }
            }

            // add all to result
            if (mismatchedIds.isEmpty()) {
                result.addAll(matchData);
                return result;
            } else {

                // report error
                LOG.error("mismatching match ids but all season matches are present: " + String.join(",", mismatchedIds));
            }
        }

        // delegate per matchday
        for (int i = 1; i <= 34; i++) {
            List<MatchData> matchData = retrieveMatchData(league, season, i);
            if (matchData == null || matchData.isEmpty()) {

                // abort this season if no data is available
                break;
            }
            result.addAll(matchData);
        }

        // done
        return result;
    }

    private List<MatchData> retrieveMatchData(String league, int season, int matchday) {

        // load data
        List<MatchData> matchData = loadMatchData(getMatchdataUrl() + league + "/" + season + "/" + matchday, true);

        // fix league and season values
        matchData.stream().forEach(md -> {
            md.setLeague(league);
            md.setSeason(season);
            md.setMatchday(matchday);
        });

        // done
        return matchData;
    }

    private List<MatchData> loadMatchData(String url, boolean matchday) {

        // get JSON response
        String json = get(url);
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }

        // map to objects
        List<MatchData> matchData = mapToList(MatchData.class, new TypeReference<Collection<MatchData>>() {
        }, json);

        // handle responses if max number of requests per day was exceeded
        if (matchData.size() == 1 && matchData.get(0).getId() == -1) {

            LOG.error("openligadb request limit exceeded");
            return new ArrayList<>();
        }

        // done
        return matchData;
    }

    private String get(String url) {
        try {

            // triggerHTTP request
            LOG.info("calling : " + url);
            HttpResponse<String> response = Unirest.get(url).asString();
            if (response.getStatus() == HTTP_OK) {

                // fine
                return response.getBody();
            } else {
                LOG.error("GET response from '" + url + "' was not successful " + response.getStatus() + ": " + response.getStatusText());
                notificationService.send(NotificationEventConfig.OPENLIGADB_MATCHDATA_FAILED, "" + response.getStatus(), response.getStatusText());
            }
        } catch (UnirestException e) {
            LOG.error("unable to retrieve GET response from '" + url + "': " + e.getMessage(), e);
            notificationService.send(NotificationEventConfig.OPENLIGADB_MATCHDATA_FAILED, e.getClass().getName(), e.getMessage());
        }

        // failed
        return null;
    }

    private List<MatchData> mapToList(Class<MatchData> type, TypeReference<Collection<MatchData>> typeReference, String json) {
        try {
            return new ObjectMapper().readValue(json, typeReference);
        } catch (IOException e) {
            LOG.error("unable to map JSON to list of " + type.getName() + ": " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public void update(List<MatchData> matchData) {
        boolean dataChanged = false;

        // update teams
        Set<Team> teams = dataTransformationService.teams(matchData);
        Set<Team> teamsDelete = new HashSet<>();
        Set<Team> teamsNew = new HashSet<>();
        for (Team team : teams) {

            // find existing
            Team existing = dataService.team(team.getId());

            // add
            if (existing == null) {

                // download image
                boolean imageSaved = downloadTeamImage(team);

                // add to store
                if (imageSaved) {
                    dataChanged = true;
                    teamsNew.add(team);
                }
            }

            // update
            else if (!StringUtils.equals(existing.getName(), team.getName()) || !StringUtils.equals(existing.getShortName(), team.getShortName())
                    || !StringUtils.equals(existing.getIconUrl(), team.getIconUrl())) {
                dataChanged = true;

                // update local image cache
                if (!StringUtils.equals(existing.getIconUrl(), team.getIconUrl())) {
                    dataService.teamImageDelete(existing.getLocalFileName());
                    downloadTeamImage(team);
                }

                // update in store
                teamsDelete.add(team);
                teamsNew.add(team);
            }
        }

        // update teams storage
        dataService.teamsDelete(teamsDelete);
        dataService.teams(teamsNew);

        // convert to entities
        Set<Match> matches = dataTransformationService.matches(matchData);

        // update stores - consider update timestamp and change notifications
        Set<Match> matchesDelete = new HashSet<>();
        Set<Match> matchesNew = new HashSet<>();
        for (Match match : matches) {

            // find existing
            Match existing = dataService.match(match.getId());

            // add
            if (existing == null) {
                dataChanged = true;
                matchesNew.add(match);
            }

            // update
            else if (!StringUtils.equals(existing.getLastUpdateTimeString(), match.getLastUpdateTimeString())) {
                dataChanged = true;

                // update in store
                matchesDelete.add(match);
                matchesNew.add(match);
            }
        }

        // update matchesstorage
        dataService.matchesDelete(matchesDelete);
        dataService.matches(matchesNew);

        // update statistics on match changes
        if (dataChanged) {
            seasonService.updateStatistics();
            bulibotService.updateRankings();
        }
    }

    public boolean downloadTeamImage(Team team) {
        try {

            // execute request
            LOG.info("downloading team image: " + team.getIconUrl());
            HttpResponse<InputStream> response = Unirest.get(team.getIconUrl()).asBinary();
            if (response.getStatus() == HTTP_OK) {

                // save
                return dataService.teamImageSave(team.getLocalFileName(), response.getBody());
            } else {
                LOG.error("Unable to download team image from " + team.getIconUrl() + ", " + response.getStatus() + ": " + response.getStatusText());
                notificationService.send(NotificationEventConfig.OPENLIGADB_TEAM_IMAGE_FAILED, "" + response.getStatus(), response.getStatusText());
                return false;
            }
        } catch (UnirestException e) {
            LOG.error("unable to retrieve GET response from " + team.getIconUrl() + ": " + e.getMessage(), e);
            notificationService.send(NotificationEventConfig.OPENLIGADB_TEAM_IMAGE_FAILED, e.getClass().getName(), e.getMessage());
            return false;
        }
    }

    private String getMatchdataUrl() {
        String urlBase = ninjaProperties.getOrDie(OpenligaDbConfig.URL_BASE.getKey());
        String urlMatchdataSuffix = ninjaProperties.getOrDie(OpenligaDbConfig.URL_MATCHDATA_POSTFIX.getKey());
        return urlBase + urlMatchdataSuffix;
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped openligadb rest service.");
    }
}
