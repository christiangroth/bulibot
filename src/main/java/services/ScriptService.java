package services;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.BulibotConfig;
import groovy.lang.GroovySystem;
import model.statistics.MatchMetadata;
import model.statistics.MatchStatistics;
import model.statistics.Statistics;
import model.statistics.result.GoalGetter;
import model.statistics.result.GoalGetters;
import model.statistics.result.Rank;
import model.statistics.result.Ranking;
import model.statistics.result.StatisticsResult;
import model.user.BulibotVersion;
import model.user.User;
import net.datenwerke.sandbox.SandboxContext;
import net.datenwerke.sandbox.SandboxContext.AccessType;
import net.datenwerke.sandbox.SandboxContext.RuntimeMode;
import net.datenwerke.sandbox.SandboxService;
import net.datenwerke.sandbox.SandboxServiceImpl;
import net.datenwerke.sandbox.permissions.SecurityPermission;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.metrics.Timed;
import ninja.utils.NinjaProperties;
import services.model.ScriptServiceResult;
import services.model.ScriptServiceSandboxEnvironment;
import util.TimerUtils;

@Singleton
public class ScriptService {

    private static final Logger LOG = LoggerFactory.getLogger(ScriptService.class);
    private static final String ENGINE_GROOVY = "groovy";

    private ScriptEngineManager factory;
    private ScriptEngine engine;

    private boolean groovyEnabled;
    private SandboxService sandboxService;
    private SandboxContext sandboxContext;
    private ClassLoader sandboxClassloader;

    private Cache<String, CompiledScript> compiledScripts;

    @Inject
    private NinjaProperties ninjaProperties;

    @Start
    public void startup() {

        // get factory
        LOG.info("initializing scripting engine...");
        factory = new ScriptEngineManager();
        if (isScriptingEnabled()) {

            // check groovy support
            engine = factory.getEngineByName(ENGINE_GROOVY);
            groovyEnabled = engine != null;
            if (!isGroovyEnabled()) {
                LOG.error("unable to load groovy engine!!");
                return;
            }

            // setup sandboxing service
            LOG.info("initializing scripting sandbox...");
            sandboxService = SandboxServiceImpl.initLocalSandboxService();
            LOG.info("scripting system initialized.");

            // create sandbox
            LOG.info("initializing scripting sandbox context...");
            sandboxContext = createSandboxContext();
            LOG.info("scripting sandbox initialized.");

            // set sandbox classloader
            LOG.info("initializing scripting sandbox context...");
            sandboxClassloader = getClass().getClassLoader();
            LOG.info("scripting sandbox initialized.");

            // build cache for compiled scripts
            compiledScripts = CacheBuilder.newBuilder().maximumSize(ninjaProperties.getIntegerWithDefault(BulibotConfig.SCRIPT_CACHE_SIZE.getKey(), 100)).recordStats().build();

            // log groovy version
            // this is a workaround for "IOException: stream closed" when run inside docker container
            LOG.info("initialized groovy version: " + GroovySystem.getVersion());
        } else {
            LOG.error("unable to load scripting engine factory!!");
        }
        LOG.info("started scripting service.");
    }

    private SandboxContext createSandboxContext() {

        // basic setup
        SandboxContext sandboxContext = new SandboxContext();
        sandboxContext.setMaximumRunTime(ninjaProperties.getIntegerOrDie(BulibotConfig.SCRIPT_TIMEOUT.getKey()), TimeUnit.MILLISECONDS, RuntimeMode.CPU_TIME);
        sandboxContext.setRunInThread(true);

        // basic classes
        sandboxContext.addClassForApplicationLoader("groovyjarjarantlr.Token");
        sandboxContext.addClassForApplicationLoader("javax.script.ScriptEngine");
        sandboxContext.addClassForApplicationLoader(ScriptEngine.class.getName());
        sandboxContext.addClassForApplicationLoader(ScriptContext.class.getName());
        sandboxContext.addClassForApplicationLoader(InvokerHelper.class.getName());
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("groovy.security.GroovyCodeSourcePermission", "/groovy/script"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "accessDeclaredMembers"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", "antlr.ast"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", "groovy.ast"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", "groovy.use.classvalue", "read"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", "java.vm.name", "read"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", "line.separator", "read"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", "ANTLR_DO_NOT_EXIT", "read"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", "ANTLR_USE_DIRECT_CLASS_LOADING", "read"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.reflect.ReflectPermission", "suppressAccessChecks"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "getProtectionDomain"));
        sandboxContext.addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.logging.LoggingPermission", "control"));

