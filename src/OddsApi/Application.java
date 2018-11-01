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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static OddsApi.EmailSender.*;

public class Application {

    private final static Gson sGson = new Gson();
    final static Instant sInstant = Instant.now();

    public static void main(String args[]) throws InterruptedException, ExecutionException, IOException {

        System.out.println("Starting odds bet analyzer....");

        Map<Long, Match> mapIdToMatch = null;

        final List<Match> todayMatches = getAllMatches();
        mapIdToMatch = todayMatches.stream().collect(Collectors.toMap(Match::getId, Function.identity()));

        final List<HomeXAwayOdds> allHomeXAwayOddsList = new ArrayList<>(10000);
        getHomeXAwayOddPerMatch(todayMatches, allHomeXAwayOddsList);
        final Map<Long, List<HomeXAwayOdds>> newData =
                allHomeXAwayOddsList
                        .stream()
                        .collect(Collectors.groupingBy(HomeXAwayOdds::getMatchId, Collectors.toList()));

//        FetchingUtils.writeListToJson(newData, "HomeXAway_15_09_2018");
//        final Map<Long, List<HomeXAwayOdds>> newData =
//                FetchingUtils.readListFromJson("HomeXAway_15_09_2018");

        final List<SureHomeXAwayPrediction> sureBetsHomeXAwayResults = new ArrayList<>(10000);
        calculatingSureBetsHomeXAway(newData, mapIdToMatch, sureBetsHomeXAwayResults);
        final List<SureHomeXAwayPrediction> homeXAwaySureBets =
                sureBetsHomeXAwayResults
                        .stream()
                        .sorted(Comparator.comparing(SureHomeXAwayPrediction::getScore))
                        .collect(Collectors.toList());

        EmailSender.sendEmail(homeXAwaySureBets);

//        final List<OverUnderOdds> allUnderOverOddsList = new ArrayList<>(10000);
//        retrieveOverUnderOddPerMatch(todayMatches, allUnderOverOddsList);
//        final Map<Long, List<OverUnderOdds>> data =
//                allUnderOverOddsList
//                        .stream()
//                        .collect(Collectors.groupingBy(OverUnderOdds::getMatchId, Collectors.toList()));
//        calculatingOverUnderSureBets(data);
//
//        final List<SureOverUnderPrediction> sureBetsOverUnderResults = new ArrayList<>(10000);
//        final List<SureOverUnderPrediction> overAwaySureBets =
//                new HashSet<>(sureBetsOverUnderResults)
//                        .stream()
//                        .sorted(Comparator.comparing(SureOverUnderPrediction::getScore))
//                        .collect(Collectors.toList());

        System.out.println("Finished odds bet analyzer....");
    }

    public static void calculatingOverUnderSureBets(final Map<Long, List<OverUnderOdds>> allUnderOverList) {

        allUnderOverList.
                forEach((key, value) -> {
                    final String[][] arrStr = new String[value.size()][3];

                    IntStream.range(0, value.size()).forEach(idx -> {
                                arrStr[idx][0] = value.get(idx).getBookmaker().getName();
                                arrStr[idx][1] = String.valueOf(value.get(idx).getLive().getOver());
                                arrStr[idx][2] = String.valueOf(value.get(idx).getLive().getUnder());
                            }
                    );

                    try {

                        for (int i = 0; i < value.size(); i++) {
                            for (int j = 0; j < value.size(); j++) {
                                if (SureBetUtils.isMatchSureBet(Double.parseDouble(arrStr[i][1]), Double.parseDouble(arrStr[j][2]))) {

                                    SureOverUnderPrediction.create(
                                            value.get(0).getMatchId(),
                                            arrStr[i][0],
                                            arrStr[j][0],
                                            Double.parseDouble(arrStr[i][1]),
                                            Double.parseDouble(arrStr[j][1]),
                                            SureBetUtils.calculateSureBet(Double.parseDouble(arrStr[i][1]), Double.parseDouble(arrStr[j][1])),
                                            0);

                                }
                            }
                        }
                    } catch (final Throwable ex) {
                        System.out.println(ex);
                    }

                });
    }

