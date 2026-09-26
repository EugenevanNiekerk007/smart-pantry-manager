package com.example.smartpantry;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditIngredientActivity extends Activity {
    private DatabaseHelper database;
    private EditText name, quantity, expiry;
    private Spinner unit;
    private long ingredientId;
    private final String[] units = {"piece", "g", "kg", "ml", "l"};

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ingredient);
        database = new DatabaseHelper(this);
        name = findViewById(R.id.nameInput);
        quantity = findViewById(R.id.quantityInput);
        expiry = findViewById(R.id.expiryInput);
        unit = findViewById(R.id.unitInput);
        unit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units));
        ingredientId = getIntent().getLongExtra("ingredient_id", -1);
        if (ingredientId != -1) {
            Ingredient item = database.getIngredient(ingredientId);
            if (item == null) { finish(); return; }
            ((TextView) findViewById(R.id.formTitle)).setText("Edit ingredient");
            name.setText(item.name);
            quantity.setText(UnitConverter.number(item.quantity));
            expiry.setText(item.expiry);
            for (int i = 0; i < units.length; i++) if (units[i].equals(item.unit)) unit.setSelection(i);
        }
        findViewById(R.id.saveButton).setOnClickListener(v -> save());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());
    }

    private void save() {
        String ingredientName = name.getText().toString().trim();
        String enteredQuantity = quantity.getText().toString().trim();
        String expiryDate = expiry.getText().toString().trim();
        if (ingredientName.isEmpty()) { name.setError("Enter an ingredient name"); return; }
        if (enteredQuantity.isEmpty()) { quantity.setError("Enter a quantity"); return; }
        double amount;
        try { amount = Double.parseDouble(enteredQuantity); }
        catch (NumberFormatException error) { quantity.setError("Enter a valid number"); return; }
        if ((Double.isNaN(amount) || Double.isInfinite(amount)) || amount <= 0) { quantity.setError("Quantity must be greater than zero"); return; }
        if (!expiryDate.isEmpty()) {
            try { SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT); format.setLenient(false); format.parse(expiryDate); }
            catch (ParseException error) { expiry.setError("Use YYYY-MM-DD, e.g. 2026-10-05"); return; }
        }
        int result;
        if (ingredientId == -1) result = database.addIngredient(ingredientName, amount, unit.getSelectedItem().toString(), expiryDate) < 0 ? 0 : 1;
        else result = database.updateIngredient(ingredientId, ingredientName, amount, unit.getSelectedItem().toString(), expiryDate);
        if (result > 0) { Toast.makeText(this, "Ingredient saved", Toast.LENGTH_SHORT).show(); finish(); }
        else Toast.makeText(this, "Could not save ingredient", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onDestroy() { database.close(); super.onDestroy(); }
}
