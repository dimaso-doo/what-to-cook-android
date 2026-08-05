# What to Cook?

A lightweight native Android app that suggests meals based on ingredients the user already has.

The app interface and all recipe content are in English.

## Features

- ingredient selection and search without entering quantities;
- recipe ranking based on available ingredients;
- a clear overview of missing ingredients;
- recipe details with quantities, cooking time, and preparation steps;
- an offline starter database with 14 recipes and 24 ingredients;
- no user account or internet connection required.

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
