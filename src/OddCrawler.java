//
//import Objects.MarketOdd;
//import Objects.Match;
//
//import java.io.IOException;
//import java.net.URL;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//import java.util.stream.Stream;
//
//import static Utils.FetchingUtils.Pair;
//
//public class OddCrawler {
//
//    public static void crawlForMatchesPerLink(final String url) throws IOException {
//
//        final String siteContext = new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();
//
//        extractAllMatchesPerLeaques(siteContext, rawMatchesMap);
//    }
//
//    public static void crawlToExtractActualOddsPageUrl(final String url, final Match match) {
//        try {
//            final String siteContext = new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();
//
//            final Matcher probablyInterestingContextMatcher = oddsReg.matcher(siteContext);
//
//            while (probablyInterestingContextMatcher.find()) {
//                int start = probablyInterestingContextMatcher.start(0);
//                int end = probablyInterestingContextMatcher.end(0);
//
//                final String matchedContext = siteContext.substring(start, end);
//
//                final String array[] = {"Home", "Draw", "Away"};
//                int idx = 0;
//
//                String oddType = null;
//                final Matcher actualOddsPerMarketMatcher = actualOddPerBetMarketReg.matcher(matchedContext);
//                while (actualOddsPerMarketMatcher.find()) {
//
//                    final int start1 = actualOddsPerMarketMatcher.start(0);
//                    final int end1 = actualOddsPerMarketMatcher.end(0);
//
//                    final String actualOddsContext = matchedContext.substring(start1, end1);
//                    if (actualOddsContext.contains("Competitor")) {
//                        oddType = array[idx++];
//                    }
//
//                    extractMarketOdds(actualOddsContext, oddType, match);
//
//                }
//
//            }
//        } catch (final Throwable ex) {
//            System.err.println(ex);
//        }
//    }
//
//    private static void extractMarketOdds(final String actualOddsContext, final String oddType, final Match match) {
//        final Matcher exactOddMatcher = exactOddReg.matcher(actualOddsContext);
//        final Matcher exactMarketMatcher = exactMarketReg.matcher(actualOddsContext);
//        while (exactOddMatcher.find() && exactMarketMatcher.find()) {
//            final String exactMarket = actualOddsContext.substring(exactMarketMatcher.start(0), exactMarketMatcher.end(0)).replace("<td title=", "").trim();
//
//            final String exactOdd = actualOddsContext.substring(exactOddMatcher.start(0), exactOddMatcher.end(0)).replace("</td>", "").trim();
//
//            if (exactOdd.trim().length() > 0) {
//
//                realMarketOddsSet.add(MarketOdd.create(match.getHomeTeam(), match.getAwayTeam(), match.getDivision(), exactMarket, oddType, Double.valueOf(exactOdd)));
//            }
//        }
//    }
//
//    private static void extractAllMatchesPerLeaques(final String context, final Map<String, Match> matchMap) {
//
//        final Matcher teamMatcher = teamsReg.matcher(context);
//        while (teamMatcher.find()) {
//            final String teams[] = context.substring(teamMatcher.start(0), teamMatcher.end(0)).split("-v-");
//            final String firstTeam = teams[0].replace("-", " ").trim();
//            final String secondTeam = teams[1].replace("-", " ").trim();
//
//            final String rawMatch = context.substring(teamMatcher.start(0), teamMatcher.end(0));
//
//            matchMap.put(rawMatch, Match.create(firstTeam, secondTeam, "TestDivision"));
//        }
//    }
//
//
//    public static void main(String[] args) throws IOException {
//
//        IntStream.range(0, matcherPerCountryWithOddsLinks.size() - 2).forEach(idx -> {
//            try {
//                crawlForMatchesPerLink(matcherPerCountryWithOddsLinks.get(idx).getFirst());
//                rawMatchesMap.forEach((k, v) -> crawlToExtractActualOddsPageUrl(
//                        String.format(matcherPerCountryWithOddsLinks.get(idx).getSecond(), k), v));
//
//                rawMatchesMap.clear();
//
//            } catch (final Throwable ex) {
//                System.out.println(ex);
//            }
//        });
//
//    }
//
//    final static Map<String, Match> rawMatchesMap = new ConcurrentHashMap<>(100);
//    final static Set<MarketOdd> realMarketOddsSet = new HashSet(100);
//
//    final static List<Pair<String, String>> matcherPerCountryWithOddsLinks =
//            Stream.of(
//                    Pair.create(
//                            "http://odds-comparison.bestbetting.com/football/england/premier-league/match-re",
//                            "hhttp://odds-comparison.bestbetting.com/ajax/GetMarket//football/england/premier-league/regular-season/round-5/%s/match-result/all-odds/defaultSort"),
//                    Pair.create(
//                            "http://odds-comparison.bestbetting.com/football/england/football-league-championship/match-result",
//                            "http://odds-comparison.bestbetting.com/ajax/GetMarket//football/england/football-league-championship/regular-season/round-7/%s/match-result/all-odds/defaultSort"),
//                    Pair.create(
//                            "http://odds-comparison.bestbetting.com/football/scotland/irn-bru-cup/match-result",
//                            "http://odds-comparison.bestbetting.com/ajax/GetMarket//football/scotland/irn-bru-cup/round-2/%s/match-result/all-odds/defaultSort")
//
//            ).collect(Collectors.toList());
//
//
//    final static Pattern oddsReg = Pattern.compile("<a href=\"\\/football[\\w\\s\\\"=/<>\\-\\.\\/\\n?&;(,'):]+<\\/tr>");
//    final static Pattern teamsReg = Pattern.compile("[\\w-']+-v-[\\w'-]+");
//    final static Pattern actualOddPerBetMarketReg = Pattern.compile("<[\\w\\s=\"(,);>.]+</td>");
//    final static Pattern exactOddReg = Pattern.compile("[\\d.\\s]+<\\/td>");
//    final static Pattern exactMarketReg = Pattern.compile("<td([\\s\\w=.]+|\\\"){3}\"");
//
//}
