# Smart Pantry Manager

Java Android application that stores a pantry on the device and suggests only
recipes for which every required ingredient and quantity is available.

## Database choice

SQLite via SQLiteOpenHelper. It works offline, needs no server, and persists
pantry data across app restarts. The schema is in `schema_sqlite.sql`; the
schema and 18 recipe seed records are created by `DatabaseHelper.onCreate()`.
MySQL is not one of the databases permitted by the assignment brief.

## Setup with an existing Android Studio project (recommended)

1. Keep your original `C:\AndroidSDK\Projects\smartpantry` folder and its
   `.git`, Gradle wrapper, and generated Gradle configuration.
2. Copy this package's `app/src/main` folder over the original project's
   `app/src/main` folder, replacing matching files. Copy `README.md` and
   `schema_sqlite.sql` into the original project root.
3. Check that the project package is `com.example.smartpantry` and that the
   project minimum SDK is at least API 24. Your existing API 24 setting works.
4. Sync Gradle, run on an emulator or Android phone, and test the features.
5. Make honest Git commits as you integrate and test each feature; do not
   fabricate a development timeline or claim a feature was written earlier.

## Standalone import

Open this folder in Android Studio. Its Gradle files are examples. This ZIP
cannot include a Gradle wrapper JAR from the local build environment; use
Android Studio's installed Gradle support or transfer the `gradle/wrapper`
folder and `gradlew` files from your original working project if prompted.

## Add your own recipe

Open **Browse all recipes** from the pantry or suggestions screen, then tap
**Add my recipe**. Enter a unique recipe name, preparation steps, and every
required ingredient with its quantity and unit. Use **Add another ingredient**
for more requirements. The recipe is saved in SQLite immediately. It will
appear in the catalog regardless of pantry contents, and in Suggestions only
when all requirements are met. This version lets you add custom recipes; it
does not yet edit or delete recipes. Pantry items have full CRUD.

## Core logic

`DatabaseHelper.getSuggestions()` collects available pantry quantities by
normalized ingredient name and unit group. It converts kg to g and l to ml,
then checks every requirement of each recipe. A missing item or insufficient
quantity excludes the recipe. It never assumes unlisted pantry staples.

Supported unit groups: mass (g, kg), volume (ml, l), count (piece). It does
not convert between groups, such as a piece of tomato to grams of tomato.
Common plurals such as `tomatoes` and `eggs` are normalized, with simple `s` and `ies` handling for other names. The main screen has a bottom navigation bar for Suggestions, Recipes, and Settings. Almost There is a separate screen reached from Suggestions. It lists only recipes with exactly one deficient requirement and shows the additional amount needed; these recipes never appear as strict suggestions. Dates are shown
as reminders but expired items are not excluded from recipe matching; check
food safety before cooking.

## Manual checks

- Start with an empty pantry: suggestions show an empty-state message.
- Add 1 banana and 250 ml milk: Banana Milk appears.
- Change milk to 200 ml: Banana Milk disappears.
- Change milk to 0.25 l: Banana Milk reappears.
- Delete banana: Banana Milk disappears again.
- Add 2 eggs: Boiled Eggs appears; change to 1 egg and it disappears.
- Add, edit and delete an ingredient; restart the app and verify persistence.
- Add a custom two-ingredient recipe, restart, and confirm it remains in the catalog.
- Confirm the custom recipe is absent from Suggestions until both required quantities exist.
- With 1 banana and 200 ml milk, Banana Milk appears in Almost There needing 50 ml milk, but not in Suggestions. At 250 ml it moves to Suggestions and leaves Almost There.
- Enter zero quantity or malformed date to see form validation.
- Set an expiry date within three days and toggle the reminder in Settings.

## Submission evidence

Capture screenshots from the actual running app. Record your own narrated
5–7 minute video and explain the specific code you tested. The report needs
its own introduction, design diagrams, screenshots, code snippets, actual
challenges, reflection, references, and a link to the public GitHub repository.
