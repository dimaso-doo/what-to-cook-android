package rs.brainno.stakuvamo;

import java.util.List;

public final class AiSuggestionResult {
    public final List<Recipe> recipes;
    public final boolean cached;
    public final Integer remainingToday;

    public AiSuggestionResult(List<Recipe> recipes, boolean cached, Integer remainingToday) {
        this.recipes = recipes;
        this.cached = cached;
        this.remainingToday = remainingToday;
    }
}
