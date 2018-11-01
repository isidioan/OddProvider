package OddsApi;

public class Bookmakers {

    public static Bookmakers create(final int id, final String name) {
        return new Bookmakers(id, name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private Bookmakers(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    final private int id;
    final private String name;
}
