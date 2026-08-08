package rs.brainno.stakuvamo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RecipeRepository {
    public interface IngredientsCallback {
        void onComplete(boolean loadedFromSupabase);
    }

    public interface MatchesCallback {
        void onComplete(List<RecipeMatch> matches, boolean loadedFromSupabase);
    }

    private static final List<Ingredient> OFFLINE_INGREDIENTS = Arrays.asList(
            new Ingredient("eggs", "Eggs", "🥚", "Protein"),
            new Ingredient("chicken", "Chicken", "🍗", "Protein"),
            new Ingredient("ground_meat", "Ground beef", "🥩", "Protein"),
            new Ingredient("tuna", "Tuna", "🐟", "Protein"),
            new Ingredient("beans", "Beans", "🫘", "Protein"),
            new Ingredient("rice", "Rice", "🍚", "Grains"),
            new Ingredient("pasta", "Pasta", "🍝", "Grains"),
            new Ingredient("potato", "Potatoes", "🥔", "Vegetables"),
            new Ingredient("bread", "Bread", "🍞", "Grains"),
            new Ingredient("tortilla", "Tortillas", "🫓", "Grains"),
            new Ingredient("onion", "Onion", "🧅", "Vegetables"),
            new Ingredient("garlic", "Garlic", "🧄", "Vegetables"),
            new Ingredient("tomato", "Tomatoes", "🍅", "Vegetables"),
            new Ingredient("pepper", "Bell pepper", "🫑", "Vegetables"),
            new Ingredient("zucchini", "Zucchini", "🥒", "Vegetables"),
            new Ingredient("carrot", "Carrot", "🥕", "Vegetables"),
            new Ingredient("mushrooms", "Mushrooms", "🍄", "Vegetables"),
            new Ingredient("spinach", "Spinach", "🥬", "Vegetables"),
            new Ingredient("corn", "Corn", "🌽", "Vegetables"),
            new Ingredient("peas", "Peas", "🟢", "Vegetables"),
            new Ingredient("cucumber", "Cucumber", "🥒", "Vegetables"),
            new Ingredient("cheese", "Cheese", "🧀", "Dairy"),
            new Ingredient("yogurt", "Yogurt", "🥛", "Dairy"),
            new Ingredient("lemon", "Lemon", "🍋", "Other")
    );

    private static final List<Recipe> OFFLINE_RECIPES = Arrays.asList(
            recipe("chicken_risotto", "Chicken Risotto", "🍲",
                    "A creamy one-pan meal that's perfect for a busy weeknight.", 35, "Easy", 2,
                    list("chicken", "rice", "onion", "carrot", "peas"),
                    list("300 g chicken breast", "180 g rice", "1 onion", "1 carrot",
                            "100 g peas", "600 ml water or stock", "Salt, pepper, and 2 tbsp oil"),
                    list("Cut the chicken and vegetables into bite-sized pieces.",
                            "Sauté the onion in oil, add the chicken, and cook until golden.",
                            "Add the carrot, rice, and warm stock. Simmer for about 18 minutes.",
                            "Stir in the peas, season, cover, and rest for another 5 minutes.")),
            recipe("veggie_omelette", "Vegetable Omelet", "🍳",
                    "A quick, fluffy omelet for breakfast or a light dinner.", 15, "Very easy", 1,
                    list("eggs", "pepper", "tomato", "cheese"),
                    list("3 eggs", "1/2 bell pepper", "1 small tomato", "40 g cheese",
                            "Salt, pepper, and a little oil"),
                    list("Whisk the eggs with a pinch of salt and pepper.",
                            "Briefly sauté the chopped bell pepper and tomato.",
                            "Pour in the eggs, add the cheese, and cook over low heat for 4–5 minutes.")),
            recipe("tomato_pasta", "Pasta with Tomato Sauce", "🍝",
                    "A simple classic with plenty of garlic flavor.", 25, "Easy", 2,
                    list("pasta", "tomato", "garlic", "onion"),
                    list("200 g pasta", "3 tomatoes or 300 ml crushed tomatoes",
                            "2 garlic cloves", "1/2 onion", "Salt, pepper, and oil"),
                    list("Cook the pasta according to the package directions and reserve some pasta water.",
                            "Sauté the onion and garlic, then add the tomatoes and seasoning.",
                            "Simmer the sauce for 10 minutes, stir in the pasta, and loosen with pasta water if needed.")),
            recipe("fried_rice", "Egg Fried Rice", "🥘",
                    "A delicious way to use up yesterday's cooked rice.", 20, "Easy", 2,
                    list("rice", "eggs", "carrot", "peas", "onion"),
                    list("300 g cold cooked rice", "2 eggs", "1 carrot",
                            "80 g peas", "1/2 onion", "Salt, pepper, and oil"),
                    list("Sauté the onion, carrot, and peas in a hot pan.",
                            "Move the vegetables aside, add the beaten eggs, and scramble briefly.",
                            "Add the cold rice, mix everything together, and fry for another 4–5 minutes.")),
            recipe("potato_frittata", "Potato Frittata", "🥔",
                    "A satisfying meal made with a few everyday ingredients.", 35, "Easy", 2,
                    list("potato", "eggs", "onion", "cheese"),
                    list("400 g potatoes", "4 eggs", "1 onion", "60 g cheese",
                            "Salt, pepper, and oil"),
                    list("Slice the potatoes thinly and boil for 8 minutes.",
                            "Sauté the onion, add the drained potatoes, and cook until lightly golden.",
                            "Pour in the beaten eggs, sprinkle with cheese, cover, and cook for 12 minutes.")),
            recipe("chicken_pasta", "Creamy Chicken Pasta", "🍗",
                    "Tender chicken and pasta in a light yogurt sauce.", 30, "Easy", 2,
                    list("chicken", "pasta", "yogurt", "garlic"),
                    list("300 g chicken breast", "200 g pasta", "150 ml thick yogurt",
                            "1 garlic clove", "Salt, pepper, and oil"),
                    list("Cook the pasta and reserve half a cup of pasta water.",
                            "Season the sliced chicken and cook it in a pan until golden.",
                            "Lower the heat, add the garlic, yogurt, and a splash of pasta water.",
                            "Stir in the pasta and warm gently without bringing the sauce to a hard boil.")),
            recipe("stuffed_zucchini", "Stuffed Zucchini", "🥒",
                    "Tender zucchini filled with ground beef and rice.", 55, "Medium", 3,
                    list("zucchini", "ground_meat", "rice", "onion", "tomato"),
                    list("3 medium zucchini", "300 g ground beef", "80 g rice",
                            "1 onion", "200 ml tomato sauce", "Salt, pepper, and oil"),
                    list("Halve the zucchini and scoop out the centers with a spoon.",
                            "Sauté the onion and beef, then add the rinsed rice and seasoning.",
                            "Fill the zucchini, pour over the tomato sauce, and cover with foil.",
                            "Bake for 35 minutes at 200 °C, uncover, and bake for 10 more minutes.")),
            recipe("tuna_salad", "Tuna Salad", "🥗",
                    "A fresh, protein-packed salad ready in ten minutes.", 10, "Very easy", 2,
                    list("tuna", "tomato", "cucumber", "corn", "lemon"),
                    list("1 can tuna", "2 tomatoes", "1 cucumber",
                            "100 g corn", "1/2 lemon", "Salt, pepper, and olive oil"),
                    list("Drain the tuna and corn.",
                            "Chop the tomatoes and cucumber, then combine everything in a bowl.",
                            "Season with lemon juice, olive oil, salt, and pepper.")),
            recipe("shakshuka", "Shakshuka", "🍅",
                    "Eggs gently cooked in a rich tomato and bell pepper sauce.", 30, "Easy", 2,
                    list("eggs", "tomato", "pepper", "onion", "garlic"),
                    list("4 eggs", "400 g tomatoes", "1 bell pepper", "1 onion",
                            "1 garlic clove", "Salt, pepper, paprika, and oil"),
                    list("Sauté the onion and bell pepper until soft.",
                            "Add the garlic, tomatoes, and seasoning, then simmer for 12 minutes.",
                            "Make four wells, add the eggs, cover, and cook for another 6–8 minutes.")),
            recipe("potato_stew", "Rustic Potato Stew", "🥣",
                    "A warm, comforting one-pot meal with no fuss.", 45, "Easy", 4,
                    list("potato", "onion", "carrot", "pepper"),
                    list("800 g potatoes", "1 large onion", "2 carrots", "1 bell pepper",
                            "1 tsp paprika", "Salt, pepper, oil, and water"),
                    list("Soften the onion, carrots, and bell pepper in oil.",
                            "Stir in the paprika, then add the diced potatoes.",
                            "Cover with water, season, and simmer for 30 minutes.")),
            recipe("chicken_wrap", "Chicken Wraps", "🌯",
                    "Crisp vegetables and seasoned chicken wrapped in warm tortillas.", 25, "Easy", 2,
                    list("tortilla", "chicken", "pepper", "yogurt", "tomato"),
                    list("4 tortillas", "300 g chicken breast", "1 bell pepper", "1 tomato",
                            "120 ml yogurt", "Salt, pepper, paprika, and oil"),
                    list("Slice the chicken into strips, season, and cook until golden.",
                            "Chop the vegetables and lightly season the yogurt with salt.",
                            "Warm the tortillas, add the fillings, and roll them up tightly.")),
            recipe("mushroom_toast", "Mushroom Toast", "🍄",
                    "A warm, savory toast for a quick breakfast or dinner.", 20, "Very easy", 2,
                    list("bread", "mushrooms", "garlic", "cheese"),
                    list("4 slices bread", "250 g mushrooms", "1 garlic clove",
                            "80 g cheese", "Salt, pepper, and a little oil"),
                    list("Slice the mushrooms and cook in a hot pan until their moisture evaporates.",
                            "Add the garlic and seasoning, then remove from the heat.",
                            "Top the bread with mushrooms and cheese, then bake for 6–8 minutes at 200 °C.")),
            recipe("spinach_eggs", "Eggs with Spinach", "🥬",
                    "A simple, nutritious one-pan meal.", 15, "Very easy", 1,
                    list("eggs", "spinach", "cheese", "garlic"),
                    list("3 eggs", "150 g spinach", "40 g cheese", "1 garlic clove",
                            "Salt, pepper, and a little oil"),
                    list("Warm the garlic briefly in oil and add the spinach.",
                            "Once the spinach wilts, add the beaten eggs.",
                            "Stir over low heat, sprinkle with cheese, and serve immediately.")),
            recipe("bean_salad", "Quick Bean Salad", "🫘",
                    "A filling pantry salad that requires no cooking.", 12, "Very easy", 2,
                    list("beans", "onion", "pepper", "tomato", "lemon"),
                    list("300 g cooked or canned beans", "1/2 onion",
                            "1 bell pepper", "1 tomato", "1/2 lemon", "Salt, pepper, and oil"),
                    list("Rinse and drain the beans well.",
                            "Finely chop the onion, bell pepper, and tomato.",
                            "Combine everything and season with lemon juice, oil, salt, and pepper."))
    );

    private static volatile List<Ingredient> currentIngredients = OFFLINE_INGREDIENTS;
    private static volatile Map<String, Ingredient> ingredientMap = ingredientMap(OFFLINE_INGREDIENTS);
    private static volatile Map<String, Recipe> recipeMap = recipeMap(OFFLINE_RECIPES);

    private RecipeRepository() {}

    public static List<Ingredient> ingredients() {
        return currentIngredients;
    }

    public static Ingredient ingredient(String id) {
        return ingredientMap.get(id);
    }

    public static Recipe recipe(String id) {
        return recipeMap.get(id);
    }

    public static List<RecipeMatch> findMatches(Set<String> selected) {
        List<RecipeMatch> matches = new ArrayList<>();
        for (Recipe recipe : OFFLINE_RECIPES) {
            if (selected.containsAll(recipe.coreIngredientIds)) {
                matches.add(new RecipeMatch(
                        recipe, recipe.coreIngredientIds.size(), Collections.emptyList()));
            }
        }
        Collections.sort(matches, (first, second) -> {
            int byIngredients = Integer.compare(second.matchedCount, first.matchedCount);
            if (byIngredients != 0) return byIngredients;
            return Integer.compare(first.recipe.minutes, second.recipe.minutes);
        });
        return matches;
    }

    public static void refreshIngredients(IngredientsCallback callback) {
        SupabaseRecipeService.loadIngredients((ingredients, error) -> {
            boolean loaded = error == null && ingredients != null && !ingredients.isEmpty();
            if (loaded) {
                currentIngredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
                ingredientMap = ingredientMap(currentIngredients);
            }
            callback.onComplete(loaded);
        });
    }

    public static void findMatchesAsync(Set<String> selected, MatchesCallback callback) {
        if (selected.isEmpty()) {
            callback.onComplete(Collections.emptyList(), false);
            return;
        }
        Set<String> selectedSnapshot = new java.util.LinkedHashSet<>(selected);
        SupabaseRecipeService.loadMatches(selectedSnapshot, (recipes, error) -> {
            if (error == null && recipes != null) {
                Map<String, Recipe> updated = new LinkedHashMap<>(recipeMap);
                List<RecipeMatch> matches = new ArrayList<>();
                for (Recipe recipe : recipes) {
                    updated.put(recipe.id, recipe);
                    matches.add(new RecipeMatch(
                            recipe, recipe.coreIngredientIds.size(), Collections.emptyList()));
                }
                recipeMap = Collections.unmodifiableMap(updated);
                callback.onComplete(Collections.unmodifiableList(matches), true);
            } else {
                callback.onComplete(findMatches(selectedSnapshot), false);
            }
        });
    }

    private static Map<String, Ingredient> ingredientMap(List<Ingredient> ingredients) {
        Map<String, Ingredient> result = new LinkedHashMap<>();
        for (Ingredient ingredient : ingredients) result.put(ingredient.id, ingredient);
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Recipe> recipeMap(List<Recipe> recipes) {
        Map<String, Recipe> result = new LinkedHashMap<>();
        for (Recipe recipe : recipes) result.put(recipe.id, recipe);
        return Collections.unmodifiableMap(result);
    }

    private static List<String> list(String... values) {
        return Arrays.asList(values);
    }

    private static Recipe recipe(String id, String title, String emoji, String description,
                                 int minutes, String difficulty, int servings,
                                 List<String> coreIngredients, List<String> ingredients,
                                 List<String> steps) {
        return new Recipe(id, title, emoji, description, minutes, difficulty, servings,
                coreIngredients, ingredients, steps);
    }
}
