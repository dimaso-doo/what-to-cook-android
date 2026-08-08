package rs.brainno.stakuvamo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class SupabaseRecipeService {
    interface IngredientsCallback {
        void onComplete(List<Ingredient> ingredients, Exception error);
    }

    interface RecipesCallback {
        void onComplete(List<Recipe> recipes, Exception error);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final int CONNECT_TIMEOUT_MS = 8_000;
    private static final int READ_TIMEOUT_MS = 12_000;

    private SupabaseRecipeService() {}

    static void loadIngredients(IngredientsCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                String path = "/rest/v1/ingredients"
                        + "?select=slug,name,emoji,category"
                        + "&active=eq.true"
                        + "&order=is_featured.desc,sort_order.asc,name.asc";
                JSONArray rows = new JSONArray(request("GET", path, null));
                List<Ingredient> ingredients = new ArrayList<>();
                for (int index = 0; index < rows.length(); index++) {
                    JSONObject row = rows.getJSONObject(index);
                    ingredients.add(new Ingredient(
                            row.getString("slug"),
                            row.getString("name"),
                            row.optString("emoji", ""),
                            row.getString("category")));
                }
                callback.onComplete(ingredients, null);
            } catch (Exception error) {
                callback.onComplete(null, error);
            }
        });
    }

    static void loadMatches(Collection<String> selected, RecipesCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("available_ingredient_slugs", new JSONArray(selected));
                JSONArray rows = new JSONArray(request(
                        "POST", "/rest/v1/rpc/match_recipes", body.toString()));
                List<Recipe> recipes = new ArrayList<>();
                for (int index = 0; index < rows.length(); index++) {
                    JSONObject row = rows.getJSONObject(index);
                    recipes.add(new Recipe(
                            row.getString("recipe_slug"),
                            row.getString("title"),
                            row.optString("emoji", "🍽️"),
                            row.getString("description"),
                            row.getInt("total_minutes"),
                            row.getString("difficulty"),
                            row.getInt("servings"),
                            strings(row.getJSONArray("required_ingredient_slugs")),
                            strings(row.getJSONArray("ingredient_lines")),
                            strings(row.getJSONArray("steps"))));
                }
                callback.onComplete(recipes, null);
            } catch (Exception error) {
                callback.onComplete(null, error);
            }
        });
    }

    private static List<String> strings(JSONArray values) throws Exception {
        List<String> result = new ArrayList<>();
        for (int index = 0; index < values.length(); index++) result.add(values.getString(index));
        return result;
    }

    private static String request(String method, String path, String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BuildConfig.SUPABASE_URL + path)
                .openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY);
        connection.setRequestProperty("Accept", "application/json");
        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }
        }

        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        String response = read(stream);
        connection.disconnect();
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("Supabase request failed with HTTP " + status + ": " + response);
        }
        return response;
    }

    private static String read(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }
}
