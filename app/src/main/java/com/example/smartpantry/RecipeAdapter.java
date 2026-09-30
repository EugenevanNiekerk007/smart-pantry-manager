package com.example.smartpantry;

import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

// Adapter used to display recipes in a ListView
public class RecipeAdapter extends ArrayAdapter<Recipe> {

    // Pass the list of recipes to the adapter
    public RecipeAdapter(Context context, List<Recipe> recipes) {
        super(context, R.layout.row_item, R.id.rowTitle, recipes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Reuse an existing row if possible, otherwise create a new one
        View row = convertView == null
                ? LayoutInflater.from(getContext())
                .inflate(R.layout.row_item, parent, false)
                : convertView;

        // Display the recipe name for the current position
        ((TextView) row.findViewById(R.id.rowTitle))
                .setText(getItem(position).name);

        // Give the user instructions for opening the recipe
        ((TextView) row.findViewById(R.id.rowSubtitle))
                .setText("Tap to view ingredients and method");

        return row;
    }
}