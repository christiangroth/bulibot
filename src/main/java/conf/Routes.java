package conf;

import com.google.inject.Inject;

import controllers.ApiController;
import controllers.FixedAssetsController;
import controllers.TeamImageAssetsController;
import controllers.TemplateController;
import ninja.Results;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import ninja.utils.NinjaProperties;

/**
 * Definition of all exisiting HTTP routes.
 *
 * @author Christian Groth
 */
public class Routes implements ApplicationRoutes {

    public static final String PATH_EXTERNAL_MAIN = "/";
    public static final String PATH_INTERNAL_MAIN = "/rankings";

    @Inject
    private NinjaProperties ninjaProperties;

    @Override
    public void init(Router router) {

        /*
         * public
         */

        // login template & logout
        router.GET().route(PATH_EXTERNAL_MAIN).with(TemplateController.class, "login");
        router.GET().route("/logout").with(ApiController.class, "logout");

        // authentication API
        router.GET().route("/verify/{verificationPhrase}").with(ApiController.class, "registrationVerify");
        router.GET().route("/api/v1/config").with(ApiController.class, "authConfig");
        router.POST().route("/api/v1/registration").with(ApiController.class, "registration");
        router.POST().route("/api/v1/resendVerification").with(ApiController.class, "registrationResendVerification");
        router.POST().route("/api/v1/resetPassword").with(ApiController.class, "registrationResetPassword");
        router.POST().route("/api/v1/authenticate").with(ApiController.class, "login");

        // assets
        router.GET().route("/assets/team/{fileName: .*}").with(TeamImageAssetsController.class, "serveStatic");
        router.GET().route("/assets/webjars/{fileName: .*}").with(FixedAssetsController.class, "serveWebJars");
        router.GET().route("/assets/{fileName: .*}").with(FixedAssetsController.class, "serveStatic");

        /*
         * private - all users
         */

        // templates
        router.GET().route(PATH_INTERNAL_MAIN).with(TemplateController.class, "rankings");
        router.GET().route("/editor").with(TemplateController.class, "editor");
        router.GET().route("/matches").with(TemplateController.class, "matches");
        router.GET().route("/profile").with(TemplateController.class, "profile");
        router.GET().route("/releasenotes").with(TemplateController.class, "releasenotes");

        // openligadb data
        router.GET().route("/api/v1/currentSeason").with(ApiController.class, "currentSeason");
        router.GET().route("/api/v1/currentMatchday").with(ApiController.class, "currentMatchday");

        // data
        router.GET().route("/api/v1/data/seasons").with(ApiController.class, "seasons");
        router.GET().route("/api/v1/data/teams").with(ApiController.class, "teams");
        router.GET().route("/api/v1/data/matches/{season}/{matchday}").with(ApiController.class, "matchesSeasonMatchday");
        router.GET().route("/api/v1/data/statistics").with(ApiController.class, "statistics");
        router.GET().route("/api/v1/data/statistics/{season}/{matchday}").with(ApiController.class, "statisticsSeasonMatchday");
        router.GET().route("/api/v1/data/rankingSeasons").with(ApiController.class, "rankingSeasons");
        router.GET().route("/api/v1/data/rankingData/{season}").with(ApiController.class, "rankingDataSeason");
        router.GET().route("/api/v1/data/rankingData/{season}/{matchday}").with(ApiController.class, "rankingDataSeasonMatchday");
        router.GET().route("/api/v1/data/user").with(ApiController.class, "user");
        router.GET().route("/api/v1/data/userNames").with(ApiController.class, "userNames");
        router.GET().route("/api/v1/data/bulibotConfig").with(ApiController.class, "bulibotConfiguration");
        router.GET().route("/api/v1/data/bulibot").with(ApiController.class, "bulibot");
        router.GET().route("/api/v1/data/bulibots").with(ApiController.class, "bulibots");
        router.POST().route("/api/v1/data/bulibot/{name}").with(ApiController.class, "bulibotUpdate");
        router.POST().route("/api/v1/data/bulibot/{name}/rename").with(ApiController.class, "bulibotRename");
        router.POST().route("/api/v1/data/bulibot/{name}/copy").with(ApiController.class, "bulibotCopy");
        router.GET().route("/api/v1/data/bulibot/{name}/setLive").with(ApiController.class, "bulibotSetLive");
        router.DELETE().route("/api/v1/data/bulibot/{name}").with(ApiController.class, "bulibotDelete");
        router.GET().route("/api/v1/data/bulibot/test").with(ApiController.class, "testdataStrategies");
        router.GET().route("/api/v1/data/bulibot/{name}/test/{code}").with(ApiController.class, "bulibotTest");
        router.POST().route("/api/v1/data/user/jsonExportExecutions").with(ApiController.class, "userChangeJsonExportExecutions");
        router.GET().route("/api/v1/data/user/jsonExportExecutions").with(ApiController.class, "userTestJsonExportExecutions");
        router.POST().route("/api/v1/data/user/slackExportExecutions").with(ApiController.class, "userChangeSlackExportExecutions");
        router.GET().route("/api/v1/data/user/slackExportExecutions").with(ApiController.class, "userTestSlackExportExecutions");
        router.POST().route("/api/v1/data/user/name").with(ApiController.class, "userChangeName");
        router.POST().route("/api/v1/data/user/bulibotName").with(ApiController.class, "userChangeBulibotName");
        router.POST().route("/api/v1/data/user/password").with(ApiController.class, "userChangePassword");
        router.POST().route("/api/v1/data/user/delete").with(ApiController.class, "userDelete");

        /*
         * private - admin users only
         */

        // template
        router.GET().route("/pendingBulibotExecutions").with(TemplateController.class, "pendingBulibotExecutions");
        router.GET().route("/users").with(TemplateController.class, "users");
        router.GET().route("/smartcrons").with(TemplateController.class, "smartcrons");
        router.GET().route("/persistence").with(TemplateController.class, "persistence");
        router.GET().route("/api").with(TemplateController.class, "api");
        router.GET().route("/config").with(TemplateController.class, "config");
        router.GET().route("/killswitch").with(TemplateController.class, "killswitch");

        // help
        router.GET().route("/api/v1/help").with(ApiController.class, "help");

        // data
        router.GET().route("/api/v1/data/users").with(ApiController.class, "users");
        router.POST().route("/api/v1/data/user/invite").with(ApiController.class, "inviteUser");
        router.POST().route("/api/v1/data/user/{id}/flags").with(ApiController.class, "userFlags");
        router.GET().route("/api/v1/data/user/{id}/pwreset").with(ApiController.class, "userPasswordReset");
        router.DELETE().route("/api/v1/data/user/{id}").with(ApiController.class, "deleteUser");

        // system information
        router.GET().route("/api/v1/system/pendingBulibotExecutions/{season}").with(ApiController.class, "pendingBulibotExecutions");
        router.DELETE().route("/api/v1/system/pendingBulibotExecutions/{season}/{matchId}").with(ApiController.class, "deletePendingBulibotExecutions");
        router.GET().route("/api/v1/system/config").with(ApiController.class, "config");
        router.POST().route("/api/v1/system/config/{group}/{key}").with(ApiController.class, "configChange");
        router.GET().route("/api/v1/system/smartcrons").with(ApiController.class, "smartcrons");
        router.POST().route("/api/v1/system/smartcrons/{name}").with(ApiController.class, "smartcronsControl");
        router.GET().route("/api/v1/system/storage").with(ApiController.class, "storage");
        router.PUT().route("/api/v1/system/killswitch").with(ApiController.class, "killswitch");

        /*
         * dev testing export callback
         */
        if (ninjaProperties.isDev()) {
            // router.POST().route("/testExportCallback").with(ApiController.class, "testExportCallback");
        }

        /*
         * misc
         */

        // avoid 404
        router.GET().route("{path: .*}").with(Results.redirect("/"));
    }
}