        // deny runtime permissions
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "getenv"));
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "exitVM"));
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "shutdownHooks"));
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "setIO"));
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "setSecurityManager"));
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "modifyThread"));
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "stopThread"));
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "modifyThreadGroup"));
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "queuePrintJob"));
        sandboxContext.addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.RuntimePermission", "getStackTrace"));

        // bulibot datamodel
        sandboxContext.addClassForApplicationLoader(ScriptServiceResult.class.getName());
        sandboxContext.addClassForApplicationLoader(Statistics.class.getName());
        sandboxContext.addClassForApplicationLoader(StatisticsResult.class.getName());
        sandboxContext.addClassForApplicationLoader(MatchMetadata.class.getName());
        sandboxContext.addClassForApplicationLoader(MatchStatistics.class.getName());
        sandboxContext.addClassForApplicationLoader(Ranking.class.getName());
        sandboxContext.addClassForApplicationLoader(Rank.class.getName());
        sandboxContext.addClassForApplicationLoader(GoalGetters.class.getName());
        sandboxContext.addClassForApplicationLoader(GoalGetter.class.getName());

        // done
        return sandboxContext;
    }

    @Timed
    public ScriptServiceResult executeGroovy(Map<String, Object> bindingValues, User user, BulibotVersion version, Set<String> returnValues) {
        ScriptServiceResult result = new ScriptServiceResult();

        // check if groovy scripting is available
        if (!isGroovyEnabled()) {
            return result;
        }

        // prepare script
        ScriptContext context = new SimpleScriptContext();
        CompiledScript compiledScript;
        try {
            compiledScript = ensureCompiledScript(generateScriptId(user, version), version.getSource(), result);
        } catch (ScriptException e) {
            LOG.error("unable to compile script: " + e.getMessage());
            result.setSuccess(false);
            result.setError(e);
            return result;
        }

        // set bindings
        Bindings binding = new SimpleBindings();
        if (bindingValues != null) {
            for (Entry<String, Object> entry : bindingValues.entrySet()) {
                binding.put(entry.getKey(), entry.getValue());
            }
        }
        context.setBindings(binding, ScriptContext.ENGINE_SCOPE);

        // redirect outputs
        context.setWriter(result.getStdout());
        context.setErrorWriter(result.getStdout());

        long duration = -1;
        try {

            // execute
            duration = TimerUtils
                    .measure(() -> sandboxService.runSandboxed(ScriptServiceSandboxEnvironment.class, sandboxContext, sandboxClassloader, compiledScript, context, result));
            result.setDuration(duration);

            // check for error
            Throwable scriptError = result.getError();
            if (scriptError != null) {
                LOG.error("failed to execute groovy script: " + scriptError.getMessage());
            }
        } catch (Throwable e) {
            LOG.error("groovy security violation detected: " + e.getMessage());
            result.setSuccess(false);
            result.setError(e);
        }
        LOG.debug("executed groovy script in " + duration + "ms");

        // flush outputs
        try {
            context.getWriter().flush();
            context.getErrorWriter().flush();
        } catch (IOException e) {
            LOG.error("unable to flush script stdout: " + e.getMessage());
        }

        // copy state
        for (Entry<String, Object> entry : binding.entrySet()) {
            result.getState().put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
        }

        // collect return values
        if (returnValues != null) {
            for (String returnValue : returnValues) {
                result.getReturnValues().put(returnValue, binding.get(returnValue));
            }
        }

        // done
        return result;
    }

    public void invalidateCompiledScript(User user, BulibotVersion version) {
        compiledScripts.invalidate(generateScriptId(user, version));
    }

    private String generateScriptId(User user, BulibotVersion version) {
        return user.getId() + "-" + version.getName();
    }

    private CompiledScript ensureCompiledScript(String scriptId, String scriptScource, ScriptServiceResult result) throws ScriptException {

        // ensure script
        CompiledScript compiledScript = compiledScripts.getIfPresent(scriptId);
        if (compiledScript == null) {
            synchronized (compiledScripts) {
                compiledScript = compiledScripts.getIfPresent(scriptId);
                if (compiledScript == null) {
                    LOG.info("compiling script for " + scriptId);
                    compiledScript = ((Compilable) engine).compile(scriptScource);
                    compiledScripts.put(scriptId, compiledScript);
                }
            }
        }

        // done
        return compiledScript;
    }

    public boolean isGroovyEnabled() {
        return isScriptingEnabled() && groovyEnabled;
    }

    public boolean isScriptingEnabled() {
        return factory != null;
    }

    @Dispose
    public void stop() {
        LOG.info("stopping scripting sandbox...");
        sandboxService.shutdown();
        LOG.info("stopped scripting service.");
    }
}
