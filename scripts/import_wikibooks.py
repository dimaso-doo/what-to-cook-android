#!/usr/bin/env python3
"""Build a validated, attributed recipe catalogue from Wikibooks Cookbook.

The script deliberately imports text only. It does not download recipe images.
Every accepted recipe must expose structured ingredients and procedure sections,
parseable time and serving data, and at least two exact-match requirements.
"""

from __future__ import annotations

import argparse
import hashlib
import html
import json
import re
import subprocess
import time
import unicodedata
import urllib.parse
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


API_URL = "https://en.wikibooks.org/w/api.php"
USER_AGENT = (
    "WhatToCookImporter/1.0 "
    "(https://github.com/dimaso-doo/what-to-cook-android; "
    "contact: predragstojanovic1@gmail.com)"
)
LICENSE_NAME = "CC BY-SA 4.0"
ATTRIBUTION = "Wikibooks contributors (see linked page history)"
CATEGORIES = (
    "Recipes",
    "Featured recipes",
    "Main course recipes",
    "Recipes for salad",
    "Recipes for sandwiches",
    "Recipes for soup",
    "Recipes for stew",
    "Recipes for curry",
    "Recipes for casserole",
    "Recipes for fried rice",
    "Recipes for risotto",
    "Recipes for biryani",
    "Recipes for burritos",
    "Recipes for tacos",
    "Recipes using beef",
    "Recipes using chicken",
    "Recipes using pork",
    "Recipes using fish",
    "Vegetarian recipes",
    "Bean recipes",
    "Recipes using egg",
    "Recipes using meat",
    "Recipes using pasta",
    "Recipes using pasta and noodles",
    "Recipes using seafood",
    "Recipes using vegetables",
)
PROCEDURE_HEADINGS = ("procedure", "directions", "preparation", "method", "instructions")
TITLE_EXCLUSIONS = (
    "brownie", "cake", "candy", "chimichurri", "condiment", "cookie", "crème anglaise",
    "dessert", "dip", "dressing", "drink", "grenadine", "jelly",
    "muffin", "pancake", "pie", "pickle", "pudding", "sauce", "smoothie", "soda",
    "syrup", "tart", "tea", "waffle",
)
PANTRY_SLUGS = {
    "salt",
    "black_pepper",
    "water",
    "oil",
    "cooking_oil",
    "vegetable_oil",
    "olive_oil",
}
NON_INGREDIENT_LINKS = {
    "c", "cup", "cups", "deciliter", "decilitre", "dessertspoon", "fluid ounce",
    "g", "gallon", "gram", "grams", "kg", "kilogram", "l", "liter", "litre",
    "ml", "milliliter", "millilitre", "ounce", "ounces", "pinch", "pound",
    "quart", "tablespoon", "tablespoons", "teaspoon", "teaspoons",
    "baking", "blanching", "boiling", "braising", "chopping", "dicing", "frying",
    "grating", "grilling", "kneading", "marinating", "mincing", "peeling",
    "poaching", "roasting", "sautéing", "sauteing", "simmering", "slicing",
    "steaming", "stir-frying", "toasting",
    "baking dish", "baking sheet", "blender", "bowl", "casserole dish", "colander",
    "frying pan", "grater", "griddle", "knife", "oven", "pan", "pot", "saucepan",
    "skillet", "wok",
    "centimetre", "centimeter", "dl", "each", "inch", "pint", "tsp",
    "chop", "cube", "cubing", "dice", "garnish", "gluten free", "julienne", "mince",
    "parboiling", "scalding", "separate", "shredding", "smoking", "whipping", "zest",
    "deep fat frying",
    "meat and poultry", "tomatoes with cheese stuffing",
}
ALIASES = {
    "egg": ("eggs", "Eggs"),
    "eggs": ("eggs", "Eggs"),
    "egg white": ("eggs", "Eggs"),
    "egg whites": ("eggs", "Eggs"),
    "egg yolk": ("eggs", "Eggs"),
    "egg yolks": ("eggs", "Eggs"),
    "chicken": ("chicken", "Chicken"),
    "chicken breast": ("chicken", "Chicken"),
    "chicken thigh": ("chicken", "Chicken"),
    "chicken tikka": ("chicken", "Chicken"),
    "beef": ("beef", "Beef"),
    "ground beef": ("ground_meat", "Ground beef"),
    "minced beef": ("ground_meat", "Ground beef"),
    "ground meat": ("ground_meat", "Ground beef"),
    "pork": ("pork", "Pork"),
    "lamb": ("lamb", "Lamb"),
    "fish": ("fish", "Fish"),
    "rice": ("rice", "Rice"),
    "pasta": ("pasta", "Pasta"),
    "egg noodles": ("pasta", "Pasta"),
    "macaroni": ("pasta", "Pasta"),
    "noodle": ("pasta", "Pasta"),
    "noodles": ("pasta", "Pasta"),
    "spaghetti": ("pasta", "Pasta"),
    "ziti": ("pasta", "Pasta"),
    "all purpose flour": ("flour", "Flour"),
    "all-purpose flour": ("flour", "Flour"),
    "wheat flour": ("flour", "Flour"),
    "potato": ("potato", "Potatoes"),
    "potatoes": ("potato", "Potatoes"),
    "onion": ("onion", "Onion"),
    "onions": ("onion", "Onion"),
    "tomato": ("tomato", "Tomatoes"),
    "tomatoes": ("tomato", "Tomatoes"),
    "garlic": ("garlic", "Garlic"),
    "cheese": ("cheese", "Cheese"),
    "bell pepper": ("pepper", "Bell pepper"),
    "bell peppers": ("pepper", "Bell pepper"),
    "capsicum": ("pepper", "Bell pepper"),
    "pepper": ("black_pepper", "Black pepper"),
    "mushroom": ("mushrooms", "Mushrooms"),
    "mushrooms": ("mushrooms", "Mushrooms"),
    "bean": ("beans", "Beans"),
    "beans": ("beans", "Beans"),
    "yoghurt": ("yogurt", "Yogurt"),
    "yogurt": ("yogurt", "Yogurt"),
    "vegetable oil": ("oil", "Cooking oil"),
    "cooking oil": ("oil", "Cooking oil"),
    "olive oil": ("oil", "Cooking oil"),
    "water": ("water", "Water"),
    "stock": ("stock", "Stock"),
    "broth": ("stock", "Stock"),
    "broth and stock": ("stock", "Stock"),
    "beef broth": ("stock", "Stock"),
    "chicken broth": ("stock", "Stock"),
    "vegetable broth": ("stock", "Stock"),
    "bouillon cube": ("stock", "Stock"),
    "dehydrated broth": ("stock", "Stock"),
    "oil and fat": ("oil", "Cooking oil"),
    "cooking spray": ("oil", "Cooking oil"),
    "black pepper": ("black_pepper", "Black pepper"),
}


