package com.example.hackathon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.reactions.ReactionType;
import com.example.hackathon.reactions.ReactionsFacade;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MessageFragment extends Fragment {

    private static final String ARG_MESSAGE_CONTENT = "message_content";
    private static final String ARG_MESSAGE_AUTHOR = "message_author";
    private static final String ARG_MESSAGE_TIMESTAMP = "message_timestamp";
    private static final String ARG_MESSAGE_UUID = "message_uuid"; // use Bundle key, not static var

    private ImageView imageViewAuthor;
    private TextView textViewContent;
    private TextView textViewAuthor;
    private TextView textViewTimestamp;

    private TextView emojiLike, emojiLove, emojiLaugh, emojiHappy, emojiGoodLuck, emojiCongrats;
    private User currentUser;
    private UUID messageUUID;

    private TextView emojiSurprise, emojiSad, emojiAngry;
    private TextView reactionCounts;      // already added previously
    private View emojiBar;                // picker container
    private TextView selectedReaction;

    // map ReactionType -> emoji char for display
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


    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance(Message message) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();

        String authorName = getAuthorName(message.poster());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String formattedTime = dateFormat.format(new Date(message.timestamp()));

        args.putString(ARG_MESSAGE_CONTENT, message.message());
        args.putString(ARG_MESSAGE_AUTHOR, authorName);
        args.putString(ARG_MESSAGE_TIMESTAMP, formattedTime);
        args.putString(ARG_MESSAGE_UUID, message.id().toString());
        fragment.setArguments(args);

        return fragment;
    }

    private static String getAuthorName(UUID posterUUID) {
        try {
            User author = UserDAO.getInstance().getByUUID(posterUUID);
            return author != null ? author.username() : "Unknown User";
        } catch (Exception e) {
            return "Unknown User";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        imageViewAuthor = view.findViewById(R.id.imageViewAuthor);
        textViewContent = view.findViewById(R.id.textViewMessageContent);
        textViewAuthor = view.findViewById(R.id.textViewMessageAuthor);
        textViewTimestamp = view.findViewById(R.id.textViewMessageTimestamp);

        displayMessageData();

        reactionCounts   = view.findViewById(R.id.reactionCounts);
        emojiBar         = view.findViewById(R.id.emojiBar);
        selectedReaction = view.findViewById(R.id.selectedReaction);

        emojiLike      = view.findViewById(R.id.emojiLike);
        emojiLove      = view.findViewById(R.id.emojiLove);
        emojiLaugh     = view.findViewById(R.id.emojiLaugh);
        emojiHappy     = view.findViewById(R.id.emojiHappy);
        emojiGoodLuck  = view.findViewById(R.id.emojiGoodLuck);
        emojiCongrats  = view.findViewById(R.id.emojiCongrats);
        emojiSurprise  = view.findViewById(R.id.emojiSurprise);
        emojiSad       = view.findViewById(R.id.emojiSad);
        emojiAngry     = view.findViewById(R.id.emojiAngry);

        currentUser = UserDAO.getInstance().getCurrentUser();
        if (getArguments() != null) {
            messageUUID = UUID.fromString(getArguments().getString(ARG_MESSAGE_UUID));
        }

        displayMessageData();

        setupEmoji(emojiLike,     ReactionType.LIKE);
        setupEmoji(emojiLove,     ReactionType.LOVE);
        setupEmoji(emojiLaugh,    ReactionType.LAUGH);
        setupEmoji(emojiHappy,    ReactionType.HAPPY);
        setupEmoji(emojiGoodLuck, ReactionType.GOOD_LUCK);
        setupEmoji(emojiCongrats, ReactionType.CONGRATULATIONS);
        setupEmoji(emojiSurprise, ReactionType.SURPRISE);
        setupEmoji(emojiSad,      ReactionType.SAD);
        setupEmoji(emojiAngry,    ReactionType.ANGRY);

        selectedReaction.setOnClickListener(v -> {
            if (currentUser == null || messageUUID == null) return;
            List<ReactionType> existing = ReactionsFacade.getReactions(currentUser.id(), messageUUID);
            if (existing != null) {
                for (ReactionType r : existing) {
                    ReactionsFacade.removeReaction(currentUser.id(), messageUUID, r);
                }
            }
            updateReactionUI();
        });

        updateReactionUI();
        return view;
    }

    private void displayMessageData() {
        if (getArguments() == null) return;

        String content = getArguments().getString(ARG_MESSAGE_CONTENT);
        String author = getArguments().getString(ARG_MESSAGE_AUTHOR);
        String timestamp = getArguments().getString(ARG_MESSAGE_TIMESTAMP);

        if (textViewContent != null) textViewContent.setText(content);
        if (textViewAuthor != null) textViewAuthor.setText(author);
        if (textViewTimestamp != null) textViewTimestamp.setText(timestamp);

        try {
            // Load author's profile image
            User authorObj = UserDAO.getInstance().getByUUID(UserDAO.getInstance().getByUUID(UUID.fromString(getArguments().getString(ARG_MESSAGE_UUID))).id());
            if (authorObj != null && imageViewAuthor != null) {
                Glide.with(requireContext())
                        .load(authorObj.profilePictureUrl())
                        .placeholder(R.drawable.default_profile)
                        .circleCrop()
                        .into(imageViewAuthor);
            }
        } catch (Exception e) {
            imageViewAuthor.setImageResource(R.drawable.default_profile);
        }
    }

    private void setupEmoji(TextView emojiView, ReactionType type) {
        emojiView.setOnClickListener(v -> {
            if (currentUser == null || messageUUID == null) return;

            List<ReactionType> mine = ReactionsFacade.getReactions(currentUser.id(), messageUUID);
            boolean alreadySelected = mine != null && mine.size() == 1 && mine.contains(type);

            if (alreadySelected) {
                ReactionsFacade.removeReaction(currentUser.id(), messageUUID, type);
            } else {
                if (mine != null) {
                    for (ReactionType r : mine) {
                        if (r != type) {
                            ReactionsFacade.removeReaction(currentUser.id(), messageUUID, r);
                        }
                    }
                }
                if (mine == null || !mine.contains(type)) {
                    ReactionsFacade.addReaction(currentUser.id(), messageUUID, type, System.currentTimeMillis());
                }
            }
            updateReactionUI();
        });
    }

    private void updateReactionUI() {
        updateCountsRow();

        if (currentUser == null || messageUUID == null) {
            emojiBar.setVisibility(View.VISIBLE);
            selectedReaction.setVisibility(View.GONE);
            dimAll();
            return;
        }

        List<ReactionType> mine = ReactionsFacade.getReactions(currentUser.id(), messageUUID);
        if (mine == null || mine.isEmpty()) {
            // show picker
            emojiBar.setVisibility(View.VISIBLE);
            selectedReaction.setVisibility(View.GONE);
            dimAll();
        } else {
            ReactionType selected = mine.get(mine.size() - 1); // newest selection
            String emoji = emojiMap.get(selected);
            selectedReaction.setText(emoji != null ? emoji : "•");
            selectedReaction.setVisibility(View.VISIBLE);
            emojiBar.setVisibility(View.GONE);
        }
    }

    private void dimAll() {
        resetEmojiStyle(emojiLike);
        resetEmojiStyle(emojiLove);
        resetEmojiStyle(emojiLaugh);
        resetEmojiStyle(emojiHappy);
        resetEmojiStyle(emojiGoodLuck);
        resetEmojiStyle(emojiCongrats);
        resetEmojiStyle(emojiSurprise);
        resetEmojiStyle(emojiSad);
        resetEmojiStyle(emojiAngry);
    }

    private void updateCountsRow() {
        if (messageUUID == null || reactionCounts == null) return;

        // counters
        int like=0,love=0,laugh=0,happy=0,luck=0,congrats=0,surprise=0,sad=0,angry=0;

        // aggregate across users (uses existing API)
        for (var it = UserDAO.getInstance().getAll(); it.hasNext();) {
            User u = it.next();
            List<ReactionType> r = ReactionsFacade.getReactions(u.id(), messageUUID);
            if (r == null) continue;
            for (ReactionType t : r) {
                switch (t) {
                    case LIKE -> like++;
                    case LOVE -> love++;
                    case LAUGH -> laugh++;
                    case HAPPY -> happy++;
                    case GOOD_LUCK -> luck++;
                    case CONGRATULATIONS -> congrats++;
                    case SURPRISE -> surprise++;      // NEW
                    case SAD -> sad++;                // NEW
                    case ANGRY -> angry++;            // NEW
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        append(sb, emojiMap.get(ReactionType.LOVE), love);
        append(sb, emojiMap.get(ReactionType.LIKE), like);
        append(sb, emojiMap.get(ReactionType.LAUGH), laugh);
        append(sb, emojiMap.get(ReactionType.HAPPY), happy);
        append(sb, emojiMap.get(ReactionType.GOOD_LUCK), luck);
        append(sb, emojiMap.get(ReactionType.CONGRATULATIONS), congrats);
        append(sb, emojiMap.get(ReactionType.SURPRISE), surprise); // NEW
        append(sb, emojiMap.get(ReactionType.SAD), sad);           // NEW
        append(sb, emojiMap.get(ReactionType.ANGRY), angry);       // NEW

        reactionCounts.setText(sb.length() == 0 ? "" : sb.toString().trim());
        reactionCounts.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);
    }


    private void refreshEmojiHighlights() {
        if (currentUser == null || messageUUID == null) return;

        UUID userId = currentUser.id();
        List<ReactionType> reactions = ReactionsFacade.getReactions(userId, messageUUID);

        resetEmojiStyle(emojiLike);
        resetEmojiStyle(emojiLove);
        resetEmojiStyle(emojiLaugh);
        resetEmojiStyle(emojiHappy);
        resetEmojiStyle(emojiGoodLuck);
        resetEmojiStyle(emojiCongrats);

        for (ReactionType reaction : reactions) {
            switch (reaction) {
                case LIKE -> highlightEmoji(emojiLike);
                case LOVE -> highlightEmoji(emojiLove);
                case LAUGH -> highlightEmoji(emojiLaugh);
                case HAPPY -> highlightEmoji(emojiHappy);
                case GOOD_LUCK -> highlightEmoji(emojiGoodLuck);
                case CONGRATULATIONS -> highlightEmoji(emojiCongrats);
            }
        }
    }

    private void append(StringBuilder sb, String emoji, int count) {
        if (count > 0 && emoji != null) sb.append(emoji).append("×").append(count).append("  ");
    }


    private void highlightEmoji(TextView emoji) {
        emoji.setAlpha(1.0f);
        emoji.setBackgroundResource(R.drawable.reaction_highlight_bg);
    }

    private void resetEmojiStyle(TextView emoji) {
        emoji.setAlpha(0.6f);
        emoji.setBackgroundResource(0);
    }
}
