package com.example.hackathon.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.hackathon.R;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.managers.ProfilePictureManager;
import java.io.File;
import java.util.UUID;

/**
 * Helper class for loading user profile pictures
 * Handles custom uploaded pictures and fallback to default
 */
public class ProfilePictureHelper {

    private static final String TAG = "ProfilePictureHelper";

    /**
     * Loads profile picture for a user into an ImageView
     * Checks for custom uploaded picture first, then falls back to default
     *
     * @param context Android context
     * @param userUUID UUID of the user
     * @param imageView ImageView to load the picture into
     */
    public static void loadProfilePicture(Context context, UUID userUUID, ImageView imageView) {
        User user = UserDAO.getInstance().getByUUID(userUUID);

        if (user == null) {
            Log.e(TAG, "User not found: " + userUUID);
            imageView.setImageResource(R.drawable.default_profile);
            return;
        }

        // Check for custom uploaded profile picture
        ProfilePictureManager profilePictureManager = ProfilePictureManager.getInstance(context);
        String savedPicturePath = profilePictureManager.getProfilePicture(userUUID);

        if (savedPicturePath != null && new File(savedPicturePath).exists()) {
            // Load from saved file
            Log.d(TAG, "Loading custom picture from: " + savedPicturePath);
            Glide.with(context)
                    .load(new File(savedPicturePath))
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(imageView);
        } else {
            // Load default from user model (URL)
            Log.d(TAG, "Loading default picture for user: " + user.username());
            Glide.with(context)
                    .load(user.profilePictureUrl())
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(imageView);
        }
    }

    /**
     * Gets the profile picture URL for a user
     * Useful when you need the URL without loading into an ImageView
     *
     * @param context Android context
     * @param userUUID UUID of the user
     * @return Profile picture URL or null if user not found
     */
    public static String getProfilePictureUrl(Context context, UUID userUUID) {
        User user = UserDAO.getInstance().getByUUID(userUUID);

        if (user == null) {
            return null;
        }

        ProfilePictureManager profilePictureManager = ProfilePictureManager.getInstance(context);
        String savedPicture = profilePictureManager.getProfilePicture(userUUID);

        return savedPicture != null ? savedPicture : user.profilePictureUrl();
    }
}