@dataclass(frozen=True)
class Ingredient:
    slug: str
    name: str
    emoji: str
    category: str


def api(params: dict[str, str]) -> dict:
    query = dict(params)
    query.update({"format": "json", "formatversion": "2", "maxlag": "5"})
    url = API_URL + "?" + urllib.parse.urlencode(query)
    completed = subprocess.run(
        ["curl", "-fsS", "--retry", "3", "--retry-delay", "1", "-A", USER_AGENT, url],
        check=True,
        capture_output=True,
        text=True,
    )
    time.sleep(0.08)
    return json.loads(completed.stdout)


def category_members(category: str) -> list[str]:
    titles: list[str] = []
    continuation: str | None = None
    while True:
        params = {
            "action": "query",
            "list": "categorymembers",
            "cmtitle": "Category:" + category,
            "cmtype": "page",
            "cmnamespace": "102",
            "cmlimit": "500",
        }
        if continuation:
            params["cmcontinue"] = continuation
        data = api(params)
        titles.extend(row["title"] for row in data.get("query", {}).get("categorymembers", []))
        continuation = data.get("continue", {}).get("cmcontinue")
        if not continuation:
            return titles


def chunks(values: list[str], size: int) -> Iterable[list[str]]:
    for start in range(0, len(values), size):
        yield values[start:start + size]


