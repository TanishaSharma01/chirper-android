package com.example.hackathon.dao;

import com.example.hackathon.dao.model.Post;
import com.example.hackathon.dao.model.Message;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Comparator for Posts that sorts by most recent first (reverse chronological order).
 * Uses the timestamp of the most recent message in the post.
 */
public class PostComparator implements Comparator<Post> {
    private static PostComparator instance;

    public static PostComparator getInstance() {
        if (instance == null) instance = new PostComparator();
        return instance;
    }

    private PostComparator() {}

    @Override
    public int compare(Post o1, Post o2) {
        // Get the most recent message timestamp for each post
        long timestamp1 = getLatestTimestamp(o1);
        long timestamp2 = getLatestTimestamp(o2);

        // Sort in REVERSE order (newest first)
        int delta = Long.compare(timestamp2, timestamp1);
        if (delta != 0) return delta;

        // If timestamps are equal, compare by post ID
        return o1.id.compareTo(o2.id);
    }

    /**
     * Gets the timestamp of the most recent message in a post.
     * If no messages exist, uses 0.
     */
    private long getLatestTimestamp(Post post) {
        if (post.messages == null) {
            return 0;
        }

        long latestTimestamp = 0;
        Iterator<Message> messageIterator = post.messages.getAll();

        while (messageIterator.hasNext()) {
            Message message = messageIterator.next();
            if (message.timestamp() > latestTimestamp) {
                latestTimestamp = message.timestamp();
            }
        }

        return latestTimestamp;
    }
}
