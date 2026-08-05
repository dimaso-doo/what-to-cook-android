package rs.brainno.stakuvamo;

import java.util.List;

public final class RecipeMatch {
    public final Recipe recipe;
    public final int matchedCount;
    public final List<String> missingIngredientIds;

    public RecipeMatch(Recipe recipe, int matchedCount, List<String> missingIngredientIds) {
        this.recipe = recipe;
        this.matchedCount = matchedCount;
        this.missingIngredientIds = missingIngredientIds;
    }

    public boolean isReady() {
        return missingIngredientIds.isEmpty();
    }
}
