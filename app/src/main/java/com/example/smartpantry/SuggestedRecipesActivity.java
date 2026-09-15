package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

public class SuggestedRecipesActivity extends Activity {
    private DatabaseHelper database;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);
        database = new DatabaseHelper(this);
        findViewById(R.id.almostButton).setOnClickListener(v -> startActivity(new Intent(this, AlmostThereActivity.class)));
        findViewById(R.id.catalogButton).setOnClickListener(v -> startActivity(new Intent(this, RecipeCatalogActivity.class)));
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
    @Override protected void onResume() {
        super.onResume();
        List<Recipe> recipes = database.getSuggestions();
        ListView list = findViewById(R.id.recipeList);
        list.setAdapter(new RecipeAdapter(this, recipes));
        ((TextView)findViewById(R.id.noRecipes)).setVisibility(recipes.isEmpty() ? View.VISIBLE : View.GONE);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipes.get(position).id);
            startActivity(intent);
        });
    }
    @Override protected void onDestroy() { database.close(); super.onDestroy(); }
}
