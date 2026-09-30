package com.example.smartpantry;

import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

// Adapter used to display recipes that are almost a match
public class AlmostThereAdapter extends ArrayAdapter<NearMatch> {

    // Pass the list of near matches to the adapter
    public AlmostThereAdapter(Context context, List<NearMatch> items) {
        super(context, R.layout.row_item, R.id.rowTitle, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Reuse an existing row if possible, otherwise create a new one
        View row = convertView == null
                ? LayoutInflater.from(getContext())
                .inflate(R.layout.row_item, parent, false)
                : convertView;

        // Get the recipe for the current position in the list
        NearMatch match = getItem(position);

        // Display the recipe name
        ((TextView) row.findViewById(R.id.rowTitle))
                .setText(match.recipe.name);

        // Show which ingredients are still needed
        ((TextView) row.findViewById(R.id.rowSubtitle))
                .setText("Need: " + match.missing);

        return row;
    }
}