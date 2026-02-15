package com.example.hackathon.managers;

import android.content.Context;
import android.util.Log;

import com.example.hackathon.auth.Session;
import com.example.hackathon.dao.PinnedPostDAO;
import com.example.hackathon.dao.model.PinnedPost;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.persistentdata.DataManager;
import com.example.hackathon.persistentdata.io.AndroidIOFactory;

import java.util.*;

public class PinPostManager {
    private static final String TAG = "PinPostManager";
    private static PinPostManager instance;

    private final Context context;
    private UUID currentUserId;

    private PinPostManager(Context context) {
        this.context = context.getApplicationContext();
        loadCurrentUser();
    }

    public static synchronized PinPostManager getInstance(Context context) {
        if (instance == null) {
            instance = new PinPostManager(context);
        }
        return instance;
    }

    private void loadCurrentUser() {
        currentUserId = Session.load(context);
    }

    public boolean isPinned(Post post) {
        if (currentUserId == null) return false;
        return PinnedPostDAO.getInstance().isPinned(currentUserId, post.getUUID());
    }

    public int pinPost(List<Post> posts, Post post, int currentPosition) {
        if (currentUserId == null) return currentPosition;

        PinnedPostDAO.getInstance().add(new PinnedPost(currentUserId, post.getUUID()));
        savePinnedPosts();

        posts.remove(currentPosition);
        posts.add(0, post);

        Log.d(TAG, "Pinned post: " + post.topic);
        return 0;
    }

    public int unpinPostAndReposition(List<Post> posts, Post post, int currentPosition) {
        if (currentUserId == null) return currentPosition;

        PinnedPostDAO.getInstance().remove(currentUserId, post.getUUID());
        savePinnedPosts();

        posts.remove(currentPosition);

        int newPosition = 0;
        for (Post p : posts) {
            if (!isPinned(p)) {
                break;
            }
            newPosition++;
        }

        posts.add(newPosition, post);

        Log.d(TAG, "Unpinned post: " + post.topic);
        return newPosition;
    }

    public void sortWithPinnedFirst(List<Post> posts) {
        loadCurrentUser();

        if (currentUserId == null) {
            Log.d(TAG, "No current user, skipping pin sort");
            return;
        }

        List<Post> pinned = new ArrayList<>();
        List<Post> unpinned = new ArrayList<>();

        for (Post post : posts) {
            if (isPinned(post)) {
                pinned.add(post);
            } else {
                unpinned.add(post);
            }
        }

        posts.clear();
        posts.addAll(pinned);
        posts.addAll(unpinned);

        Log.d(TAG, "Sorting " + posts.size() + " posts with pinned first for user " + currentUserId);
        Log.d(TAG, "Found " + pinned.size() + " pinned posts and " + unpinned.size() + " unpinned posts");
    }

    private void savePinnedPosts() {
        DataManager dm = new DataManager(new AndroidIOFactory(context));
        try {
            dm.writeAll();
            Log.d(TAG, "Saved pinned posts to CSV");
        } catch (Exception e) {
            Log.e(TAG, "Error saving pinned posts", e);
        }
    }

    /**
     * Refresh user ID - call this after login
     */
    public void refresh() {
        loadCurrentUser();
    }
}
