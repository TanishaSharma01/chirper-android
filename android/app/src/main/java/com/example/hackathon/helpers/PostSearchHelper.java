package com.example.hackathon.helpers;

import com.example.hackathon.dao.PostComparator;
import com.example.hackathon.dao.PostDAO;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.managers.PinPostManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for post searching and filtering functionality.
 * Eliminates code duplication between MainActivity and SearchActivity.
 */
public class PostSearchHelper {

    private final PinPostManager pinManager;

    public PostSearchHelper(PinPostManager pinManager) {
        this.pinManager = pinManager;
    }

    /**
     * Loads all posts from DAO, sorted by newest first with pinned at top
     */
    public List<Post> loadAllPosts() {
        List<Post> posts = new ArrayList<>();

        Iterator<Post> postIterator = PostDAO.getInstance().getAll();
        while (postIterator.hasNext()) {
            posts.add(postIterator.next());
        }

        // Sort posts by newest first
        posts.sort(PostComparator.getInstance());
        pinManager.sortWithPinnedFirst(posts);

        return posts;
    }

    /**
     * Filters posts based on search query
     * Searches in: post title, post messages, and author username
     */
    public List<Post> filterPosts(List<Post> posts, String query) {
        List<Post> filteredPosts = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            filteredPosts.addAll(posts);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();

            for (Post post : posts) {
                // Search in post title
                if (post.topic != null && post.topic.toLowerCase().contains(lowerCaseQuery)) {
                    filteredPosts.add(post);
                    continue;
                }

                // Search in post messages
                if (searchInMessages(post, lowerCaseQuery)) {
                    filteredPosts.add(post);
                    continue;
                }

                // Search in author username
                User author = UserDAO.getInstance().getByUUID(post.poster);
                if (author != null && author.username() != null &&
                        author.username().toLowerCase().contains(lowerCaseQuery)) {
                    filteredPosts.add(post);
                }
            }
        }

        // Sort filtered posts with pinned first
        pinManager.sortWithPinnedFirst(filteredPosts);

        return filteredPosts;
    }

    /**
     * Searches for query in post messages
     */
    private boolean searchInMessages(Post post, String query) {
        if (post.messages == null) {
            return false;
        }

        Iterator<Message> messageIterator = post.messages.getAll();
        while (messageIterator.hasNext()) {
            Message message = messageIterator.next();
            if (message.message() != null && message.message().toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }
}
