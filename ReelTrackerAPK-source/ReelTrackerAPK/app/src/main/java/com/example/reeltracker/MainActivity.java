package com.example.reeltracker;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private TextView countText, statusText;
    private EditText limitInput;
    private static final String PREFS = "reel_tracker";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(36, 48, 36, 36);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setBackgroundColor(0xFF101827);

        TextView title = new TextView(this);
        title.setText("REEL TRACKER");
        title.setTextSize(25);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        page.addView(title);

        countText = new TextView(this);
        countText.setTextColor(0xFF61DDAA);
        countText.setTextSize(42);
        countText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.topMargin = 42;
        page.addView(countText, cp);

        statusText = new TextView(this);
        statusText.setTextColor(0xFFD5DEEA);
        statusText.setTextSize(16);
        statusText.setGravity(Gravity.CENTER);
        page.addView(statusText);

        TextView label = new TextView(this);
        label.setText("Daily Reel limit");
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(16);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = 38;
        page.addView(label, lp);

        limitInput = new EditText(this);
        limitInput.setSingleLine(true);
        limitInput.setInputType(2);
        limitInput.setTextColor(0xFFFFFFFF);
        limitInput.setHintTextColor(0xFF9AA8BA);
        limitInput.setHint("Example: 50");
        page.addView(limitInput);

        Button save = new Button(this);
        save.setText("Save daily limit");
        page.addView(save);
        save.setOnClickListener(v -> {
            try {
                int limit = Integer.parseInt(limitInput.getText().toString().trim());
                if (limit < 1 || limit > 1000) throw new NumberFormatException();
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt("limit", limit).apply();
                updateDashboard();
                Toast.makeText(this, "Daily limit saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Enter a number from 1 to 1000", Toast.LENGTH_SHORT).show();
            }
        });

        Button accessibility = new Button(this);
        accessibility.setText("Enable Reel monitoring");
        page.addView(accessibility);
        accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        TextView note = new TextView(this);
        note.setText("Monitoring uses Android Accessibility permission. Reel detection is experimental and may miss or miscount Reels when Instagram changes its interface.");
        note.setTextColor(0xFF9AA8BA);
        note.setTextSize(12);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
        np.topMargin = 28;
        page.addView(note, np);

        setContentView(page);
        int limit = getSharedPreferences(PREFS, MODE_PRIVATE).getInt("limit", 50);
        limitInput.setText(String.valueOf(limit));
        updateDashboard();
    }

    @Override protected void onResume() { super.onResume(); updateDashboard(); }

    private void updateDashboard() {
        int count = getSharedPreferences(PREFS, MODE_PRIVATE).getInt("count", 0);
        int limit = getSharedPreferences(PREFS, MODE_PRIVATE).getInt("limit", 50);
        countText.setText(count + " / " + limit);
        statusText.setText(Math.max(0, limit - count) + " Reels remaining today");
    }
}
