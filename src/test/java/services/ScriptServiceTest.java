package services;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import configuration.BulibotConfig;
import model.match.Goal;
import model.match.Match;
import model.match.Match.Status;
import model.statistics.GoalStatistics;
import model.statistics.MatchMetadata;
import model.statistics.MatchStatistics;
import model.statistics.result.EloHelper;
import model.statistics.result.StatisticsResult;
import model.user.BulibotVersion;
import model.user.User;
import ninja.utils.NinjaProperties;
import services.model.ScriptServiceResult;
import util.TestData;

public class ScriptServiceTest {

    private static boolean started;
    private static ScriptService service;

    private Map<String, Object> bindings;
    private Set<String> returnValues;

    @BeforeClass
    public static void initialize() {

        // create service
        service = new ScriptService();
        started = false;
    }

    @Mock
    private NinjaProperties ninjaProperties;

    private int modelId;

    private User dummyUser;
    private BulibotVersion dummyVersion;

    @Before
    public void setup() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        // init mocks
        MockitoAnnotations.initMocks(this);
        Mockito.when(ninjaProperties.getIntegerOrDie(Mockito.eq(BulibotConfig.SCRIPT_TIMEOUT.getKey()))).thenReturn(1000);
        injectField("ninjaProperties", ninjaProperties);

        // prepare scripting
        bindings = new HashMap<String, Object>();
        returnValues = new HashSet<String>();

        // start service on first test
        if (!started) {
            service.startup();
            Assert.assertTrue(service.isScriptingEnabled());
            Assert.assertTrue(service.isGroovyEnabled());
            started = true;
        }

        // init id sequence
        modelId = 0;

