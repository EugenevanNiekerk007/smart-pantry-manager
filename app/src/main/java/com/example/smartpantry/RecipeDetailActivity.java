package com.example.smartpantry;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class RecipeDetailActivity extends Activity {

    // Database used to get the selected recipe information
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Connect to the database
        database = new DatabaseHelper(this);

        // Get the recipe ID passed from the previous screen
        long recipeId = getIntent()
                .getLongExtra("recipe_id", -1);

        // Use the ID to get the selected recipe from the database
        Recipe recipe = database.getRecipe(recipeId);

        // Close the screen if the recipe cannot be found
        if (recipe == null) {
            finish();
            return;
        }

        // Display the recipe name
        ((TextView) findViewById(R.id.recipeTitle))
                .setText(recipe.name);

        // Get and display the ingredients needed for the recipe
        ((TextView) findViewById(R.id.ingredientsText))
                .setText(
                        database.getRecipeIngredients(recipe.id)
                );

        // Display the preparation method
        ((TextView) findViewById(R.id.methodText))
                .setText(recipe.method);

        // Return to the previous screen
        findViewById(R.id.backButton)
                .setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {

        // Close the database when this screen is destroyed
        database.close();
        super.onDestroy();
    }
}