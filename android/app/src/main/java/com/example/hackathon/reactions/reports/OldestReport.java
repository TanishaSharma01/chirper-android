package  com.example.hackathon.reactions.reports;

import com.example.hackathon.dao.ReactionDAO;
import  com.example.hackathon.dao.UserDAO;
import  com.example.hackathon.dao.model.Message;
import  com.example.hackathon.dao.model.User;
import  com.example.hackathon.dao.model.UserReactions;
import  com.example.hackathon.reactions.ReactionDisplayTag;
import  com.example.hackathon.reactions.ReactionType;

import java.util.*;

public class OldestReport extends Report {

    @Override
    public ReactionDisplayTag[] generateHelper(Message message) {
        if (message == null) return new ReactionDisplayTag[0];

        // Map: userId -> (reactionType, timestamp)
        Map<UUID, ReactionEntry> oldestPerUser = new HashMap<>();

        for (Iterator<UserReactions> it = ReactionDAO.getInstance().getAll(); it.hasNext(); ) {
            UserReactions userReactions = it.next();
            Map<ReactionType, Long> reactions = userReactions.getReactionsByMessage().get(message.id());
            if (reactions != null) {
                UUID userId = userReactions.getUserId();
                ReactionType oldestType = null;
                long oldestTime = Long.MAX_VALUE;
                for (Map.Entry<ReactionType, Long> entry : reactions.entrySet()) {
                    if (entry.getValue() < oldestTime) {
                        oldestTime = entry.getValue();
                        oldestType = entry.getKey();
                    }
                }
                if (oldestType != null) {
                    oldestPerUser.put(userId, new ReactionEntry(oldestType, oldestTime));
                }
            }
        }

        // Sort by timestamp ascending
        List<Map.Entry<UUID, ReactionEntry>> sorted = new ArrayList<>(oldestPerUser.entrySet());
        sorted.sort(Comparator.comparingLong(e -> e.getValue().timestamp));

        int reportSize = Math.min(5, sorted.size());
        ReactionDisplayTag[] tags = new ReactionDisplayTag[reportSize];
        for (int i = 0; i < reportSize; i++) {
            Map.Entry<UUID, ReactionEntry> e = sorted.get(i);
            ReactionType type = e.getValue().type;
            User user = UserDAO.getInstance().getByUUID(e.getKey());
            String username = (user != null && user.username() != null) ? user.username() : "Unknown";
            tags[i] = new ReactionDisplayTag(type, username);
        }

        return tags;
    }

    private static class ReactionEntry {
        ReactionType type;
        long timestamp;
        ReactionEntry(ReactionType type, long timestamp) {
            this.type = type;
            this.timestamp = timestamp;
        }
    }
}
