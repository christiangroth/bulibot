package services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.DataConfig;
import de.chrgroth.jsonstore.JsonStores;
import de.chrgroth.jsonstore.JsonStoresMetrics;
import de.chrgroth.jsonstore.store.JsonStore;
import de.chrgroth.jsonstore.store.exception.JsonStoreException;
import model.community.BulibotExecution;
import model.match.Match;
import model.match.Match.Status;
import model.match.Team;
import model.statistics.Statistics;
import model.user.User;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;

@Singleton
public class DataService {
    private static final Logger LOG = LoggerFactory.getLogger(DataService.class);

    private static final int SERVICE_ORDER = 10;

    private File media;
    private File storage;

    private JsonStores stores;
    private JsonStore<Team> teamsStore;
    private JsonStore<Match> matchesStore;
    private JsonStore<User> usersStore;
    private JsonStore<BulibotExecution> bulibotExecutionsStore;

    @Inject
    private NinjaProperties ninjaProperties;

    @Start(order = SERVICE_ORDER)
    public void startup() {

        // prepare media directory
        LOG.info("initializing media directory...");
        media = new File(ninjaProperties.getOrDie(DataConfig.MEDIA_DIR.getKey()));
        try {
            FileUtils.forceMkdir(media);
        } catch (IOException e) {
            throw new IllegalStateException("unable to prepare media directory!!", e);
        }

        // define persistent storage
        LOG.info("defining json stores...");
        storage = new File(ninjaProperties.getOrDie(DataConfig.STORAGE_DIR.getKey()));
        Boolean prettyPrint = ninjaProperties.getBooleanWithDefault(DataConfig.STORAGE_PRETTY_PRINT.getKey(), Boolean.TRUE);
        stores = JsonStores.builder().storage(storage, StandardCharsets.UTF_8, prettyPrint, false, true).useStringInterning().build();
        try {
            teamsStore = stores.ensure(Team.class, Team.VERSION, Team.HANDLERS);
            matchesStore = stores.ensure(Match.class, Match.VERSION, Match.HANDLERS);
            usersStore = stores.ensure(User.class, User.VERSION, User.HANDLERS);
            bulibotExecutionsStore = stores.ensure(BulibotExecution.class, BulibotExecution.VERSION, BulibotExecution.HANDLERS);
        } catch (JsonStoreException e) {
            throw new IllegalStateException("unable to restore data", e);
        }

        // load all stores
        LOG.info("initializing json stores...");
        teamsStore.load();
        matchesStore.load();
        usersStore.load();
        bulibotExecutionsStore.load();
        LOG.info("started data service.");
    }

    public JsonStoresMetrics storageData() {
        return stores.computeMetrics();
    }

    public List<Integer> seasons() {
        return matchesStore.stream().map(m -> m.getSeason()).distinct().sorted().collect(Collectors.toList());
    }

    public List<Team> teams() {
        return teamsStore.stream().collect(Collectors.toList());
    }

    public Team team(long id) {
        return teamsStore.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }

    public synchronized void teams(Set<Team> teams) {
        if (teams != null && !teams.isEmpty()) {
            teamsStore.addAll(teams);
            teamsStore.save();
        }
    }

    public synchronized void teamsDelete(Set<Team> teams) {
        if (teams != null && !teams.isEmpty()) {
            teamsStore.removeAll(teams);
            teamsStore.save();
        }
    }

    public boolean teamImageSave(String localFilename, InputStream inputStream) {

        // write to file
        File localTeamImageFile = new File(media, localFilename);
        LOG.info("storing team image: " + localTeamImageFile.getAbsolutePath());
        try (OutputStream outputStream = new FileOutputStream(localTeamImageFile)) {

            // write the inputStream to a FileOutputStream
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            // done
            return true;
        } catch (IOException e) {
            LOG.error("unable to write team image data to local file " + localTeamImageFile.getAbsolutePath() + ": " + e.getMessage(), e);
            return false;
        }
    }

    public void teamImageDelete(String localFilename) {

        // remove local cached image
        File localTeamImageFile = new File(media, localFilename);
        try {
            FileUtils.forceDelete(localTeamImageFile);
        } catch (NullPointerException | IOException e) {
            LOG.error("skipping to delete local team image file " + localTeamImageFile.getAbsolutePath() + ", seems not to be present: " + e.getMessage());
        }

    }

    public synchronized void matches(Set<Match> matches) {
        if (matches != null && !matches.isEmpty()) {
            matchesStore.addAll(matches);
            matchesStore.save();
        }
    }

    public synchronized void matchesDelete(Set<Match> matches) {
        if (matches != null && !matches.isEmpty()) {
            matchesStore.removeAll(matches);
            matchesStore.save();
        }
    }

    public Match match(long matchId) {
        return matchesStore.stream().filter(m -> m.getId() == matchId).findFirst().orElse(null);
    }

    public List<Match> matches() {
        return matches(Match.COMPARATOR_DATE_DESC);
    }

    public List<Match> matches(Comparator<? super Match> comparator) {
        return matchesStore.stream().sorted(comparator).collect(Collectors.toList());
    }

    public List<Match> matches(int season) {
        return matches().stream().filter(m -> season == m.getSeason()).collect(Collectors.toList());
    }

