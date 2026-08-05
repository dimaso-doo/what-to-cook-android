package rs.brainno.stakuvamo;

import java.util.List;

public final class Recipe {
    public final String id;
    public final String title;
    public final String emoji;
    public final String description;
    public final int minutes;
    public final String difficulty;
    public final int servings;
    public final List<String> coreIngredientIds;
    public final List<String> ingredientLines;
    public final List<String> steps;

    public Recipe(String id, String title, String emoji, String description, int minutes,
                  String difficulty, int servings, List<String> coreIngredientIds,
                  List<String> ingredientLines, List<String> steps) {
        this.id = id;
        this.title = title;
        this.emoji = emoji;
        this.description = description;
        this.minutes = minutes;
        this.difficulty = difficulty;
        this.servings = servings;
        this.coreIngredientIds = coreIngredientIds;
        this.ingredientLines = ingredientLines;
        this.steps = steps;
    }
}