def fetch_pages(titles: list[str]) -> Iterable[dict]:
    for title_batch in chunks(titles, 20):
        data = api({
            "action": "query",
            "prop": "revisions",
            "rvprop": "content|ids",
            "rvslots": "main",
            "titles": "|".join(title_batch),
        })
        for page in data.get("query", {}).get("pages", []):
            revisions = page.get("revisions") or []
            if not revisions:
                continue
            revision = revisions[0]
            content = revision.get("slots", {}).get("main", {}).get("content", "")
            yield {
                "title": page["title"],
                "revision_id": revision["revid"],
                "wikitext": content,
            }


def balanced_template(text: str, name: str) -> str:
    match = re.search(r"\{\{\s*" + re.escape(name) + r"\b", text, re.I)
    if not match:
        return ""
    depth = 0
    index = match.start()
    while index < len(text) - 1:
        pair = text[index:index + 2]
        if pair == "{{":
            depth += 1
            index += 2
            continue
        if pair == "}}":
            depth -= 1
            index += 2
            if depth == 0:
                return text[match.start():index]
            continue
        index += 1
    return ""


def summary_fields(wikitext: str) -> dict[str, str]:
    template = balanced_template(wikitext, "recipesummary")
    fields: dict[str, str] = {}
    for key, value in re.findall(r"^\s*\|\s*([^=\n]+?)\s*=\s*(.*?)\s*$", template, re.M):
        fields[key.strip().lower()] = value.strip()
    return fields


def section(wikitext: str, names: Iterable[str]) -> str:
    wanted = {name.lower() for name in names}
    matches = list(re.finditer(r"^==\s*([^=\n]+?)\s*==\s*$", wikitext, re.M))
    for index, match in enumerate(matches):
        if match.group(1).strip().lower() not in wanted:
            continue
        end = matches[index + 1].start() if index + 1 < len(matches) else len(wikitext)
        return wikitext[match.end():end]
    return ""


def replace_templates(text: str) -> str:
    def fraction(match: re.Match[str]) -> str:
        parts = [part.strip() for part in match.group(1).split("|") if part.strip()]
        return "/".join(parts[-2:]) if len(parts) >= 2 else ""

    text = re.sub(r"\{\{\s*(?:frac|fraction)\s*\|([^{}]+)\}\}", fraction, text, flags=re.I)
    for _ in range(6):
        updated = re.sub(r"\{\{[^{}]*\}\}", "", text)
        if updated == text:
            break
        text = updated
    return text


def clean_wikitext(value: str) -> str:
    value = re.sub(r"<!--.*?-->", "", value, flags=re.S)
    value = re.sub(r"<ref\b[^>]*>.*?</ref\s*>", "", value, flags=re.I | re.S)
    value = re.sub(r"<ref\b[^>]*/\s*>", "", value, flags=re.I)
    value = replace_templates(value)
    value = re.sub(r"\[\[[^\]|]+\|([^\]]+)\]\]", r"\1", value)
    value = re.sub(r"\[\[(?:Cookbook:)?([^\]]+)\]\]", r"\1", value)
    value = re.sub(r"\[https?://\S+\s+([^\]]+)\]", r"\1", value)
    value = re.sub(r"\[https?://[^\]]+\]", "", value)
    value = re.sub(r"</?[^>]+>", " ", value)
    value = value.replace("'''", "").replace("''", "")
    value = html.unescape(value)
    value = re.sub(r"\s+", " ", value)
    return value.strip(" -*#;:")


def slugify(value: str) -> str:
    normalized = unicodedata.normalize("NFKD", value).encode("ascii", "ignore").decode()
    normalized = normalized.lower().replace("&", " and ")
    return re.sub(r"[^a-z0-9]+", "_", normalized).strip("_")[:72]


def canonical_ingredient(target: str) -> Ingredient | None:
    target = re.sub(r"\s*\([^)]*\)\s*$", "", target).strip()
    key = target.lower().replace("_", " ").strip()
    if key in NON_INGREDIENT_LINKS:
        return None
    if key.startswith("cuisine of ") or key.endswith(" recipes") or key.startswith("recipe"):
        return None
    if key in ALIASES:
        slug, name = ALIASES[key]
    else:
        slug = slugify(target)
        name = target.strip().title()
    if not slug or len(slug) < 2:
        return None
    category, emoji = ingredient_style(slug + " " + name.lower())
    return Ingredient(slug, name, emoji, category)


