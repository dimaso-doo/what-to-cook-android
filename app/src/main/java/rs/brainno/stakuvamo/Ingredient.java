package rs.brainno.stakuvamo;

public final class Ingredient {
    public final String id;
    public final String name;
    public final String emoji;
    public final String category;

    public Ingredient(String id, String name, String emoji, String category) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.category = category;
    }
}
