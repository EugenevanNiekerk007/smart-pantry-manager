package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import java.util.List;

public class AlmostThereActivity extends Activity {

    // Database used to get recipes that are nearly a match
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_almost_there);

        // Connect to the database
        database = new DatabaseHelper(this);

        // Go back to the previous screen
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get recipes where the user is only missing some ingredients
        List<NearMatch> matches = database.getAlmostThere();

        // Display the almost matching recipes in the list
        ListView list = findViewById(R.id.almostList);
        list.setAdapter(new AlmostThereAdapter(this, matches));

        // Show a message if there are no almost matching recipes
        findViewById(R.id.noAlmost).setVisibility(
                matches.isEmpty() ? View.VISIBLE : View.GONE
        );

        // Open the selected recipe when the user taps on it
        list.setOnItemClickListener((parent, view, position, id) -> {

            Intent intent = new Intent(
                    this,
                    RecipeDetailActivity.class
            );

            // Pass the selected recipe ID to the detail screen
            intent.putExtra(
                    "recipe_id",
                    matches.get(position).recipe.id
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {

        // Close the database when this screen is destroyed
        database.close();
        super.onDestroy();
    }
}