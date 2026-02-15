package com.example.hackathon.managers;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

/**
 * Manages profile picture URLs using SharedPreferences
 */
public class ProfilePictureManager {

    private static final String PREFS_NAME = "profile_pictures";
    private static ProfilePictureManager instance;
    private SharedPreferences prefs;

    private ProfilePictureManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ProfilePictureManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfilePictureManager(context);
        }
        return instance;
    }

    /**
     * Save profile picture URL for a user
     */
    public void saveProfilePicture(UUID userUUID, String imageUrl) {
        prefs.edit()
                .putString(userUUID.toString(), imageUrl)
                .apply();
    }

    /**
     * Get profile picture URL for a user
     * Returns null if no custom picture is set
     */
    public String getProfilePicture(UUID userUUID) {
        return prefs.getString(userUUID.toString(), null);
    }

    /**
     * Remove profile picture for a user
     */
    public void removeProfilePicture(UUID userUUID) {
        prefs.edit()
                .remove(userUUID.toString())
                .apply();
    }

    /**
     * Check if user has a custom profile picture
     */
    public boolean hasProfilePicture(UUID userUUID) {
        return prefs.contains(userUUID.toString());
    }
}
