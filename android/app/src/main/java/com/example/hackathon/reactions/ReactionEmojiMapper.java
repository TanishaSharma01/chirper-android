package com.example.hackathon.reactions;

import java.util.HashMap;
import java.util.Map;

public class ReactionEmojiMapper {

    private static final Map<ReactionType, String> emojiMap = new HashMap<>();

    static {
        emojiMap.put(ReactionType.LIKE, "👍");
        emojiMap.put(ReactionType.LOVE, "❤️");
        emojiMap.put(ReactionType.LAUGH, "😂");
        emojiMap.put(ReactionType.SURPRISE, "😮");
        emojiMap.put(ReactionType.SAD, "😢");
        emojiMap.put(ReactionType.ANGRY, "😡");
        emojiMap.put(ReactionType.HAPPY, "😊");
        emojiMap.put(ReactionType.GOOD_LUCK, "🍀");
        emojiMap.put(ReactionType.CONGRATULATIONS, "🎉");
        }

    public static String getEmoji(ReactionType type) {
        return emojiMap.getOrDefault(type, "❓");
    }
}