def ingredient_style(value: str) -> tuple[str, str]:
    rules = (
        (("chicken",), ("Protein", "🍗")),
        (("beef", "meat", "pork", "lamb", "bacon", "sausage"), ("Protein", "🥩")),
        (("fish", "salmon", "tuna", "shrimp", "prawn", "cod"), ("Protein", "🐟")),
        (("egg",), ("Protein", "🥚")),
        (("cheese",), ("Dairy", "🧀")),
        (("milk", "cream", "yogurt", "butter"), ("Dairy", "🥛")),
        (("rice",), ("Grains", "🍚")),
        (("pasta", "noodle", "spaghetti"), ("Grains", "🍝")),
        (("bread", "flour", "oat", "barley", "couscous"), ("Grains", "🍞")),
        (("potato",), ("Vegetables", "🥔")),
        (("tomato",), ("Vegetables", "🍅")),
        (("onion",), ("Vegetables", "🧅")),
        (("garlic",), ("Vegetables", "🧄")),
        (("pepper", "chile", "chili"), ("Vegetables", "🫑")),
        (("carrot",), ("Vegetables", "🥕")),
        (("mushroom",), ("Vegetables", "🍄")),
        (("spinach", "cabbage", "lettuce", "kale"), ("Vegetables", "🥬")),
        (("lemon",), ("Fruit", "🍋")),
        (("apple", "pear", "orange", "lime", "banana", "fruit"), ("Fruit", "🍎")),
        (("salt", "oil", "water", "stock", "vinegar"), ("Pantry", "🧂")),
        (("spice", "cumin", "paprika", "turmeric", "cinnamon", "herb"), ("Herbs and spices", "🌿")),
    )
    for needles, result in rules:
        if any(needle in value for needle in needles):
            return result
    return "Other", "•"


def ingredient_candidates(line: str) -> list[Ingredient]:
    candidates: list[Ingredient] = []
    seen: set[str] = set()
    for target in re.findall(r"\[\[\s*Cookbook:([^\]|#]+)", line, re.I):
        ingredient = canonical_ingredient(target)
        if ingredient and ingredient.slug not in seen:
            seen.add(ingredient.slug)
            candidates.append(ingredient)
    return candidates


def parse_minutes(value: str) -> int | None:
    cleaned = clean_wikitext(value).lower()
    cleaned = cleaned.replace("½", ".5").replace("¼", ".25").replace("¾", ".75")
    total = 0.0
    for number, unit in re.findall(r"(\d+(?:\.\d+)?)\s*(hours?|hrs?|h|minutes?|mins?|m)\b", cleaned):
        total += float(number) * (60 if unit.startswith("h") else 1)
    if total == 0:
        simple = re.search(r"\b(\d{1,3})\b", cleaned)
        total = float(simple.group(1)) if simple else 0
    rounded = int(round(total))
    return rounded if 5 <= rounded <= 240 else None


def parse_servings(value: str) -> int | None:
    cleaned = clean_wikitext(value).lower()
    match = re.search(r"\b(\d{1,2})\b", cleaned)
    if not match:
        return None
    servings = int(match.group(1))
    return servings if 1 <= servings <= 20 else None


def parse_difficulty(value: str) -> str:
    cleaned = clean_wikitext(value).lower()
    match = re.search(r"\b([1-5])\b", cleaned)
    if match:
        return {"1": "Very easy", "2": "Easy", "3": "Medium", "4": "Advanced", "5": "Advanced"}[match.group(1)]
    if "very easy" in cleaned:
        return "Very easy"
    if "advanced" in cleaned or "hard" in cleaned:
        return "Advanced"
    if "medium" in cleaned or "moderate" in cleaned:
        return "Medium"
    return "Easy"


