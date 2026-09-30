package com.example.smartpantry;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Switch;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Connect to the expiry alert switch
        Switch toggle = findViewById(R.id.expiryToggle);

        // Load the saved setting and use true as the default
        toggle.setChecked(
                getSharedPreferences(
                        "settings",
                        MODE_PRIVATE
                ).getBoolean(
                        "expiry_alerts",
                        true
                )
        );

        // Save the setting whenever the user changes the switch
        toggle.setOnCheckedChangeListener(
                (button, enabled) ->
                        getSharedPreferences(
                                "settings",
                                MODE_PRIVATE
                        )
                                .edit()
                                .putBoolean(
                                        "expiry_alerts",
                                        enabled
                                )
                                .apply()
        );

        // Return to the previous screen
        findViewById(R.id.backButton)
                .setOnClickListener(v -> finish());
    }
}