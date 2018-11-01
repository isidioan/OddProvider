package OddsApi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TheOddApiRetriever {


    private final static Gson sGson = new Gson();

    public static void main(String args[]) throws InterruptedException, ExecutionException, IOException {
        getAllMatches();

    }

    public static List<Data> getAllMatches() throws IOException {

        final Type bookmakersListToken = new TypeToken<Obj>() {
        }.getType();

        try {

            final HttpURLConnection conn = getHttpURLConnection("https://api.the-odds-api.com/v3/odds/?sport=UPCOMING&region=uk&apiKey=6e9a2e55f2546111aa2e4d90ebbdcafe");

            final BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            StringBuilder stringBuilder = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                stringBuilder.append(output);
            }

            conn.disconnect();

            final List<Data> matchesList =
                    ((Obj) sGson.fromJson(stringBuilder.toString(), bookmakersListToken)).getData();


            final Map<Match, List<HomeXAwayOdds>> matchWithOdds =
                    matchesList
                            .stream()
                            .map(Data::getHomeXAwayOddsList)
                            .flatMap(List::stream)
                            .collect(Collectors.groupingBy(HomeXAwayOdds::getMatch, Collectors.toList()));

            final List<SureHomeXAwayPrediction> sureBetsHomeXAwayResults = new ArrayList<>(10000);

            calculatingSureBetsHomeXAway(matchWithOdds, sureBetsHomeXAwayResults);

            final List<SureHomeXAwayPrediction> homeXAwaySureBets =
                    sureBetsHomeXAwayResults
                            .stream()
                            .sorted(Comparator.comparing(SureHomeXAwayPrediction::getScore))
                            .collect(Collectors.toList());

            return matchesList;

        } catch (final Throwable ex) {

            throw ex;

        }
    }

    public static void calculatingSureBetsHomeXAway(
            final Map<Match, List<HomeXAwayOdds>> homeXAwayOdds,
             final List<SureHomeXAwayPrediction> sureBetsHomeXAwayResults) {

        homeXAwayOdds.
                forEach((key, value) -> {
                    final String[][] arrStr = new String[value.size()][4];

                    IntStream.range(0, value.size()).forEach(idx -> {
                        arrStr[idx][0] = value.get(idx).getBookmaker().getName();
                        arrStr[idx][1] = String.valueOf(value.get(idx).getLiveHomeXDrawOdd().getHomeOdd());
                        arrStr[idx][2] = String.valueOf(value.get(idx).getLiveHomeXDrawOdd().getDrawOdd());
                        arrStr[idx][3] = String.valueOf(value.get(idx).getLiveHomeXDrawOdd().getAwayOdd());
                    });

                    try {

                        for (int i = 0; i < value.size(); i++) {
                            for (int j = 0; j < value.size(); j++) {
                                for (int g = 0; g < value.size(); g++) {

                                    if (SureBetUtils.isMatchSureBet(
                                            Double.parseDouble(arrStr[i][1]),
                                            Double.parseDouble(arrStr[j][2]),
                                            Double.parseDouble(arrStr[g][3]))) {

                                        final SureHomeXAwayPrediction res =
                                                SureHomeXAwayPrediction.create(
                                                        key.getHomeTeam(),
                                                        key.getAwayTeam(),
                                                        arrStr[i][0],
                                                        arrStr[j][0],
                                                        arrStr[g][0],
                                                        Double.parseDouble(arrStr[i][1]),
                                                        Double.parseDouble(arrStr[j][2]),
                                                        Double.parseDouble(arrStr[g][3]),
                                                        SureBetUtils.calculateSureBet(Double.parseDouble(arrStr[i][1]), Double.parseDouble(arrStr[j][2]), Double.parseDouble(arrStr[g][3])),
                                                        SureBetUtils.calculateProfitPercentageForHomeXAway(Double.parseDouble(arrStr[i][1]), Double.parseDouble(arrStr[j][2]), Double.parseDouble(arrStr[g][3])));

                                        final int idx = sureBetsHomeXAwayResults.indexOf(res);
                                        if (idx != -1 && sureBetsHomeXAwayResults.get(idx).getScore() > res.getScore()) {
                                            sureBetsHomeXAwayResults.set(idx, res);
                                        }
                                        if (idx == -1) {
                                            sureBetsHomeXAwayResults.add(res);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (final Throwable ex) {
                        System.out.println(ex);
                    }
                });
    }

    public final static class SureHomeXAwayPrediction {

        public static SureHomeXAwayPrediction create(
                final String homeTeam,
                final String awayTeam,
                final String firstBookmaker,
                final String secondBookmaker,
                final String thirdBookmaker,
                final double homeOdd,
                final double drawOdd,
                final double awayOdd,
                final double score,
                final double profitPercentage) {

            return new SureHomeXAwayPrediction( homeTeam, awayTeam, firstBookmaker, secondBookmaker, thirdBookmaker, homeOdd, drawOdd, awayOdd, score, profitPercentage);
        }




        public String getFirstBookmaker() {
            return firstBookmaker;
        }

        public String getSecondBookmaker() {
            return secondBookmaker;
        }

        public String getThirdBookmaker() {
            return thirdBookmaker;
        }

        public double getHomeOdd() {
            return homeOdd;
        }

        public double getDrawOdd() {
            return drawOdd;
        }

        public double getAwayOdd() {
            return awayOdd;
        }

        public double getScore() {
            return score;
        }

        public String getHomeTeam() {
            return homeTeam;
        }

        public String getAwayTeam() {
            return awayTeam;
        }

        public double getProfitPercentage() {
            return profitPercentage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SureHomeXAwayPrediction that = (SureHomeXAwayPrediction) o;
            return Objects.equals(homeTeam, that.homeTeam) &&
                    Objects.equals(awayTeam, that.awayTeam);
        }

        @Override
        public int hashCode() {

            return Objects.hash(homeTeam, awayTeam);
        }

        public SureHomeXAwayPrediction(
                final String homeTeam,
                final String awayTeam,
                String firstBookmaker,
                String secondBookmaker,
                String thirdBookmaker,
                double homeOdd,
                double drawOdd,
                double awayOdd,
                double score,
                final double profitPercentage) {

            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
            this.firstBookmaker = firstBookmaker;
            this.secondBookmaker = secondBookmaker;
            this.thirdBookmaker = thirdBookmaker;
            this.homeOdd = homeOdd;
            this.drawOdd = drawOdd;
            this.awayOdd = awayOdd;
            this.score = score;
            this.profitPercentage = profitPercentage;

            df.setRoundingMode(RoundingMode.DOWN);
        }

        public String toHtml(final int number) {
            return "  <tr>\n" +
                    "    <td> " + number +  " </td>\n" +
                    "    <td> " + homeTeam +  " </td>\n" +
                    "    <td> " + awayTeam +  " </td>\n" +
                    "    <td> " + firstBookmaker + " </td>\n" +
                    "    <td> " + secondBookmaker + " </td>\n" +
                    "    <td> " + thirdBookmaker + " </td>\n" +
                    "    <td> " + homeOdd + " </td>\n" +
                    "    <td> " + drawOdd + " </td>\n" +
                    "    <td> " + awayOdd + " </td>\n" +
                    "    <td> " + score + " </td>\n" +
                    "    <td> " + df.format(profitPercentage) + "%" + " </td>\n" +
                    "  </tr>\n";

        }


        private final String homeTeam;
        private final String awayTeam;
        private final String firstBookmaker;
        private final String secondBookmaker;
        private final String thirdBookmaker;
        private final double homeOdd;
        private final double drawOdd;
        private final double awayOdd;
        private final double score;
        private final double profitPercentage;


        final static DecimalFormat df = new DecimalFormat(".##");

    }


    private static HttpURLConnection getHttpURLConnection(final String link) throws IOException {
        final URL url = new URL(link);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        //        conn.setRequestProperty("X-Mashape-Key", "xcMA6q0YzYmsh6lk28d7sdcfh2Wdp1ICCL1jsnXFtA2jCubElg");
//        conn.setRequestProperty("X-Mashape-Key", "uWkiJPGHCqmshTVSJ8GndwdD4zhDp1QQ4PajsnV30lmntTcg7d");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }
        return conn;
    }

    public static class Obj {

        public Obj(String success, List<Data> data) {
            this.success = success;
            this.data = data;
        }

        public String getSuccess() {
            return success;
        }

        public List<Data> getData() {
            return data;
        }

        public List<HomeXAwayOdds> getAllHomeXAwayOdds() {
            return data
                    .stream()
                    .map(Data::getHomeXAwayOddsList)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        private final String success;
        private final List<Data> data;

    }

    public static class SiteObj {

        public SiteObj(String site_key, String site_nice, String last_update, HashMap<String, List<Double>> odds) {
            this.site_key = site_key;
            this.site_nice = site_nice;
            this.last_update = last_update;
            this.odds = odds;
        }

        public String getBookmaker() {
            return site_key;
        }

        public String getSite_nice() {
            return site_nice;
        }

        public String getLast_update() {
            return last_update;
        }

        public double getHomeOdd() {
            return odds.get("h2h").size() < 3
                    ? 0.0:
                    odds.get("h2h").get(0);
        }

        public double getDrawOdd() {
            return odds.get("h2h").size() < 3
                    ? 0.0:
            odds.get("h2h").get(2);
        }

        public double getAwayOdd() {
            return odds.get("h2h").size() < 3
                    ? 0.0:
             odds.get("h2h").get(1);
        }

        public HomeXAwayOdds getHomeXAwayOdd() {
            return HomeXAwayOdds.create(null, Bookmaker.create(getBookmaker()), LiveHomeXAwayOdd.create(getHomeOdd(), getDrawOdd(), getAwayOdd()));
        }

        public HashMap<String, List<Double>> getOdds() {
            return odds;
        }

        private final String site_key;
        private final String site_nice;
        private final String last_update;
        private final HashMap<String, List<Double>> odds;

    }

    public static class HomeXAwayOdds {
        public static HomeXAwayOdds create(final Match match, final Bookmaker name, final LiveHomeXAwayOdd live) {
            return new HomeXAwayOdds(match, name, live);
        }

        public Bookmaker getBookmaker() {
            return bookmaker;
        }

        public LiveHomeXAwayOdd getLiveHomeXDrawOdd() {
            return live;
        }

        public Match getMatch() {
            return match;
        }

        public LiveHomeXAwayOdd getLive() {
            return live;
        }

        public HomeXAwayOdds(final Match match, final Bookmaker bookmaker, final LiveHomeXAwayOdd liveOverUnderOdd) {
            this.match = match;
            this.bookmaker = bookmaker;
            this.live = liveOverUnderOdd;
        }

//        public String toHtml() {
//            return "  <tr>\n" +
//                    "    <td> " + matchId +  " </td>\n" +
//                    "    <td> " + id + " </td>\n" +
//                    "    <td> " + bookmaker.getName() + " </td>\n" +
//                    "  </tr>\n";

//        }

        private final Match match;
        private final Bookmaker bookmaker;
        private final LiveHomeXAwayOdd live;
    }

    private static class Match {

        public static Match create(final String homeTeam, final String awayTeam) {
            return new Match(homeTeam, awayTeam);
        }

        public String getHomeTeam() {
            return homeTeam;
        }

        public String getAwayTeam() {
            return awayTeam;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Match match = (Match) o;
            return Objects.equals(homeTeam, match.homeTeam) &&
                    Objects.equals(awayTeam, match.awayTeam);
        }

        @Override
        public int hashCode() {

            return Objects.hash(homeTeam, awayTeam);
        }

        private Match(String homeTeam, String awayTeam) {
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
        }

        private final String homeTeam;
        private final String awayTeam;
    }

    public static class LiveHomeXAwayOdd {

        public static LiveHomeXAwayOdd create(double home, double draw, double away) {
            return new LiveHomeXAwayOdd(home, draw, away);
        }

        public double getHomeOdd() {
            return home;
        }

        public double getDrawOdd() {
            return draw;
        }

        public double getAwayOdd() {
            return away;
        }

        private LiveHomeXAwayOdd(double home, double draw, double away) {
            this.home = home;
            this.draw = draw;
            this.away = away;
        }

        private final double home;
        private final double draw;
        private final double away;
    }

    public final static class Bookmaker {
        public static Bookmaker create(final String name) {
            return new Bookmaker(name);
        }

        public String getName() {
            return name;
        }

        public Bookmaker(String name) {
            this.name = name;
        }

        final private String name;
    }

    public static class Data {

        public Data(
                String sport_key,
                String sport_nice,
                List teams,
                String commence_time,
                String home_team,
                List<SiteObj> sites) {

            this.sport_key = sport_key;
            this.sport_nice = sport_nice;
            this.teams = teams;
            this.commence_time = commence_time;
            this.home_team = home_team;
            this.sites = sites;
        }

        public String getSport_key() {
            return sport_key;
        }

        public String getSport_nice() {
            return sport_nice;
        }

        public List getTeams() {
            return teams;
        }

        public String getCommence_time() {
            return commence_time;
        }

        public String getHome_team() {
            return home_team;
        }


        public  List<HomeXAwayOdds> getHomeXAwayOddsList() {
            return sites.stream().map(x ->
                    HomeXAwayOdds.create(
                            Match.create(
                                    String.valueOf(teams.get(0)),
                                    String.valueOf(teams.get(1))),
                            Bookmaker.create(x.getBookmaker()),
                            LiveHomeXAwayOdd.create(x.getHomeOdd(), x.getDrawOdd(), x.getAwayOdd()))


            ).collect(Collectors.toList());
        }

        public List<SiteObj> getSites() {
            return sites;
        }

        private final String sport_key;
        private final String sport_nice;
        private List teams;
        private final String commence_time;
        private final String home_team;
        private final List<SiteObj> sites;
    }


    public class Datum {

        private String sportKey;
        private String sportNice;
        private List<String> teams = null;
        private Integer commenceTime;
        private String homeTeam;
        private List<Site> sites = null;
        private Integer sitesCount;

        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        public String getSportKey() {
            return sportKey;
        }

        public void setSportKey(String sportKey) {
            this.sportKey = sportKey;
        }

        public String getSportNice() {
            return sportNice;
        }

        public void setSportNice(String sportNice) {
            this.sportNice = sportNice;
        }

        public List<String> getTeams() {
            return teams;
        }

        public void setTeams(List<String> teams) {
            this.teams = teams;
        }

        public Integer getCommenceTime() {
            return commenceTime;
        }

        public void setCommenceTime(Integer commenceTime) {
            this.commenceTime = commenceTime;
        }

        public String getHomeTeam() {
            return homeTeam;
        }

        public void setHomeTeam(String homeTeam) {
            this.homeTeam = homeTeam;
        }

        public List<Site> getSites() {
            return sites;
        }

        public void setSites(List<Site> sites) {
            this.sites = sites;
        }

        public Integer getSitesCount() {
            return sitesCount;
        }

        public void setSitesCount(Integer sitesCount) {
            this.sitesCount = sitesCount;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }


        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    public class Example {

        private Boolean success;
        private List<Datum> data = null;

        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public List<Datum> getData() {
            return data;
        }

        public void setData(List<Datum> data) {
            this.data = data;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }
    }

    public class Odds {

        private List<Double> h2h = null;
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        public List<Double> getH2h() {
            return h2h;
        }

        public void setH2h(List<Double> h2h) {
            this.h2h = h2h;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    public class Site {

        private String siteKey;
        private String siteNice;
        private Integer lastUpdate;
        private Odds odds;
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        public String getSiteKey() {
            return siteKey;
        }

        public void setSiteKey(String siteKey) {
            this.siteKey = siteKey;
        }

        public String getSiteNice() {
            return siteNice;
        }

        public void setSiteNice(String siteNice) {
            this.siteNice = siteNice;
        }

        public Integer getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(Integer lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

        public Odds getOdds() {
            return odds;
        }

        public void setOdds(Odds odds) {
            this.odds = odds;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

}
