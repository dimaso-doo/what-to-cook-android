package rs.brainno.stakuvamo;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class IngredientSearchTest {
    private final List<Ingredient> ingredients = Arrays.asList(
            new Ingredient("apple", "Apple", "", "Fruit"),
            new Ingredient("apricot", "Apricot", "", "Fruit"),
            new Ingredient("pineapple", "Pineapple", "", "Fruit"),
            new Ingredient("potato", "Potato", "", "Vegetable"));

    @Test
    public void returnsNothingBeforeTwoLetters() {
        assertTrue(IngredientSearch.suggestions(
                ingredients, "a", Collections.emptySet()).isEmpty());
    }

    @Test
    public void matchesAfterTwoLetters() {
        List<Ingredient> matches = IngredientSearch.suggestions(
                ingredients, "ap", Collections.emptySet());

        assertEquals(Arrays.asList("apple", "apricot", "pineapple"), Arrays.asList(
                matches.get(0).id, matches.get(1).id, matches.get(2).id));
    }

    @Test
    public void excludesAlreadySelectedIngredients() {
        List<Ingredient> matches = IngredientSearch.suggestions(
                ingredients, "ap", new HashSet<>(Collections.singletonList("apple")));

        assertEquals(2, matches.size());
        assertEquals("apricot", matches.get(0).id);
    }
}