def description_from(wikitext: str, title: str) -> str:
    prefix = re.split(r"^==\s*Ingredients\s*==\s*$", wikitext, maxsplit=1, flags=re.I | re.M)[0]
    prefix = re.sub(r"\{\{.*?\}\}", "", prefix, flags=re.S)
    paragraphs = [clean_wikitext(part) for part in re.split(r"\n\s*\n", prefix)]
    paragraphs = [part for part in paragraphs if len(part) >= 25 and not part.startswith("Category:")]
    description = paragraphs[-1] if paragraphs else f"A practical recipe for {title}, adapted from the Wikibooks Cookbook."
    description = re.sub(r"\[[0-9]+\]", "", description)
    if len(description) > 220:
        description = description[:217].rsplit(" ", 1)[0] + "..."
    return description


def recipe_emoji(title: str, requirements: list[str]) -> str:
    value = (title + " " + " ".join(requirements)).lower()
    for needle, emoji in (
        ("salad", "🥗"), ("soup", "🥣"), ("stew", "🍲"), ("curry", "🍛"),
        ("pasta", "🍝"), ("noodle", "🍜"), ("rice", "🍚"), ("chicken", "🍗"),
        ("fish", "🐟"), ("beef", "🥩"), ("sandwich", "🥪"), ("bread", "🍞"),
        ("potato", "🥔"), ("egg", "🍳"),
    ):
        if needle in value:
            return emoji
    return "🍽️"


def parse_recipe(page: dict) -> tuple[dict | None, str | None]:
    wikitext = page["wikitext"]
    title = page["title"].removeprefix("Cookbook:").strip()
    if any(token in title.lower() for token in TITLE_EXCLUSIONS):
        return None, "not a meal recipe"
    fields = summary_fields(wikitext)
    minutes = parse_minutes(fields.get("time", ""))
    servings = parse_servings(fields.get("servings", fields.get("yield", "")))
    if minutes is None or servings is None:
        return None, "missing parseable time or servings"

    ingredient_section = section(wikitext, ("ingredients",))
    procedure_section = section(wikitext, PROCEDURE_HEADINGS)
    raw_lines = [line for line in ingredient_section.splitlines() if re.match(r"^\s*\*", line)]
    raw_steps = [line for line in procedure_section.splitlines() if re.match(r"^\s*#", line)]
    if not (2 <= len(raw_lines) <= 24 and 2 <= len(raw_steps) <= 16):
        return None, "ingredient or step count outside quality bounds"

    all_ingredients: dict[str, Ingredient] = {}
    ingredient_lines: list[dict] = []
    requirements: set[str] = set()
    for position, raw_line in enumerate(raw_lines, 1):
        line = clean_wikitext(raw_line)
        candidates = ingredient_candidates(raw_line)
        if len(line) < 3 or not candidates:
            return None, "ingredient line without an identifiable Cookbook ingredient link"
        optional = "optional" in line.lower()
        for ingredient in candidates:
            all_ingredients[ingredient.slug] = ingredient
            if not optional and ingredient.slug not in PANTRY_SLUGS:
                requirements.add(ingredient.slug)
        ingredient_lines.append({
            "position": position,
            "display_text": line,
            "ingredient_slug": candidates[0].slug,
            "required_for_match": not optional and candidates[0].slug not in PANTRY_SLUGS,
            "optional": optional,
        })

    if not 2 <= len(requirements) <= 15:
        return None, "exact-match requirement count outside quality bounds"

    steps = [clean_wikitext(line) for line in raw_steps]
    if any(len(step) < 8 for step in steps):
        return None, "procedure contains an incomplete step"

    slug = "wikibooks_" + slugify(title)
    if len(slug) > 80:
        return None, "recipe slug too long"
    source_url = "https://en.wikibooks.org/w/index.php?" + urllib.parse.urlencode({
        "title": page["title"],
        "oldid": str(page["revision_id"]),
    })
    requirement_list = sorted(requirements)
    return {
        "slug": slug,
        "title": title,
        "emoji": recipe_emoji(title, requirement_list),
        "description": description_from(wikitext, title),
        "total_minutes": minutes,
        "difficulty": parse_difficulty(fields.get("difficulty", "")),
        "servings": servings,
        "source_name": "Wikibooks Cookbook",
        "source_url": source_url,
        "source_revision_id": page["revision_id"],
        "license_name": LICENSE_NAME,
        "attribution": ATTRIBUTION,
        "modified_from_source": True,
        "ingredients": [ingredient.__dict__ for ingredient in sorted(all_ingredients.values(), key=lambda item: item.slug)],
        "ingredient_lines": ingredient_lines,
        "requirements": requirement_list,
        "steps": steps,
    }, None


