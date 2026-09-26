package com.example.smartpantry;

import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class AlmostThereAdapter extends ArrayAdapter<NearMatch> {
    public AlmostThereAdapter(Context context, List<NearMatch> items) {
        super(context, R.layout.row_item, R.id.rowTitle, items);
    }
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView == null ? LayoutInflater.from(getContext()).inflate(R.layout.row_item, parent, false) : convertView;
        NearMatch match = getItem(position);
        ((TextView)row.findViewById(R.id.rowTitle)).setText(match.recipe.name);
        ((TextView)row.findViewById(R.id.rowSubtitle)).setText("Need: " + match.missing);
        return row;
    }
}
