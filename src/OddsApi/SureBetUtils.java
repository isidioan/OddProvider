package OddsApi;

import Utils.FetchingUtils;
import static Utils.FetchingUtils.*;

public class SureBetUtils {

    public static boolean isMatchSureBet(final double homeOdd, final double drawOdd, final double awayOdd) {

        return
                ((1 / homeOdd) + (1 / drawOdd) + (1 / awayOdd)) < 1.0;

    }

    public static boolean isMatchSureBet(final double overOdd, final double underOdd) {

        return
                ((1 / overOdd) + (1 / underOdd)) < 1.0;

    }

    public static double calculateSureBet(final double homeOdd, final double drawOdd, final double awayOdd) {

        return
                ((1 / homeOdd) + (1 / drawOdd) + (1 / awayOdd));

    }

    public static double calculateSureBet(final double overOdd, final double underOdd) {

        return
                ((1 / overOdd) + (1 / underOdd));

    }

    public static Triplet<Double, Double, Double> calculateMaximunProfitForHomeXAway(final double homeOdd, final double drawOdd, final double awayOdd) {

        final double yx = 1 / homeOdd + 1 / drawOdd + 1 / awayOdd;

        final double y = 100 / yx;

        final double x1 = y / homeOdd;
        final double x2 = y / drawOdd;
        final double x3 = y / awayOdd;

        return Triplet.create(x1, x2, x3);
    }

    public static double calculateProfitPercentageForHomeXAway(final double homeOdd, final double drawOdd, final double awayOdd) {


        final double profit = (calculateMaximunProfitForHomeXAway(homeOdd, drawOdd, awayOdd).getFirst() * homeOdd) - 100;

        return (profit / 100) * 100;
    }

}
