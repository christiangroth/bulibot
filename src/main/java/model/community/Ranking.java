package model.community;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flexjson.JSON;
import model.match.Match;
import model.match.Match.Status;
import model.statistics.Winner;
import model.user.User;

public class Ranking {

    private static final RankComparator RANK_COMPARATOR = new RankComparator();

    private static class RankComparator implements Comparator<Rank> {

        @Override
        public int compare(Rank o1, Rank o2) {

            // compare points
            int o1Points = o1.getPoints();
            int o2Points = o2.getPoints();
            if (o1Points != o2Points) {
                return compare(o2Points, o1Points);
            }

            // compare exact hits
            double o1ExactHits = o1.getExactHitPercentage();
            double o2ExactHits = o2.getExactHitPercentage();
            if (o1ExactHits != o2ExactHits) {
                return compare(o2ExactHits, o1ExactHits);
            }

            // compare relative hits
            double o1RelativeHits = o1.getRelativeHitPercentage();
            double o2RelativeHits = o2.getRelativeHitPercentage();
            if (o1RelativeHits != o2RelativeHits) {
                return compare(o2RelativeHits, o1RelativeHits);
            }

            // compare number of different versions
            // TODO test
            long o1BulibotVersions = o1.getBulibotVersions();
            long o2BulibotVersions = o2.getBulibotVersions();
            if (o1BulibotVersions != o2BulibotVersions) {
                return compare(o1BulibotVersions, o2BulibotVersions);
            }

            // compare error percentage
            double o1ErrorPercentage = o1.getErrorPercentage();
            double o2ErrorPercentage = o2.getErrorPercentage();
            if (o1ErrorPercentage != o2ErrorPercentage) {
                return compare(o1ErrorPercentage, o2ErrorPercentage);
            }

            // equal position, compare user ids (must not be equal otherwise ranks will be considered same!!)
            return Long.valueOf(o1.getUserId()).compareTo(Long.valueOf(o2.getUserId()));
        }

        int compare(int one, int two) {
            return Integer.valueOf(one).compareTo(Integer.valueOf(two));
        }

        int compare(double one, double two) {
            return Double.valueOf(one).compareTo(Double.valueOf(two));
        }
    }

    @JSON
    private final List<Rank> ranks = new ArrayList<>();

    private int resultsHomeTeam;
    private int resultsAwayTeam;
    private int resultsDraw;

    public Ranking(List<User> users, List<BulibotExecution> bulibotExecutions, boolean allowOnEmptyBulibotsData, List<Match> matches, int pointsExactHit, int pointsRelativeHit,
            int pointsWinnerHit) {

        // skip on empty match data
        if (matches.isEmpty()) {
            return;
        }

        // skip on empty bulibot data if not the current season
        if (bulibotExecutions.isEmpty() && !allowOnEmptyBulibotsData) {
            return;
        }

        // prepare matches cache
        Map<Long, Match> matchesCache = new HashMap<>();
        for (Match match : matches) {
            matchesCache.put(match.getId(), match);

            Winner winner = Winner.calc(match);
            if (winner != null) {
                switch (winner) {
                    case ONE:
                        resultsHomeTeam++;
                        break;
                    case DRAW:
                        resultsDraw++;
                        break;
                    case TWO:
                        resultsAwayTeam++;
                        break;
                }
            }
        }

        // collect data
        users.forEach(u -> ensureRank(u));
        bulibotExecutions.forEach(be -> add(be, matchesCache, pointsExactHit, pointsRelativeHit, pointsWinnerHit));

        // count bulibot versions per user
        // TODO test?
        for (User user : users) {
            ensureRank(user).setBulibotVersions(bulibotExecutions.stream().filter(be -> be.getUserId() == user.getId()).map(be -> be.getBulibotVersionName()).distinct().count());
        }

        // set positions values
        Collections.sort(ranks, RANK_COMPARATOR);
        for (int i = 0; i < ranks.size(); i++) {
            ranks.get(i).setPosition(i + 1);
        }
    }

    private void add(BulibotExecution bulibotExecution, Map<Long, Match> matchesCache, int pointsExactHit, int pointsRelativeHit, int pointsWinnerHit) {

        // update rank
        Rank rank = ensureRank(bulibotExecution);
        Match match = matchesCache.get(bulibotExecution.getMatchId());
        if (match != null && match.getStatus() != Status.WAITING) {

            // update results
            Winner winner = Winner.calc(bulibotExecution);
            switch (Result.calc(bulibotExecution, match)) {
                case ERROR:
                    rank.resultsWithErrorInc();
                    break;
                case EXACT:
                    rank.resultsWithExactHitInc(pointsExactHit, winner);
                    break;
                case RELATIVE:
                    rank.resultsWithRelativeHitInc(pointsRelativeHit, winner);
                    break;
                case WINNER:
                    rank.resultsWithWinnerHitInc(pointsWinnerHit, winner);
                    break;
                case WRONG:
                    rank.resultsWithNoHitInc();
                    break;
            }

            // update statistics
            if (winner != null) {
                switch (winner) {
                    case ONE:
                        rank.resultsHomeTeamInc();
                        break;
                    case DRAW:
                        rank.resultsDrawInc();
                        break;
                    case TWO:
                        rank.resultsAwayTeamInc();
                        break;
                }
            }
        }
    }

    private Rank ensureRank(User user) {
        return ensureRank(user.getId());
    }

    private Rank ensureRank(BulibotExecution bulibotExecution) {
        return ensureRank(bulibotExecution.getUserId());
    }

    private Rank ensureRank(long userId) {

        // search for user rank
        Rank rank = getRank(userId);

        // create new rank if needed
        if (rank == null) {
            rank = new Rank(userId);
            ranks.add(rank);
        }

        // done
        return rank;
    }

    public Rank getRank(long userId) {
        return ranks.stream().filter(r -> r.getUserId() == userId).findFirst().orElse(null);
    }

    public List<Rank> getRanks() {
        return ranks;
    }

    public int getResultsHomeTeam() {
        return resultsHomeTeam;
    }

    public int getResultsAwayTeam() {
        return resultsAwayTeam;
    }

    public int getResultsDraw() {
        return resultsDraw;
    }
}
