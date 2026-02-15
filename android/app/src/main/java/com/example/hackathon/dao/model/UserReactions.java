package com.example.hackathon.dao.model;

import com.example.hackathon.reactions.ReactionType;

import java.util.*;

public class UserReactions implements HasUUID{
    private final UUID userId;
    private final Set<UUID> reactedMessageIds;
    private final Map<UUID, Map<ReactionType, Long>> reactionsByMessage;

    public UserReactions(UUID userId) {
        this.userId = userId;
        this.reactedMessageIds = new HashSet<>();
        this.reactionsByMessage = new HashMap<>();
    }

    public void addReaction(UUID messageId, ReactionType reaction, long timestamp) {
        reactedMessageIds.add(messageId);
        reactionsByMessage.computeIfAbsent(messageId, k -> new HashMap<>()).put(reaction, timestamp);
    }

    public Set<UUID> getReactedMessageIds() {
        return reactedMessageIds;
    }

    public Map<UUID, Map<ReactionType, Long>> getReactionsByMessage() {
        return reactionsByMessage;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public UUID getUUID() {
        return userId;
    }

    public boolean removeReaction(UUID messageId, ReactionType reaction) {
        Map<ReactionType, Long> reactions = reactionsByMessage.get(messageId);
        if (reactions != null && reactions.remove(reaction) != null) {
            if (reactions.isEmpty()) {
                reactionsByMessage.remove(messageId);
                reactedMessageIds.remove(messageId);
            }
            return true;
        }
        return false;
    }
}
