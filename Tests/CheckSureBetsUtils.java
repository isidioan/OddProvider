
import OddsApi.SureBetUtils;
import org.junit.jupiter.api.Test;

import static Utils.FetchingUtils.*;

public class CheckSureBetsUtils {

    @Test
    public void checkMaximazationOfProfitSplit() {

        final double homeOdd = 2.61;
        final double drawOdd = 3.4;
        final double awayOdd = 3.1;

        final Triplet<Double, Double, Double> res = SureBetUtils.calculateMaximunProfitForHomeXAway(homeOdd, drawOdd, awayOdd);
        System.out.println(res);

    }

}