        // init dummy data
        dummyUser = new User();
        dummyUser.setId(modelId++);
        dummyVersion = new BulibotVersion();
    }

    public void injectField(String fieldName, Object fieldValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = ScriptService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, fieldValue);
    }

    @Test
    public void basic() {

        // execute
        bindings.put("x", 3);
        returnValues.add("resultValue");
        ScriptServiceResult result = execute("println(\"message\");resultValue = 2 + x;println(\"done.\");");

        // assert value
        Object resultValue = result.getReturnValues().get("resultValue");
        Assert.assertNotNull(resultValue);
        Assert.assertEquals(Integer.class, resultValue.getClass());
        Assert.assertEquals(Integer.valueOf(5), resultValue);
        Assert.assertEquals(result.getState().get("resultValue"), resultValue.toString());
        Assert.assertEquals("message\ndone.\n", result.getStdout().toString());
    }

    @Test
    public void bulibot() {

        // execute
        TestData testData = new TestData();
        bindings.put("match", new MatchMetadata(testData.m1));
        bindings.put("statistics", testData.statistics);
        bindings.put("statisticsResult", testData.statistics.builder().build());
        execute("x = 1;");
    }

    @Test
    public void compileError() {
        execute("some bad non groovy stuff", false);
    }

    @Test
    public void endlessLoop() {
        execute("while(true){x = 234 * 17.5 + 5 / 3; }", false);
    }

    @Test
    public void systemExit() {
        execute("System.exit(0);", false);
    }

    @Test
    public void executeShellCommand() {
        execute("println \"ls\".execute().text", false);
        execute("\"ping -c 1 google.de\".execute();", false);
    }

    @Test
    public void threadStart() {
        // unfortunately there is no explicit permission to control thread creation so this can't be blocked
        execute("t = new Thread({System.out.println(\"ok\");});t.start();t.join();", true);
    }

    @Test
    public void threadStop() {
        execute("Thread.currentThread().stop();", false);
    }

    @Test
    public void openSocket() {
        execute("server = new java.net.ServerSocket(4444);", false);
    }

    @Test
    public void conectToSocket() {
        execute("s = new java.net.Socket(\"localhost\", 4444);", false);
    }

    @Test
    public void readFile() {
        execute("new File('/whatever/test.txt').getText();", false);
    }

    @Test
    public void writeFile() {
        execute("fw = new FileWriter('/whatever/test.txt');fw.write('yay');fw.flush();", false);
    }

    @Test
    public void changeFinalProperty() {

        // test object
        List<MatchStatistics> matches = new ArrayList<>();
        matches.add(new MatchStatistics(match(2016, 1, 1, "BvB", 2, "FcB", Status.IN_PROGRESS, 0, 0, 1, 0)));
        matches.get(0).getGoals().add(new GoalStatistics(goal()));
        StatisticsResult testObj = new StatisticsResult(EloHelper.builder().build(), matches);
        bindings.put("testObj", testObj);

        // script
        StringBuffer sb = new StringBuffer();
        sb.append("println testObj.numberOfMatches;");
        sb.append("testObj.numberOfMatches = 17;");
        sb.append("println testObj.numberOfMatches;");
        execute(sb.toString(), false);
    }

    @Test
    public void changeFinalPropertyUsingReflection() {

        // test object
        List<MatchStatistics> matches = new ArrayList<>();
        matches.add(new MatchStatistics(match(2016, 1, 1, "BvB", 2, "FcB", Status.IN_PROGRESS, 0, 0, 1, 0)));
        matches.get(0).getGoals().add(new GoalStatistics(goal()));
        StatisticsResult testObj = new StatisticsResult(EloHelper.builder().build(), matches);
        bindings.put("testObj", testObj);

        // script
        StringBuffer sb = new StringBuffer();
        sb.append("println testObj.numberOfMatches;");
        sb.append("field = testObj.class.declaredFields[0];");
        // this can't be blocked cause java.lang.reflect.ReflectPermission:suppressAccessChecks has to be allowed for groovy to work
        // sb.append("field.setAccessible(true);");
        sb.append("field.set(testObj, 17);");
        sb.append("println testObj.numberOfMatches;");
        execute(sb.toString(), false);
    }

    private Match match(int season, int matchday, long teamOneId, String teamOneName, long teamTwoId, String teamTwoName, Status status, int goalsTeamOneHalfTime,
            int goalsTeamTwoHalfTime, int goalsTeamOneFullTime, int goalsTeamTwoFullTime) {
        final Match match = new Match();
        match.setId(modelId++);
        match.setSeason(season);
        match.setMatchday(matchday);
        match.setAssignedTime(LocalDateTime.now());
        match.setLastUpdateTimeString(LocalDateTime.now().toString());
        match.setTeamOneId(teamOneId);
        match.setTeamOneName(teamOneName);
        match.setTeamOneIconUrl("http://" + teamOneName);
        match.setTeamTwoId(teamTwoId);
        match.setTeamTwoName(teamTwoName);
        match.setTeamTwoIconUrl("http://" + teamTwoName);
        match.setStatus(status);
        match.setGoalsTeamOneHalfTime(goalsTeamOneHalfTime);
        match.setGoalsTeamTwoHalfTime(goalsTeamTwoHalfTime);
        match.setGoalsTeamOneFullTime(goalsTeamOneFullTime);
        match.setGoalsTeamTwoFullTime(goalsTeamTwoFullTime);
        match.setGoals(new ArrayList<>());
        return match;
    }

    private Goal goal() {
        final Goal goal = new Goal();
        goal.setGoalGetterId(modelId++);
        return goal;
    }

    private ScriptServiceResult execute(String script) {
        return execute(script, true);
    }

    private ScriptServiceResult execute(String script, boolean success) {
        dummyVersion.setSource(script);
        ScriptServiceResult result = service.executeGroovy(bindings, dummyUser, dummyVersion, returnValues);
        Assert.assertTrue(result.isExecuted());
        Assert.assertEquals(success, result.isSuccess());
        if (success) {
            Assert.assertNull(result.getError());
        } else {
            Assert.assertNotNull(result.getError());
        }
        return result;
    }

    @AfterClass
    public static void teardown() {
        service.stop();
    }
}
