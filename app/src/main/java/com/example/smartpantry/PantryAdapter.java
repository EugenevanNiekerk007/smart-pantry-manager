package com.example.smartpantry;

import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class PantryAdapter extends ArrayAdapter<Ingredient> {
    public PantryAdapter(Context context, List<Ingredient> items) {
        super(context, R.layout.row_item, R.id.rowTitle, items);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView == null ? LayoutInflater.from(getContext()).inflate(R.layout.row_item, parent, false) : convertView;
        Ingredient item = getItem(position);
        ((TextView) row.findViewById(R.id.rowTitle)).setText(item.name);
        String detail = UnitConverter.number(item.quantity) + " " + item.unit;
        if (!item.expiry.isEmpty()) detail += "  •  Expires " + item.expiry;
        ((TextView) row.findViewById(R.id.rowSubtitle)).setText(detail);
        return row;
    }
}
