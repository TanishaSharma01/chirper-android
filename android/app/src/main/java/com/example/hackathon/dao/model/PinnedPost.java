package com.example.hackathon.dao.model;

import java.util.UUID;

/**
 * Represents a pinned post for a specific user
 */
public class PinnedPost {
    private final UUID userId;
    private final UUID postId;

    public PinnedPost(UUID userId, UUID postId) {
        this.userId = userId;
        this.postId = postId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getPostId() {
        return postId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PinnedPost that = (PinnedPost) o;
        return userId.equals(that.userId) && postId.equals(that.postId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode() * 31 + postId.hashCode();
    }
}
