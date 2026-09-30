package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;

public class RecipeCatalogActivity extends Activity {

    // Database used to get the saved recipes
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_catalog);

        // Connect to the database
        database = new DatabaseHelper(this);

        // Open the screen where the user can add a new recipe
        findViewById(R.id.addRecipeButton)
                .setOnClickListener(v ->
                        startActivity(new Intent(
                                this,
                                AddRecipeActivity.class
                        ))
                );

        // Return to the previous screen
        findViewById(R.id.backButton)
                .setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get the latest list of recipes from the database
        List<Recipe> recipes = database.getAllRecipes();

        // Display the recipes using the recipe adapter
        ListView list = findViewById(R.id.catalogList);
        list.setAdapter(new RecipeAdapter(this, recipes));

        // Open the recipe details when a recipe is selected
        list.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Intent intent = new Intent(
                            this,
                            RecipeDetailActivity.class
                    );

                    // Pass the selected recipe ID to the detail screen
                    intent.putExtra(
                            "recipe_id",
                            recipes.get(position).id
                    );

                    startActivity(intent);
                }
        );
    }

    @Override
    protected void onDestroy() {

        // Close the database when this screen is destroyed
        database.close();
        super.onDestroy();
    }
}