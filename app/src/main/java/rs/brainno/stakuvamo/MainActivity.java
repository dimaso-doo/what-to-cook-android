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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MainActivity extends Activity {
    private static final String STATE_SELECTED = "selected";
    private static final String STATE_RESULTS = "results";
    private static final String STATE_QUERY = "query";
    private static final String STATE_MODE = "mode";
    private static final String PREFS = "what_to_cook";
    private static final String PREF_INSTALLATION_ID = "installation_id";

    private final Set<String> selectedIngredients = new LinkedHashSet<>();
    private boolean showingResults;
    private String searchQuery = "";
    private CookingMode cookingMode = CookingMode.STRICT;
    private TextView selectedStatus;
    private Button findButton;
    private FlowLayout selectedIngredientFlow;
    private LinearLayout ingredientSuggestions;
    private TextView searchMessage;
    private EditText ingredientSearch;
    private Button strictModeButton;
    private Button ideasModeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        if (savedInstanceState != null) {
            ArrayList<String> restored = savedInstanceState.getStringArrayList(STATE_SELECTED);
            if (restored != null) selectedIngredients.addAll(restored);
            showingResults = savedInstanceState.getBoolean(STATE_RESULTS, false);
            searchQuery = savedInstanceState.getString(STATE_QUERY, "");
            String restoredMode = savedInstanceState.getString(STATE_MODE, CookingMode.STRICT.name());
            cookingMode = CookingMode.valueOf(restoredMode);
        }
        if (showingResults) loadResults();
        else renderIngredientPicker();
        RecipeRepository.refreshIngredients(loaded -> runOnUiThread(() -> {
            if (loaded && !showingResults && ingredientSuggestions != null) {
                renderIngredientSuggestions(searchQuery);
                renderSelectedIngredients();
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
        outState.putString(STATE_MODE, cookingMode.name());
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

        TextView title = Ui.text(this, "What ingredients do\nyou have?", 35, Ui.INK, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = Ui.dp(this, 26);
        page.addView(title, titleParams);

        TextView subtitle = Ui.text(this,
                "Type at least 2 letters, then select an ingredient from the list.",
                16, Ui.MUTED, false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = Ui.dp(this, 10);
        subtitleParams.bottomMargin = Ui.dp(this, 24);
        page.addView(subtitle, subtitleParams);

        ingredientSearch = new EditText(this);
        ingredientSearch.setHint("Enter an ingredient...");
        ingredientSearch.setHintTextColor(Color.rgb(132, 139, 130));
        ingredientSearch.setTextColor(Ui.INK);
        ingredientSearch.setTextSize(16);
        ingredientSearch.setSingleLine(true);
        ingredientSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        ingredientSearch.setPadding(Ui.dp(this, 17), 0, Ui.dp(this, 17), 0);
        ingredientSearch.setBackground(Ui.outlined(Ui.WHITE, Ui.LINE, 15, this));
        ingredientSearch.setText(searchQuery);
        ingredientSearch.setSelection(ingredientSearch.getText().length());
        page.addView(ingredientSearch, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));

        searchMessage = Ui.text(this, "", 13, Ui.MUTED, false);
        LinearLayout.LayoutParams searchMessageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        searchMessageParams.topMargin = Ui.dp(this, 8);
        page.addView(searchMessage, searchMessageParams);

        ingredientSuggestions = new LinearLayout(this);
        ingredientSuggestions.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams suggestionsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        suggestionsParams.topMargin = Ui.dp(this, 8);
        page.addView(ingredientSuggestions, suggestionsParams);

        LinearLayout selectionRow = new LinearLayout(this);
        selectionRow.setOrientation(LinearLayout.HORIZONTAL);
        selectionRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams selectionRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectionRowParams.topMargin = Ui.dp(this, 28);
        selectionRowParams.bottomMargin = Ui.dp(this, 13);
        page.addView(selectionRow, selectionRowParams);

        TextView sectionTitle = Ui.text(this, "Selected ingredients", 20, Ui.INK, true);
        selectionRow.addView(sectionTitle, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        selectedStatus = Ui.text(this, "", 13, Ui.GREEN, true);
        selectedStatus.setGravity(Gravity.CENTER);
        selectedStatus.setPadding(Ui.dp(this, 11), Ui.dp(this, 6), Ui.dp(this, 11), Ui.dp(this, 6));
        selectedStatus.setBackground(Ui.background(Ui.PALE_GREEN, 20, this));
        selectionRow.addView(selectedStatus);

        selectedIngredientFlow = new FlowLayout(this);
        page.addView(selectedIngredientFlow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        renderSelectedIngredients();
        renderIngredientSuggestions(searchQuery);

        TextView modeTitle = Ui.text(this, "How should AI help?", 20, Ui.INK, true);
        LinearLayout.LayoutParams modeTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        modeTitleParams.topMargin = Ui.dp(this, 24);
        page.addView(modeTitle, modeTitleParams);

        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams modeRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        modeRowParams.topMargin = Ui.dp(this, 12);
        page.addView(modeRow, modeRowParams);

        strictModeButton = modeButton("Only what I have");
        strictModeButton.setOnClickListener(v -> setCookingMode(CookingMode.STRICT));
        modeRow.addView(strictModeButton, new LinearLayout.LayoutParams(
                0, Ui.dp(this, 50), 1));
        ideasModeButton = modeButton("Give me ideas");
        ideasModeButton.setOnClickListener(v -> setCookingMode(CookingMode.IDEAS));
        LinearLayout.LayoutParams ideasParams = new LinearLayout.LayoutParams(
                0, Ui.dp(this, 50), 1);
        ideasParams.leftMargin = Ui.dp(this, 10);
        modeRow.addView(ideasModeButton, ideasParams);

        TextView modeHint = Ui.text(this,
                cookingMode == CookingMode.STRICT
                        ? "AI will use only your ingredients plus salt, pepper, oil, and water."
                        : "AI may suggest up to 5 extra ingredients and will label them clearly.",
                13, Ui.MUTED, false);
        modeHint.setTag("mode_hint");
        LinearLayout.LayoutParams modeHintParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        modeHintParams.topMargin = Ui.dp(this, 9);
        page.addView(modeHint, modeHintParams);
        updateModeUi();

        TextView note = Ui.text(this,
                "No need to enter quantities — you'll see them when you open a recipe.",
                13, Ui.MUTED, false);
        note.setPadding(Ui.dp(this, 14), Ui.dp(this, 13), Ui.dp(this, 14), Ui.dp(this, 13));
        note.setBackground(Ui.background(Color.rgb(247, 241, 231), 12, this));
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = Ui.dp(this, 22);
        page.addView(note, noteParams);

        ingredientSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                renderIngredientSuggestions(searchQuery);
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

    private void renderIngredientSuggestions(String query) {
        if (ingredientSuggestions == null || searchMessage == null) return;
        ingredientSuggestions.removeAllViews();
        String trimmedQuery = query == null ? "" : query.trim();
        if (trimmedQuery.length() < IngredientSearch.MIN_QUERY_LENGTH) {
            searchMessage.setText(trimmedQuery.isEmpty() ? "" : "Enter at least 2 letters.");
            return;
        }

        List<Ingredient> matches = IngredientSearch.suggestions(
                RecipeRepository.ingredients(), trimmedQuery, selectedIngredients);
        if (matches.isEmpty()) {
            searchMessage.setText("No ingredients found.");
            return;
        }

        searchMessage.setText("Select an ingredient:");
        for (Ingredient ingredient : matches) {
            TextView suggestion = Ui.text(this, ingredient.name, 16, Ui.INK, false);
            suggestion.setGravity(Gravity.CENTER_VERTICAL);
            suggestion.setPadding(Ui.dp(this, 16), 0, Ui.dp(this, 16), 0);
            suggestion.setBackground(Ui.outlined(Ui.WHITE, Ui.LINE, 10, this));
            suggestion.setContentDescription("Add " + ingredient.name);
            suggestion.setOnClickListener(v -> selectIngredient(ingredient));
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 50));
            rowParams.bottomMargin = Ui.dp(this, 6);
            ingredientSuggestions.addView(suggestion, rowParams);
        }
    }

    private void selectIngredient(Ingredient ingredient) {
        selectedIngredients.add(ingredient.id);
        searchQuery = "";
        if (ingredientSearch != null) ingredientSearch.setText("");
        renderSelectedIngredients();
        updateSelectionUi();
    }

    private void renderSelectedIngredients() {
        if (selectedIngredientFlow == null) return;
        selectedIngredientFlow.removeAllViews();
        if (selectedIngredients.isEmpty()) {
            TextView empty = Ui.text(this, "No ingredients selected yet.", 14, Ui.MUTED, false);
            empty.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 10));
            selectedIngredientFlow.addView(empty);
            return;
        }

        for (String ingredientId : selectedIngredients) {
            Ingredient ingredient = RecipeRepository.ingredient(ingredientId);
            if (ingredient == null) continue;
            TextView chip = selectedIngredientChip(ingredient);
            selectedIngredientFlow.addView(chip, new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 42)));
        }
    }

    private TextView selectedIngredientChip(Ingredient ingredient) {
        TextView chip = Ui.text(this, ingredient.name + "  ×", 15, Ui.WHITE, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        chip.setMinHeight(Ui.dp(this, 42));
        chip.setContentDescription("Remove " + ingredient.name);
        chip.setBackground(Ui.background(Ui.GREEN, 21, this));
        chip.setOnClickListener(v -> {
            selectedIngredients.remove(ingredient.id);
            renderSelectedIngredients();
            renderIngredientSuggestions(searchQuery);
            updateSelectionUi();
        });
        return chip;
    }

    private void updateSelectionUi() {
        int count = selectedIngredients.size();
        if (selectedStatus != null) selectedStatus.setText(count == 0 ? "None selected" : count + " selected");
        if (findButton != null) {
            findButton.setEnabled(count > 0);
            findButton.setAlpha(count > 0 ? 1f : 0.5f);
            findButton.setText(count > 0 ? "Ask AI for meals  ✨" : "Choose at least one ingredient");
            findButton.setBackground(Ui.background(count > 0 ? Ui.GREEN : Color.rgb(128, 145, 134), 17, this));
        }
    }

    private void loadResults() {
        showingResults = true;
        renderLoadingResults();
        RecipeRepository.findAiMatchesAsync(selectedIngredients, cookingMode, installationId(),
                (matches, result, error) -> runOnUiThread(() -> {
                    if (!showingResults || isFinishing()) return;
                    if (error == null) renderResults(matches, result);
                    else renderAiError(error);
                }));
    }

    private void renderResults(List<RecipeMatch> matches) {
        renderResults(matches, null);
    }

    private void renderResults(List<RecipeMatch> matches, AiSuggestionResult aiResult) {
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
                ? "AI could not find a sensible meal for this combination."
                : matches.size() == 1
                        ? "AI found 1 practical meal for you."
                        : "AI found " + matches.size() + " practical meals for you.";
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

        if (aiResult != null) {
            String quota = aiResult.cached
                    ? "Saved AI result — this search did not use your daily quota."
                    : aiResult.remainingToday == null
                            ? "Fresh AI result."
                            : "Fresh AI result · " + aiResult.remainingToday + " AI searches left today.";
            TextView quotaView = Ui.text(this, quota, 12, Ui.MUTED, false);
            LinearLayout.LayoutParams quotaParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            quotaParams.bottomMargin = Ui.dp(this, 14);
            page.addView(quotaView, quotaParams);
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

    private void renderLoadingResults() {
        LinearLayout page = resultPage("AI is cooking up ideas…",
                cookingMode == CookingMode.STRICT
                        ? "Using only what you have, plus salt, pepper, oil, and water."
                        : "Looking for useful ideas and clearly marking anything extra.");
        TextView sparkle = Ui.text(this, "✨", 58, Ui.INK, false);
        sparkle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = Ui.dp(this, 44);
        page.addView(sparkle, params);
    }

    private void renderAiError(Exception error) {
        String message = error.getMessage() != null && error.getMessage().contains("HTTP 429")
                ? "Today's AI quota has been used. Try a saved combination or come back tomorrow."
                : "AI is temporarily unavailable. Check your connection and try again.";
        LinearLayout page = resultPage("We couldn't generate ideas", message);
        Button retry = new Button(this);
        retry.setAllCaps(false);
        retry.setText("Try again");
        retry.setTextColor(Ui.WHITE);
        retry.setTextSize(16);
        retry.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        retry.setBackground(Ui.background(Ui.GREEN, 15, this));
        retry.setOnClickListener(v -> loadResults());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54));
        params.topMargin = Ui.dp(this, 28);
        page.addView(retry, params);
    }

    private LinearLayout resultPage(String titleText, String subtitleText) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Ui.CREAM);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(Ui.dp(this, 20), Ui.dp(this, 18), Ui.dp(this, 20), Ui.dp(this, 32));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView back = Ui.text(this, "‹  Change ingredients", 15, Ui.GREEN, true);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setOnClickListener(v -> {
            showingResults = false;
            renderIngredientPicker();
        });
        page.addView(back, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 48)));
        TextView title = Ui.text(this, titleText, 30, Ui.INK, true);
        page.addView(title);
        TextView subtitle = Ui.text(this, subtitleText, 15, Ui.MUTED, false);
        subtitle.setPadding(0, Ui.dp(this, 10), 0, 0);
        page.addView(subtitle);
        setContentView(scroll);
        return page;
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

        if (recipe.aiGenerated) {
            TextView aiBadge = Ui.text(this, "✨  AI-generated idea", 12, Ui.GREEN, true);
            aiBadge.setPadding(Ui.dp(this, 10), Ui.dp(this, 7), Ui.dp(this, 10), Ui.dp(this, 7));
            aiBadge.setBackground(Ui.background(Ui.PALE_GREEN, 10, this));
            LinearLayout.LayoutParams aiParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            aiParams.topMargin = Ui.dp(this, 12);
            card.addView(aiBadge, aiParams);
        }

        if (!match.missingIngredientIds.isEmpty()) {
            TextView missing = Ui.text(this,
                    "Extra needed: " + android.text.TextUtils.join(", ", match.missingIngredientIds),
                    13, Ui.ORANGE, true);
            LinearLayout.LayoutParams missingParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            missingParams.topMargin = Ui.dp(this, 10);
            card.addView(missing, missingParams);
        }

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

    private Button modeButton(String text) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(14);
        button.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        button.setStateListAnimator(null);
        return button;
    }

    private void setCookingMode(CookingMode mode) {
        cookingMode = mode;
        updateModeUi();
    }

    private void updateModeUi() {
        if (strictModeButton == null || ideasModeButton == null) return;
        styleModeButton(strictModeButton, cookingMode == CookingMode.STRICT);
        styleModeButton(ideasModeButton, cookingMode == CookingMode.IDEAS);
        View root = strictModeButton.getRootView();
        if (root != null) {
            View tagged = root.findViewWithTag("mode_hint");
            if (tagged instanceof TextView) {
                ((TextView) tagged).setText(cookingMode == CookingMode.STRICT
                        ? "AI will use only your ingredients plus salt, pepper, oil, and water."
                        : "AI may suggest up to 5 extra ingredients and will label them clearly.");
            }
        }
    }

    private void styleModeButton(Button button, boolean selected) {
        button.setTextColor(selected ? Ui.WHITE : Ui.GREEN);
        button.setBackground(selected
                ? Ui.background(Ui.GREEN, 14, this)
                : Ui.outlined(Ui.WHITE, Ui.GREEN, 14, this));
    }

    private String installationId() {
        android.content.SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String existing = preferences.getString(PREF_INSTALLATION_ID, null);
        if (existing != null && !existing.isEmpty()) return existing;
        String created = java.util.UUID.randomUUID().toString();
        preferences.edit().putString(PREF_INSTALLATION_ID, created).apply();
        return created;
    }

}