def sql_string(value: str | None) -> str:
    if value is None:
        return "null"
    return "'" + value.replace("'", "''") + "'"


def bool_sql(value: bool) -> str:
    return "true" if value else "false"


def values_block(rows: list[tuple]) -> str:
    return ",\n".join("    (" + ", ".join(str(value) for value in row) + ")" for row in rows)


def generate_sql(recipes: list[dict], batch_number: int) -> str:
    ingredient_map: dict[str, dict] = {}
    for recipe in recipes:
        for ingredient in recipe["ingredients"]:
            ingredient_map[ingredient["slug"]] = ingredient

    ingredient_rows = [
        (
            sql_string(item["slug"]), sql_string(item["name"]), sql_string(item["emoji"]),
            sql_string(item["category"]), "false", "100",
        )
        for item in sorted(ingredient_map.values(), key=lambda row: row["slug"])
    ]
    recipe_rows = [
        (
            sql_string(item["slug"]), sql_string(item["title"]), sql_string(item["emoji"]),
            sql_string(item["description"]), str(item["total_minutes"]),
            sql_string(item["difficulty"]), str(item["servings"]), "true",
            sql_string(item["source_name"]), sql_string(item["source_url"]),
            sql_string(item["license_name"]), sql_string(item["attribution"]), "true",
        )
        for item in recipes
    ]
    slugs_sql = ", ".join(sql_string(item["slug"]) for item in recipes)

    line_rows: list[tuple] = []
    requirement_rows: list[tuple] = []
    step_rows: list[tuple] = []
    source_rows: list[tuple] = []
    for recipe in recipes:
        for line in recipe["ingredient_lines"]:
            line_rows.append((
                sql_string(recipe["slug"]), sql_string(line["ingredient_slug"]),
                str(line["position"]), sql_string(line["display_text"]),
                bool_sql(line["required_for_match"]), bool_sql(line["optional"]),
            ))
        for ingredient_slug in recipe["requirements"]:
            requirement_rows.append((sql_string(recipe["slug"]), sql_string(ingredient_slug)))
        for index, instruction in enumerate(recipe["steps"], 1):
            step_rows.append((sql_string(recipe["slug"]), str(index), sql_string(instruction)))
        source_rows.append((
            sql_string(recipe["slug"]),
            sql_string(f"{recipe['slug']}:wikibooks:{recipe['source_revision_id']}"),
            sql_string(recipe["source_name"]), sql_string(recipe["source_url"]),
            sql_string(recipe["license_name"]), sql_string(recipe["attribution"]),
        ))

    return f"""-- Generated Wikibooks import batch {batch_number:03d}. Do not edit by hand.

insert into public.ingredients (slug, name, emoji, category, is_featured, sort_order)
values
{values_block(ingredient_rows)}
on conflict (slug) do update set
    name = excluded.name,
    emoji = excluded.emoji,
    category = excluded.category,
    active = true,
    updated_at = now();

insert into public.recipes
    (slug, title, emoji, description, total_minutes, difficulty, servings, published,
     source_name, source_url, license_name, attribution, modified_from_source)
values
{values_block(recipe_rows)}
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

delete from public.recipe_requirements
where recipe_id in (select id from public.recipes where slug in ({slugs_sql}));
delete from public.recipe_ingredients
where recipe_id in (select id from public.recipes where slug in ({slugs_sql}));
delete from public.recipe_steps
where recipe_id in (select id from public.recipes where slug in ({slugs_sql}));
delete from private.recipe_sources
where recipe_id in (select id from public.recipes where slug in ({slugs_sql}));

insert into public.recipe_ingredients
    (recipe_id, ingredient_id, position, display_text, required_for_match, optional)
select r.id, i.id, v.position, v.display_text, v.required_for_match, v.optional
from (values
{values_block(line_rows)}
) as v(recipe_slug, ingredient_slug, position, display_text, required_for_match, optional)
join public.recipes r on r.slug = v.recipe_slug
join public.ingredients i on i.slug = v.ingredient_slug;

insert into public.recipe_requirements (recipe_id, ingredient_id)
select r.id, i.id
from (values
{values_block(requirement_rows)}
) as v(recipe_slug, ingredient_slug)
join public.recipes r on r.slug = v.recipe_slug
join public.ingredients i on i.slug = v.ingredient_slug;

insert into public.recipe_steps (recipe_id, step_number, instruction)
select r.id, v.step_number, v.instruction
from (values
{values_block(step_rows)}
) as v(recipe_slug, step_number, instruction)
join public.recipes r on r.slug = v.recipe_slug;

insert into private.recipe_sources
    (recipe_id, source_key, source_type, source_name, source_url, license_name,
     attribution, editorial_notes, reviewed_at)
select r.id, v.source_key, 'adapted', v.source_name, v.source_url, v.license_name,
       v.attribution, 'AI-assisted normalization with deterministic ingredient validation.', now()
from (values
{values_block(source_rows)}
) as v(recipe_slug, source_key, source_name, source_url, license_name, attribution)
join public.recipes r on r.slug = v.recipe_slug;
"""


