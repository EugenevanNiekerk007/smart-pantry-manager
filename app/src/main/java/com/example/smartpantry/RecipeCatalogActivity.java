package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import java.util.List;

public class RecipeCatalogActivity extends Activity {
    private DatabaseHelper database;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_catalog);
        database = new DatabaseHelper(this);
        findViewById(R.id.addRecipeButton).setOnClickListener(v -> startActivity(new Intent(this, AddRecipeActivity.class)));
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
    @Override protected void onResume() {
        super.onResume();
        List<Recipe> recipes = database.getAllRecipes();
        ListView list = findViewById(R.id.catalogList);
        list.setAdapter(new RecipeAdapter(this, recipes));
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipes.get(position).id);
            startActivity(intent);
        });
    }
    @Override protected void onDestroy() { database.close(); super.onDestroy(); }
}
