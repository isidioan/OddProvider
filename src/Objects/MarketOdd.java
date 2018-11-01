package Objects;

import java.security.PublicKey;
import java.util.Objects;

public class MarketOdd {


    public static MarketOdd create(final String homeTeam, final String awayTeam, final String division, final String market, final String oddType, final double actualOdd) {
        return new MarketOdd(homeTeam, awayTeam, division, market, oddType, actualOdd);
    }

    public String getMarket() {
        return market;
    }

    public String getOddType() {
        return oddType;
    }

    public double getActualOdd() {
        return actualOdd;
    }

    public String getmHomeTeam() {
        return mHomeTeam;
    }

    public String getmAwayTeam() {
        return mAwayTeam;
    }

    public String getmDivision() {
        return mDivision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketOdd marketOdd = (MarketOdd) o;
        return Objects.equals(mHomeTeam, marketOdd.mHomeTeam) &&
                Objects.equals(mAwayTeam, marketOdd.mAwayTeam) &&
                Objects.equals(mDivision, marketOdd.mDivision) &&
                Objects.equals(market, marketOdd.market);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mHomeTeam, mAwayTeam, mDivision, market);
    }

    public MarketOdd(final String homeTeam, final String awayTeam, final String division, final String market, final String oddType, final double actualOdd) {
        this.mHomeTeam = homeTeam;
        this.mAwayTeam = awayTeam;
        this.mDivision = division;
        this.market = market;
        this.oddType = oddType;
        this.actualOdd = actualOdd;
    }

    private final String mHomeTeam;
    private final String mAwayTeam;
    private final String mDivision;
    private final String market;
    private final String oddType;
    private final double actualOdd;


}


