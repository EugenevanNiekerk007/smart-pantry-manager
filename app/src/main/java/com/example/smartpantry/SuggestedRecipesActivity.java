package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class SuggestedRecipesActivity extends Activity {

    // Database used to find recipes that match the pantry
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        // Connect to the database
        database = new DatabaseHelper(this);

        // Open recipes where the user is only missing one requirement
        findViewById(R.id.almostButton)
                .setOnClickListener(v ->
                        startActivity(new Intent(
                                this,
                                AlmostThereActivity.class
                        ))
                );

        // Open the full recipe catalog
        findViewById(R.id.catalogButton)
                .setOnClickListener(v ->
                        startActivity(new Intent(
                                this,
                                RecipeCatalogActivity.class
                        ))
                );

        // Return to the previous screen
        findViewById(R.id.backButton)
                .setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get recipes that can be made with the current pantry
        List<Recipe> recipes = database.getSuggestions();

        // Display the matching recipes in the list
        ListView list = findViewById(R.id.recipeList);
        list.setAdapter(new RecipeAdapter(this, recipes));

        // Show a message if there are no matching recipes
        ((TextView) findViewById(R.id.noRecipes))
                .setVisibility(
                        recipes.isEmpty()
                                ? View.VISIBLE
                                : View.GONE
                );

        // Open the selected recipe when the user taps on it
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