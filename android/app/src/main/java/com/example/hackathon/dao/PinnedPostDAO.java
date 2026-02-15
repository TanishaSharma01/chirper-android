package com.example.hackathon.dao;

import com.example.hackathon.dao.model.PinnedPost;

import java.util.*;

/**
 * DAO for managing pinned posts
 */
public class PinnedPostDAO {
    private static PinnedPostDAO instance;
    private final List<PinnedPost> pinnedPosts;

    private PinnedPostDAO() {
        this.pinnedPosts = new ArrayList<>();
    }

    public static synchronized PinnedPostDAO getInstance() {
        if (instance == null) {
            instance = new PinnedPostDAO();
        }
        return instance;
    }

    public void add(PinnedPost pinnedPost) {
        if (!pinnedPosts.contains(pinnedPost)) {
            pinnedPosts.add(pinnedPost);
        }
    }

    public void remove(UUID userId, UUID postId) {
        pinnedPosts.removeIf(p -> p.getUserId().equals(userId) && p.getPostId().equals(postId));
    }

    public boolean isPinned(UUID userId, UUID postId) {
        return pinnedPosts.stream()
                .anyMatch(p -> p.getUserId().equals(userId) && p.getPostId().equals(postId));
    }

    public Set<UUID> getPinnedPostIds(UUID userId) {
        Set<UUID> postIds = new HashSet<>();
        for (PinnedPost p : pinnedPosts) {
            if (p.getUserId().equals(userId)) {
                postIds.add(p.getPostId());
            }
        }
        return postIds;
    }

    public Iterator<PinnedPost> getAll() {
        return new ArrayList<>(pinnedPosts).iterator();
    }

    public void clear() {
        pinnedPosts.clear();
    }
}
