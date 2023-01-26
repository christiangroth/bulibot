package util;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

/**
 * Utility class to encapsulate time measurements.
 * 
 * @author Christian Groth
 */
public final class TimerUtils {

    private TimerUtils() {

        // utility class, private constructor
    }

    public static long measure(Runnable code) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        code.run();
        stopwatch.stop();
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }
}
