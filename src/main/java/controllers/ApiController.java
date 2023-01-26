package controllers;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import conf.Routes;
import configuration.AuthConfig;
import configuration.api.Config;
import controllers.filters.AdminFilter;
import controllers.filters.UserFilter;
import controllers.util.AuthenticationHelper;
import de.chrgroth.smartcron.api.Smartcron;
import de.svenkubiak.ninja.auth.services.Authentications;
import model.AjaxResult;
import model.SmartcronData;
import model.community.BulibotExecution;
import model.community.BulibotExecutionResultsExport;
import model.community.TestdataResult;
import model.match.Match;
import model.user.BulibotNameData;
import model.user.BulibotSourceData;
import model.user.BulibotVersion;
import model.user.User;
import model.user.UserBulibotNameData;
import model.user.UserExportExecutionsData;
import model.user.UserFlagsData;
import model.user.UserLoginData;
import model.user.UserNameData;
import model.user.UserPasswordData;
import model.user.UserRegistrationData;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.metrics.Counted;
import ninja.metrics.Timed;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import ninja.utils.NinjaProperties;
import services.AuthenticationService;
import services.BulibotService;
import services.DataExportService;
import services.DataService;
import services.DataTransformationService;
import services.InfoService;
import services.ScriptService;
import services.SeasonService;
import services.SmartcronService;
import services.exception.ConfigPropertyInvalidValueException;
import services.exception.ConfigPropertyMandatoryException;
import services.exception.MailException;
import services.exception.UnknownConfigGroupException;
import services.exception.UnknownConfigPropertyException;
import services.testdata.BulibotTestdataStrategy;

@Singleton
public class ApiController {

    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private SmartcronService smartcronService;

    @Inject
    private DataTransformationService dataTransformationService;

    @Inject
    private BulibotService bulibotService;

    @Inject
    private SeasonService seasonService;

    @Inject
    private DataExportService dataExportService;

    @Inject
    private DataService dataService;

    @Inject
    private InfoService infoService;

    @Inject
    private ScriptService scriptService;

    @Inject
    private Authentications authentications;

    @Inject
    private AuthenticationHelper authenticationHelper;

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private Router router;

    /*
     * public
     */

    public Result authConfig() {
        return Results.json().render(infoService.getAuthConfig());
    }

    @Counted
    public Result registration(UserRegistrationData data) {

        // check if registration is enabled
        if (!infoService.isRegistrationEnabled()) {
            return Results.json().render(AjaxResult.error("registrationDisabled"));
        }

        // check name
        if (StringUtils.isBlank(data.getName())) {
            return Results.json().render(AjaxResult.error("registrationNameMissing"));
        }

        // check passwords match
        if (!StringUtils.equals(data.getPassword(), data.getPasswordAgain())) {
            return Results.json().render(AjaxResult.error("registrationPasswordMismatch"));
        }

        // check if user already exists
        if (dataService.userByEmail(data.getEmail()) != null) {
            return Results.json().render(AjaxResult.error("registrationUserExists"));
        }

        // create user
        try {
            authenticationService.createUser(data.getEmail(), data.getName(), data.getPassword());
        } catch (MailException e) {
            return Results.json().render(AjaxResult.error("registrationEmailFailed"));
        }

        // done
        return Results.json().render(AjaxResult.ok("registrationSuccess"));
    }

    @Counted
    public Result registrationResendVerification(UserLoginData data) {

        // check if registration is enabled
        if (!infoService.isRegistrationEnabled()) {
            return Results.json().render(AjaxResult.error("registrationDisabled"));
        }

        // get user
        User user = dataService.userByEmail(data.getEmail());
        if (user != null && !user.isGenerated() && !user.isVerified()) {

            // reset verification
            try {
                authenticationService.startVerification(user);
            } catch (MailException e) {
                return Results.json().render(AjaxResult.error("registrationResendEmailFailed"));
            }

            // done
            return Results.json().render(AjaxResult.ok("registrationResendEmail"));
        }

        // failed
        return Results.json().render(AjaxResult.error("registrationResendEmailFailed"));
    }

