package rs.brainno.stakuvamo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RecipeRepository {
    private static final List<Ingredient> INGREDIENTS = Arrays.asList(
            new Ingredient("eggs", "Jaja", "🥚", "Proteini"),
            new Ingredient("chicken", "Piletina", "🍗", "Proteini"),
            new Ingredient("ground_meat", "Mleveno meso", "🥩", "Proteini"),
            new Ingredient("tuna", "Tunjevina", "🐟", "Proteini"),
            new Ingredient("beans", "Pasulj", "🫘", "Proteini"),
            new Ingredient("rice", "Pirinač", "🍚", "Žitarice"),
            new Ingredient("pasta", "Testenina", "🍝", "Žitarice"),
            new Ingredient("potato", "Krompir", "🥔", "Povrće"),
            new Ingredient("bread", "Hleb", "🍞", "Žitarice"),
            new Ingredient("tortilla", "Tortilje", "🫓", "Žitarice"),
            new Ingredient("onion", "Crni luk", "🧅", "Povrće"),
            new Ingredient("garlic", "Beli luk", "🧄", "Povrće"),
            new Ingredient("tomato", "Paradajz", "🍅", "Povrće"),
            new Ingredient("pepper", "Paprika", "🫑", "Povrće"),
            new Ingredient("zucchini", "Tikvice", "🥒", "Povrće"),
            new Ingredient("carrot", "Šargarepa", "🥕", "Povrće"),
            new Ingredient("mushrooms", "Pečurke", "🍄", "Povrće"),
            new Ingredient("spinach", "Spanać", "🥬", "Povrće"),
            new Ingredient("corn", "Kukuruz", "🌽", "Povrće"),
            new Ingredient("peas", "Grašak", "🟢", "Povrće"),
            new Ingredient("cucumber", "Krastavac", "🥒", "Povrće"),
            new Ingredient("cheese", "Sir", "🧀", "Mlečno"),
            new Ingredient("yogurt", "Jogurt", "🥛", "Mlečno"),
            new Ingredient("lemon", "Limun", "🍋", "Ostalo")
    );

    private static final List<Recipe> RECIPES = Arrays.asList(
            recipe("chicken_risotto", "Rižoto sa piletinom", "🍲",
                    "Kremast ručak iz jednog tiganja, idealan za radni dan.", 35, "Lako", 2,
                    list("chicken", "rice", "onion", "carrot", "peas"),
                    list("300 g pilećeg filea", "180 g pirinča", "1 crni luk", "1 šargarepa",
                            "100 g graška", "600 ml vode ili supe", "So, biber i 2 kašike ulja"),
                    list("Iseci piletinu i povrće na manje komade.",
                            "Na ulju proprži luk, dodaj piletinu i peci dok ne porumeni.",
                            "Dodaj šargarepu, pirinač i toplu supu. Kuvaj oko 18 minuta.",
                            "Umešaj grašak, začini i ostavi poklopljeno još 5 minuta.")),
            recipe("veggie_omelette", "Omlet sa povrćem", "🍳",
                    "Brz, mekan omlet za doručak ili laganu večeru.", 15, "Veoma lako", 1,
                    list("eggs", "pepper", "tomato", "cheese"),
                    list("3 jaja", "1/2 paprike", "1 manji paradajz", "40 g sira",
                            "So, biber i malo ulja"),
                    list("Umuti jaja sa prstohvatom soli i bibera.",
                            "Kratko proprži seckanu papriku i paradajz.",
                            "Prelij jajima, dodaj sir i peci na tihoj vatri 4–5 minuta.")),
            recipe("tomato_pasta", "Testenina u paradajz sosu", "🍝",
                    "Jednostavan klasik sa mirisom belog luka.", 25, "Lako", 2,
                    list("pasta", "tomato", "garlic", "onion"),
                    list("200 g testenine", "3 paradajza ili 300 ml pasiranog paradajza",
                            "2 čena belog luka", "1/2 crnog luka", "So, biber i ulje"),
                    list("Skuvaj testeninu prema uputstvu i sačuvaj malo vode od kuvanja.",
                            "Proprži luk i beli luk, pa dodaj paradajz i začine.",
                            "Kuvaj sos 10 minuta, umešaj testeninu i po potrebi dodaj malo vode.")),
            recipe("fried_rice", "Prženi pirinač sa jajima", "🥘",
                    "Odličan način da iskoristiš kuvani pirinač od juče.", 20, "Lako", 2,
                    list("rice", "eggs", "carrot", "peas", "onion"),
                    list("300 g kuvanog hladnog pirinča", "2 jaja", "1 šargarepa",
                            "80 g graška", "1/2 crnog luka", "So, biber i ulje"),
                    list("Na jakom tiganju proprži luk, šargarepu i grašak.",
                            "Pomeri povrće u stranu, sipaj umućena jaja i kratko ih ispeci.",
                            "Dodaj hladan pirinač, sve sjedini i prži još 4–5 minuta.")),
            recipe("potato_frittata", "Fritata sa krompirom", "🥔",
                    "Zasitno jelo od nekoliko svakodnevnih sastojaka.", 35, "Lako", 2,
                    list("potato", "eggs", "onion", "cheese"),
                    list("400 g krompira", "4 jaja", "1 crni luk", "60 g sira",
                            "So, biber i ulje"),
                    list("Krompir iseci na tanke kolutove i kuvaj 8 minuta.",
                            "Proprži luk, dodaj oceđen krompir i kratko zapeci.",
                            "Prelij umućenim jajima, pospi sirom i peci poklopljeno 12 minuta.")),
            recipe("chicken_pasta", "Piletina sa testeninom", "🍗",
                    "Sočna testenina sa piletinom i blagim sosom od jogurta.", 30, "Lako", 2,
                    list("chicken", "pasta", "yogurt", "garlic"),
                    list("300 g pilećeg filea", "200 g testenine", "150 ml čvrstog jogurta",
                            "1 čen belog luka", "So, biber i ulje"),
                    list("Skuvaj testeninu i sačuvaj pola šolje vode od kuvanja.",
                            "Isečenu piletinu začini i ispeci u tiganju.",
                            "Smanji temperaturu, dodaj beli luk, jogurt i malo vode od testenine.",
                            "Umešaj testeninu i zagrej bez jakog ključanja.")),
            recipe("stuffed_zucchini", "Punjene tikvice", "🥒",
                    "Lagane tikvice punjene mesom i pirinčem.", 55, "Srednje", 3,
                    list("zucchini", "ground_meat", "rice", "onion", "tomato"),
                    list("3 srednje tikvice", "300 g mlevenog mesa", "80 g pirinča",
                            "1 crni luk", "200 ml paradajz sosa", "So, biber i ulje"),
                    list("Prepolovi tikvice i izdubi sredinu kašikom.",
                            "Proprži luk i meso, pa dodaj opran pirinač i začine.",
                            "Napuni tikvice, prelij paradajz sosom i pokrij folijom.",
                            "Peci 35 minuta na 200 °C, zatim otkrij i zapeci još 10 minuta.")),
            recipe("tuna_salad", "Salata sa tunjevinom", "🥗",
                    "Sveža proteinska salata gotova za deset minuta.", 10, "Veoma lako", 2,
                    list("tuna", "tomato", "cucumber", "corn", "lemon"),
                    list("1 konzerva tunjevine", "2 paradajza", "1 krastavac",
                            "100 g kukuruza", "1/2 limuna", "So, biber i maslinovo ulje"),
                    list("Ocedi tunjevinu i kukuruz.",
                            "Iseci paradajz i krastavac, pa sve sjedini u činiji.",
                            "Začini limunom, uljem, solju i biberom.")),
            recipe("shakshuka", "Šakšuka", "🍅",
                    "Jaja kuvana u bogatom sosu od paradajza i paprike.", 30, "Lako", 2,
                    list("eggs", "tomato", "pepper", "onion", "garlic"),
                    list("4 jaja", "400 g paradajza", "1 paprika", "1 crni luk",
                            "1 čen belog luka", "So, biber, aleva paprika i ulje"),
                    list("Proprži luk i papriku dok ne omekšaju.",
                            "Dodaj beli luk, paradajz i začine, pa kuvaj 12 minuta.",
                            "Napravi udubljenja, dodaj jaja i kuvaj poklopljeno još 6–8 minuta.")),
            recipe("potato_stew", "Krompir paprikaš", "🥣",
                    "Toplo domaće jelo na kašiku bez komplikovanja.", 45, "Lako", 4,
                    list("potato", "onion", "carrot", "pepper"),
                    list("800 g krompira", "1 veliki crni luk", "2 šargarepe", "1 paprika",
                            "1 kašičica aleve paprike", "So, biber, ulje i voda"),
                    list("Na ulju omekšaj luk, šargarepu i papriku.",
                            "Dodaj alevu papriku, zatim krompir isečen na kocke.",
                            "Nalij vodom da ogrezne, začini i kuvaj 30 minuta.")),
            recipe("chicken_wrap", "Pileće tortilje", "🌯",
                    "Hrskavo povrće i začinjena piletina u toploj tortilji.", 25, "Lako", 2,
                    list("tortilla", "chicken", "pepper", "yogurt", "tomato"),
                    list("4 tortilje", "300 g pilećeg filea", "1 paprika", "1 paradajz",
                            "120 ml jogurta", "So, biber, aleva paprika i ulje"),
                    list("Piletinu iseci na trake, začini i ispeci do zlatne boje.",
                            "Iseci povrće, a jogurt blago posoli.",
                            "Zagrej tortilje, rasporedi nadev i čvrsto ih uvij.")),
            recipe("mushroom_toast", "Tost sa pečurkama", "🍄",
                    "Topao, mirisan tost za brz doručak ili večeru.", 20, "Veoma lako", 2,
                    list("bread", "mushrooms", "garlic", "cheese"),
                    list("4 kriške hleba", "250 g pečuraka", "1 čen belog luka",
                            "80 g sira", "So, biber i malo ulja"),
                    list("Pečurke iseci i peci na jakom tiganju dok voda ne ispari.",
                            "Dodaj beli luk i začine, pa skloni sa vatre.",
                            "Stavi pečurke i sir na hleb i zapeci 6–8 minuta na 200 °C.")),
            recipe("spinach_eggs", "Jaja sa spanaćem", "🥬",
                    "Jednostavan i hranljiv obrok iz jednog tiganja.", 15, "Veoma lako", 1,
                    list("eggs", "spinach", "cheese", "garlic"),
                    list("3 jaja", "150 g spanaća", "40 g sira", "1 čen belog luka",
                            "So, biber i malo ulja"),
                    list("Na ulju kratko zagrej beli luk i dodaj spanać.",
                            "Kada spanać splasne, dodaj umućena jaja.",
                            "Mešaj na tihoj vatri, pospi sirom i odmah posluži.")),
            recipe("bean_salad", "Brza salata od pasulja", "🫘",
                    "Zasitna salata iz ostave koja ne zahteva kuvanje.", 12, "Veoma lako", 2,
                    list("beans", "onion", "pepper", "tomato", "lemon"),
                    list("300 g kuvanog ili konzerviranog pasulja", "1/2 crnog luka",
                            "1 paprika", "1 paradajz", "1/2 limuna", "So, biber i ulje"),
                    list("Isperi i dobro ocedi pasulj.",
                            "Sitno iseci luk, papriku i paradajz.",
                            "Sve pomešaj i začini limunom, uljem, solju i biberom."))
    );

    private static final Map<String, Ingredient> INGREDIENT_MAP = new LinkedHashMap<>();
    private static final Map<String, Recipe> RECIPE_MAP = new LinkedHashMap<>();

    static {
        for (Ingredient ingredient : INGREDIENTS) INGREDIENT_MAP.put(ingredient.id, ingredient);
        for (Recipe recipe : RECIPES) RECIPE_MAP.put(recipe.id, recipe);
    }

    private RecipeRepository() {}

    public static List<Ingredient> ingredients() {
        return INGREDIENTS;
    }

    public static Ingredient ingredient(String id) {
        return INGREDIENT_MAP.get(id);
    }

    public static Recipe recipe(String id) {
        return RECIPE_MAP.get(id);
    }

    public static List<RecipeMatch> findMatches(Set<String> selected) {
        List<RecipeMatch> matches = new ArrayList<>();
        for (Recipe recipe : RECIPES) {
            int matchedCount = 0;
            List<String> missing = new ArrayList<>();
            for (String ingredientId : recipe.coreIngredientIds) {
                if (selected.contains(ingredientId)) matchedCount++;
                else missing.add(ingredientId);
            }
            if (matchedCount > 0) matches.add(new RecipeMatch(recipe, matchedCount, missing));
        }
        Collections.sort(matches, new Comparator<RecipeMatch>() {
            @Override
            public int compare(RecipeMatch first, RecipeMatch second) {
                if (first.isReady() != second.isReady()) return first.isReady() ? -1 : 1;
                int byMissing = Integer.compare(first.missingIngredientIds.size(), second.missingIngredientIds.size());
                if (byMissing != 0) return byMissing;
                int byMatched = Integer.compare(second.matchedCount, first.matchedCount);
                if (byMatched != 0) return byMatched;
                return Integer.compare(first.recipe.minutes, second.recipe.minutes);
            }
        });
        return matches;
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