    public List<Match> matches(int season, int matchday) {
        return matches(season).stream().filter(m -> m.getMatchday() == matchday).collect(Collectors.toList());
    }

    public List<Match> matches(int season, int fromMatchday, int toMatchday) {
        return matches(season).stream().filter(m -> m.getMatchday() >= fromMatchday && m.getMatchday() <= toMatchday).collect(Collectors.toList());
    }

    public List<Match> matchesToday(LocalDateTime reference, boolean onlyUnfinished, int maxResults) {

        // calendar for reference
        int year = reference.getYear();
        int day = reference.getDayOfYear();

        // filter
        return matchesStore.stream().filter(m -> m.getAssignedTime() != null).filter(m -> onlyUnfinished ? m.getStatus() != Status.FINISHED : true)
                .filter(m -> m.getAssignedTime().getYear() == year && m.getAssignedTime().getDayOfYear() == day).sorted(Match.COMPARATOR_DATE_ASC).limit(maxResults)
                .collect(Collectors.toList());
    }

    public List<Match> matchesBefore(LocalDateTime reference, int maxResults) {
        return matchesStore.stream().filter(m -> m.getAssignedTime() != null).filter(m -> {
            return reference.isAfter(m.getAssignedTime()) || reference.equals(m.getAssignedTime());
        }).sorted(Match.COMPARATOR_DATE_DESC).limit(maxResults).collect(Collectors.toList());
    }

    public List<Match> matchesAfter(LocalDateTime reference, int maxResults) {
        return matchesStore.stream().filter(m -> m.getAssignedTime() != null).filter(m -> {
            return reference.isBefore(m.getAssignedTime()) || reference.equals(m.getAssignedTime());
        }).sorted(Match.COMPARATOR_DATE_ASC).limit(maxResults).collect(Collectors.toList());
    }

    public Statistics statisticsCopy(Integer season, Integer beforeMatchday) {
        Statistics copy = new Statistics();

        // add all matches before season / matchday
        matchesStore.stream().filter(m -> m.getSeason() != null && m.getMatchday() != null)
                .filter(m -> m.getSeason().intValue() < season || m.getSeason().intValue() == season && m.getMatchday().intValue() < beforeMatchday).forEach(m -> copy.update(m));

        // done
        return copy;
    }

    public long nextUserId() {
        return usersStore.stream().mapToLong(u -> u.getId()).max().orElse(0) + 1;
    }

    public User userById(Long id) {
        return id == null ? null : usersStore.stream().filter(u -> u.getId() == id.longValue()).findFirst().orElse(null);
    }

    public User userByEmail(String email) {
        return usersStore.stream().filter(u -> StringUtils.equalsIgnoreCase(email, u.getEmail())).findFirst().orElse(null);
    }

    public User userByVerificationPhrase(String verificationPhrase) {
        return usersStore.stream().filter(u -> StringUtils.equals(verificationPhrase, u.getVerificationPhrase())).findFirst().orElse(null);
    }

    public List<User> users() {
        return usersStore.stream().collect(Collectors.toList());
    }

    public synchronized void user(User user) {
        usersStore.add(user);
        usersStore.save();
    }

    public synchronized void userDelete(User user) {

        boolean removedUser = usersStore.remove(user);
        if (removedUser) {
            usersStore.save();
        }

        boolean removedExecutions = bulibotExecutionsStore.removeIf(be -> be.getUserId() == user.getId());
        if (removedExecutions) {
            bulibotExecutionsStore.save();
        }
    }

    public List<BulibotExecution> bulibotExecutions() {
        return bulibotExecutionsStore.stream().collect(Collectors.toList());
    }

    public List<BulibotExecution> bulibotExecutions(int season) {
        return bulibotExecutionsStore.stream().filter(be -> season == be.getSeason()).collect(Collectors.toList());
    }

    public List<BulibotExecution> bulibotExecutions(int season, int matchday) {
        return bulibotExecutions(season).stream().filter(be -> be.getMatchday() == matchday).collect(Collectors.toList());
    }

    public List<BulibotExecution> bulibotExecutions(int season, int fromMatchday, int toMatchday) {
        return bulibotExecutions(season).stream().filter(be -> be.getMatchday() >= fromMatchday && be.getMatchday() <= toMatchday).collect(Collectors.toList());
    }

    public synchronized void bulibotExecutions(List<BulibotExecution> bulibotExecutions) {
        if (bulibotExecutions != null && !bulibotExecutions.isEmpty()) {
            bulibotExecutionsStore.addAll(bulibotExecutions);
            bulibotExecutionsStore.save();
        }
    }

    public BulibotExecution bulibotExecution(long userId, long matchId) {
        return bulibotExecutionsStore.stream().filter(be -> be.getUserId() == userId && be.getMatchId() == matchId).findFirst().orElse(null);
    }

    public boolean bulibotExecutionsDelete(int season, long matchId) {
        return bulibotExecutionsStore.removeIf((e) -> e.getSeason() != null && e.getSeason().intValue() == season && e.getMatchId() == matchId);
    }

    @Dispose(order = SERVICE_ORDER)
    public void shutdown() {
        LOG.info("saving json stores...");
        stores.save();
        LOG.info("stopped data service.");
    }
}
