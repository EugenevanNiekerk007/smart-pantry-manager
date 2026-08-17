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
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, quantity REAL NOT NULL CHECK(quantity > 0), unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, method TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL CHECK(quantity > 0), unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");
        seedRecipes(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Add non-destructive migrations here if the schema changes in a future version.
    }

    public long addIngredient(String name, double quantity, String unit, String expiry) {
        ContentValues values = pantryValues(name, quantity, unit, expiry);
        return getWritableDatabase().insert("pantry", null, values);
    }

    public int updateIngredient(long id, String name, double quantity, String unit, String expiry) {
        return getWritableDatabase().update("pantry", pantryValues(name, quantity, unit, expiry), "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteIngredient(long id) {
        return getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    private ContentValues pantryValues(String name, double quantity, String unit, String expiry) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit);
        values.put("expiry", expiry);
        return values;
    }

    public List<Ingredient> getPantry() {
        List<Ingredient> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,quantity,unit,COALESCE(expiry,'') FROM pantry ORDER BY name COLLATE NOCASE", null)) {
            while (c.moveToNext()) list.add(new Ingredient(c.getLong(0), c.getString(1), c.getDouble(2), c.getString(3), c.getString(4)));
        }
        return list;
    }

    public Ingredient getIngredient(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,quantity,unit,COALESCE(expiry,'') FROM pantry WHERE id=?", new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return new Ingredient(c.getLong(0), c.getString(1), c.getDouble(2), c.getString(3), c.getString(4));
        }
        return null;
    }

    public long addRecipe(String name, String method, List<Ingredient> requirements) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("name", name.trim());
            values.put("method", method.trim());
            long id = db.insertOrThrow("recipes", null, values);
            for (Ingredient item : requirements) {
                ContentValues part = new ContentValues();
                part.put("recipe_id", id);
                part.put("name", item.name.trim());
                part.put("quantity", item.quantity);
                part.put("unit", item.unit);
                db.insertOrThrow("recipe_ingredients", null, part);
            }
            db.setTransactionSuccessful();
            return id;
        } finally { db.endTransaction(); }
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,method FROM recipes ORDER BY name", null)) {
            while (c.moveToNext()) recipes.add(new Recipe(c.getLong(0), c.getString(1), c.getString(2)));
        }
        return recipes;
    }

    private static double quantityOrZero(Map<String, Double> quantities, String key) {
        Double amount = quantities.get(key);
        return amount == null ? 0.0 : amount;
    }

    public List<Recipe> getSuggestions() {
        // Multiple pantry rows for the same food and compatible unit are summed.
        Map<String, Double> available = new HashMap<>();
        for (Ingredient item : getPantry()) {
            String key = UnitConverter.normalize(item.name) + "|" + UnitConverter.group(item.unit);
            available.put(key, quantityOrZero(available, key) + UnitConverter.base(item.quantity, item.unit));
        }
        List<Recipe> matches = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor recipes = db.rawQuery("SELECT id,name,method FROM recipes ORDER BY name", null)) {
            while (recipes.moveToNext()) {
                long id = recipes.getLong(0);
                Map<String, Double> required = new HashMap<>();
                try (Cursor ingredients = db.rawQuery("SELECT name,quantity,unit FROM recipe_ingredients WHERE recipe_id=?", new String[]{String.valueOf(id)})) {
                    while (ingredients.moveToNext()) {
                        String key = UnitConverter.normalize(ingredients.getString(0)) + "|" + UnitConverter.group(ingredients.getString(2));
                        required.put(key, quantityOrZero(required, key) + UnitConverter.base(ingredients.getDouble(1), ingredients.getString(2)));
                    }
                }
                if (required.isEmpty()) continue;
                boolean allAvailable = true;
                for (Map.Entry<String, Double> needed : required.entrySet()) {
                    if (quantityOrZero(available, needed.getKey()) + 0.000001 < needed.getValue()) {
                        allAvailable = false;
                        break;
                    }
                }
                if (allAvailable) matches.add(new Recipe(id, recipes.getString(1), recipes.getString(2)));
            }
        }
        return matches;
    }

    public List<NearMatch> getAlmostThere() {
        Map<String, Double> available = new HashMap<>();
        for (Ingredient item : getPantry()) {
            String key = UnitConverter.normalize(item.name) + "|" + UnitConverter.group(item.unit);
            available.put(key, quantityOrZero(available, key) + UnitConverter.base(item.quantity, item.unit));
        }
        List<NearMatch> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor recipes = db.rawQuery("SELECT id,name,method FROM recipes ORDER BY name", null)) {
            while (recipes.moveToNext()) {
                long recipeId = recipes.getLong(0);
                Map<String, Double> required = new HashMap<>();
                Map<String, String> labels = new HashMap<>();
                Map<String, String> units = new HashMap<>();
                try (Cursor c = db.rawQuery("SELECT name,quantity,unit FROM recipe_ingredients WHERE recipe_id=?", new String[]{String.valueOf(recipeId)})) {
                    while (c.moveToNext()) {
                        String ingredient = c.getString(0);
                        String unit = c.getString(2);
                        String key = UnitConverter.normalize(ingredient) + "|" + UnitConverter.group(unit);
                        required.put(key, quantityOrZero(required, key) + UnitConverter.base(c.getDouble(1), unit));
                        labels.put(key, ingredient);
                        units.put(key, unit);
                    }
                }
                int shortageCount = 0;
                String shortDescription = "";
                for (Map.Entry<String, Double> entry : required.entrySet()) {
                    double deficit = entry.getValue() - quantityOrZero(available, entry.getKey());
                    if (deficit > 0.000001) {
                        shortageCount++;
                        String unit = units.get(entry.getKey());
                        double displayAmount = deficit / ((unit.equals("kg") || unit.equals("l")) ? 1000.0 : 1.0);
                        shortDescription = UnitConverter.number(displayAmount) + " " + unit + " " + labels.get(entry.getKey());
                        if (shortageCount > 1) break;
                    }
                }
                if (shortageCount == 1) results.add(new NearMatch(
                    new Recipe(recipeId, recipes.getString(1), recipes.getString(2)), shortDescription));
            }
        }
        return results;
    }

    public Recipe getRecipe(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,method FROM recipes WHERE id=?", new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return new Recipe(c.getLong(0), c.getString(1), c.getString(2));
        }
        return null;
    }

    public String getRecipeIngredients(long id) {
        StringBuilder text = new StringBuilder();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT name,quantity,unit FROM recipe_ingredients WHERE recipe_id=? ORDER BY id", new String[]{String.valueOf(id)})) {
            while (c.moveToNext()) text.append("• ").append(UnitConverter.number(c.getDouble(1))).append(" ").append(c.getString(2)).append(" ").append(c.getString(0)).append("\n");
        }
        return text.toString();
    }

    private void seedRecipes(SQLiteDatabase db) {
        // Each ingredient is name:quantity:unit; no implicit staples are assumed.
        seed(db, "Boiled Eggs", "Place eggs in boiling water for 9 minutes; cool and peel.", "egg:2:piece");
        seed(db, "Banana Milk", "Blend the banana and milk until smooth.", "banana:1:piece", "milk:250:ml");
        seed(db, "Tomato Toast", "Toast bread, slice the tomato, and place on toast.", "bread:2:piece", "tomato:1:piece");
        seed(db, "Cheese Toast", "Top bread with cheese and grill until melted.", "bread:2:piece", "cheese:40:g");
        seed(db, "Egg on Toast", "Cook the egg in a pan and serve on toasted bread.", "egg:1:piece", "bread:2:piece", "oil:10:ml");
        seed(db, "Cheese Omelet", "Beat eggs; cook in oil, add cheese, and fold.", "egg:2:piece", "cheese:40:g", "oil:10:ml");
        seed(db, "Tomato Omelet", "Beat eggs; cook in oil with chopped tomato.", "egg:2:piece", "tomato:1:piece", "oil:10:ml");
        seed(db, "Apple Yogurt Bowl", "Chop the apple and stir into yogurt.", "apple:1:piece", "yogurt:150:g");
        seed(db, "Banana Yogurt Bowl", "Slice the banana and stir into yogurt.", "banana:1:piece", "yogurt:150:g");
        seed(db, "Carrot Salad", "Grate the carrot and toss with lemon juice.", "carrot:2:piece", "lemon juice:20:ml");
        seed(db, "Cucumber Tomato Salad", "Chop cucumber and tomatoes; toss with oil.", "cucumber:1:piece", "tomato:2:piece", "oil:15:ml");
        seed(db, "Peanut Butter Toast", "Toast bread and spread with peanut butter.", "bread:2:piece", "peanut butter:30:g");
        seed(db, "Rice and Beans", "Cook rice in water; warm beans and combine.", "rice:100:g", "beans:150:g", "water:250:ml");
        seed(db, "Simple Pasta", "Cook pasta in water; drain and stir through oil.", "pasta:100:g", "water:1000:ml", "oil:15:ml");
        seed(db, "Tomato Pasta", "Cook pasta in water; drain and toss with chopped tomato and oil.", "pasta:100:g", "water:1000:ml", "tomato:2:piece", "oil:15:ml");
        seed(db, "Mashed Potatoes", "Boil potatoes in water, drain, then mash with milk.", "potato:2:piece", "water:500:ml", "milk:100:ml");
        seed(db, "Scrambled Eggs", "Beat eggs with milk and cook gently in oil.", "egg:2:piece", "milk:30:ml", "oil:10:ml");
        seed(db, "Apple Banana Bowl", "Slice apple and banana and serve together.", "apple:1:piece", "banana:1:piece");
    }

    private void seed(SQLiteDatabase db, String name, String method, String... requirements) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", name);
        recipe.put("method", method);
        long recipeId = db.insertOrThrow("recipes", null, recipe);
        for (String specification : requirements) {
            String[] parts = specification.split(":");
            ContentValues ingredient = new ContentValues();
            ingredient.put("recipe_id", recipeId);
            ingredient.put("name", parts[0]);
            ingredient.put("quantity", Double.parseDouble(parts[1]));
            ingredient.put("unit", parts[2]);
            db.insertOrThrow("recipe_ingredients", null, ingredient);
        }
    }
}
