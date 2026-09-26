package com.example.smartpantry;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Switch;

public class SettingsActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Switch toggle = findViewById(R.id.expiryToggle);
        toggle.setChecked(getSharedPreferences("settings", MODE_PRIVATE).getBoolean("expiry_alerts", true));
        toggle.setOnCheckedChangeListener((button, enabled) ->
            getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("expiry_alerts", enabled).apply());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}