def validate_catalog(recipes: list[dict]) -> None:
    slugs = [recipe["slug"] for recipe in recipes]
    if len(slugs) != len(set(slugs)):
        raise ValueError("Duplicate recipe slugs")
    for recipe in recipes:
        known = {item["slug"] for item in recipe["ingredients"]}
        if not set(recipe["requirements"]).issubset(known):
            raise ValueError(f"Unknown requirement in {recipe['slug']}")
        if any(not line["display_text"] for line in recipe["ingredient_lines"]):
            raise ValueError(f"Blank ingredient line in {recipe['slug']}")
        if any(not step for step in recipe["steps"]):
            raise ValueError(f"Blank step in {recipe['slug']}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--limit", type=int, default=100)
    parser.add_argument("--batch-size", type=int, default=20)
    parser.add_argument("--workspace", type=Path, default=Path(__file__).resolve().parents[1])
    args = parser.parse_args()

    candidates: set[str] = set()
    for category in CATEGORIES:
        candidates.update(category_members(category))
    ordered = sorted(
        candidates,
        key=lambda title: hashlib.sha256(title.encode("utf-8")).hexdigest(),
    )

    accepted: list[dict] = []
    rejected: dict[str, int] = {}
    for page in fetch_pages(ordered):
        recipe, reason = parse_recipe(page)
        if recipe:
            accepted.append(recipe)
            if len(accepted) >= args.limit:
                break
        else:
            rejected[reason or "unknown"] = rejected.get(reason or "unknown", 0) + 1

    if len(accepted) < args.limit:
        raise RuntimeError(f"Only {len(accepted)} recipes passed validation; requested {args.limit}")
    validate_catalog(accepted)

    data_dir = args.workspace / "data"
    generated_dir = args.workspace / "supabase" / "generated"
    data_dir.mkdir(parents=True, exist_ok=True)
    generated_dir.mkdir(parents=True, exist_ok=True)
    for old_file in generated_dir.glob("wikibooks_batch_*.sql"):
        old_file.unlink()

    payload = {
        "source": "Wikibooks Cookbook",
        "license": LICENSE_NAME,
        "attribution": ATTRIBUTION,
        "recipe_count": len(accepted),
        "rejected_summary": rejected,
        "recipes": accepted,
    }
    (data_dir / "wikibooks_recipes.json").write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    for index, batch in enumerate(chunks(accepted, args.batch_size), 1):
        (generated_dir / f"wikibooks_batch_{index:03d}.sql").write_text(
            generate_sql(batch, index),
            encoding="utf-8",
        )

    print(json.dumps({
        "candidate_pages": len(candidates),
        "accepted_recipes": len(accepted),
        "sql_batches": (len(accepted) + args.batch_size - 1) // args.batch_size,
        "rejected_summary": rejected,
    }, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
