package rs.brainno.stakuvamo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MainActivity extends Activity {
    private static final String STATE_SELECTED = "selected";
    private static final String STATE_RESULTS = "results";
    private static final String STATE_QUERY = "query";

    private final Set<String> selectedIngredients = new LinkedHashSet<>();
    private boolean showingResults;
    private String searchQuery = "";
    private TextView selectedStatus;
    private Button findButton;
    private FlowLayout ingredientFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        if (savedInstanceState != null) {
            ArrayList<String> restored = savedInstanceState.getStringArrayList(STATE_SELECTED);
            if (restored != null) selectedIngredients.addAll(restored);
            showingResults = savedInstanceState.getBoolean(STATE_RESULTS, false);
            searchQuery = savedInstanceState.getString(STATE_QUERY, "");
        }
        if (showingResults) loadResults();
        else renderIngredientPicker();
        RecipeRepository.refreshIngredients(loaded -> runOnUiThread(() -> {
            if (loaded && !showingResults && ingredientFlow != null) {
                renderIngredientChips(searchQuery);
            }
        }));
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Ui.CREAM);
        window.setNavigationBarColor(Ui.CREAM);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(STATE_SELECTED, new ArrayList<>(selectedIngredients));
        outState.putBoolean(STATE_RESULTS, showingResults);
        outState.putString(STATE_QUERY, searchQuery);
    }

    @Override
    public void onBackPressed() {
        if (showingResults) {
            showingResults = false;
            renderIngredientPicker();
        } else {
            super.onBackPressed();
        }
    }

    private void renderIngredientPicker() {
        showingResults = false;
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Ui.CREAM);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(scroll, scrollParams);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(Ui.dp(this, 20), Ui.dp(this, 22), Ui.dp(this, 20), Ui.dp(this, 118));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = Ui.text(this, "🥕", 24, Ui.INK, false);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(Ui.background(Ui.PALE_ORANGE, 14, this));
        brand.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 48)));
        TextView brandName = Ui.text(this, "WHAT TO COOK?", 13, Ui.GREEN, true);
        brandName.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams brandNameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        brandNameParams.leftMargin = Ui.dp(this, 12);
        brand.addView(brandName, brandNameParams);
        page.addView(brand);

        TextView title = Ui.text(this, "What's in your\nkitchen?", 35, Ui.INK, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = Ui.dp(this, 26);
        page.addView(title, titleParams);

        TextView subtitle = Ui.text(this,
                "Choose the ingredients you have and we'll find the best matching meals.",
                16, Ui.MUTED, false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = Ui.dp(this, 10);
        subtitleParams.bottomMargin = Ui.dp(this, 24);
        page.addView(subtitle, subtitleParams);

        EditText search = new EditText(this);
        search.setHint("Search ingredients...");
        search.setHintTextColor(Color.rgb(132, 139, 130));
        search.setTextColor(Ui.INK);
        search.setTextSize(16);
        search.setSingleLine(true);
        search.setImeOptions(EditorInfo.IME_ACTION_DONE);
        search.setPadding(Ui.dp(this, 17), 0, Ui.dp(this, 17), 0);
        search.setBackground(Ui.outlined(Ui.WHITE, Ui.LINE, 15, this));
        search.setText(searchQuery);
        search.setSelection(search.getText().length());
        page.addView(search, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));

        LinearLayout selectionRow = new LinearLayout(this);
        selectionRow.setOrientation(LinearLayout.HORIZONTAL);
        selectionRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams selectionRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectionRowParams.topMargin = Ui.dp(this, 24);
        selectionRowParams.bottomMargin = Ui.dp(this, 13);
        page.addView(selectionRow, selectionRowParams);

        TextView sectionTitle = Ui.text(this, "Ingredients", 20, Ui.INK, true);
        selectionRow.addView(sectionTitle, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        selectedStatus = Ui.text(this, "", 13, Ui.GREEN, true);
        selectedStatus.setGravity(Gravity.CENTER);
        selectedStatus.setPadding(Ui.dp(this, 11), Ui.dp(this, 6), Ui.dp(this, 11), Ui.dp(this, 6));
        selectedStatus.setBackground(Ui.background(Ui.PALE_GREEN, 20, this));
        selectionRow.addView(selectedStatus);

        ingredientFlow = new FlowLayout(this);
        page.addView(ingredientFlow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        renderIngredientChips(searchQuery);

        TextView note = Ui.text(this,
                "No need to enter quantities — you'll see them when you open a recipe.",
                13, Ui.MUTED, false);
        note.setPadding(Ui.dp(this, 14), Ui.dp(this, 13), Ui.dp(this, 14), Ui.dp(this, 13));
        note.setBackground(Ui.background(Color.rgb(247, 241, 231), 12, this));
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = Ui.dp(this, 22);
        page.addView(note, noteParams);

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                renderIngredientChips(searchQuery);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findButton = new Button(this);
        findButton.setAllCaps(false);
        findButton.setTextSize(17);
        findButton.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        findButton.setTextColor(Ui.WHITE);
        findButton.setGravity(Gravity.CENTER);
        findButton.setStateListAnimator(null);
        findButton.setOnClickListener(v -> {
            if (!selectedIngredients.isEmpty()) {
                loadResults();
            }
        });
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58), Gravity.BOTTOM);
        buttonParams.setMargins(Ui.dp(this, 20), Ui.dp(this, 12), Ui.dp(this, 20), Ui.dp(this, 20));
        root.addView(findButton, buttonParams);

        updateSelectionUi();
        setContentView(root);
    }

    private void renderIngredientChips(String query) {
        if (ingredientFlow == null) return;
        ingredientFlow.removeAllViews();
        String normalizedQuery = normalize(query);
        int popularCount = 0;
        for (Ingredient ingredient : RecipeRepository.ingredients()) {
            if (!normalizedQuery.isEmpty() && !normalize(ingredient.name).contains(normalizedQuery)) continue;
            if (normalizedQuery.isEmpty()
                    && popularCount >= 10
                    && !selectedIngredients.contains(ingredient.id)) continue;
            TextView chip = ingredientChip(ingredient);
            ingredientFlow.addView(chip, new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 46)));
            if (normalizedQuery.isEmpty()) popularCount++;
        }
        if (ingredientFlow.getChildCount() == 0) {
            TextView empty = Ui.text(this, "No ingredients match your search.", 14, Ui.MUTED, false);
            empty.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 12));
            ingredientFlow.addView(empty);
        }
    }

    private TextView ingredientChip(Ingredient ingredient) {
        TextView chip = Ui.text(this, ingredient.emoji + "  " + ingredient.name, 15, Ui.INK, false);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        chip.setMinHeight(Ui.dp(this, 46));
        chip.setContentDescription(ingredient.name);
        styleIngredientChip(chip, selectedIngredients.contains(ingredient.id));
        chip.setOnClickListener(v -> {
            if (selectedIngredients.contains(ingredient.id)) selectedIngredients.remove(ingredient.id);
            else selectedIngredients.add(ingredient.id);
            styleIngredientChip(chip, selectedIngredients.contains(ingredient.id));
            updateSelectionUi();
        });
        return chip;
    }

    private void styleIngredientChip(TextView chip, boolean selected) {
        chip.setTextColor(selected ? Ui.WHITE : Ui.INK);
        chip.setTypeface(android.graphics.Typeface.create("sans",
                selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL));
        chip.setBackground(selected
                ? Ui.background(Ui.GREEN, 23, this)
                : Ui.outlined(Ui.WHITE, Ui.LINE, 23, this));
        chip.setSelected(selected);
    }

    private void updateSelectionUi() {
        int count = selectedIngredients.size();
        if (selectedStatus != null) selectedStatus.setText(count == 0 ? "None selected" : count + " selected");
        if (findButton != null) {
            findButton.setEnabled(count > 0);
            findButton.setAlpha(count > 0 ? 1f : 0.5f);
            findButton.setText(count > 0 ? "Find meals  →" : "Choose at least one ingredient");
            findButton.setBackground(Ui.background(count > 0 ? Ui.GREEN : Color.rgb(128, 145, 134), 17, this));
        }
    }

    private void loadResults() {
        showingResults = true;
        renderResults(RecipeRepository.findMatches(selectedIngredients));
        RecipeRepository.findMatchesAsync(selectedIngredients, (matches, loadedFromSupabase) ->
                runOnUiThread(() -> {
                    if (showingResults && !isFinishing()) renderResults(matches);
                }));
    }

    private void renderResults(List<RecipeMatch> matches) {
        showingResults = true;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setBackgroundColor(Ui.CREAM);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(Ui.dp(this, 20), Ui.dp(this, 18), Ui.dp(this, 20), Ui.dp(this, 32));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView back = Ui.text(this, "‹  Change ingredients", 15, Ui.GREEN, true);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setMinHeight(Ui.dp(this, 48));
        back.setOnClickListener(v -> {
            showingResults = false;
            renderIngredientPicker();
        });
        page.addView(back, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 48)));

        TextView title = Ui.text(this, "Ideas for you", 32, Ui.INK, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = Ui.dp(this, 10);
        page.addView(title, titleParams);

        String summary = matches.isEmpty()
                ? "No meals can be made with only these ingredients. Add more ingredients and try again."
                : matches.size() == 1
                        ? "We found 1 meal you can make with the ingredients you have."
                        : "We found " + matches.size() + " meals you can make with the ingredients you have.";
        TextView subtitle = Ui.text(this, summary, 15, Ui.MUTED, false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = Ui.dp(this, 8);
        page.addView(subtitle, subtitleParams);

        FlowLayout selectedFlow = new FlowLayout(this);
        LinearLayout.LayoutParams selectedFlowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectedFlowParams.topMargin = Ui.dp(this, 20);
        selectedFlowParams.bottomMargin = Ui.dp(this, 18);
        page.addView(selectedFlow, selectedFlowParams);
        for (String id : selectedIngredients) {
            Ingredient ingredient = RecipeRepository.ingredient(id);
            if (ingredient == null) continue;
            TextView chip = Ui.text(this, "✓  " + ingredient.name, 13, Ui.GREEN, true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
            chip.setBackground(Ui.background(Ui.PALE_GREEN, 19, this));
            selectedFlow.addView(chip, new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 38)));
        }

        for (RecipeMatch match : matches) page.addView(recipeCard(match));

        if (matches.isEmpty()) {
            TextView emptyEmoji = Ui.text(this, "🧺", 54, Ui.INK, false);
            emptyEmoji.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            emojiParams.topMargin = Ui.dp(this, 36);
            page.addView(emptyEmoji, emojiParams);
        }
        setContentView(scroll);
    }

    private View recipeCard(RecipeMatch match) {
        Recipe recipe = match.recipe;
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 17), Ui.dp(this, 17), Ui.dp(this, 17), Ui.dp(this, 16));
        card.setBackground(Ui.outlined(Ui.WHITE, Ui.LINE, 18, this));
        card.setElevation(Ui.dp(this, 1));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> openRecipe(recipe.id));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = Ui.dp(this, 13);
        card.setLayoutParams(cardParams);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView emoji = Ui.text(this, recipe.emoji, 31, Ui.INK, false);
        emoji.setGravity(Gravity.CENTER);
        emoji.setBackground(Ui.background(Ui.PALE_ORANGE, 15, this));
        header.addView(emoji, new LinearLayout.LayoutParams(Ui.dp(this, 62), Ui.dp(this, 62)));

        LinearLayout titleColumn = new LinearLayout(this);
        titleColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleColumnParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        titleColumnParams.leftMargin = Ui.dp(this, 14);
        header.addView(titleColumn, titleColumnParams);
        TextView title = Ui.text(this, recipe.title, 18, Ui.INK, true);
        titleColumn.addView(title);
        TextView meta = Ui.text(this, "⏱ " + recipe.minutes + " min  ·  " + recipe.difficulty, 13, Ui.MUTED, false);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metaParams.topMargin = Ui.dp(this, 5);
        titleColumn.addView(meta, metaParams);

        TextView arrow = Ui.text(this, "›", 28, Ui.GREEN, false);
        header.addView(arrow);

        TextView description = Ui.text(this, recipe.description, 14, Ui.MUTED, false);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionParams.topMargin = Ui.dp(this, 13);
        card.addView(description, descriptionParams);

        if (match.isReady()) {
            TextView matchStatus = Ui.text(this,
                    "✓  You have all the main ingredients", 13, Ui.GREEN, true);
            matchStatus.setPadding(Ui.dp(this, 11), Ui.dp(this, 8), Ui.dp(this, 11), Ui.dp(this, 8));
            matchStatus.setBackground(Ui.background(Ui.PALE_GREEN, 10, this));
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            statusParams.topMargin = Ui.dp(this, 13);
            card.addView(matchStatus, statusParams);
        }
        return card;
    }

    private void openRecipe(String recipeId) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipeId);
        intent.putStringArrayListExtra(RecipeDetailActivity.EXTRA_SELECTED,
                new ArrayList<>(selectedIngredients));
        startActivity(intent);
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}