    @Counted
    public Result registrationVerify(FlashScope flashScope, @PathParam("verificationPhrase") String verificationPhrase) {

        // check if registration is enabled
        if (!infoService.isRegistrationEnabled()) {
            return Results.json().render(AjaxResult.error("registrationDisabled"));
        }

        // check verification phrase
        if (StringUtils.isNotBlank(verificationPhrase)) {

            // get user
            User user = dataService.userByVerificationPhrase(verificationPhrase);
            if (user != null) {

                // finish verification
                if (!user.isVerified()) {
                    try {
                        authenticationService.finishVerification(user);
                    } catch (MailException e) {
                        return Results.json().render(AjaxResult.error("loginPasswordResetEmailFailed"));
                    }
                    flashScope.put("verificationSuccess", "true");
                }

                // done
                return Results.redirect(Routes.PATH_EXTERNAL_MAIN);
            }
        }

        // failed
        flashScope.put("verificationFailed", "true");
        return Results.redirect(Routes.PATH_EXTERNAL_MAIN);
    }

    @Counted
    public Result registrationResetPassword(UserLoginData data) {

        // get user
        User user = dataService.userByEmail(data.getEmail());
        if (user != null && !user.isGenerated()) {
            try {
                authenticationService.resetPassword(user);
                return Results.json().render(AjaxResult.ok("loginPasswordReset"));
            } catch (MailException e) {
                return Results.json().render(AjaxResult.error("loginPasswordResetEmailFailed"));
            }
        }

        // failed
        return Results.json().render(AjaxResult.error("loginPasswordResetFailed"));
    }

    @Timed
    public Result login(Context context, UserLoginData data) {

        // get user
        User user = dataService.userByEmail(data.getEmail());
        if (user != null) {

            // check verification status
            if (!user.isVerified()) {
                return Results.json().render(AjaxResult.error("loginNotVerified"));
            }

            // check locked status
            if (user.isLocked()) {
                return Results.json().render(AjaxResult.error("loginLocked"));
            }

            // check authentication data
            if (authenticationService.authenticate(user, data.getPassword())) {

                // reset failed logins
                user.setFailedLogins(0);
                dataService.user(user);

                // login
                authenticationService.login(context, user);
                return Results.json().render(AjaxResult.ok());
            } else {

                // increment number of failed logins
                user.setFailedLogins(user.getFailedLogins() + 1);
                dataService.user(user);
            }
        }

        // prevent brute force
        if (user != null) {

            // check if login delay is enabled
            Boolean delayLogin = ninjaProperties.getBooleanWithDefault(AuthConfig.AUTH_CONFIG_DELAY_LOGIN_ENABLED.getKey(), Boolean.TRUE);
            if (delayLogin) {

                // check login delay threshold
                long failedLogins = user.getFailedLogins();
                Integer threshold = ninjaProperties.getIntegerWithDefault(AuthConfig.AUTH_CONFIG_DELAY_LOGIN_THRESHOLD.getKey(), 3);
                if (failedLogins >= threshold) {

                    // delay
                    Integer delay = ninjaProperties.getIntegerWithDefault(AuthConfig.AUTH_CONFIG_DELAY_LOGIN_SECONDS.getKey(), 3);
                    try {
                        LOG.info("login for " + user.getEmail() + " will be delayed because of already " + failedLogins + " failed login attempts!!");
                        TimeUnit.SECONDS.sleep(delay);
                    } catch (InterruptedException e) {
                        LOG.error("unable to delay login for configured amount of seconds!!");
                    }
                }
            }
        }

        // failed
        return Results.json().render(AjaxResult.error("loginFailed"));
    }

    @Timed
    public Result logout(Context context) {

        // just logout
        authenticationService.logout(context);
        return Results.redirect(Routes.PATH_EXTERNAL_MAIN);
    }

    /*
     * private - all users
     */

