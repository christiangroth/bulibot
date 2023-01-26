package services.testdata;

import java.util.List;

/**
 * Interface for all testdata strategies.
 *
 * @author chris
 */
public interface BulibotTestdataStrategy {

    public static class BulibotTestdata {
        private final int season;
        private final int matchday;

        public BulibotTestdata(int season, int matchday) {
            this.season = season;
            this.matchday = matchday;
        }

        public int getSeason() {
            return season;
        }

        public int getMatchday() {
            return matchday;
        }
    }

    /**
     * Unique code for strategy.
     *
     * @return unique code
     */
    String code();

    /**
     * Checks if this strategy is available.
     *
     * @return true if available, false if not
     */
    boolean available();

    /**
     * Returns test data for this strategy.
     *
     * @return list of test data
     */
    List<BulibotTestdata> data();
}
