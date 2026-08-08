package rs.brainno.stakuvamo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class RecipeDetailActivity extends Activity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_SELECTED = "selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        String recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        Recipe recipe = RecipeRepository.recipe(recipeId);
        if (recipe == null) {
            finish();
            return;
        }
        ArrayList<String> selectedExtra = getIntent().getStringArrayListExtra(EXTRA_SELECTED);
        Set<String> selected = selectedExtra == null ? new HashSet<>() : new HashSet<>(selectedExtra);
        render(recipe, selected);
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Ui.CREAM);
        window.setNavigationBarColor(Ui.CREAM);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void render(Recipe recipe, Set<String> selected) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setBackgroundColor(Ui.CREAM);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(Ui.dp(this, 20), Ui.dp(this, 18), Ui.dp(this, 20), Ui.dp(this, 40));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView back = Ui.text(this, "‹  All ideas", 15, Ui.GREEN, true);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setMinHeight(Ui.dp(this, 48));
        back.setOnClickListener(v -> finish());
        page.addView(back, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 48)));

        TextView hero = Ui.text(this, recipe.emoji, 62, Ui.INK, false);
        hero.setGravity(Gravity.CENTER);
        hero.setBackground(Ui.background(Ui.PALE_ORANGE, 26, this));
        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 164));
        heroParams.topMargin = Ui.dp(this, 8);
        page.addView(hero, heroParams);

        TextView title = Ui.text(this, recipe.title, 31, Ui.INK, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = Ui.dp(this, 23);
        page.addView(title, titleParams);

        TextView description = Ui.text(this, recipe.description, 15, Ui.MUTED, false);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionParams.topMargin = Ui.dp(this, 8);
        page.addView(description, descriptionParams);

        LinearLayout meta = new LinearLayout(this);
        meta.setOrientation(LinearLayout.HORIZONTAL);
        meta.setGravity(Gravity.CENTER);
        meta.setPadding(Ui.dp(this, 8), Ui.dp(this, 15), Ui.dp(this, 8), Ui.dp(this, 15));
        meta.setBackground(Ui.outlined(Ui.WHITE, Ui.LINE, 16, this));
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metaParams.topMargin = Ui.dp(this, 20);
        page.addView(meta, metaParams);
        meta.addView(metaCell("⏱", recipe.minutes + " min", "Time"));
        meta.addView(metaDivider());
        meta.addView(metaCell("●", recipe.difficulty, "Difficulty"));
        meta.addView(metaDivider());
        meta.addView(metaCell("♨", recipe.servings + (recipe.servings == 1 ? " serving" : " servings"), "Yield"));

        if (selected.containsAll(recipe.coreIngredientIds)) {
            TextView availability = Ui.text(this,
                    "✓  You have all the main ingredients", 14, Ui.GREEN, true);
            availability.setBackground(Ui.background(Ui.PALE_GREEN, 13, this));
            availability.setPadding(Ui.dp(this, 15), Ui.dp(this, 13), Ui.dp(this, 15), Ui.dp(this, 13));
            LinearLayout.LayoutParams availabilityParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            availabilityParams.topMargin = Ui.dp(this, 14);
            page.addView(availability, availabilityParams);
        }

        addSectionTitle(page, "Main ingredients", "Items you already have are marked in green", 29);
        FlowLayout coreFlow = new FlowLayout(this);
        page.addView(coreFlow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        for (String id : recipe.coreIngredientIds) {
            Ingredient ingredient = RecipeRepository.ingredient(id);
            if (ingredient == null) continue;
            boolean hasIngredient = selected.contains(id);
            TextView chip = Ui.text(this,
                    (hasIngredient ? "✓  " : "+  ") + ingredient.name,
                    13, hasIngredient ? Ui.GREEN : Ui.MUTED, true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
            chip.setBackground(hasIngredient
                    ? Ui.background(Ui.PALE_GREEN, 20, this)
                    : Ui.outlined(Ui.WHITE, Ui.LINE, 20, this));
            coreFlow.addView(chip, new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 40)));
        }

        addSectionTitle(page, "Quantities", "For " + recipe.servings +
                (recipe.servings == 1 ? " serving" : " servings"), 29);
        LinearLayout ingredientCard = sectionCard();
        page.addView(ingredientCard);
        for (int i = 0; i < recipe.ingredientLines.size(); i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            row.setPadding(0, Ui.dp(this, 9), 0, Ui.dp(this, 9));
            TextView bullet = Ui.text(this, "•", 20, Ui.ORANGE, true);
            row.addView(bullet, new LinearLayout.LayoutParams(Ui.dp(this, 23),
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView line = Ui.text(this, recipe.ingredientLines.get(i), 15, Ui.INK, false);
            row.addView(line, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            ingredientCard.addView(row);
            if (i < recipe.ingredientLines.size() - 1) ingredientCard.addView(divider());
        }

        addSectionTitle(page, "Directions", recipe.steps.size() + " simple steps", 31);
        for (int i = 0; i < recipe.steps.size(); i++) {
            LinearLayout step = new LinearLayout(this);
            step.setOrientation(LinearLayout.HORIZONTAL);
            step.setGravity(Gravity.TOP);
            LinearLayout.LayoutParams stepParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            stepParams.bottomMargin = Ui.dp(this, 17);
            page.addView(step, stepParams);

            TextView number = Ui.text(this, String.valueOf(i + 1), 15, Ui.WHITE, true);
            number.setGravity(Gravity.CENTER);
            number.setBackground(Ui.background(Ui.GREEN, 18, this));
            step.addView(number, new LinearLayout.LayoutParams(Ui.dp(this, 36), Ui.dp(this, 36)));
            TextView instruction = Ui.text(this, recipe.steps.get(i), 15, Ui.INK, false);
            LinearLayout.LayoutParams instructionParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            instructionParams.leftMargin = Ui.dp(this, 13);
            instructionParams.topMargin = Ui.dp(this, 5);
            step.addView(instruction, instructionParams);
        }

        TextView enjoy = Ui.text(this, "Enjoy!  🍽", 19, Ui.GREEN, true);
        enjoy.setGravity(Gravity.CENTER);
        enjoy.setPadding(Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18));
        enjoy.setBackground(Ui.background(Ui.PALE_GREEN, 16, this));
        LinearLayout.LayoutParams enjoyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        enjoyParams.topMargin = Ui.dp(this, 8);
        page.addView(enjoy, enjoyParams);

        setContentView(scroll);
    }

    private View metaCell(String icon, String value, String label) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        TextView valueView = Ui.text(this, icon + "  " + value, 13, Ui.INK, true);
        valueView.setGravity(Gravity.CENTER);
        cell.addView(valueView);
        TextView labelView = Ui.text(this, label, 11, Ui.MUTED, false);
        labelView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = Ui.dp(this, 4);
        cell.addView(labelView, labelParams);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return cell;
    }

    private View metaDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(Ui.LINE);
        divider.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 1), Ui.dp(this, 36)));
        return divider;
    }

    private void addSectionTitle(LinearLayout page, String title, String subtitle, int topDp) {
        TextView titleView = Ui.text(this, title, 22, Ui.INK, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = Ui.dp(this, topDp);
        page.addView(titleView, titleParams);
        TextView subtitleView = Ui.text(this, subtitle, 13, Ui.MUTED, false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = Ui.dp(this, 3);
        subtitleParams.bottomMargin = Ui.dp(this, 13);
        page.addView(subtitleView, subtitleParams);
    }

    private LinearLayout sectionCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 6));
        card.setBackground(Ui.outlined(Ui.WHITE, Ui.LINE, 16, this));
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return card;
    }

    private View divider() {
        View divider = new View(this);
        divider.setBackgroundColor(Ui.LINE);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 1)));
        return divider;
    }
}
