package Objects;

import java.util.Objects;

public class Match {

    public static Match create(final String homeTeam, final String awayTeam, final String division) {
        return new Match(homeTeam, awayTeam, division);
    }

    public String getHomeTeam() {
        return mHomeTeam;
    }

    public String getAwayTeam() {
        return mAwayTeam;
    }

    public String getDivision() {
        return mDivision;
    }

    private Match(final String homeTeam, final String awayTeam, final String division) {
        mHomeTeam = homeTeam;
        mAwayTeam = awayTeam;
        mDivision = division;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(mHomeTeam, match.mHomeTeam) &&
                Objects.equals(mAwayTeam, match.mAwayTeam);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mHomeTeam, mAwayTeam);
    }

    @Override
    public String toString() {
        return
                "[" + mHomeTeam + " VS " + mAwayTeam + "]";
    }

    private final String mHomeTeam;
    private final String mAwayTeam;
    private final String mDivision;
}