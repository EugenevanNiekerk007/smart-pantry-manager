package com.example.smartpantry;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class RecipeDetailActivity extends Activity {
    private DatabaseHelper database;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        database = new DatabaseHelper(this);
        Recipe recipe = database.getRecipe(getIntent().getLongExtra("recipe_id", -1));
        if (recipe == null) { finish(); return; }
        ((TextView)findViewById(R.id.recipeTitle)).setText(recipe.name);
        ((TextView)findViewById(R.id.ingredientsText)).setText(database.getRecipeIngredients(recipe.id));
        ((TextView)findViewById(R.id.methodText)).setText(recipe.method);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
    @Override protected void onDestroy() { database.close(); super.onDestroy(); }
}
