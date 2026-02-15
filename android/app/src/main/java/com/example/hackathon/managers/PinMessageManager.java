package com.example.hackathon.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.hackathon.auth.Session;
import com.example.hackathon.dao.MessageComparator;
import com.example.hackathon.dao.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages pinned messages per user per thread using SharedPreferences
 */
public class PinMessageManager {
    private static final String TAG = "PinMessageManager";
    private static PinMessageManager instance;
    private Map<UUID, Set<UUID>> pinnedMessagesByThread;
    private SharedPreferences prefs;
    private Context context;
    private UUID currentUserId;

    private static final String PREFS_NAME = "pinned_messages";

    private PinMessageManager(Context context) {
        this.context = context.getApplicationContext();
        pinnedMessagesByThread = new HashMap<>();
        prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadPinnedMessagesForCurrentUser();
    }

    public static synchronized PinMessageManager getInstance(Context context) {
        if (instance == null) {
            instance = new PinMessageManager(context);
        } else {
            // Check if user changed
            instance.checkAndRefreshUser();
        }
        return instance;
    }

    public static PinMessageManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PinMessageManager must be initialized with context first");
        }
        instance.checkAndRefreshUser();
        return instance;
    }

    /**
     * Checks if the user has changed and refreshes pins accordingly
     */
    private void checkAndRefreshUser() {
        UUID sessionUserId = Session.load(context);

        // If user changed, reload pins
        if ((currentUserId == null && sessionUserId != null) ||
                (currentUserId != null && !currentUserId.equals(sessionUserId))) {

            Log.d(TAG, "User changed from " + currentUserId + " to " + sessionUserId + ", reloading pins");
            loadPinnedMessagesForCurrentUser();
        }
    }

    /**
     * Loads pinned messages for the currently logged-in user
     */
    private void loadPinnedMessagesForCurrentUser() {
        currentUserId = Session.load(context);
        pinnedMessagesByThread.clear();

        if (currentUserId == null) {
            Log.w(TAG, "No user logged in, using empty pinned messages");
            return;
        }

        String userKeyPrefix = "user_" + currentUserId.toString() + "_thread_";

        Log.d(TAG, "Loading pinned messages for user " + currentUserId);

        Map<String, ?> allEntries = prefs.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();

            // Check if this key belongs to current user
            if (key.startsWith(userKeyPrefix)) {
                try {
                    // Extract thread UUID from key: user_{userId}_thread_{threadId}
                    String threadIdString = key.substring(userKeyPrefix.length());
                    UUID threadId = UUID.fromString(threadIdString);

                    // Get pinned message IDs for this thread
                    @SuppressWarnings("unchecked")
                    Set<String> pinnedMessageIds = (Set<String>) entry.getValue();
                    Set<UUID> messageUUIDs = new HashSet<>();

                    for (String msgId : pinnedMessageIds) {
                        messageUUIDs.add(UUID.fromString(msgId));
                    }

                    pinnedMessagesByThread.put(threadId, messageUUIDs);
                    Log.d(TAG, "Loaded " + messageUUIDs.size() + " pinned messages for thread " + threadId);

                } catch (Exception e) {
                    Log.e(TAG, "Error loading pinned messages for key: " + key, e);
                }
            }
        }
    }

    /**
     * Saves pinned messages for a specific thread for current user
     */
    private void savePinnedMessagesForThread(UUID threadId) {
        if (currentUserId == null) {
            Log.w(TAG, "No user logged in, cannot save pins");
            return;
        }

        Set<UUID> pinnedMessages = pinnedMessagesByThread.get(threadId);
        String key = "user_" + currentUserId.toString() + "_thread_" + threadId.toString();

        if (pinnedMessages == null || pinnedMessages.isEmpty()) {
            prefs.edit().remove(key).apply();
            Log.d(TAG, "Removed pinned messages for thread " + threadId);
        } else {
            Set<String> messageIdStrings = new HashSet<>();
            for (UUID msgId : pinnedMessages) {
                messageIdStrings.add(msgId.toString());
            }

            // IMPORTANT: Create new HashSet for SharedPreferences
            Set<String> newSet = new HashSet<>(messageIdStrings);
            boolean success = prefs.edit().putStringSet(key, newSet).commit();

            Log.d(TAG, "Saved " + pinnedMessages.size() + " pinned messages for user " +
                    currentUserId + " in thread " + threadId + ". Success: " + success);
        }
    }

    /**
     * Pins a message in a specific thread
     */
    public void pinMessage(UUID threadId, Message message) {
        checkAndRefreshUser();

        Log.d(TAG, "Pinning message " + message.id() + " in thread " + threadId + " for user " + currentUserId);

        Set<UUID> pinnedMessages = pinnedMessagesByThread.computeIfAbsent(threadId, k -> new HashSet<>());
        pinnedMessages.add(message.id());

        savePinnedMessagesForThread(threadId);
    }

    /**
     * Unpins a message from a specific thread
     */
    public void unpinMessage(UUID threadId, Message message) {
        checkAndRefreshUser();

        Log.d(TAG, "Unpinning message " + message.id() + " from thread " + threadId + " for user " + currentUserId);

        Set<UUID> pinnedMessages = pinnedMessagesByThread.get(threadId);
        if (pinnedMessages != null) {
            pinnedMessages.remove(message.id());

            if (pinnedMessages.isEmpty()) {
                pinnedMessagesByThread.remove(threadId);
            }

            savePinnedMessagesForThread(threadId);
        }
    }

    /**
     * Checks if a message is pinned for current user
     */
    public boolean isPinned(UUID threadId, Message message) {
        checkAndRefreshUser();

        Set<UUID> pinnedMessages = pinnedMessagesByThread.get(threadId);
        return pinnedMessages != null && pinnedMessages.contains(message.id());
    }

    /**
     * Sorts messages with pinned messages first
     */
    public void sortWithPinnedFirst(UUID threadId, List<Message> messages) {
        checkAndRefreshUser();

        Log.d(TAG, "Sorting messages for thread " + threadId + " for user " + currentUserId);

        List<Message> pinnedMessages = new ArrayList<>();
        List<Message> unpinnedMessages = new ArrayList<>();

        for (Message message : messages) {
            if (isPinned(threadId, message)) {
                pinnedMessages.add(message);
            } else {
                unpinnedMessages.add(message);
            }
        }

        pinnedMessages.sort(MessageComparator.getInstance());
        unpinnedMessages.sort(MessageComparator.getInstance());

        messages.clear();
        messages.addAll(pinnedMessages);
        messages.addAll(unpinnedMessages);
    }

    /**
     * Clears all pinned messages for current user
     */
    public void clearAllPinnedMessages() {
        checkAndRefreshUser();

        Log.d(TAG, "Clearing all pinned messages for user " + currentUserId);
        pinnedMessagesByThread.clear();

        if (currentUserId != null) {
            String userKeyPrefix = "user_" + currentUserId.toString();
            Map<String, ?> allEntries = prefs.getAll();
            SharedPreferences.Editor editor = prefs.edit();

            for (String key : allEntries.keySet()) {
                if (key.startsWith(userKeyPrefix)) {
                    editor.remove(key);
                }
            }

            editor.apply();
        }
    }
}
