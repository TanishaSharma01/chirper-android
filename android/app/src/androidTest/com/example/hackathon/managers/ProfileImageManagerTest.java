package com.example.hackathon.managers;

import static org.junit.Assert.*;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class ProfileImageManagerTest {

    private Context context;
    private ProfileImageManager manager;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        manager = new ProfileImageManager(context);

        File dir = new File(context.getFilesDir(), "profile_photos");
        if (!dir.exists()) {
            assertTrue("Failed to create profile_photos directory", dir.mkdirs());
        }
    }

    @Test
    public void save_get_delete_ProfileImage_successFlow() throws Exception {
        UUID uid = UUID.randomUUID();
        byte[] data = new byte[8 * 1024];
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        // Save
        String path = manager.saveProfileImage(uid, in);
        assertNotNull("saveProfileImage should return a non-null path", path);
        File saved = new File(path);
        assertTrue("Saved file should exsist", saved.exists());
        assertTrue("Parent directory should be profile_photos", saved.getParentFile().getName().equals("profile_photos"));
        assertTrue("File name should contain the user UUID", saved.getName().contains(uid.toString()));

        File got = manager.getProfileImage(uid);
        assertNotNull("getProfileImage should return a file", got);
        assertEquals("Returned file path should match the saved file", saved.getAbsolutePath(), got.getAbsolutePath());

        // Delete
        manager.deleteProfileImage(uid);
        assertFalse("File should not exsist after deletion", saved.exists());
        assertNull("getProfileImage should return null after deletion", manager.getProfileImage(uid));
    }
}
