package com.example.hackathon.managers;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ProfileImageManager {

    private final File profileDir;

    public ProfileImageManager(Context context) {
        this.profileDir = new File(context.getFilesDir(), "profile_photos");
        //if (!profileDir.exists()) profileDir.mkdirs();
    }

    /**
     * Saves a user's profile image locally and returns the absolute file path.
     */
    public String saveProfileImage(UUID userId, InputStream imageStream) throws IOException {
        File outFile = new File(profileDir, "profile_" + userId + ".jpg");

        try (OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = imageStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        return outFile.getAbsolutePath();
    }

    /**
     * Deletes a user's stored image (optional cleanup).
     */
    public void deleteProfileImage(UUID userId) {
        File file = new File(profileDir, "profile_" + userId + ".jpg");
        if (file.exists()) file.delete();
    }

    /**
     * Returns a File handle for Glide or future UI use.
     */
    public File getProfileImage(UUID userId) {
        File file = new File(profileDir, "profile_" + userId + ".jpg");
        return file.exists() ? file : null;
    }
}