    @Timed
    @FilterWith({ UserFilter.class })
    public Result bulibotConfiguration() {
        return Results.json().render(infoService.getBulibotConfig());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result currentSeason() {
        return Results.json().render(seasonService.currentSeason());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result currentMatchday() {
        return Results.json().render(seasonService.currentMatchday());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result seasons() {
        return Results.json().render(dataService.seasons());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result teams() {
        return Results.json().render(dataService.teams());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result matchesSeasonMatchday(@PathParam("season") int season, @PathParam("matchday") int matchday) {
        return Results.json().render(dataService.matches(season, matchday));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result statistics() {
        return Results.json().render(seasonService.statisticsResult());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result statisticsSeasonMatchday(@PathParam("season") int season, @PathParam("matchday") int matchday) {
        return Results.json().render(seasonService.statisticsResultSeasonMatchday(season, matchday));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result rankingSeasons() {
        return Results.json().render(bulibotService.rankingDataSeasons());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result rankingDataSeason(@PathParam("season") int season) {
        return Results.json().render(bulibotService.seasonRankingData(season));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result rankingDataSeasonMatchday(@PathParam("season") int season, @PathParam("matchday") int matchday) {
        return Results.json().render(bulibotService.rankingData(season, matchday));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result user(Context context) {
        return Results.json().render(dataTransformationService.user(authenticationHelper.activeUser(context)));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result userNames(Context context) {
        return Results.json().render(dataTransformationService.userNamesData(dataService.users()));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result bulibot(Context context) {
        return Results.json().render(dataTransformationService.bulibotSource(authenticationHelper.activeUser(context).getBulibotLiveVersion()));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result bulibots(Context context) {
        return Results.json().render(dataTransformationService.bulibotSources(authenticationHelper.activeUser(context).getBulibotUserVersions()));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result bulibotUpdate(Context context, @PathParam("name") String name, BulibotSourceData data) {

        // get bulibot
        User user = authenticationHelper.activeUser(context);
        BulibotVersion bulibotVersion = user.getBulibotUserVersionByName(name);
        if (bulibotVersion == null) {
            return Results.json().render(AjaxResult.error("bulibotNotFound"));
        }

        // invalidate script cache if source changed
        if (!StringUtils.equals(data.getSource(), bulibotVersion.getSource())) {
            scriptService.invalidateCompiledScript(user, bulibotVersion);
        }

        // update source
        bulibotVersion.setSource(data.getSource());

        // save
        dataService.user(user);

        // done
        return Results.json().render(AjaxResult.ok("bulibotSaved"));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result bulibotRename(Context context, @PathParam("name") String name, BulibotNameData data) {

        // get bulibot
        User user = authenticationHelper.activeUser(context);
        BulibotVersion bulibotVersion = user.getBulibotUserVersionByName(name);
        if (bulibotVersion == null) {
            return Results.json().render(AjaxResult.error("bulibotNotFound"));
        }

        // validate name
        BulibotVersion userVersionByName = user.getBulibotUserVersionByName(data.getName());
        if (userVersionByName != null) {
            return Results.json().render(AjaxResult.error("bulibotNameAlreadyExists"));
        }

        // invalidate script cache
        scriptService.invalidateCompiledScript(user, bulibotVersion);

        // update name
        bulibotVersion.setName(data.getName());

        // save
        dataService.user(user);

        // done
        return Results.json().render(AjaxResult.ok("bulibotSaved"));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result bulibotCopy(Context context, @PathParam("name") String name, BulibotNameData data) {

        // get bulibot
        User user = authenticationHelper.activeUser(context);
        BulibotVersion bulibotVersion = user.getBulibotUserVersionByName(name);
        if (bulibotVersion == null) {
            return Results.json().render(AjaxResult.error("bulibotNotFound"));
        }

        // validate name
        BulibotVersion copy = user.getBulibotUserVersionByName(data.getName());
        if (copy != null) {
            return Results.json().render(AjaxResult.error("bulibotNameAlreadyExists"));
        }

        // create copy
        copy = new BulibotVersion();
        copy.setName(data.getName());
        copy.setSource(bulibotVersion.getSource());
        copy.setLive(false);
        copy.setSystemTag(false);
        user.getBulibotVersions().add(copy);

        // save
        dataService.user(user);

        // done
        return Results.json().render(AjaxResult.ok("bulibotSaved"));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result bulibotSetLive(Context context, @PathParam("name") String name) {

        // get bulibot
        User user = authenticationHelper.activeUser(context);
        BulibotVersion bulibotVersion = user.getBulibotUserVersionByName(name);
        if (bulibotVersion == null) {
            return Results.json().render(AjaxResult.error("bulibotNotFound"));
        }

        // set live
        if (!bulibotVersion.isLive()) {
            user.getBulibotVersions().forEach(bv -> bv.setLive(false));
            bulibotVersion.setLive(true);
        }

        // save
        dataService.user(user);

        // done
        return Results.json().render(AjaxResult.ok("bulibotSetLive"));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result bulibotDelete(Context context, @PathParam("name") String name) {

        // get bulibot
        User user = authenticationHelper.activeUser(context);
        BulibotVersion bulibotVersion = user.getBulibotUserVersionByName(name);
        if (bulibotVersion == null) {
            return Results.json().render(AjaxResult.error("bulibotNotFound"));
        }

        // validate non-live
        if (bulibotVersion.isLive()) {
            return Results.json().render(AjaxResult.error("bulibotLiveVersionMustNotBeDeleted"));
        }

        // invalidate script cache
        scriptService.invalidateCompiledScript(user, bulibotVersion);

        // remove
        boolean removed = user.getBulibotVersions().remove(bulibotVersion);
        if (!removed) {
            return Results.json().render(AjaxResult.error("bulibotVersionCanNotBeDeleted"));
        }

        // save
        dataService.user(user);

        // done
        return Results.json().render(AjaxResult.ok("bulibotDeleted"));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result testdataStrategies(Context context) {
        return Results.json().render(AjaxResult.ok(bulibotService.availableStrategies().stream().map(s -> s.code()).collect(Collectors.toList())));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result bulibotTest(Context context, @PathParam("name") String name, @PathParam("code") String code) {

        // get user
        User user = authenticationHelper.activeUser(context);
        if (user.isLocked() || !user.isVerified()) {
            return Results.json().render(AjaxResult.error("userNotActive"));
        }

        // get bulibot
        BulibotVersion bulibotVersion = user.getBulibotUserVersionByName(name);
        if (bulibotVersion == null) {
            return Results.json().render(AjaxResult.error("bulibotNotFound"));
        }

        // get testdata strategy
        BulibotTestdataStrategy strategy = bulibotService.availableStrategy(code);
        if (strategy == null) {
            return Results.json().render(AjaxResult.error("strategyNotAvailable"));
        }

        // execute test
        TestdataResult testdataResult = bulibotService.test(user, bulibotVersion, strategy);

        // done
        return Results.json().render(AjaxResult.ok(testdataResult));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result userChangeJsonExportExecutions(Context context, UserExportExecutionsData data) {

        // check url if enabled
        if (data.isEnabled()) {
            try {
                new URL(data.getUrl());
            } catch (Exception e) {
                return Results.json().render(AjaxResult.error("exportExcutionsMalformedUrl"));
            }
        }

        // save changes
        User user = authenticationHelper.activeUser(context);
        user.setJsonExportBulibotResults(data.isEnabled());
        user.setJsonExportBulibotResultsUrl(data.getUrl());
        dataService.user(user);

        // done
        return Results.json().render(AjaxResult.ok("exportExcutionsChanged"));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result userTestJsonExportExecutions(Context context) {

        // export dummy data
        User user = authenticationHelper.activeUser(context);
        BulibotExecutionResultsExport exportData = createDummyExportData();
        String testDataJson = dataExportService.toJson(exportData);
        dataExportService.jsonExportBulibotExecutionResults(user, exportData, true);

        // done
        return Results.json().render(AjaxResult.ok("exportExcutionsTestSuccessful", testDataJson));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result userChangeSlackExportExecutions(Context context, UserExportExecutionsData data) {

        // check url if enabled
        if (data.isEnabled()) {
            try {
                new URL(data.getUrl());
            } catch (Exception e) {
                return Results.json().render(AjaxResult.error("exportExcutionsMalformedUrl"));
            }
        }

        // save changes
        User user = authenticationHelper.activeUser(context);
        user.setSlackExportBulibotResults(data.isEnabled());
        user.setSlackExportBulibotResultsUrl(data.getUrl());
        dataService.user(user);

        // done
        return Results.json().render(AjaxResult.ok("exportExcutionsChanged"));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result userTestSlackExportExecutions(Context context) {

        // export dummy data
        User user = authenticationHelper.activeUser(context);
        BulibotExecutionResultsExport exportData = createDummyExportData();
        String testData = dataExportService.toSlackText(exportData);
        dataExportService.slackExportBulibotExecutionResults(user, exportData, true);

        // done
        return Results.json().render(AjaxResult.ok("exportExcutionsTestSuccessful", testData));
    }

    private BulibotExecutionResultsExport createDummyExportData() {
        List<Match> matches = ensureDummyExportMatchData(dataService.matches(seasonService.currentSeason(), seasonService.currentMatchday()));
        return new BulibotExecutionResultsExport(seasonService.currentSeason(), seasonService.currentMatchday(), matches, createDummyExportExecutions(matches));
    }

    private List<Match> ensureDummyExportMatchData(List<Match> matches) {
        if (matches.size() != 9) {
            matches.clear();
            for (int i = 0; i < 9; i++) {
                matches.add(createDummyExportMatch(i));
            }
        }
        return matches;
    }

    private Match createDummyExportMatch(int i) {
        Match match = new Match();
        int base = i * 10;
        match.setId(base);
        match.setAssignedTime(LocalDateTime.now());
        match.setTeamOneId(base + 1);
        match.setTeamOneName("Mannschaft " + match.getTeamOneId());
        match.setTeamTwoId(base + 2);
        match.setTeamTwoName("Mannschaft " + match.getTeamTwoId());
        return match;
    }

    private List<BulibotExecution> createDummyExportExecutions(List<Match> matches) {
        List<BulibotExecution> data = new ArrayList<>();
        data.add(createDummyExportExecution(matches.get(0).getId(), 2, 1, null, null));
        data.add(createDummyExportExecution(matches.get(1).getId(), 4, 0, null, null));
        data.add(createDummyExportExecution(matches.get(2).getId(), null, null, "NullPointerException", null));
        data.add(createDummyExportExecution(matches.get(3).getId(), null, null, "A really bad error happened", "Some detailed error message goes here ..."));
        data.add(createDummyExportExecution(matches.get(4).getId(), 1, 1, null, null));
        data.add(createDummyExportExecution(matches.get(5).getId(), 0, 12, null, null));
        data.add(createDummyExportExecution(matches.get(6).getId(), 47, 13, null, null));
        data.add(createDummyExportExecution(matches.get(7).getId(), 1, null, "ScriptException", "Result partly missing"));
        data.add(createDummyExportExecution(matches.get(8).getId(), 0, 0, null, null));
        return data;
    }

    private BulibotExecution createDummyExportExecution(long matchId, Integer goalsTeamOne, Integer goalsTeamTwo, String errorCauseType, String errorCauseMessage) {
        BulibotExecution execution = new BulibotExecution();
        execution.setMatchId(matchId);
        execution.setGoalsTeamOne(goalsTeamOne);
        execution.setGoalsTeamTwo(goalsTeamTwo);
        execution.setErrorCauseType(errorCauseType);
        execution.setErrorCauseMessage(errorCauseMessage);
        return execution;
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result userChangeName(Context context, UserNameData data) {

        // save changes
        User user = authenticationHelper.activeUser(context);
        user.setName(data.getName());
        dataService.user(user);

        // done
        return Results.json().render(AjaxResult.ok("nameChanged"));
    }

    // TODO move to service
    @Timed
    @FilterWith({ UserFilter.class })
    public Result userChangeBulibotName(Context context, UserBulibotNameData data) {

        // save changes
        User user = authenticationHelper.activeUser(context);
        user.setBulibotName(data.getBulibotName());
        dataService.user(user);

        // done
        return Results.json().render(AjaxResult.ok("bulibotNameChanged"));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result userChangePassword(Context context, UserPasswordData data) {

        // check old password
        User user = authenticationHelper.activeUser(context);
        if (StringUtils.isBlank(data.getPassword()) || !authentications.authenticate(data.getPassword(), user.getPassword())) {
            return Results.json().render(AjaxResult.error("passwordInvalid"));
        }

        // check new password
        if (StringUtils.isBlank(data.getNewPassword()) || !StringUtils.equals(data.getNewPassword(), data.getNewPasswordAgain())) {
            return Results.json().render(AjaxResult.error("passwordsMismatch"));
        }

        // save changes
        authenticationService.changePassword(user, data.getNewPassword());

        // done
        return Results.json().render(AjaxResult.ok("passwordChanged"));
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result userDelete(Context context, UserPasswordData data) {

        // check password
        User user = authenticationHelper.activeUser(context);
        if (StringUtils.isBlank(data.getPassword()) || !authentications.authenticate(data.getPassword(), user.getPassword())) {
            return Results.json().render(AjaxResult.error("passwordInvalid"));
        }

        // delete user
        dataService.userDelete(user);

        // trigger rankings recalculation
        bulibotService.updateRankings();

        // logout
        authentications.logout(context);

        // done
        return Results.json().render(AjaxResult.ok());
    }

    /*
     * private - admin users only
     */

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result help() {
        return Results.json().render(dataTransformationService.routes(router.getRoutes()));
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result users() {
        return Results.json().render(dataService.users());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result inviteUser(Context context, UserRegistrationData data) {

        // check name
        if (StringUtils.isBlank(data.getName())) {
            return Results.json().render(AjaxResult.error("inviteNameMissing"));
        }

        // check email
        if (StringUtils.isBlank(data.getEmail())) {
            return Results.json().render(AjaxResult.error("inviteEmailMissing"));
        }

        // check if user already exists
        if (dataService.userByEmail(data.getEmail()) != null) {
            return Results.json().render(AjaxResult.error("registrationUserExists"));
        }

        // create user
        try {
            authenticationService.createUser(data.getEmail(), data.getName(), null);
        } catch (MailException e) {
            return Results.json().render(AjaxResult.error("registrationEmailFailed"));
        }

        // done
        return Results.json().render(AjaxResult.ok());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result userFlags(Context context, @PathParam("id") long id, UserFlagsData data) {

        // get user
        User user = dataService.userById(id);
        if (user != null) {

            // update flags
            authenticationService.updateFlags(user, data.isLocked(), data.isAdmin());
            return Results.json().render(AjaxResult.ok());
        }

        // failed
        return Results.json().render(AjaxResult.error("userNotFound"));
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result userPasswordReset(Context context, @PathParam("id") long id) {

        // get user
        User user = dataService.userById(id);
        if (user != null && !user.isGenerated()) {

            // reset password
            try {
                authenticationService.resetPassword(user);
            } catch (MailException e) {
                return Results.json().render(AjaxResult.error("loginPasswordResetEmailFailed"));
            }

            // done
            return Results.json().render(AjaxResult.ok());
        }

        // failed
        return Results.json().render(AjaxResult.error("userNotFound"));
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result deleteUser(Context context, @PathParam("id") long id) {

        // get user
        User user = dataService.userById(id);
        if (user != null) {

            // delete user
            dataService.userDelete(user);

            // trigger rankings recalculation
            bulibotService.updateRankings();

            // done
            return Results.json().render(AjaxResult.ok());
        }

        // failed
        return Results.json().render(AjaxResult.error("userNotFound"));
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result pendingBulibotExecutions(@PathParam("season") int season) {
        return Results.json().render(bulibotService.findUnstartedMatchesWithExistingBulibotExecutions(season));
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result deletePendingBulibotExecutions(@PathParam("season") int season, @PathParam("matchId") long matchId) {
        bulibotService.deleteBulibotExecutions(season, matchId);
        return Results.json().render(AjaxResult.ok());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result config() {
        return Results.json().render(dataTransformationService.properties(Config.CONFIGS, ninjaProperties));
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result configChange(@PathParam("key") String key, @PathParam("group") String group, String value) {
        try {
            infoService.changeProperty(group, key, value);
        } catch (UnknownConfigGroupException e) {
            return Results.json().render(AjaxResult.error("unknownGroup"));
        } catch (UnknownConfigPropertyException e) {
            return Results.json().render(AjaxResult.error("unknownProperty"));
        } catch (ConfigPropertyMandatoryException e) {
            return Results.json().render(AjaxResult.error("valueMandatory"));
        } catch (ConfigPropertyInvalidValueException e) {
            return Results.json().render(AjaxResult.error("valueInvalid"));
        }
        return Results.json().render(AjaxResult.ok());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result smartcrons() {
        return Results.json().render(smartcronService.metadata());
    }

    // TODO move to service
    @Timed
    @SuppressWarnings("unchecked")
    @FilterWith({ AdminFilter.class })
    public Result smartcronsControl(@PathParam("name") String name, SmartcronData data) {

        // resolve type
        Class<? extends Smartcron> type;
        try {
            type = (Class<? extends Smartcron>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            return Results.json().render(AjaxResult.error("smartcronNotFound"));
        }

        // check if execute smartcron
        if (data.getExecute() != null && data.getExecute().booleanValue()) {
            smartcronService.executeNowAndReschedule(type);
        }

        // toggle status for smartcrons
        if (data.getEnable() != null) {
            if (data.getEnable().booleanValue()) {
                smartcronService.activate(type);
            } else {
                smartcronService.deactivate(type);
            }
        }

        // done
        return Results.json().render(AjaxResult.ok("smartcronStatusToggled"));
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result storage() {
        return Results.json().render(dataService.storageData());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result killswitch() {
        LOG.info("killswitch triggered ... going down now!");
        System.exit(13);
        return null;
    }

    /*
     * dev testing
     */
    public Result testExportCallback(Map<String, Object> data) {
        LOG.info("received test data: " + data);
        return Results.ok().text().render("OK");
    }
}
