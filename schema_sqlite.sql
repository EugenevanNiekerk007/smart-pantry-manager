-- SQLite schema used by Smart Pantry Manager. SQLiteOpenHelper creates these
-- tables in DatabaseHelper.onCreate() and seeds 18 recipes on first launch.
CREATE TABLE pantry (
 id INTEGER PRIMARY KEY AUTOINCREMENT,
 name TEXT NOT NULL,
 quantity REAL NOT NULL CHECK(quantity > 0),
 unit TEXT NOT NULL,
 expiry TEXT
);
CREATE TABLE recipes (
 id INTEGER PRIMARY KEY AUTOINCREMENT,
 name TEXT NOT NULL UNIQUE,
 method TEXT NOT NULL
);
CREATE TABLE recipe_ingredients (
 id INTEGER PRIMARY KEY AUTOINCREMENT,
 recipe_id INTEGER NOT NULL,
 name TEXT NOT NULL,
 quantity REAL NOT NULL CHECK(quantity > 0),
 unit TEXT NOT NULL,
 FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);
