# What to Cook?

A lightweight native Android app that suggests meals based on ingredients the user already has.

The app interface and all recipe content are in English.

## Features

- ingredient selection and search without entering quantities;
- only recipes whose required ingredients are all available;
- recipe details with the full ingredient list, quantities, cooking time, and preparation steps;
- a Supabase recipe catalogue with exact required-ingredient matching;
- ten popular ingredients on the starting panel and searchable suggestions for the full catalogue;
- 84 online recipes with validated required ingredients and recorded provenance;
- an offline fallback with 14 recipes when the network is unavailable;
- no user account required.

## Supabase

The production-safe schema and starter data are versioned in `supabase/schema.sql` and
`supabase/seed.sql`. The Android client uses only the public read-only key. Row Level
Security prevents anonymous clients from adding, changing, or deleting catalogue data.

## Recipe import

The repeatable Wikibooks importer accepts only recipes with parseable time and servings,
structured ingredient and procedure sections, and two to fifteen exact-match ingredients:

```bash
python3 scripts/import_wikibooks.py --limit 70 --batch-size 14
```

It writes the reviewed source snapshot to `data/wikibooks_recipes.json` and generates
five idempotent SQL batches under `supabase/generated/`. Imported text is CC BY-SA 4.0;
full attribution and reuse details are in `RECIPES_LICENSE.md`.

## Build and run

Open the project directly in Android Studio, or build it from the terminal:

```bash
./gradlew assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

Run the automated tests with:

```bash
./gradlew testDebugUnitTest
```

The minimum supported version is Android 7.0 (API 24), and the project targets Android 16 (API 36).

Google Play application ID: `com.dimaso.whattocook`.

Privacy policy: <https://dimaso-doo.github.io/what-to-cook-privacy/index.html>
