package com.example.reeltracker;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.content.Intent;
import android.provider.Settings;
import android.os.Build;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReelAccessibilityService extends AccessibilityService {
    private static final String PREFS = "reel_tracker";
    private View blocker;
    private long lastCountAt = 0;

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        if (!pkg.equals("com.instagram.android")) {
            removeBlocker();
            return;
        }

        resetIfNewDay();
        int limit = getSharedPreferences(PREFS, MODE_PRIVATE).getInt("limit", 50);
        int count = getSharedPreferences(PREFS, MODE_PRIVATE).getInt("count", 0);
        if (count >= limit) {
            showBlocker();
            return;
        }

        // Experimental heuristic: count scroll events only when Instagram's visible
        // accessibility tree appears to contain Reels-related labels or viewer IDs.
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
            event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if (looksLikeReels(getRootInActiveWindow())) {
                long now = System.currentTimeMillis();
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED &&
                    now - lastCountAt > 1200) {
                    lastCountAt = now;
                    count++;
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt("count", count).apply();
                    if (count >= limit) showBlocker();
                }
            }
        }
    }

    private boolean looksLikeReels(AccessibilityNodeInfo node) {
        if (node == null) return false;
        StringBuilder data = new StringBuilder();
        collectText(node, data, 0);
        String s = data.toString().toLowerCase(Locale.ROOT);
        return s.contains("reels") || s.contains("clips_viewer") ||
               s.contains("reel_viewer") || s.contains("watch reels");
    }

    private void collectText(AccessibilityNodeInfo n, StringBuilder out, int depth) {
        if (n == null || depth > 18) return;
        if (n.getText() != null) out.append(n.getText()).append(' ');
        if (n.getContentDescription() != null) out.append(n.getContentDescription()).append(' ');
        if (n.getViewIdResourceName() != null) out.append(n.getViewIdResourceName()).append(' ');
        for (int i = 0; i < n.getChildCount(); i++) collectText(n.getChild(i), out, depth + 1);
    }

    private void resetIfNewDay() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        android.content.SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = p.getString("date", "");
        if (!today.equals(saved)) p.edit().putString("date", today).putInt("count", 0).apply();
    }

    private void showBlocker() {
        if (blocker != null) return;
        try {
            LinearLayout panel = new LinearLayout(this);
            panel.setOrientation(LinearLayout.VERTICAL);
            panel.setGravity(Gravity.CENTER);
            panel.setPadding(40, 40, 40, 40);
            panel.setBackgroundColor(Color.rgb(16, 24, 39));
            TextView message = new TextView(this);
            message.setText("Daily Reel limit reached\n\nTake a break. Your Reel limit will reset tomorrow.");
            message.setTextColor(Color.WHITE);
            message.setTextSize(23);
            message.setGravity(Gravity.CENTER);
            panel.addView(message);
            TextView close = new TextView(this);
            close.setText(" CLOSE INSTAGRAM ");
            close.setTextColor(Color.WHITE);
            close.setTextSize(18);
            close.setGravity(Gravity.CENTER);
            close.setPadding(24, 30, 24, 20);
            panel.addView(close);
            close.setOnClickListener(v -> {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(home);
            });
            int type = Build.VERSION.SDK_INT >= 26 ?
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                -1, -1, type,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.CENTER;
            ((WindowManager)getSystemService(WINDOW_SERVICE)).addView(panel, params);
            blocker = panel;
        } catch (Exception ignored) { }
    }

    private void removeBlocker() {
        if (blocker == null) return;
        try { ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(blocker); }
        catch (Exception ignored) { }
        blocker = null;
    }

    @Override public void onInterrupt() { removeBlocker(); }
    @Override public void onDestroy() { removeBlocker(); super.onDestroy(); }
}
