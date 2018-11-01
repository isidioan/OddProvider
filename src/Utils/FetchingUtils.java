package Utils;

import OddsApi.Application;
import OddsApi.Bookmakers;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FetchingUtils {

    final static Gson sGson = new Gson();

    public final static class Pair<K, V> {

        private final K first;
        private final V second;

        public K getFirst() {
            return first;
        }

        public V getSecond() {
            return second;
        }

        private Pair(final K first, final V second) {
            this.first = first;
            this.second = second;
        }

        public static <K, V> Pair<K, V> create(final K first, final V second) {

            return new Pair<K, V>(first, second);
        }
    }

    public final static class Triplet<A, B, C> {

        private final A first;
        private final B second;
        private final C third;

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }

        public C getThird() {
            return third;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
            return Objects.equals(first, triplet.first) &&
                    Objects.equals(second, triplet.second) &&
                    Objects.equals(third, triplet.third);
        }

        @Override
        public int hashCode() {

            return Objects.hash(first, second, third);
        }

        private Triplet(final A first, final B second, final C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public static <A, B, C> Triplet<A, B, C> create(final A first, final B second, final C third) {

            return new Triplet<A, B, C>(first, second, third);
        }
    }

    private static Type bookmakersListToken = new TypeToken<List<Bookmakers>>() {}.getType();

    public static <K, V> void writeListToJson(final Map<K, V> data, final String filename) throws IOException {

        final String jsonData = sGson.toJson(data);

        final Path file = Paths.get(
                String.format("C:\\Users\\Elias\\Desktop\\projects\\OddCrawler\\Data\\%s_%s", filename, LocalDate.now().toString()));

        Files.write(
                file,
                jsonData.getBytes(),
                StandardOpenOption.CREATE);
    }

    public static <T> T readListFromJson(final String filename) throws IOException {

        final Path file = Paths.get(
                String.format("C:\\Users\\Elias\\Desktop\\projects\\OddCrawler\\Data\\HomeXAway_15_09_2018_2018-09-15", filename));

        final String json = new String(Files.readAllBytes(file));

        return sGson.fromJson(json, sListOfTestObject);
    }

    private static Type sListOfTestObject = new TypeToken<Map<Long, List<Application.HomeXAwayOdds>>>() {}.getType();


}
