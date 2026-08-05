package rs.brainno.stakuvamo;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class RecipeRepositoryTest {
    @Test
    public void emptySelectionReturnsNoRecipes() {
        assertTrue(RecipeRepository.findMatches(Collections.emptySet()).isEmpty());
    }

    @Test
    public void exactRecipeIsRankedFirstAndMarkedReady() {
        HashSet<String> selected = new HashSet<>(Arrays.asList(
                "pasta", "tomato", "garlic", "onion"));

        List<RecipeMatch> matches = RecipeRepository.findMatches(selected);

        assertEquals("tomato_pasta", matches.get(0).recipe.id);
        assertTrue(matches.get(0).isReady());
    }

    @Test
    public void closestPartialRecipeIsRankedFirst() {
        HashSet<String> selected = new HashSet<>(Arrays.asList(
                "chicken", "rice", "onion"));

        List<RecipeMatch> matches = RecipeRepository.findMatches(selected);

        assertEquals("chicken_risotto", matches.get(0).recipe.id);
        assertEquals(2, matches.get(0).missingIngredientIds.size());
    }
}
