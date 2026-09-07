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
    private DatabaseHelper database;
    private ListView pantryList;
    private TextView emptyMessage;
    private List<Ingredient> ingredients;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = new DatabaseHelper(this);
        pantryList = findViewById(R.id.pantryList);
        emptyMessage = findViewById(R.id.emptyMessage);
        findViewById(R.id.addButton).setOnClickListener(v -> startActivity(new Intent(this, EditIngredientActivity.class)));
        findViewById(R.id.suggestionsButton).setOnClickListener(v -> startActivity(new Intent(this, SuggestedRecipesActivity.class)));
        findViewById(R.id.catalogButton).setOnClickListener(v -> startActivity(new Intent(this, RecipeCatalogActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        pantryList.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, EditIngredientActivity.class);
            intent.putExtra("ingredient_id", ingredients.get(position).id);
            startActivity(intent);
        });
        pantryList.setOnItemLongClickListener((parent, view, position, id) -> {
            Ingredient selected = ingredients.get(position);
            new AlertDialog.Builder(this).setTitle("Delete " + selected.name + "?")
                .setMessage("This item will be removed from your pantry.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> { database.deleteIngredient(selected.id); refresh(); })
                .show();
            return true;
        });
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    private void refresh() {
        ingredients = database.getPantry();
        pantryList.setAdapter(new PantryAdapter(this, ingredients));
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        boolean alerts = settings.getBoolean("expiry_alerts", true);
        int soon = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        format.setLenient(false);
        Calendar cutoff = Calendar.getInstance();
        cutoff.add(Calendar.DAY_OF_YEAR, 3);
        for (Ingredient item : ingredients) {
            if (item.expiry.isEmpty()) continue;
            try {
                Date date = format.parse(item.expiry);
                if (date != null && !date.after(cutoff.getTime())) soon++;
            } catch (Exception ignored) { /* Form validation normally prevents invalid dates. */ }
        }
        if (ingredients.isEmpty()) {
            emptyMessage.setText("Your pantry is empty. Add your first ingredient.");
            emptyMessage.setVisibility(View.VISIBLE);
        } else if (alerts && soon > 0) {
            emptyMessage.setText(soon + " item(s) are expired or expire within 3 days. Check before cooking.");
            emptyMessage.setVisibility(View.VISIBLE);
        } else emptyMessage.setVisibility(View.GONE);
    }

    @Override protected void onDestroy() { database.close(); super.onDestroy(); }
}
