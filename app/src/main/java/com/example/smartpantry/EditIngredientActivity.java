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
import java.util.Date;
import java.util.Locale;

public class EditIngredientActivity extends Activity {

    // Database and input fields used on this screen
    private DatabaseHelper database;
    private EditText name, quantity, expiry;
    private Spinner unit;

    // ID is -1 when adding a new ingredient
    private long ingredientId;

    // Units available in the dropdown
    private final String[] units = {"piece", "g", "kg", "ml", "l"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ingredient);

        // Connect to the database
        database = new DatabaseHelper(this);

        // Connect the Java variables to the screen inputs
        name = findViewById(R.id.nameInput);
        quantity = findViewById(R.id.quantityInput);
        expiry = findViewById(R.id.expiryInput);
        unit = findViewById(R.id.unitInput);

        // Add the measurement units to the dropdown
        unit.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                units
        ));

        // Get the ingredient ID passed from the previous screen
        ingredientId = getIntent().getLongExtra("ingredient_id", -1);

        // If an ID was received, load the existing ingredient for editing
        if (ingredientId != -1) {

            Ingredient item = database.getIngredient(ingredientId);

            // Close the screen if the ingredient cannot be found
            if (item == null) {
                finish();
                return;
            }

            // Change the heading because an existing ingredient is being edited
            ((TextView) findViewById(R.id.formTitle))
                    .setText("Edit ingredient");

            // Fill the inputs with the current ingredient information
            name.setText(item.name);
            quantity.setText(UnitConverter.number(item.quantity));
            expiry.setText(item.expiry);

            // Select the ingredient's current unit
            for (int i = 0; i < units.length; i++) {

                if (units[i].equals(item.unit)) {
                    unit.setSelection(i);
                    break;
                }
            }
        }

        // Save the ingredient when the save button is pressed
        findViewById(R.id.saveButton)
                .setOnClickListener(v -> save());

        // Close the screen without saving
        findViewById(R.id.cancelButton)
                .setOnClickListener(v -> finish());
    }

    // Check the user's input and save the ingredient
    private void save() {

        // Get the values entered by the user
        String ingredientName =
                name.getText().toString().trim();

        String enteredQuantity =
                quantity.getText().toString().trim();

        String expiryDate =
                expiry.getText().toString().trim();

        // Ingredient name cannot be empty
        if (ingredientName.isEmpty()) {
            name.setError("Enter an ingredient name");
            return;
        }

        // Quantity cannot be empty
        if (enteredQuantity.isEmpty()) {
            quantity.setError("Enter a quantity");
            return;
        }

        double amount;

        // Convert the entered quantity into a number
        try {
            amount = Double.parseDouble(enteredQuantity);

        } catch (NumberFormatException error) {

            quantity.setError("Enter a valid number");
            return;
        }

        // Quantity must be a valid number greater than zero
        if (Double.isNaN(amount)
                || Double.isInfinite(amount)
                || amount <= 0) {

            quantity.setError(
                    "Quantity must be greater than zero"
            );
            return;
        }

        // Expiry date is optional, but must be valid if entered
        if (!expiryDate.isEmpty()) {

            try {

                // Dates must be entered in YYYY-MM-DD format
                SimpleDateFormat format =
                        new SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.ROOT
                        );

                // Prevent invalid dates such as 2026-13-40
                format.setLenient(false);

                // Convert the entered expiry date into a Date
                Date enteredDate = format.parse(expiryDate);

                // Get today's date without the current time
                Date today = format.parse(
                        format.format(new Date())
                );

                // Do not allow an expiry date before today
                if (enteredDate != null
                        && today != null
                        && enteredDate.before(today)) {

                    expiry.setError(
                            "Expiry date cannot be before today"
                    );
                    return;
                }

            } catch (ParseException error) {

                expiry.setError(
                        "Enter the date as YYYY-MM-DD"
                );
                return;
            }
        }

        int result;

        // Add a new ingredient if there is no existing ingredient ID
        if (ingredientId == -1) {

            result = database.addIngredient(
                    ingredientName,
                    amount,
                    unit.getSelectedItem().toString(),
                    expiryDate
            ) < 0 ? 0 : 1;

        } else {

            // Otherwise update the existing ingredient
            result = database.updateIngredient(
                    ingredientId,
                    ingredientName,
                    amount,
                    unit.getSelectedItem().toString(),
                    expiryDate
            );
        }

        // Show whether the ingredient was saved successfully
        if (result > 0) {

            Toast.makeText(
                    this,
                    "Ingredient saved",
                    Toast.LENGTH_SHORT
            ).show();

            // Return to the previous screen
            finish();

        } else {

            Toast.makeText(
                    this,
                    "Could not save ingredient",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {

        // Close the database when this screen is destroyed
        database.close();
        super.onDestroy();
    }
}