    public static void calculatingSureBetsHomeXAway(
            final Map<Long, List<HomeXAwayOdds>> allUnderOverList,
            final Map<Long, Match> mapIdToMatch,
            final List<SureHomeXAwayPrediction> sureBetsHomeXAwayResults) {

        allUnderOverList.
                forEach((key, value) -> {
                    final String[][] arrStr = new String[value.size()][4];

                    IntStream.range(0, value.size()).forEach(idx -> {
                        arrStr[idx][0] = value.get(idx).getBookmaker().getName();
                        arrStr[idx][1] = String.valueOf(value.get(idx).getLiveOverUnderOdd().getHome());
                        arrStr[idx][2] = String.valueOf(value.get(idx).getLiveOverUnderOdd().getDraw());
                        arrStr[idx][3] = String.valueOf(value.get(idx).getLiveOverUnderOdd().getAway());
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
                                                        value.get(0).getMatchId(),
                                                        mapIdToMatch.get(value.get(0).getMatchId()).getHomeTeam().getName(),
                                                        mapIdToMatch.get(value.get(0).getMatchId()).getAwayTeam().getName(),
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

    public static void retrieveOverUnderOddPerMatch(final List<Match> matchesList,
                                                    final List<OverUnderOdds> underOverOdds) throws ExecutionException, InterruptedException {

        matchesList.parallelStream().forEach(x -> {
            try {
                getOverUnderOddPerMatch(x.getId(), underOverOdds);
            } catch (final Throwable ex) {
                System.out.println("Unable to Retrieve odd for match id: " + x.getId());
            }
        });
    }

    public static void getHomeXAwayOddPerMatch(final List<Match> matchesList,
                                               final List<HomeXAwayOdds> homeXAwayOddsList) throws ExecutionException, InterruptedException {

        matchesList
                .parallelStream()
                .forEach(x -> {
                    try {
                        getHomeXAwayOddsPerMatch(x.getId(), homeXAwayOddsList);
                    } catch (final Throwable ex) {
                        System.out.println("Unable to Retrieve odd for match id: " + x.getId());
                    }
                });

    }

    public static List<Match> asynchronousCall(final ExecutorService executor) throws ExecutionException, InterruptedException {
        final Callable<List<Match>> task = Application::getAllMatches;

        final Future<List<Match>> matchFuture = executor.submit(task);
        return matchFuture.get();
//        return matchFuture.get().stream().filter(x -> !x.getStatus().contains("FT")).collect(Collectors.toList());
//        return matchFuture.get().stream().filter(x-> x.getStatus().contains("NS")).collect(Collectors.toList());
    }

    ///////////////////////////////////////////////////////////////
    //////              Get All Match details
    ///////////////////////////////////////////////////////////////
    public static List<Match> getAllMatches() throws IOException {

        final Type bookmakersListToken = new TypeToken<HashMap<String, List<Match>>>() {
        }.getType();

        try {

            final HttpURLConnection conn = getHttpURLConnection("https://myanmarunicorn-bhawlone-v1.p.mashape.com/matches");

            final BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            StringBuilder stringBuilder = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                stringBuilder.append(output);
            }

            conn.disconnect();

            final List<Match> matchesList =
                    ((Map<String, List<Match>>) sGson.fromJson(stringBuilder.toString(), bookmakersListToken))
                            .values()
                            .stream()
                            .flatMap(List::stream)
                            .filter(x -> x.getStatus().contains("NS"))
                            .collect(Collectors.toList());

            return matchesList;

        } catch (final Throwable ex) {

            throw ex;

        }
    }

    ///////////////////////////////////////////////////////////////
    //////              Get Under/Over odds
    ///////////////////////////////////////////////////////////////
    public static void getOverUnderOddPerMatch(final long matchId,
                                               final List<OverUnderOdds> allUnderOverList) throws IOException {
        final Type underOverOddsListToken = new TypeToken<HashMap<String, List<OverUnderOdds>>>() {
        }.getType();

        try {

            final HttpURLConnection conn =
                    getHttpURLConnection(
                            String.format("https://myanmarunicorn-bhawlone-v1.p.mashape.com/matches/%d/odds?type=2", matchId));

            final BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            final StringBuilder stringBuilder = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                stringBuilder.append(output);
            }

            conn.disconnect();

            final List<OverUnderOdds> matchesList =
                    ((Map<String, List<OverUnderOdds>>) sGson.fromJson(stringBuilder.toString(), underOverOddsListToken))
                            .values()
                            .stream()
                            .flatMap(List::stream)
                            .map(x -> OverUnderOdds.create(matchId, x.id, x.bookmaker, x.live))
                            .collect(Collectors.toList());

            allUnderOverList.addAll(matchesList);

        } catch (final Throwable ex) {

            throw ex;
        }
    }

    public static void getHomeXAwayOddsPerMatch(final long matchId,
                                                final List<HomeXAwayOdds> allHomeXAwayList) throws IOException {
        final Type homeXAwayToken = new TypeToken<HashMap<String, List<HomeXAwayOdds>>>() {
        }.getType();

        try {

            final HttpURLConnection conn =
                    getHttpURLConnection(
                            String.format("https://myanmarunicorn-bhawlone-v1.p.mashape.com/matches/%d/odds?type=3", matchId));

            final BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            final StringBuilder stringBuilder = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                stringBuilder.append(output);
            }

            conn.disconnect();

            final List<HomeXAwayOdds> matchesList =
                    ((Map<String, List<HomeXAwayOdds>>) sGson.fromJson(stringBuilder.toString(), homeXAwayToken))
                            .values()
                            .stream()
                            .flatMap(List::stream)
                            .map(x -> HomeXAwayOdds.create(matchId, x.id, x.bookmaker, x.live))
                            .collect(Collectors.toList());

            allHomeXAwayList.addAll(matchesList);

        } catch (final Throwable ex) {

            throw ex;
        }
    }

    private static HttpURLConnection getHttpURLConnection(final String link) throws IOException {
        final URL url = new URL(link);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        //        conn.setRequestProperty("X-Mashape-Key", "xcMA6q0YzYmsh6lk28d7sdcfh2Wdp1ICCL1jsnXFtA2jCubElg");
        conn.setRequestProperty("X-Mashape-Key", "uWkiJPGHCqmshTVSJ8GndwdD4zhDp1QQ4PajsnV30lmntTcg7d");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }
        return conn;
    }

    public final static class PairOdd {

        private final int id;
        private final String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        private PairOdd(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        public static PairOdd create(final int id, final String name) {

            return new PairOdd(id, name);
        }
    }

    public final static class PairMatch {

        public static PairMatch create(String name, int score) {
            return new PairMatch(name, score);
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        private PairMatch(String name, int score) {
            this.name = name;
            this.score = score;
        }

        private final String name;
        private final int score;
    }

    public final static class Match {


        public static Match create(long id, int competition_id, final String time, final String matchTime, final PairMatch homeTeam, final PairMatch awayTeam, boolean isLiveStream, String status) {
            return new Match(id, competition_id, time, matchTime, homeTeam, awayTeam, isLiveStream, status);
        }

        public Match(long id, int competition_id, final String time, final String matchTime, final PairMatch homeTeam, final PairMatch awayTeam, boolean isLiveStream, String status) {
            this.id = id;
            this.competition_id = competition_id;
            this.time = time;
            this.matchTime = matchTime;
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;

            this.isLiveStream = isLiveStream;
            this.status = status;
        }

        public long getId() {
            return id;
        }

        public int getCompetition_id() {
            return competition_id;
        }

        public String getTime() {
            return time;
        }

        public LocalDateTime getMatchTimeInCyprusTime() {

            return LocalDateTime.ofInstant(Instant.parse(matchTime), OffsetDateTime.now().getOffset());

        }

        public LocalDateTime getMatchTimeInUTC() {
            return LocalDateTime.ofInstant(Instant.parse(matchTime), ZoneOffset.UTC);

        }

        public PairMatch getHomeTeam() {
            return homeTeam;
        }

        public PairMatch getAwayTeam() {
            return awayTeam;
        }

        public boolean isLiveStream() {
            return isLiveStream;
        }

        public String getStatus() {
            return status;
        }

        private final long id;
        private final int competition_id;
        private final String time;
        private final String matchTime;
        private final PairMatch homeTeam;
        private final PairMatch awayTeam;
        private final boolean isLiveStream;
        private final String status;

    }

    public static class OverUnderOdds {
        public static OverUnderOdds create(final long matchId, long id, Bookmaker name, LiveOverUnderOdd liveOverUnderOdd) {
            return new OverUnderOdds(matchId, id, name, liveOverUnderOdd);
        }

        public long getId() {
            return id;
        }

        public Bookmaker getBookmaker() {
            return bookmaker;
        }

        public LiveOverUnderOdd getLive() {
            return live;
        }

        public long getMatchId() {
            return matchId;
        }

        public OverUnderOdds(final long matchId,
                             final long id,
                             final Bookmaker bookmaker,
                             final LiveOverUnderOdd live
        ) {
            this.matchId = matchId;
            this.id = id;
            this.bookmaker = bookmaker;
            this.live = live;
        }

        private long matchId;
        private long id;
        private Bookmaker bookmaker;
        private LiveOverUnderOdd live;
    }

    public static class HomeXAwayOdds {
        public static HomeXAwayOdds create(final long matchId, final long id, final Bookmaker name, final LiveHomeXAwayOdd live) {
            return new HomeXAwayOdds(matchId, id, name, live);
        }

        public long getId() {
            return id;
        }

        public Bookmaker getBookmaker() {
            return bookmaker;
        }

        public LiveHomeXAwayOdd getLiveOverUnderOdd() {
            return live;
        }

        public long getMatchId() {
            return matchId;
        }

        public HomeXAwayOdds(final long matchId, long id, Bookmaker bookmaker, LiveHomeXAwayOdd liveOverUnderOdd) {
            this.matchId = matchId;
            this.id = id;
            this.bookmaker = bookmaker;
            this.live = liveOverUnderOdd;
        }

        public String toHtml() {
            return "  <tr>\n" +
                    "    <td> " + matchId + " </td>\n" +
                    "    <td> " + id + " </td>\n" +
                    "    <td> " + bookmaker.getName() + " </td>\n" +
                    "  </tr>\n";

        }

        private long matchId;
        private long id;
        private Bookmaker bookmaker;
        private LiveHomeXAwayOdd live;
    }

    public static class LiveOverUnderOdd {

        public static LiveOverUnderOdd create(double over, double under) {
            return new LiveOverUnderOdd(over, under);
        }

        public double getOver() {
            return over;
        }

        public double getUnder() {
            return under;
        }

        private LiveOverUnderOdd(double over, double under) {
            this.over = over;
            this.under = under;
        }

        final private double over;
        final private double under;
    }

    public static class LiveHomeXAwayOdd {

        public static LiveHomeXAwayOdd create(double home, double draw, double away) {
            return new LiveHomeXAwayOdd(home, draw, away);
        }

        public double getHome() {
            return home;
        }

        public double getDraw() {
            return draw;
        }

        public double getAway() {
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

    public final static class SureOverUnderPrediction {

        public static SureOverUnderPrediction create(
                final long matchId,
                final String firstBookmaker,
                final String secondBookmaker,
                final double over,
                final double under,
                final double score,
                final double profitPercentage) {

            return new SureOverUnderPrediction(matchId, firstBookmaker, secondBookmaker, over, under, score, profitPercentage);
        }

        public long getMatchId() {
            return matchId;
        }

        public String getFirstBookmaker() {
            return firstBookmaker;
        }

        public String getSecondBookmaker() {
            return secondBookmaker;
        }

        public double getOverOdd() {
            return overOdd;
        }

        public double getUnderOdd() {
            return underOdd;
        }

        public double getScore() {
            return score;
        }

        public double getProfitPercentage() {
            return profitPercentage;
        }

        private SureOverUnderPrediction(
                final long matchId,
                final String firstBookmaker,
                final String secondBookmaker,
                final double overOdd,
                final double underOdd,
                final double score,
                final double profitPercentage) {

            this.matchId = matchId;
            this.firstBookmaker = firstBookmaker;
            this.secondBookmaker = secondBookmaker;
            this.overOdd = overOdd;
            this.underOdd = underOdd;
            this.score = score;
            this.profitPercentage = profitPercentage;
        }


        private final long matchId;
        private final String firstBookmaker;
        private final String secondBookmaker;
        private final double overOdd;
        private final double underOdd;
        private final double score;
        private final double profitPercentage;
    }

    public final static class SureHomeXAwayPrediction {

        public static SureHomeXAwayPrediction create(
                final long matchId,
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

            return new SureHomeXAwayPrediction(matchId, homeTeam, awayTeam, firstBookmaker, secondBookmaker, thirdBookmaker, homeOdd, drawOdd, awayOdd, score, profitPercentage);
        }


        public long getMatchId() {
            return matchId;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SureHomeXAwayPrediction that = (SureHomeXAwayPrediction) o;
            return matchId == that.matchId;
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
        public int hashCode() {

            return Objects.hash(matchId);
        }

        public SureHomeXAwayPrediction(
                long matchId,
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

            this.matchId = matchId;
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
                    "    <td> " + number + " </td>\n" +
                    "    <td> " + matchId + " </td>\n" +
                    "    <td> " + homeTeam + " </td>\n" +
                    "    <td> " + awayTeam + " </td>\n" +
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


        private final long matchId;
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
}

