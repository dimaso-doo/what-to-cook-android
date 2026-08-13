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
    public final String sourceName;
    public final String sourceUrl;
    public final String licenseName;
    public final String attribution;
    public final boolean modifiedFromSource;
    public final boolean aiGenerated;
    public final List<String> coreIngredientIds;
    public final List<String> missingIngredientNames;
    public final List<String> ingredientLines;
    public final List<String> steps;

    public Recipe(String id, String title, String emoji, String description, int minutes,
                  String difficulty, int servings, List<String> coreIngredientIds,
                  List<String> ingredientLines, List<String> steps) {
        this(id, title, emoji, description, minutes, difficulty, servings,
                null, null, null, null, false, false, coreIngredientIds,
                java.util.Collections.emptyList(), ingredientLines, steps);
    }

    public Recipe(String id, String title, String emoji, String description, int minutes,
                  String difficulty, int servings, String sourceName, String sourceUrl,
                  String licenseName, String attribution, boolean modifiedFromSource,
                  List<String> coreIngredientIds, List<String> ingredientLines,
                  List<String> steps) {
        this.id = id;
        this.title = title;
        this.emoji = emoji;
        this.description = description;
        this.minutes = minutes;
        this.difficulty = difficulty;
        this.servings = servings;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.licenseName = licenseName;
        this.attribution = attribution;
        this.modifiedFromSource = modifiedFromSource;
        this.aiGenerated = false;
        this.coreIngredientIds = coreIngredientIds;
        this.missingIngredientNames = java.util.Collections.emptyList();
        this.ingredientLines = ingredientLines;
        this.steps = steps;
    }

    public Recipe(String id, String title, String emoji, String description, int minutes,
                  String difficulty, int servings, String sourceName, String sourceUrl,
                  String licenseName, String attribution, boolean modifiedFromSource,
                  boolean aiGenerated, List<String> coreIngredientIds,
                  List<String> missingIngredientNames, List<String> ingredientLines,
                  List<String> steps) {
        this.id = id;
        this.title = title;
        this.emoji = emoji;
        this.description = description;
        this.minutes = minutes;
        this.difficulty = difficulty;
        this.servings = servings;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.licenseName = licenseName;
        this.attribution = attribution;
        this.modifiedFromSource = modifiedFromSource;
        this.aiGenerated = aiGenerated;
        this.coreIngredientIds = coreIngredientIds;
        this.missingIngredientNames = missingIngredientNames;
        this.ingredientLines = ingredientLines;
        this.steps = steps;
    }
}
