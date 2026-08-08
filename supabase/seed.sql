-- Initial English recipe catalogue. These recipes are original Dimaso content.

insert into public.ingredients (slug, name, emoji, category, search_terms, is_featured, sort_order)
values
    ('eggs', 'Eggs', '🥚', 'Protein', array['egg'], true, 1),
    ('chicken', 'Chicken', '🍗', 'Protein', array['chicken breast'], true, 2),
    ('rice', 'Rice', '🍚', 'Grains', array[]::text[], true, 3),
    ('pasta', 'Pasta', '🍝', 'Grains', array['spaghetti', 'noodles'], true, 4),
    ('potato', 'Potatoes', '🥔', 'Vegetables', array['potato'], true, 5),
    ('onion', 'Onion', '🧅', 'Vegetables', array['onions'], true, 6),
    ('tomato', 'Tomatoes', '🍅', 'Vegetables', array['tomato'], true, 7),
    ('cheese', 'Cheese', '🧀', 'Dairy', array[]::text[], true, 8),
    ('garlic', 'Garlic', '🧄', 'Vegetables', array[]::text[], true, 9),
    ('ground_meat', 'Ground beef', '🥩', 'Protein', array['ground meat', 'minced beef', 'minced meat'], true, 10),
    ('tuna', 'Tuna', '🐟', 'Protein', array[]::text[], false, 20),
    ('beans', 'Beans', '🫘', 'Protein', array['bean'], false, 21),
    ('bread', 'Bread', '🍞', 'Grains', array['toast'], false, 22),
    ('tortilla', 'Tortillas', '🫓', 'Grains', array['wrap'], false, 23),
    ('pepper', 'Bell pepper', '🫑', 'Vegetables', array['bell peppers', 'capsicum'], false, 24),
    ('zucchini', 'Zucchini', '🥒', 'Vegetables', array['courgette'], false, 25),
    ('carrot', 'Carrot', '🥕', 'Vegetables', array['carrots'], false, 26),
    ('mushrooms', 'Mushrooms', '🍄', 'Vegetables', array['mushroom'], false, 27),
    ('spinach', 'Spinach', '🥬', 'Vegetables', array[]::text[], false, 28),
    ('corn', 'Corn', '🌽', 'Vegetables', array['sweetcorn'], false, 29),
    ('peas', 'Peas', '🟢', 'Vegetables', array['pea'], false, 30),
    ('cucumber', 'Cucumber', '🥒', 'Vegetables', array[]::text[], false, 31),
    ('yogurt', 'Yogurt', '🥛', 'Dairy', array['yoghurt'], false, 32),
    ('lemon', 'Lemon', '🍋', 'Fruit', array[]::text[], false, 33),
    ('stock', 'Stock', '🥣', 'Pantry', array['broth'], false, 40),
    ('oil', 'Cooking oil', '🫒', 'Pantry', array['olive oil', 'vegetable oil'], false, 41),
    ('water', 'Water', '💧', 'Pantry', array[]::text[], false, 42)
on conflict (slug) do update set
    name = excluded.name,
    emoji = excluded.emoji,
    category = excluded.category,
    search_terms = excluded.search_terms,
    is_featured = excluded.is_featured,
    sort_order = excluded.sort_order,
    active = true,
    updated_at = now();

insert into public.recipes
    (slug, title, emoji, description, total_minutes, difficulty, servings, published,
     source_name, source_url, license_name, attribution, modified_from_source)
