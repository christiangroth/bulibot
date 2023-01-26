package conf;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import configuration.UpdateRankingsPropertyChangeCallback;
import controllers.ApiController;
import controllers.FixedAssetsController;
import controllers.TeamImageAssetsController;
import controllers.TemplateController;
import ninja.metrics.MetricsModule;
import ninja.metrics.graphite.NinjaGraphite;
import services.AuthenticationService;
import services.BulibotService;
import services.DataExportService;
import services.DataService;
import services.DataTransformationService;
import services.InfoService;
import services.InitService;
import services.MailService;
import services.NotificationService;
import services.OpenligaDbService;
import services.ScriptService;
import services.SeasonService;
import services.SlackService;
import services.SmartcronService;
import services.init.BackgroundTasksRunlevel;
import services.init.BulibotsRunlevel;
import services.init.CurrentSeasonAndMatchdayRunlevel;
import services.init.InitialDataRunlevel;
import services.init.smartcron.BulibotExecutor;
import services.init.smartcron.OpenligaDbSynchronizer;
import services.init.smartcron.SeasonAndMatchdayUpdater;
import services.testdata.BulibotTestdataStrategy;
import services.testdata.impl.CurrentSeasonTestdataStrategy;
import services.testdata.impl.LastFiveMatchdaysTestdataStrategy;
import services.testdata.impl.LastMatchdayTestdataStrategy;
import services.testdata.impl.LastSeasonTestdataStrategy;
import services.testdata.impl.LastTenMatchdaysTestdataStrategy;
import services.testdata.impl.LastThreeMatchdaysTestdataStrategy;
import services.testdata.impl.SeasonBeforeLastTestdataStrategy;

/**
 * Global module definition containing all component bindings.
 *
 * @author Christian Groth
 */
public class Module extends AbstractModule {

    @Override
    protected void configure() {

        // metrics module
        install(new MetricsModule());

        // bind metrics graphite exporter
        bind(NinjaGraphite.class);

        // bind controllers
        bind(ApiController.class);
        bind(TeamImageAssetsController.class);
        bind(FixedAssetsController.class);
        bind(TemplateController.class);

        // bind services
        bind(AuthenticationService.class);
        bind(BulibotService.class);
        bind(DataExportService.class);
        bind(DataService.class);
        bind(DataTransformationService.class);
        bind(InfoService.class);
        bind(InitService.class);
        bind(MailService.class);
        bind(NotificationService.class);
        bind(OpenligaDbService.class);
        bind(ScriptService.class);
        bind(SeasonService.class);
        bind(SlackService.class);
        bind(SmartcronService.class);

        // bind runlevel
        bind(BackgroundTasksRunlevel.class);
        bind(BulibotsRunlevel.class);
        bind(CurrentSeasonAndMatchdayRunlevel.class);
        bind(InitialDataRunlevel.class);

        // bind smartcrons (init)
        bind(BulibotExecutor.class);
        bind(OpenligaDbSynchronizer.class);
        bind(SeasonAndMatchdayUpdater.class);

        // bind smartcrons (property change)
        bind(UpdateRankingsPropertyChangeCallback.class);

        // bind testdata strategies
        Multibinder<BulibotTestdataStrategy> testdataStrategyBinder = Multibinder.newSetBinder(binder(), BulibotTestdataStrategy.class);
        testdataStrategyBinder.addBinding().to(LastMatchdayTestdataStrategy.class);
        testdataStrategyBinder.addBinding().to(LastThreeMatchdaysTestdataStrategy.class);
        testdataStrategyBinder.addBinding().to(LastFiveMatchdaysTestdataStrategy.class);
        testdataStrategyBinder.addBinding().to(LastTenMatchdaysTestdataStrategy.class);
        testdataStrategyBinder.addBinding().to(CurrentSeasonTestdataStrategy.class);
        testdataStrategyBinder.addBinding().to(LastSeasonTestdataStrategy.class);
        testdataStrategyBinder.addBinding().to(SeasonBeforeLastTestdataStrategy.class);
    }
}
