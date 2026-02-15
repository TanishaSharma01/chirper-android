package com.example.hackathon;

import android.graphics.Paint;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.helpers.ProfilePictureHelper;
import com.example.hackathon.managers.PinPostManager;
import java.util.List;
import java.util.UUID;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public interface OnAuthorClickListener {
        void onAuthorClick(UUID authorUUID);
    }

    private List<Post> posts;
    private OnPostClickListener listener;
    private OnAuthorClickListener authorClickListener;
    private PinPostManager pinManager;

    public PostListAdapter(List<Post> posts, OnPostClickListener listener, PinPostManager pinManager) {
        this.posts = posts;
        this.listener = listener;
        this.pinManager = pinManager;
    }

    public void setOnAuthorClickListener(OnAuthorClickListener listener) {
        this.authorClickListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_list_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        boolean isPinned = pinManager.isPinned(post);
        holder.bind(post, listener, authorClickListener, isPinned);
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewAuthor;
        TextView textViewMessageCount;
        ImageView imageViewAuthorProfile;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewPostTitle);
            textViewAuthor = itemView.findViewById(R.id.textViewPostAuthor);
            textViewMessageCount = itemView.findViewById(R.id.textViewMessageCount);
            imageViewAuthorProfile = itemView.findViewById(R.id.imageViewAuthorProfile);
        }

        public void bind(Post post, OnPostClickListener listener, OnAuthorClickListener authorClickListener, boolean isPinned) {
            // Visual indicator for pinned posts
            if (isPinned) {
                itemView.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.pinnedPostBackground));
                textViewTitle.setText("📌 " + post.topic);
            } else {
                itemView.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.cardBackground));
                textViewTitle.setText(post.topic);
            }

            User author = UserDAO.getInstance().getByUUID(post.poster);
            String authorName = author != null ? author.username() : "Unknown User";
            textViewAuthor.setText("by " + authorName);

            // Make author name clickable
            textViewAuthor.setPaintFlags(textViewAuthor.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            textViewAuthor.setOnClickListener(v -> {
                if (authorClickListener != null) {
                    authorClickListener.onAuthorClick(post.poster);
                }
                // Prevent triggering post click when clicking author
                v.getParent().requestDisallowInterceptTouchEvent(true);
            });

            // Load author profile picture using helper
            if (author != null && imageViewAuthorProfile != null) {
                // Use ProfilePictureHelper to load custom or default profile picture
                ProfilePictureHelper.loadProfilePicture(
                        itemView.getContext(),
                        author.getUUID(),
                        imageViewAuthorProfile
                );

                // Make profile picture clickable too
                imageViewAuthorProfile.setOnClickListener(v -> {
                    if (authorClickListener != null) {
                        authorClickListener.onAuthorClick(post.poster);
                    }
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                });
            } else if (imageViewAuthorProfile != null) {
                // Fallback if author not found
                imageViewAuthorProfile.setImageResource(R.drawable.default_profile);
            }

            int messageCount = getMessageCount(post);
            textViewMessageCount.setText(messageCount + " messages");

            // Click on the card (not author) opens the post
            itemView.setOnClickListener(v -> {
                // Only trigger if not clicking on author
                listener.onPostClick(post);
            });
        }

        private int getMessageCount(Post post) {
            int count = 0;
            if (post.messages != null) {
                java.util.Iterator<?> iterator = post.messages.getAll();
                while (iterator.hasNext()) {
                    iterator.next();
                    count++;
                }
            }
            return count;
        }
    }
}
