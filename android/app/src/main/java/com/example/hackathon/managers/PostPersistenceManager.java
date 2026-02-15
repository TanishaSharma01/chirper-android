package com.example.hackathon.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.hackathon.dao.PostDAO;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.dao.model.Post;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Manages persistence of Posts AND Messages to SharedPreferences using JSON
 */
public class PostPersistenceManager {
    private static final String TAG = "PostPersistence";
    private static final String PREFS_NAME = "post_data";
    private static final String KEY_POSTS = "posts_json";
    private static PostPersistenceManager instance;
    private SharedPreferences prefs;
    private Gson gson;

    private PostPersistenceManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized PostPersistenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PostPersistenceManager(context);
        }
        return instance;
    }

    /**
     * Saves all posts with their messages from PostDAO to SharedPreferences
     */
    public void savePosts() {
        List<PostWithMessages> postDataList = new ArrayList<>();
        Iterator<Post> iterator = PostDAO.getInstance().getAll();

        while (iterator.hasNext()) {
            Post post = iterator.next();

            // Extract messages from SortedData
            List<MessageData> messageDataList = new ArrayList<>();
            if (post.messages != null) {
                Iterator<Message> msgIterator = post.messages.getAll();
                while (msgIterator.hasNext()) {
                    Message msg = msgIterator.next();
                    MessageData msgData = new MessageData(
                            msg.id().toString(),
                            msg.poster().toString(),
                            msg.thread().toString(),
                            msg.timestamp(),
                            msg.message()
                    );
                    messageDataList.add(msgData);
                }
            }

            // Create post data with messages
            PostWithMessages data = new PostWithMessages(
                    post.id.toString(),
                    post.poster != null ? post.poster.toString() : null,
                    post.topic,
                    messageDataList
            );
            postDataList.add(data);
        }

        Log.d(TAG, "Saving " + postDataList.size() + " posts with messages");

        // Convert to JSON
        String postsJson = gson.toJson(postDataList);

        // Save to SharedPreferences
//        boolean success = prefs.edit().putString(KEY_POSTS, postsJson).commit();
        prefs.edit().putString(KEY_POSTS, postsJson).apply();
        Log.d(TAG, "Post save operation requested.");
//        Log.d(TAG, "Save " + (success ? "successful" : "failed"));
    }

    /**
     * Loads posts with messages from SharedPreferences
     */
    public boolean loadPosts() {
        String postsJson = prefs.getString(KEY_POSTS, null);

        if (postsJson == null || postsJson.isEmpty()) {
            Log.d(TAG, "No saved posts found");
            return false;
        }

        try {
            JsonArray jsonArray = JsonParser.parseString(postsJson).getAsJsonArray();

            Log.d(TAG, "Loading " + jsonArray.size() + " posts from SharedPreferences");

            // Clear PostDAO
            PostDAO.getInstance().clear();

            for (JsonElement element : jsonArray) {
                JsonObject postObj = element.getAsJsonObject();

                UUID postId = UUID.fromString(postObj.get("id").getAsString());
                UUID posterId = postObj.has("posterId") && !postObj.get("posterId").isJsonNull()
                        ? UUID.fromString(postObj.get("posterId").getAsString())
                        : null;
                String topic = postObj.get("topic").getAsString();

                // Create post using the constructor
                Post post = new Post(postId, posterId, topic);

                // Restore messages
                JsonArray messagesArray = postObj.getAsJsonArray("messages");
                if (messagesArray != null) {
                    for (JsonElement msgElement : messagesArray) {
                        JsonObject msgObj = msgElement.getAsJsonObject();

                        UUID msgId = UUID.fromString(msgObj.get("id").getAsString());
                        UUID msgPosterId = UUID.fromString(msgObj.get("posterId").getAsString());
                        UUID msgThreadId = UUID.fromString(msgObj.get("threadId").getAsString());
                        long msgTimestamp = msgObj.get("timestamp").getAsLong();
                        String msgText = msgObj.get("message").getAsString();

                        // Create Message using the record constructor
                        Message message = new Message(msgId, msgPosterId, msgThreadId, msgTimestamp, msgText);
                        post.messages.insert(message);
                    }
                }

                PostDAO.getInstance().add(post);

                Log.d(TAG, "Restored post: " + postId + " - " + topic + " with " +
                        (messagesArray != null ? messagesArray.size() : 0) + " messages");
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error loading posts: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if saved posts exist
     */
    public boolean hasSavedPosts() {
        String postsJson = prefs.getString(KEY_POSTS, null);
        return postsJson != null && !postsJson.isEmpty();
    }

    /**
     * Clears all saved posts
     */
    public void clearSavedPosts() {
        Log.d(TAG, "Clearing saved posts");
        prefs.edit().remove(KEY_POSTS).apply();
    }

    /**
     * Data class for saving post with messages
     */
    private static class PostWithMessages {
        String id;
        String posterId;
        String topic;
        List<MessageData> messages;

        PostWithMessages(String id, String posterId, String topic, List<MessageData> messages) {
            this.id = id;
            this.posterId = posterId;
            this.topic = topic;
            this.messages = messages;
        }
    }

    /**
     * Data class for saving message data
     * Matches Message record: (UUID id, UUID poster, UUID thread, long timestamp, String message)
     */
    private static class MessageData {
        String id;
        String posterId;
        String threadId;
        long timestamp;
        String message;

        MessageData(String id, String posterId, String threadId, long timestamp, String message) {
            this.id = id;
            this.posterId = posterId;
            this.threadId = threadId;
            this.timestamp = timestamp;
            this.message = message;
        }
    }
}
