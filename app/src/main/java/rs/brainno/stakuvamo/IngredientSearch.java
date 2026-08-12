package rs.brainno.stakuvamo;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

final class IngredientSearch {
    static final int MIN_QUERY_LENGTH = 2;
    static final int MAX_RESULTS = 8;

    private IngredientSearch() {}

    static List<Ingredient> suggestions(List<Ingredient> ingredients, String query,
                                        Collection<String> selectedIds) {
        List<Ingredient> matches = new ArrayList<>();
        String normalizedQuery = normalize(query);
        if (normalizedQuery.length() < MIN_QUERY_LENGTH) return matches;

        for (Ingredient ingredient : ingredients) {
            if (selectedIds.contains(ingredient.id)) continue;
            if (!normalize(ingredient.name).contains(normalizedQuery)) continue;
            matches.add(ingredient);
            if (matches.size() == MAX_RESULTS) break;
        }
        return matches;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}