values
    ('chicken_risotto', 'Chicken Risotto', '🍲', 'A creamy one-pan meal that is perfect for a busy weeknight.', 35, 'Easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('veggie_omelette', 'Vegetable Omelet', '🍳', 'A quick, fluffy omelet for breakfast or a light dinner.', 15, 'Very easy', 1, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('tomato_pasta', 'Pasta with Tomato Sauce', '🍝', 'A simple classic with plenty of garlic flavor.', 25, 'Easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('fried_rice', 'Egg Fried Rice', '🥘', 'A delicious way to use up yesterday''s cooked rice.', 20, 'Easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('potato_frittata', 'Potato Frittata', '🥔', 'A satisfying meal made with a few everyday ingredients.', 35, 'Easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('chicken_pasta', 'Creamy Chicken Pasta', '🍗', 'Tender chicken and pasta in a light yogurt sauce.', 30, 'Easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('stuffed_zucchini', 'Stuffed Zucchini', '🥒', 'Tender zucchini filled with ground beef and rice.', 55, 'Medium', 3, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('tuna_salad', 'Tuna Salad', '🥗', 'A fresh, protein-packed salad ready in ten minutes.', 10, 'Very easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('shakshuka', 'Shakshuka', '🍅', 'Eggs gently cooked in a rich tomato and bell pepper sauce.', 30, 'Easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('potato_stew', 'Rustic Potato Stew', '🥣', 'A warm, comforting one-pot meal with no fuss.', 45, 'Easy', 4, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('chicken_wrap', 'Chicken Wraps', '🌯', 'Crisp vegetables and seasoned chicken wrapped in warm tortillas.', 25, 'Easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('mushroom_toast', 'Mushroom Toast', '🍄', 'A warm, savory toast for a quick breakfast or dinner.', 20, 'Very easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('spinach_eggs', 'Eggs with Spinach', '🥬', 'A simple, nutritious one-pan meal.', 15, 'Very easy', 1, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false),
    ('bean_salad', 'Quick Bean Salad', '🫘', 'A filling pantry salad that requires no cooking.', 12, 'Very easy', 2, true, 'Dimaso original recipe', 'https://github.com/dimaso-doo/what-to-cook-android', 'All rights reserved', 'Dimaso d.o.o.', false)
on conflict (slug) do update set
    title = excluded.title,
    emoji = excluded.emoji,
    description = excluded.description,
    total_minutes = excluded.total_minutes,
    difficulty = excluded.difficulty,
    servings = excluded.servings,
    published = excluded.published,
    source_name = excluded.source_name,
    source_url = excluded.source_url,
    license_name = excluded.license_name,
    attribution = excluded.attribution,
    modified_from_source = excluded.modified_from_source,
    updated_at = now();

delete from public.recipe_ingredients
where recipe_id in (select id from public.recipes where slug in (
    'chicken_risotto', 'veggie_omelette', 'tomato_pasta', 'fried_rice',
    'potato_frittata', 'chicken_pasta', 'stuffed_zucchini', 'tuna_salad',
    'shakshuka', 'potato_stew', 'chicken_wrap', 'mushroom_toast',
    'spinach_eggs', 'bean_salad'
));

insert into public.recipe_ingredients
    (recipe_id, ingredient_id, position, display_text, required_for_match, optional)
select r.id, i.id, v.position, v.display_text, v.required_for_match, false
from (values
    ('chicken_risotto', 'chicken', 1, '300 g chicken breast', true),
    ('chicken_risotto', 'rice', 2, '180 g rice', true),
    ('chicken_risotto', 'onion', 3, '1 onion', true),
    ('chicken_risotto', 'carrot', 4, '1 carrot', true),
    ('chicken_risotto', 'peas', 5, '100 g peas', true),
    ('chicken_risotto', 'stock', 6, '600 ml water or stock', false),
    ('chicken_risotto', 'oil', 7, 'Salt, pepper, and 2 tbsp oil', false),

    ('veggie_omelette', 'eggs', 1, '3 eggs', true),
    ('veggie_omelette', 'pepper', 2, '1/2 bell pepper', true),
    ('veggie_omelette', 'tomato', 3, '1 small tomato', true),
    ('veggie_omelette', 'cheese', 4, '40 g cheese', true),
    ('veggie_omelette', 'oil', 5, 'Salt, pepper, and a little oil', false),

    ('tomato_pasta', 'pasta', 1, '200 g pasta', true),
    ('tomato_pasta', 'tomato', 2, '3 tomatoes or 300 ml crushed tomatoes', true),
    ('tomato_pasta', 'garlic', 3, '2 garlic cloves', true),
    ('tomato_pasta', 'onion', 4, '1/2 onion', true),
    ('tomato_pasta', 'oil', 5, 'Salt, pepper, and oil', false),

    ('fried_rice', 'rice', 1, '300 g cold cooked rice', true),
    ('fried_rice', 'eggs', 2, '2 eggs', true),
    ('fried_rice', 'carrot', 3, '1 carrot', true),
    ('fried_rice', 'peas', 4, '80 g peas', true),
    ('fried_rice', 'onion', 5, '1/2 onion', true),
    ('fried_rice', 'oil', 6, 'Salt, pepper, and oil', false),

    ('potato_frittata', 'potato', 1, '400 g potatoes', true),
    ('potato_frittata', 'eggs', 2, '4 eggs', true),
    ('potato_frittata', 'onion', 3, '1 onion', true),
    ('potato_frittata', 'cheese', 4, '60 g cheese', true),
    ('potato_frittata', 'oil', 5, 'Salt, pepper, and oil', false),

    ('chicken_pasta', 'chicken', 1, '300 g chicken breast', true),
    ('chicken_pasta', 'pasta', 2, '200 g pasta', true),
    ('chicken_pasta', 'yogurt', 3, '150 ml thick yogurt', true),
    ('chicken_pasta', 'garlic', 4, '1 garlic clove', true),
    ('chicken_pasta', 'oil', 5, 'Salt, pepper, and oil', false),

    ('stuffed_zucchini', 'zucchini', 1, '3 medium zucchini', true),
    ('stuffed_zucchini', 'ground_meat', 2, '300 g ground beef', true),
    ('stuffed_zucchini', 'rice', 3, '80 g rice', true),
    ('stuffed_zucchini', 'onion', 4, '1 onion', true),
    ('stuffed_zucchini', 'tomato', 5, '200 ml tomato sauce', true),
    ('stuffed_zucchini', 'oil', 6, 'Salt, pepper, and oil', false),

    ('tuna_salad', 'tuna', 1, '1 can tuna', true),
    ('tuna_salad', 'tomato', 2, '2 tomatoes', true),
    ('tuna_salad', 'cucumber', 3, '1 cucumber', true),
    ('tuna_salad', 'corn', 4, '100 g corn', true),
    ('tuna_salad', 'lemon', 5, '1/2 lemon', true),
    ('tuna_salad', 'oil', 6, 'Salt, pepper, and olive oil', false),

    ('shakshuka', 'eggs', 1, '4 eggs', true),
    ('shakshuka', 'tomato', 2, '400 g tomatoes', true),
    ('shakshuka', 'pepper', 3, '1 bell pepper', true),
    ('shakshuka', 'onion', 4, '1 onion', true),
    ('shakshuka', 'garlic', 5, '1 garlic clove', true),
    ('shakshuka', 'oil', 6, 'Salt, pepper, paprika, and oil', false),

    ('potato_stew', 'potato', 1, '800 g potatoes', true),
    ('potato_stew', 'onion', 2, '1 large onion', true),
    ('potato_stew', 'carrot', 3, '2 carrots', true),
    ('potato_stew', 'pepper', 4, '1 bell pepper', true),
    ('potato_stew', 'water', 5, '1 tsp paprika, salt, pepper, oil, and water', false),

    ('chicken_wrap', 'tortilla', 1, '4 tortillas', true),
    ('chicken_wrap', 'chicken', 2, '300 g chicken breast', true),
    ('chicken_wrap', 'pepper', 3, '1 bell pepper', true),
    ('chicken_wrap', 'tomato', 4, '1 tomato', true),
    ('chicken_wrap', 'yogurt', 5, '120 ml yogurt', true),
    ('chicken_wrap', 'oil', 6, 'Salt, pepper, paprika, and oil', false),

    ('mushroom_toast', 'bread', 1, '4 slices bread', true),
    ('mushroom_toast', 'mushrooms', 2, '250 g mushrooms', true),
    ('mushroom_toast', 'garlic', 3, '1 garlic clove', true),
    ('mushroom_toast', 'cheese', 4, '80 g cheese', true),
    ('mushroom_toast', 'oil', 5, 'Salt, pepper, and a little oil', false),

    ('spinach_eggs', 'eggs', 1, '3 eggs', true),
    ('spinach_eggs', 'spinach', 2, '150 g spinach', true),
    ('spinach_eggs', 'cheese', 3, '40 g cheese', true),
    ('spinach_eggs', 'garlic', 4, '1 garlic clove', true),
    ('spinach_eggs', 'oil', 5, 'Salt, pepper, and a little oil', false),

    ('bean_salad', 'beans', 1, '300 g cooked or canned beans', true),
    ('bean_salad', 'onion', 2, '1/2 onion', true),
    ('bean_salad', 'pepper', 3, '1 bell pepper', true),
    ('bean_salad', 'tomato', 4, '1 tomato', true),
    ('bean_salad', 'lemon', 5, '1/2 lemon', true),
    ('bean_salad', 'oil', 6, 'Salt, pepper, and oil', false)
) as v(recipe_slug, ingredient_slug, position, display_text, required_for_match)
join public.recipes r on r.slug = v.recipe_slug
join public.ingredients i on i.slug = v.ingredient_slug;

delete from public.recipe_requirements
where recipe_id in (select id from public.recipes where slug in (
    'chicken_risotto', 'veggie_omelette', 'tomato_pasta', 'fried_rice',
    'potato_frittata', 'chicken_pasta', 'stuffed_zucchini', 'tuna_salad',
    'shakshuka', 'potato_stew', 'chicken_wrap', 'mushroom_toast',
    'spinach_eggs', 'bean_salad'
));

insert into public.recipe_requirements (recipe_id, ingredient_id)
select distinct ri.recipe_id, ri.ingredient_id
from public.recipe_ingredients ri
join public.recipes r on r.id = ri.recipe_id
where ri.required_for_match
  and r.slug in (
      'chicken_risotto', 'veggie_omelette', 'tomato_pasta', 'fried_rice',
      'potato_frittata', 'chicken_pasta', 'stuffed_zucchini', 'tuna_salad',
      'shakshuka', 'potato_stew', 'chicken_wrap', 'mushroom_toast',
      'spinach_eggs', 'bean_salad'
  );

delete from public.recipe_steps
where recipe_id in (select id from public.recipes where slug in (
    'chicken_risotto', 'veggie_omelette', 'tomato_pasta', 'fried_rice',
    'potato_frittata', 'chicken_pasta', 'stuffed_zucchini', 'tuna_salad',
    'shakshuka', 'potato_stew', 'chicken_wrap', 'mushroom_toast',
    'spinach_eggs', 'bean_salad'
));

insert into public.recipe_steps (recipe_id, step_number, instruction)
select r.id, v.step_number, v.instruction
from (values
    ('chicken_risotto', 1, 'Cut the chicken and vegetables into bite-sized pieces.'),
    ('chicken_risotto', 2, 'Sauté the onion in oil, add the chicken, and cook until golden.'),
    ('chicken_risotto', 3, 'Add the carrot, rice, and warm stock. Simmer for about 18 minutes.'),
    ('chicken_risotto', 4, 'Stir in the peas, season, cover, and rest for another 5 minutes.'),
    ('veggie_omelette', 1, 'Whisk the eggs with a pinch of salt and pepper.'),
    ('veggie_omelette', 2, 'Briefly sauté the chopped bell pepper and tomato.'),
    ('veggie_omelette', 3, 'Pour in the eggs, add the cheese, and cook over low heat for 4–5 minutes.'),
    ('tomato_pasta', 1, 'Cook the pasta according to the package directions and reserve some pasta water.'),
    ('tomato_pasta', 2, 'Sauté the onion and garlic, then add the tomatoes and seasoning.'),
    ('tomato_pasta', 3, 'Simmer the sauce for 10 minutes, stir in the pasta, and loosen with pasta water if needed.'),
    ('fried_rice', 1, 'Sauté the onion, carrot, and peas in a hot pan.'),
    ('fried_rice', 2, 'Move the vegetables aside, add the beaten eggs, and scramble briefly.'),
    ('fried_rice', 3, 'Add the cold rice, mix everything together, and fry for another 4–5 minutes.'),
    ('potato_frittata', 1, 'Slice the potatoes thinly and boil for 8 minutes.'),
    ('potato_frittata', 2, 'Sauté the onion, add the drained potatoes, and cook until lightly golden.'),
    ('potato_frittata', 3, 'Pour in the beaten eggs, sprinkle with cheese, cover, and cook for 12 minutes.'),
    ('chicken_pasta', 1, 'Cook the pasta and reserve half a cup of pasta water.'),
    ('chicken_pasta', 2, 'Season the sliced chicken and cook it in a pan until golden.'),
    ('chicken_pasta', 3, 'Lower the heat, add the garlic, yogurt, and a splash of pasta water.'),
    ('chicken_pasta', 4, 'Stir in the pasta and warm gently without bringing the sauce to a hard boil.'),
    ('stuffed_zucchini', 1, 'Halve the zucchini and scoop out the centers with a spoon.'),
    ('stuffed_zucchini', 2, 'Sauté the onion and beef, then add the rinsed rice and seasoning.'),
    ('stuffed_zucchini', 3, 'Fill the zucchini, pour over the tomato sauce, and cover with foil.'),
    ('stuffed_zucchini', 4, 'Bake for 35 minutes at 200 °C, uncover, and bake for 10 more minutes.'),
    ('tuna_salad', 1, 'Drain the tuna and corn.'),
    ('tuna_salad', 2, 'Chop the tomatoes and cucumber, then combine everything in a bowl.'),
    ('tuna_salad', 3, 'Season with lemon juice, olive oil, salt, and pepper.'),
    ('shakshuka', 1, 'Sauté the onion and bell pepper until soft.'),
    ('shakshuka', 2, 'Add the garlic, tomatoes, and seasoning, then simmer for 12 minutes.'),
    ('shakshuka', 3, 'Make four wells, add the eggs, cover, and cook for another 6–8 minutes.'),
    ('potato_stew', 1, 'Soften the onion, carrots, and bell pepper in oil.'),
    ('potato_stew', 2, 'Stir in the paprika, then add the diced potatoes.'),
    ('potato_stew', 3, 'Cover with water, season, and simmer for 30 minutes.'),
    ('chicken_wrap', 1, 'Slice the chicken into strips, season, and cook until golden.'),
    ('chicken_wrap', 2, 'Chop the vegetables and lightly season the yogurt with salt.'),
    ('chicken_wrap', 3, 'Warm the tortillas, add the fillings, and roll them up tightly.'),
    ('mushroom_toast', 1, 'Slice the mushrooms and cook in a hot pan until their moisture evaporates.'),
    ('mushroom_toast', 2, 'Add the garlic and seasoning, then remove from the heat.'),
    ('mushroom_toast', 3, 'Top the bread with mushrooms and cheese, then bake for 6–8 minutes at 200 °C.'),
    ('spinach_eggs', 1, 'Warm the garlic briefly in oil and add the spinach.'),
    ('spinach_eggs', 2, 'Once the spinach wilts, add the beaten eggs.'),
    ('spinach_eggs', 3, 'Stir over low heat, sprinkle with cheese, and serve immediately.'),
    ('bean_salad', 1, 'Rinse and drain the beans well.'),
    ('bean_salad', 2, 'Finely chop the onion, bell pepper, and tomato.'),
    ('bean_salad', 3, 'Combine everything and season with lemon juice, oil, salt, and pepper.')
) as v(recipe_slug, step_number, instruction)
join public.recipes r on r.slug = v.recipe_slug;

insert into private.recipe_sources
    (recipe_id, source_key, source_type, source_name, source_url, license_name, attribution, editorial_notes, reviewed_at)
select
    r.id,
    r.slug || ':dimaso-original',
    'original',
    'Dimaso original recipe',
    'https://github.com/dimaso-doo/what-to-cook-android',
    'All rights reserved',
    'Dimaso d.o.o.',
    'Original English starter content prepared for the What to Cook application.',
    now()
from public.recipes r
where r.slug in (
    'chicken_risotto', 'veggie_omelette', 'tomato_pasta', 'fried_rice',
    'potato_frittata', 'chicken_pasta', 'stuffed_zucchini', 'tuna_salad',
    'shakshuka', 'potato_stew', 'chicken_wrap', 'mushroom_toast',
    'spinach_eggs', 'bean_salad'
)
on conflict (source_key) do update set
    source_name = excluded.source_name,
    source_url = excluded.source_url,
    license_name = excluded.license_name,
    attribution = excluded.attribution,
    editorial_notes = excluded.editorial_notes,
    reviewed_at = excluded.reviewed_at;
