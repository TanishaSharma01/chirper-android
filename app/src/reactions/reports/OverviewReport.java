package reactions.reports;

import dao.ReactionDao;
import dao.model.Message;
import dao.model.UserReactions;
import reactions.ReactionDisplayTag;
import reactions.ReactionType;

import java.util.*;

public class OverviewReport extends Report {

    @Override
    public ReactionDisplayTag[] generateHelper(Message message) {
        if (message == null) return new ReactionDisplayTag[0];

        // Map: ReactionType -> (count, oldestTimestamp)
        Map<ReactionType, ReactionStats> stats = new EnumMap<>(ReactionType.class);

        for (Iterator<UserReactions> ur = ReactionDao.getInstance().getAll(); ur.hasNext(); ) {
            UserReactions userReactions = ur.next();
            Map<ReactionType, Long> reactions = userReactions.getReactionsByMessage().get(message.id());
            if (reactions != null) {
                for (Map.Entry<ReactionType, Long> entry : reactions.entrySet()) {
                    ReactionType type = entry.getKey();
                    long timestamp = entry.getValue();
                    stats.compute(type, (_, v) -> {
                        if (v == null) return new ReactionStats(1, timestamp);
                        v.count++;
                        v.oldestTimestamp = Math.min(v.oldestTimestamp, timestamp);
                        return v;
                    });
                }
            }
        }

        // Sort by count descending, then by oldest timestamp ascending
        List<Map.Entry<ReactionType, ReactionStats>> sorted = new ArrayList<>(stats.entrySet());
        sorted.sort((a, b) -> {
            int cmp = Integer.compare(b.getValue().count, a.getValue().count);
            if (cmp != 0) return cmp;
            return Long.compare(a.getValue().oldestTimestamp, b.getValue().oldestTimestamp);
        });

        int reportSize = Math.min(5, sorted.size());
        ReactionDisplayTag[] result = new ReactionDisplayTag[reportSize];
        for (int i = 0; i < reportSize; i++) {
            ReactionType type = sorted.get(i).getKey();
            int count = sorted.get(i).getValue().count;
            result[i] = new ReactionDisplayTag(type, String.valueOf(count));
        }

        return result;
    }

    private static class ReactionStats {
        int count;
        long oldestTimestamp;
        ReactionStats(int count, long oldestTimestamp) {
            this.count = count;
            this.oldestTimestamp = oldestTimestamp;
        }
    }
}

