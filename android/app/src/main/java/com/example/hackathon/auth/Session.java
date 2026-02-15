// Session.java
package com.example.hackathon.auth;

import android.content.Context;

import java.util.UUID;

public final class Session {
    private Session() {
    }

    private static final String PREFS = "session";
    private static final String KEY_USER_ID = "current_user_id";

    public static void save(Context ctx, UUID id) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_USER_ID, id != null ? id.toString() : null).apply();
    }

    public static UUID load(Context ctx) {
        String s = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USER_ID, null);
        return (s == null || s.isBlank()) ? null : UUID.fromString(s);
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_USER_ID).apply();
    }
}
