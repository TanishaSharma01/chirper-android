package com.example.hackathon;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.helpers.ProfilePictureHelper;
import com.example.hackathon.managers.PinMessageManager;
import com.example.hackathon.reactions.ReactionType;
import com.example.hackathon.reactions.ReactionsFacade;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import androidx.core.content.ContextCompat;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Message[] localDataSet;
    private SimpleDateFormat dateFormat;
    private PinMessageManager pinManager;
    private UUID threadId;

    public MessageAdapter(ArrayList<Message> dataSet, UUID threadId) {
        this.localDataSet = dataSet.toArray(new Message[0]);
        this.dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        this.pinManager = PinMessageManager.getInstance();
        this.threadId = threadId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private TextView textViewContent;
        private TextView textViewAuthor;
        private TextView textViewTimestamp;
        private ImageView imageViewAuthor;
        private TextView reactionCounts;
        private View emojiBar;
        private TextView selectedReaction;
        private TextView emojiLike, emojiLove, emojiLaugh, emojiHappy, emojiGoodLuck, emojiCongrats, emojiSurprise, emojiSad, emojiAngry;

        private static final java.util.Map<ReactionType, String> emojiMap = new java.util.HashMap<>();
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

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            textViewContent = this.view.findViewById(R.id.textViewMessageContent);
            textViewAuthor = this.view.findViewById(R.id.textViewMessageAuthor);
            textViewTimestamp = this.view.findViewById(R.id.textViewMessageTimestamp);
            imageViewAuthor = this.view.findViewById(R.id.imageViewAuthor);

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
        }

        public void display(Message message, boolean isPinned, OnAuthorClickListener authorClickListener) {
            // Visual indicator for pinned messages
            if (isPinned) {
                view.setBackgroundColor(
                        ContextCompat.getColor(view.getContext(), R.color.pinnedMessageBackground));
                textViewContent.setText("📌 " + message.message());
            } else {
                view.setBackgroundColor(
                        ContextCompat.getColor(view.getContext(), R.color.cardBackground));
                textViewContent.setText(message.message());
            }

            String authorName = getAuthorName(message.poster());
            textViewAuthor.setText(authorName);

            // Make author name clickable - underline it
            textViewAuthor.setPaintFlags(textViewAuthor.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            textViewAuthor.setClickable(true);
            textViewAuthor.setFocusable(true);

            // Set click listener on author name
            textViewAuthor.setOnClickListener(v -> {
                if (authorClickListener != null) {
                    authorClickListener.onAuthorClick(message.poster());
                }
            });

            // Load author profile picture using helper
            loadAuthorProfilePicture(message.poster());

            // Set click listener on author image
            if (imageViewAuthor != null) {
                imageViewAuthor.setClickable(true);
                imageViewAuthor.setFocusable(true);
                imageViewAuthor.setOnClickListener(v -> {
                    if (authorClickListener != null) {
                        authorClickListener.onAuthorClick(message.poster());
                    }
                });
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            String formattedTime = dateFormat.format(new Date(message.timestamp()));
            textViewTimestamp.setText(formattedTime);

            bindReactions(message);
        }

        private void bindReactions(Message message) {
            // current user
            User me = UserDAO.getInstance().getCurrentUser();

            // picker handlers
            setupEmojiClick(emojiLike,      ReactionType.LIKE,            me, message);
            setupEmojiClick(emojiLove,      ReactionType.LOVE,            me, message);
            setupEmojiClick(emojiLaugh,     ReactionType.LAUGH,           me, message);
            setupEmojiClick(emojiHappy,     ReactionType.HAPPY,           me, message);
            setupEmojiClick(emojiGoodLuck,  ReactionType.GOOD_LUCK,       me, message);
            setupEmojiClick(emojiCongrats,  ReactionType.CONGRATULATIONS, me, message);
            setupEmojiClick(emojiSurprise,  ReactionType.SURPRISE,        me, message);
            setupEmojiClick(emojiSad,       ReactionType.SAD,             me, message);
            setupEmojiClick(emojiAngry,     ReactionType.ANGRY,           me, message);

            if (selectedReaction != null) {
                selectedReaction.setOnClickListener(v -> {
                    if (me == null) return;
                    java.util.List<ReactionType> mine = ReactionsFacade.getReactions(me.id(), message.id());
                    if (mine != null) {
                        for (ReactionType r : mine) {
                            ReactionsFacade.removeReaction(me.id(), message.id(), r);
                        }
                    }
                    updateReactionUI(me, message);
                });
            }

            // initial UI
            updateReactionUI(me, message);
        }

        private void setupEmojiClick(TextView v, ReactionType type, User me, Message message) {
            if (v == null) return;
            v.setOnClickListener(view -> {
                if (me == null) return;

                java.util.List<ReactionType> mine = ReactionsFacade.getReactions(me.id(), message.id());
                boolean alreadySelected = mine != null && mine.size() == 1 && mine.contains(type);

                if (alreadySelected) {
                    ReactionsFacade.removeReaction(me.id(), message.id(), type);
                } else {
                    if (mine != null) {
                        for (ReactionType r : mine) {
                            if (r != type) ReactionsFacade.removeReaction(me.id(), message.id(), r);
                        }
                    }
                    if (mine == null || !mine.contains(type)) {
                        ReactionsFacade.addReaction(me.id(), message.id(), type, System.currentTimeMillis());
                    }
                }
                updateReactionUI(me, message);
            });
        }

        private void updateReactionUI(User me, Message message) {
            // counts row
            updateCountsRow(message);

            // picker vs pill
            if (me == null) {
                showPicker();
                dimAll();
                return;
            }
            java.util.List<ReactionType> mine = ReactionsFacade.getReactions(me.id(), message.id());
            if (mine == null || mine.isEmpty()) {
                showPicker();
                dimAll();
            } else {
                ReactionType selected = mine.get(mine.size() - 1); // newest
                String emoji = emojiMap.get(selected);
                selectedReaction.setText(emoji != null ? emoji : "•");
                showPill();
            }
        }

        private void updateCountsRow(Message message) {
            int like=0,love=0,laugh=0,happy=0,luck=0,congrats=0,surprise=0,sad=0,angry=0;

            for (var it = UserDAO.getInstance().getAll(); it.hasNext();) {
                User u = it.next();
                java.util.List<ReactionType> r = ReactionsFacade.getReactions(u.id(), message.id());
                if (r == null) continue;
                for (ReactionType t : r) {
                    switch (t) {
                        case LIKE -> like++;
                        case LOVE -> love++;
                        case LAUGH -> laugh++;
                        case HAPPY -> happy++;
                        case GOOD_LUCK -> luck++;
                        case CONGRATULATIONS -> congrats++;
                        case SURPRISE -> surprise++;
                        case SAD -> sad++;
                        case ANGRY -> angry++;
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
            append(sb, emojiMap.get(ReactionType.SURPRISE), surprise);
            append(sb, emojiMap.get(ReactionType.SAD), sad);
            append(sb, emojiMap.get(ReactionType.ANGRY), angry);

            if (reactionCounts != null) {
                reactionCounts.setText(sb.isEmpty() ? "" : sb.toString().trim());
                reactionCounts.setVisibility(sb.isEmpty() ? View.GONE : View.VISIBLE);
            }
        }

        private void append(StringBuilder sb, String emoji, int count) {
            if (count > 0 && emoji != null) sb.append(emoji).append("×").append(count).append("  ");
        }

        private void showPicker() {
            if (emojiBar != null) emojiBar.setVisibility(View.VISIBLE);
            if (selectedReaction != null) selectedReaction.setVisibility(View.GONE);
        }

        private void showPill() {
            if (emojiBar != null) emojiBar.setVisibility(View.GONE);
            if (selectedReaction != null) selectedReaction.setVisibility(View.VISIBLE);
        }

        private void dimAll() {
            dim(emojiLike); dim(emojiLove); dim(emojiLaugh); dim(emojiHappy);
            dim(emojiGoodLuck); dim(emojiCongrats); dim(emojiSurprise); dim(emojiSad); dim(emojiAngry);
        }
        private void dim(TextView tv) { if (tv != null) { tv.setAlpha(0.6f); tv.setBackgroundResource(0); } }

        /**
         * Loads author profile picture using ProfilePictureHelper
         * Supports custom uploaded pictures
         */
        private void loadAuthorProfilePicture(UUID authorUUID) {
            if (imageViewAuthor != null) {
                ProfilePictureHelper.loadProfilePicture(view.getContext(), authorUUID, imageViewAuthor);
            }
        }

        private String getAuthorName(java.util.UUID posterUUID) {
            try {
                User author = UserDAO.getInstance().getByUUID(posterUUID);
                return author != null ? author.username() : "Unknown User";
            } catch (Exception e) {
                return "Unknown User";
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_message, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Message message = localDataSet[position];
        boolean isPinned = pinManager.isPinned(threadId, message);
        viewHolder.display(message, isPinned, onAuthorClickListener);

        // Set click listener on the entire message item
        viewHolder.itemView.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onClick(position, localDataSet[position]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.length;
    }

    // Message click listener
    private OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    public interface OnClickListener {
        void onClick(int i, Message message);
    }

    // Author click listener
    private OnAuthorClickListener onAuthorClickListener;

    public void setOnAuthorClickListener(OnAuthorClickListener listener) {
        this.onAuthorClickListener = listener;
    }

    public interface OnAuthorClickListener {
        void onAuthorClick(UUID authorUUID);
    }
}
