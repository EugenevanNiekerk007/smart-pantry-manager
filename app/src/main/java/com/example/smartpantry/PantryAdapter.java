package com.example.smartpantry;

import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

// Adapter used to display pantry ingredients in the ListView
public class PantryAdapter extends ArrayAdapter<Ingredient> {

    // Pass the list of pantry ingredients to the adapter
    public PantryAdapter(Context context, List<Ingredient> items) {
        super(context, R.layout.row_item, R.id.rowTitle, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Reuse an existing row if possible, otherwise create a new one
        View row = convertView == null
                ? LayoutInflater.from(getContext())
                .inflate(R.layout.row_item, parent, false)
                : convertView;

        // Get the ingredient for the current position
        Ingredient item = getItem(position);

        // Display the ingredient name
        ((TextView) row.findViewById(R.id.rowTitle))
                .setText(item.name);

        // Build the text showing the quantity and unit
        String detail =
                UnitConverter.number(item.quantity)
                        + " "
                        + item.unit;

        // Add the expiry date if one was entered
        if (!item.expiry.isEmpty()) {
            detail += "  •  Expires " + item.expiry;
        }

        // Display the ingredient details below the name
        ((TextView) row.findViewById(R.id.rowSubtitle))
                .setText(detail);

        return row;
    }
}