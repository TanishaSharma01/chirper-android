// PersistentBootstrap.java
package com.example.hackathon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.hackathon.persistentdata.io.AndroidIOFactory;

import java.io.InputStream;
import java.io.OutputStream;

public final class PersistentBootstrap {
    private PersistentBootstrap() {}
    private static final String TAG = "USERS";

    public static void ensureSeedUsers(Context ctx) {
        try {
            AndroidIOFactory io = new AndroidIOFactory(ctx);
            String abs = io.fileFor("users").getAbsolutePath();
            Log.d(TAG, "filesDir=" + ctx.getFilesDir().getAbsolutePath());
            Log.d(TAG, "users.csv path: " + abs + " exists=" + io.fileFor("users").exists());

            SharedPreferences sp = ctx.getSharedPreferences("bootstrap", Context.MODE_PRIVATE);
            if (io.fileFor("users").exists()) {
                sp.edit().putBoolean("users_seeded", true).apply();
                Log.d(TAG, "users.csv already present");
                return;
            }

            AssetManager am = ctx.getAssets();
            try (InputStream in = am.open("users.csv");
                 OutputStream out = io.rawOutput("users", false)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
                Log.d(TAG, "Copied assets/users.csv → " + abs);
            }

            sp.edit().putBoolean("users_seeded", true).apply();
        } catch (Exception e) {
            Log.e("USERS", "Bootstrap failed: " + e.getMessage(), e);
        }
    }
}
