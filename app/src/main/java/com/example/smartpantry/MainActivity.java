package com.example.smartpantry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.List;

public class MainActivity extends Activity {

    // Database and main pantry screen items
    private DatabaseHelper database;
    private ListView pantryList;
    private TextView emptyMessage;
    private List<Ingredient> ingredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect to the database and screen views
        database = new DatabaseHelper(this);
        pantryList = findViewById(R.id.pantryList);
        emptyMessage = findViewById(R.id.emptyMessage);

        // Open the screen to add a new ingredient
        findViewById(R.id.addButton).setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        EditIngredientActivity.class
                ))
        );

        // Open recipes that match the current pantry
        findViewById(R.id.suggestionsButton).setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        SuggestedRecipesActivity.class
                ))
        );

        // Open the full recipe catalog
        findViewById(R.id.catalogButton).setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        RecipeCatalogActivity.class
                ))
        );

        // Open the settings screen
        findViewById(R.id.settingsButton).setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        SettingsActivity.class
                ))
        );

        // Open an ingredient for editing when it is tapped
        pantryList.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Intent intent = new Intent(
                            this,
                            EditIngredientActivity.class
                    );

                    // Pass the selected ingredient ID to the edit screen
                    intent.putExtra(
                            "ingredient_id",
                            ingredients.get(position).id
                    );

                    startActivity(intent);
                }
        );

        // Allow an ingredient to be deleted with a long press
        pantryList.setOnItemLongClickListener(
                (parent, view, position, id) -> {

                    Ingredient selected =
                            ingredients.get(position);

                    // Ask the user to confirm before deleting
                    new AlertDialog.Builder(this)
                            .setTitle(
                                    "Delete "
                                            + selected.name
                                            + "?"
                            )
                            .setMessage(
                                    "This item will be removed from your pantry."
                            )
                            .setNegativeButton(
                                    "Cancel",
                                    null
                            )
                            .setPositiveButton(
                                    "Delete",
                                    (dialog, which) -> {

                                        // Delete the ingredient and update the list
                                        database.deleteIngredient(
                                                selected.id
                                        );
                                        refresh();
                                    }
                            )
                            .show();

                    return true;
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the pantry whenever the user returns to this screen
        refresh();
    }

    // Reload the pantry and check for ingredients expiring soon
    private void refresh() {

        // Get the latest pantry information from the database
        ingredients = database.getPantry();

        // Display the ingredients using the pantry adapter
        pantryList.setAdapter(
                new PantryAdapter(this, ingredients)
        );

        // Get the user's expiry alert setting
        SharedPreferences settings =
                getSharedPreferences(
                        "settings",
                        MODE_PRIVATE
                );

        boolean alerts =
                settings.getBoolean(
                        "expiry_alerts",
                        true
                );

        int soon = 0;

        // Expiry dates are stored in year-month-day format
        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.ROOT
                );

        format.setLenient(false);

        // Work out the date three days from now
        Calendar cutoff = Calendar.getInstance();
        cutoff.add(Calendar.DAY_OF_YEAR, 3);

        // Check the expiry date of each pantry ingredient
        for (Ingredient item : ingredients) {

            // Skip ingredients that do not have an expiry date
            if (item.expiry.isEmpty()) {
                continue;
            }

            try {

                Date date = format.parse(item.expiry);

                // Count expired items and items expiring within three days
                if (date != null
                        && !date.after(cutoff.getTime())) {

                    soon++;
                }

            } catch (Exception ignored) {

                // Invalid dates should already be prevented when saving
            }
        }

        // Show a message when there are no pantry ingredients
        if (ingredients.isEmpty()) {

            emptyMessage.setText(
                    "Your pantry is empty. Add your first ingredient."
            );

            emptyMessage.setVisibility(View.VISIBLE);

        } else if (alerts && soon > 0) {

            // Warn the user when ingredients have expired or expire soon
            emptyMessage.setText(
                    soon
                            + " item(s) are expired or expire within 3 days. "
                            + "Check before cooking."
            );

            emptyMessage.setVisibility(View.VISIBLE);

        } else {

            // Hide the message when there is nothing to warn the user about
            emptyMessage.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {

        // Close the database when the activity is destroyed
        database.close();
        super.onDestroy();
    }
}