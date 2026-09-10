package com.example.smartpantry;

import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class RecipeAdapter extends ArrayAdapter<Recipe> {
    public RecipeAdapter(Context context, List<Recipe> recipes) {
        super(context, R.layout.row_item, R.id.rowTitle, recipes);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView == null ? LayoutInflater.from(getContext()).inflate(R.layout.row_item, parent, false) : convertView;
        ((TextView) row.findViewById(R.id.rowTitle)).setText(getItem(position).name);
        ((TextView) row.findViewById(R.id.rowSubtitle)).setText("Tap to view ingredients and method");
        return row;
    }
}
