package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import java.util.List;

public class AlmostThereActivity extends Activity {
    private DatabaseHelper database;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_almost_there);
        database = new DatabaseHelper(this);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
    @Override protected void onResume() {
        super.onResume();
        List<NearMatch> matches = database.getAlmostThere();
        ListView list = findViewById(R.id.almostList);
        list.setAdapter(new AlmostThereAdapter(this, matches));
        findViewById(R.id.noAlmost).setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", matches.get(position).recipe.id);
            startActivity(intent);
        });
    }
    @Override protected void onDestroy() { database.close(); super.onDestroy(); }
}
