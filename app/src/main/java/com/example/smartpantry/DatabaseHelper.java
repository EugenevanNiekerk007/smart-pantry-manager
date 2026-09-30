package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create the pantry table to store ingredients the user has
        db.execSQL("CREATE TABLE pantry (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL CHECK(quantity > 0), " +
                "unit TEXT NOT NULL, " +
                "expiry TEXT)");

        // Create the main recipes table
        db.execSQL("CREATE TABLE recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "method TEXT NOT NULL)");

        // Store the ingredients required for each recipe
        db.execSQL("CREATE TABLE recipe_ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL CHECK(quantity > 0), " +
                "unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");

        // Add the starting recipes when the database is first created
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Future database changes can be added here if needed
    }

    // Add a new ingredient to the user's pantry
    public long addIngredient(String name, double quantity,
                              String unit, String expiry) {

        ContentValues values = pantryValues(
                name, quantity, unit, expiry
        );

        return getWritableDatabase()
                .insert("pantry", null, values);
    }

    // Update an ingredient that is already in the pantry
    public int updateIngredient(long id, String name,
                                double quantity, String unit,
                                String expiry) {

        return getWritableDatabase().update(
                "pantry",
                pantryValues(name, quantity, unit, expiry),
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    // Delete an ingredient using its database ID
    public int deleteIngredient(long id) {

        return getWritableDatabase().delete(
                "pantry",
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    // Put pantry information into ContentValues so it can be saved
    private ContentValues pantryValues(String name,
                                       double quantity,
                                       String unit,
                                       String expiry) {

        ContentValues values = new ContentValues();

        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit);
        values.put("expiry", expiry);

        return values;
    }

    // Get all ingredients currently stored in the pantry
    public List<Ingredient> getPantry() {

        List<Ingredient> list = new ArrayList<>();

        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,quantity,unit,COALESCE(expiry,'') " +
                        "FROM pantry ORDER BY name COLLATE NOCASE",
                null)) {

            while (c.moveToNext()) {

                list.add(new Ingredient(
                        c.getLong(0),
                        c.getString(1),
                        c.getDouble(2),
                        c.getString(3),
                        c.getString(4)
                ));
            }
        }

        return list;
    }

    // Find one pantry ingredient using its ID
    public Ingredient getIngredient(long id) {

        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,quantity,unit,COALESCE(expiry,'') " +
                        "FROM pantry WHERE id=?",
                new String[]{String.valueOf(id)})) {

            if (c.moveToFirst()) {

                return new Ingredient(
                        c.getLong(0),
                        c.getString(1),
                        c.getDouble(2),
                        c.getString(3),
                        c.getString(4)
                );
            }
        }

        return null;
    }

    // Save a new recipe and all of its required ingredients
    public long addRecipe(String name, String method,
                          List<Ingredient> requirements) {

        SQLiteDatabase db = getWritableDatabase();

        // Use a transaction so the recipe and ingredients save together
        db.beginTransaction();

        try {

            // Save the main recipe information
            ContentValues values = new ContentValues();

            values.put("name", name.trim());
            values.put("method", method.trim());

            long id = db.insertOrThrow(
                    "recipes",
                    null,
                    values
            );

            // Save each ingredient needed for the recipe
            for (Ingredient item : requirements) {

                ContentValues part = new ContentValues();

                part.put("recipe_id", id);
                part.put("name", item.name.trim());
                part.put("quantity", item.quantity);
                part.put("unit", item.unit);

                db.insertOrThrow(
                        "recipe_ingredients",
                        null,
                        part
                );
            }

            // Only complete the transaction if everything saved correctly
            db.setTransactionSuccessful();

            return id;

        } finally {

            db.endTransaction();
        }
    }

    // Get all recipes stored in the database
    public List<Recipe> getAllRecipes() {

        List<Recipe> recipes = new ArrayList<>();

        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,method FROM recipes ORDER BY name",
                null)) {

            while (c.moveToNext()) {

                recipes.add(new Recipe(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2)
                ));
            }
        }

        return recipes;
    }

    // Return zero if an ingredient does not exist in the map
    private static double quantityOrZero(
            Map<String, Double> quantities,
            String key) {

        Double amount = quantities.get(key);

        return amount == null ? 0.0 : amount;
    }

    // Find recipes that can be made using the current pantry
    public List<Recipe> getSuggestions() {

        // Store the total amount available for each ingredient
        Map<String, Double> available = new HashMap<>();

        for (Ingredient item : getPantry()) {

            // Normalize the ingredient and unit before comparing
            String key =
                    UnitConverter.normalize(item.name)
                            + "|"
                            + UnitConverter.group(item.unit);

            // Combine amounts if the same ingredient appears more than once
            available.put(
                    key,
                    quantityOrZero(available, key)
                            + UnitConverter.base(
                            item.quantity,
                            item.unit
                    )
            );
        }

        List<Recipe> matches = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Go through each recipe in the database
        try (Cursor recipes = db.rawQuery(
                "SELECT id,name,method FROM recipes ORDER BY name",
                null)) {

            while (recipes.moveToNext()) {

                long id = recipes.getLong(0);

                // Store everything required by the current recipe
                Map<String, Double> required = new HashMap<>();

                try (Cursor ingredients = db.rawQuery(
                        "SELECT name,quantity,unit " +
                                "FROM recipe_ingredients " +
                                "WHERE recipe_id=?",
                        new String[]{String.valueOf(id)})) {

                    while (ingredients.moveToNext()) {

                        String key =
                                UnitConverter.normalize(
                                        ingredients.getString(0))
                                        + "|"
                                        + UnitConverter.group(
                                        ingredients.getString(2));

                        required.put(
                                key,
                                quantityOrZero(required, key)
                                        + UnitConverter.base(
                                        ingredients.getDouble(1),
                                        ingredients.getString(2)
                                )
                        );
                    }
                }

                // Ignore a recipe if it has no ingredients
                if (required.isEmpty()) {
                    continue;
                }

                // Start by assuming all ingredients are available
                boolean allAvailable = true;

                // Check every ingredient required by the recipe
                for (Map.Entry<String, Double> needed
                        : required.entrySet()) {

                    // If there is not enough, the recipe cannot be suggested
                    if (quantityOrZero(
                            available,
                            needed.getKey())
                            + 0.000001
                            < needed.getValue()) {

                        allAvailable = false;
                        break;
                    }
                }

                // Only add recipes where every requirement is available
                if (allAvailable) {

                    matches.add(new Recipe(
                            id,
                            recipes.getString(1),
                            recipes.getString(2)
                    ));
                }
            }
        }

        return matches;
    }

    // Find recipes where the user is short of only one requirement
    public List<NearMatch> getAlmostThere() {

        // Build the list of ingredients currently available
        Map<String, Double> available = new HashMap<>();

        for (Ingredient item : getPantry()) {

            String key =
                    UnitConverter.normalize(item.name)
                            + "|"
                            + UnitConverter.group(item.unit);

            available.put(
                    key,
                    quantityOrZero(available, key)
                            + UnitConverter.base(
                            item.quantity,
                            item.unit
                    )
            );
        }

        List<NearMatch> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Check each recipe
        try (Cursor recipes = db.rawQuery(
                "SELECT id,name,method FROM recipes ORDER BY name",
                null)) {

            while (recipes.moveToNext()) {

                long recipeId = recipes.getLong(0);

                // Store required quantities, names and units
                Map<String, Double> required = new HashMap<>();
                Map<String, String> labels = new HashMap<>();
                Map<String, String> units = new HashMap<>();

                try (Cursor c = db.rawQuery(
                        "SELECT name,quantity,unit " +
                                "FROM recipe_ingredients " +
                                "WHERE recipe_id=?",
                        new String[]{
                                String.valueOf(recipeId)
                        })) {

                    while (c.moveToNext()) {

                        String ingredient = c.getString(0);
                        String unit = c.getString(2);

                        String key =
                                UnitConverter.normalize(ingredient)
                                        + "|"
                                        + UnitConverter.group(unit);

                        required.put(
                                key,
                                quantityOrZero(required, key)
                                        + UnitConverter.base(
                                        c.getDouble(1),
                                        unit
                                )
                        );

                        labels.put(key, ingredient);
                        units.put(key, unit);
                    }
                }

                // Count how many ingredient shortages the recipe has
                int shortageCount = 0;
                String shortDescription = "";

                for (Map.Entry<String, Double> entry
                        : required.entrySet()) {

                    // Work out how much of the ingredient is missing
                    double deficit =
                            entry.getValue()
                                    - quantityOrZero(
                                    available,
                                    entry.getKey()
                            );

                    if (deficit > 0.000001) {

                        shortageCount++;

                        String unit =
                                units.get(entry.getKey());

                        // Convert the amount back to the unit shown to the user
                        double displayAmount =
                                deficit /
                                        ((unit.equals("kg")
                                                || unit.equals("l"))
                                                ? 1000.0
                                                : 1.0);

                        // Create a readable description of what is missing
                        shortDescription =
                                UnitConverter.number(displayAmount)
                                        + " "
                                        + unit
                                        + " "
                                        + labels.get(entry.getKey());

                        // Stop checking if more than one item is missing
                        if (shortageCount > 1) {
                            break;
                        }
                    }
                }

                // Almost There only shows recipes with one shortage
                if (shortageCount == 1) {

                    results.add(new NearMatch(
                            new Recipe(
                                    recipeId,
                                    recipes.getString(1),
                                    recipes.getString(2)
                            ),
                            shortDescription
                    ));
                }
            }
        }

        return results;
    }

    // Get one recipe using its database ID
    public Recipe getRecipe(long id) {

        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,method FROM recipes WHERE id=?",
                new String[]{String.valueOf(id)})) {

            if (c.moveToFirst()) {

                return new Recipe(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2)
                );
            }
        }

        return null;
    }

    // Create the ingredient list displayed on the recipe detail screen
    public String getRecipeIngredients(long id) {

        StringBuilder text = new StringBuilder();

        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT name,quantity,unit " +
                        "FROM recipe_ingredients " +
                        "WHERE recipe_id=? ORDER BY id",
                new String[]{String.valueOf(id)})) {

            while (c.moveToNext()) {

                text.append("• ")
                        .append(
                                UnitConverter.number(
                                        c.getDouble(1)
                                )
                        )
                        .append(" ")
                        .append(c.getString(2))
                        .append(" ")
                        .append(c.getString(0))
                        .append("\n");
            }
        }

        return text.toString();
    }

    // Add the starting recipes when the database is first created
    private void seedRecipes(SQLiteDatabase db) {

        // Each ingredient is stored as name:quantity:unit
        seed(db,
                "Boiled Eggs",
                "Place eggs in boiling water for 9 minutes; cool and peel.",
                "egg:2:piece");

        seed(db,
                "Banana Milk",
                "Blend the banana and milk until smooth.",
                "banana:1:piece",
                "milk:250:ml");

        seed(db,
                "Tomato Toast",
                "Toast bread, slice the tomato, and place on toast.",
                "bread:2:piece",
                "tomato:1:piece");

        seed(db,
                "Cheese Toast",
                "Top bread with cheese and grill until melted.",
                "bread:2:piece",
                "cheese:40:g");

        seed(db,
                "Egg on Toast",
                "Cook the egg in a pan and serve on toasted bread.",
                "egg:1:piece",
                "bread:2:piece",
                "oil:10:ml");

        seed(db,
                "Cheese Omelet",
                "Beat eggs; cook in oil, add cheese, and fold.",
                "egg:2:piece",
                "cheese:40:g",
                "oil:10:ml");

        seed(db,
                "Tomato Omelet",
                "Beat eggs; cook in oil with chopped tomato.",
                "egg:2:piece",
                "tomato:1:piece",
                "oil:10:ml");

        seed(db,
                "Apple Yogurt Bowl",
                "Chop the apple and stir into yogurt.",
                "apple:1:piece",
                "yogurt:150:g");

        seed(db,
                "Banana Yogurt Bowl",
                "Slice the banana and stir into yogurt.",
                "banana:1:piece",
                "yogurt:150:g");

        seed(db,
                "Carrot Salad",
                "Grate the carrot and toss with lemon juice.",
                "carrot:2:piece",
                "lemon juice:20:ml");

        seed(db,
                "Cucumber Tomato Salad",
                "Chop cucumber and tomatoes; toss with oil.",
                "cucumber:1:piece",
                "tomato:2:piece",
                "oil:15:ml");

        seed(db,
                "Peanut Butter Toast",
                "Toast bread and spread with peanut butter.",
                "bread:2:piece",
                "peanut butter:30:g");

        seed(db,
                "Rice and Beans",
                "Cook rice in water; warm beans and combine.",
                "rice:100:g",
                "beans:150:g",
                "water:250:ml");

        seed(db,
                "Simple Pasta",
                "Cook pasta in water; drain and stir through oil.",
                "pasta:100:g",
                "water:1000:ml",
                "oil:15:ml");

        seed(db,
                "Tomato Pasta",
                "Cook pasta in water; drain and toss with chopped tomato and oil.",
                "pasta:100:g",
                "water:1000:ml",
                "tomato:2:piece",
                "oil:15:ml");

        seed(db,
                "Mashed Potatoes",
                "Boil potatoes in water, drain, then mash with milk.",
                "potato:2:piece",
                "water:500:ml",
                "milk:100:ml");

        seed(db,
                "Scrambled Eggs",
                "Beat eggs with milk and cook gently in oil.",
                "egg:2:piece",
                "milk:30:ml",
                "oil:10:ml");

        seed(db,
                "Apple Banana Bowl",
                "Slice apple and banana and serve together.",
                "apple:1:piece",
                "banana:1:piece");
    }

    // Helper method used to insert each starting recipe
    private void seed(SQLiteDatabase db,
                      String name,
                      String method,
                      String... requirements) {

        // Save the main recipe
        ContentValues recipe = new ContentValues();

        recipe.put("name", name);
        recipe.put("method", method);

        long recipeId = db.insertOrThrow(
                "recipes",
                null,
                recipe
        );

        // Add each ingredient required by the recipe
        for (String specification : requirements) {

            // Split name, quantity and unit using the colon
            String[] parts = specification.split(":");

            ContentValues ingredient = new ContentValues();

            // Link the ingredient to its recipe
            ingredient.put("recipe_id", recipeId);
            ingredient.put("name", parts[0]);
            ingredient.put(
                    "quantity",
                    Double.parseDouble(parts[1])
            );
            ingredient.put("unit", parts[2]);

            db.insertOrThrow(
                    "recipe_ingredients",
                    null,
                    ingredient
            );
        }
    }
}