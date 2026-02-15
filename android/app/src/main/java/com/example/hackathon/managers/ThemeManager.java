package com.example.hackathon.managers;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Manages app theme (light/dark mode) preferences.
 * Stores user's theme choice in SharedPreferences.
 */
public class ThemeManager {

    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_THEME = "theme_mode";

    private static ThemeManager instance;
    private SharedPreferences prefs;

    private ThemeManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context);
        }
        return instance;
    }

    /**
     * Checks if dark mode is currently enabled
     * @return true if dark mode is on, false otherwise
     */
    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_THEME, false);
    }

    /**
     * Toggles between light and dark mode
     */
    public void toggleTheme() {
        boolean isDark = isDarkMode();
        setDarkMode(!isDark);
    }

    /**
     * Sets the theme mode
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setDarkMode(boolean isDarkMode) {
        // Save preference
        prefs.edit().putBoolean(KEY_THEME, isDarkMode).apply();

        // Apply theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Applies the saved theme on app startup
     */
    public void applyTheme() {
        setDarkMode(isDarkMode());
    }
}
