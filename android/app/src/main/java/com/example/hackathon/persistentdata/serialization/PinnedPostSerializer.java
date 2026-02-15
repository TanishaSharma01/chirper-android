package com.example.hackathon.persistentdata.serialization;

import com.example.hackathon.dao.model.PinnedPost;

import java.util.UUID;

/**
 * Serializes pinned posts as "userId,postId"
 */
public class PinnedPostSerializer implements Serializer<PinnedPost, String[]> {

    @Override
    public PinnedPost deserialize(String[] parts) {
        if (parts.length >= 2) {
            UUID userId = UUID.fromString(parts[0]);
            UUID postId = UUID.fromString(parts[1]);
            return new PinnedPost(userId, postId);
        }
        return null;
    }

    @Override
    public String[] serialize(PinnedPost pinnedPost) {
        return new String[]{
                pinnedPost.getUserId().toString(),
                pinnedPost.getPostId().toString()
        };
    }
}
