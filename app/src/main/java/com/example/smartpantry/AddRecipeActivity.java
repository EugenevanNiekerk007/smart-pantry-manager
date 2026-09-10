package com.example.smartpantry;

import android.app.Activity;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends Activity {
    private final List<IngredientRow> rows = new ArrayList<>();
    private final String[] units = {"piece", "g", "kg", "ml", "l"};
    private DatabaseHelper database;
    private EditText title, method;
    private LinearLayout rowContainer;

    private static class IngredientRow {
        EditText name, quantity;
        Spinner unit;
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_add_recipe);
        database = new DatabaseHelper(this);
        title = findViewById(R.id.recipeNameInput);
        method = findViewById(R.id.methodInput);
        rowContainer = findViewById(R.id.ingredientRows);
        addRow();
        findViewById(R.id.addRowButton).setOnClickListener(v -> addRow());
        findViewById(R.id.saveRecipeButton).setOnClickListener(v -> save());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());
    }

    private void addRow() {
        IngredientRow row = new IngredientRow();
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setPadding(0, 12, 0, 12);
        TextView label = new TextView(this);
        label.setText("Ingredient " + (rows.size() + 1));
        label.setTextSize(16);
        group.addView(label);
        row.name = new EditText(this);
        row.name.setHint("Name (e.g. tomato)");
        row.name.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        group.addView(row.name);
        row.quantity = new EditText(this);
        row.quantity.setHint("Required quantity");
        row.quantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        group.addView(row.quantity);
        row.unit = new Spinner(this);
        row.unit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units));
        group.addView(row.unit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rowContainer.addView(group);
        rows.add(row);
    }

    private void save() {
        String name = title.getText().toString().trim();
        String steps = method.getText().toString().trim();
        if (name.isEmpty()) { title.setError("Enter a recipe name"); return; }
        if (steps.isEmpty()) { method.setError("Enter preparation steps"); return; }
        List<Ingredient> requirements = new ArrayList<>();
        for (IngredientRow row : rows) {
            String ingredient = row.name.getText().toString().trim();
            String typed = row.quantity.getText().toString().trim();
            if (ingredient.isEmpty()) { row.name.setError("Enter an ingredient name"); return; }
            if (typed.isEmpty()) { row.quantity.setError("Enter the quantity"); return; }
            double amount;
            try { amount = Double.parseDouble(typed); }
            catch (NumberFormatException ex) { row.quantity.setError("Enter a number"); return; }
            if ((Double.isNaN(amount) || Double.isInfinite(amount)) || amount <= 0) { row.quantity.setError("Use a quantity above zero"); return; }
            requirements.add(new Ingredient(-1, ingredient, amount, row.unit.getSelectedItem().toString(), ""));
        }
        try {
            database.addRecipe(name, steps, requirements);
            Toast.makeText(this, "Recipe saved", Toast.LENGTH_SHORT).show();
            finish();
        } catch (SQLiteConstraintException ex) {
            title.setError("A recipe with this name already exists");
        } catch (Exception ex) {
            Toast.makeText(this, "Could not save recipe", Toast.LENGTH_LONG).show();
        }
    }
    @Override protected void onDestroy() { database.close(); super.onDestroy(); }
}
