package com.example.hackathon.ui.handlers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.managers.PinPostManager;
import com.example.hackathon.ui.dialogs.PinConfirmationDialog;
import com.example.hackathon.ui.dialogs.UnpinConfirmationDialog;
import java.util.List;

/**
 * Handles swipe-to-pin/unpin functionality for posts.
 * Uses different visual callbacks for pin (green) and unpin (orange-red).
 */
public class PostSwipeHandler {

    public interface OnPostPinnedListener {
        void onPostPinned(int fromPosition, int toPosition);
        void onPostUnpinned(int fromPosition, int toPosition);
        void onSwipeCancelled(int position);
    }

    private Context context;
    private List<Post> posts;
    private RecyclerView recyclerView;
    private OnPostPinnedListener listener;
    private PinPostManager pinManager;
    private ItemTouchHelper currentTouchHelper;

    // Icons for pin and unpin
    private Drawable pinIcon;
    private Drawable unpinIcon;

    // Track the post being swiped to prevent state conflicts
    private Post currentlySwipedPost = null;
    private boolean isSwipeInProgress = false;

    public PostSwipeHandler(Context context, List<Post> posts,
                            RecyclerView recyclerView, OnPostPinnedListener listener) {
        this.context = context;
        this.posts = posts;
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.pinManager = PinPostManager.getInstance(context);

        // Initialize icons
        pinIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_save);
        unpinIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_close_clear_cancel);
    }

    public void attach() {
        ItemTouchHelper.Callback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < posts.size()) {
                    currentlySwipedPost = posts.get(position);
                    isSwipeInProgress = true;
                    handleSwipe(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < posts.size()) {
                    Post post = posts.get(position);

                    // Determine pin status at the START of the swipe, not during
                    boolean isPinned;
                    if (isSwipeInProgress && currentlySwipedPost != null &&
                            currentlySwipedPost.equals(post)) {
                        // Use the original state when swipe started
                        isPinned = pinManager.isPinned(currentlySwipedPost);
                    } else {
                        isPinned = pinManager.isPinned(post);
                    }

                    // Use different callback based on pin status
                    if (isPinned) {
                        drawUnpinBackground(c, viewHolder, dX, isCurrentlyActive);
                    } else {
                        drawPinBackground(c, viewHolder, dX, isCurrentlyActive);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Reset swipe tracking when swipe is complete
                currentlySwipedPost = null;
                isSwipeInProgress = false;
            }
        };

        currentTouchHelper = new ItemTouchHelper(swipeCallback);
        currentTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Draws green background with pin icon for unpinned posts
     */
    private void drawPinBackground(Canvas c, RecyclerView.ViewHolder viewHolder,
                                   float dX, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        // Don't draw if swipe is cancelled
        if (dX == 0 && !isCurrentlyActive) {
            return;
        }

        // Green background for pin
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#4CAF50")); // Green
        c.drawRect(
                itemView.getRight() + dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom(),
                paint
        );

        // Draw pin icon
        if (pinIcon != null) {
            int iconSize = (int) (itemHeight * 0.4); // Icon is 40% of item height
            int iconMargin = (itemHeight - iconSize) / 2;

            int iconLeft = itemView.getRight() - iconMargin - iconSize;
            int iconTop = itemView.getTop() + iconMargin;
            int iconRight = itemView.getRight() - iconMargin;
            int iconBottom = iconTop + iconSize;

            pinIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            pinIcon.setTint(Color.WHITE); // White icon on green background
            pinIcon.draw(c);
        }
    }

    /**
     * Draws orange-red background with unpin icon for pinned posts
     */
    private void drawUnpinBackground(Canvas c, RecyclerView.ViewHolder viewHolder,
                                     float dX, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        // Don't draw if swipe is cancelled
        if (dX == 0 && !isCurrentlyActive) {
            return;
        }

        // Orange-red background for unpin
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FF5722")); // Orange-red
        c.drawRect(
                itemView.getRight() + dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom(),
                paint
        );

        // Draw unpin icon
        if (unpinIcon != null) {
            int iconSize = (int) (itemHeight * 0.4); // Icon is 40% of item height
            int iconMargin = (itemHeight - iconSize) / 2;

            int iconLeft = itemView.getRight() - iconMargin - iconSize;
            int iconTop = itemView.getTop() + iconMargin;
            int iconRight = itemView.getRight() - iconMargin;
            int iconBottom = iconTop + iconSize;

            unpinIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            unpinIcon.setTint(Color.WHITE); // White icon on red background
            unpinIcon.draw(c);
        }
    }

    private void handleSwipe(final int position) {
        if (position < 0 || position >= posts.size()) {
            return;
        }

        final Post post = posts.get(position);
        boolean isPinned = pinManager.isPinned(post);

        if (isPinned) {
            // Show unpin confirmation dialog
            showUnpinConfirmation(post, position);
        } else {
            // Show pin confirmation dialog
            showPinConfirmation(post, position);
        }
    }

    private void showPinConfirmation(final Post post, final int position) {
        PinConfirmationDialog dialog = new PinConfirmationDialog(context,
                new PinConfirmationDialog.OnPinConfirmationListener() {
                    @Override
                    public void onPinConfirmed(Post confirmedPost, int confirmedPosition) {
                        pinPost(confirmedPost, confirmedPosition);
                    }

                    @Override
                    public void onPinCancelled(int cancelledPosition) {
                        // Clear swipe state when cancelled
                        currentlySwipedPost = null;
                        isSwipeInProgress = false;
                        if (listener != null) {
                            listener.onSwipeCancelled(cancelledPosition);
                        }
                    }
                });

        dialog.show(post, position);
    }

    private void showUnpinConfirmation(final Post post, final int position) {
        UnpinConfirmationDialog dialog = new UnpinConfirmationDialog(context,
                new UnpinConfirmationDialog.OnUnpinConfirmationListener() {
                    @Override
                    public void onUnpinConfirmed(Post confirmedPost, int confirmedPosition) {
                        unpinPost(confirmedPost, confirmedPosition);
                    }

                    @Override
                    public void onUnpinCancelled() {
                        // Clear swipe state when cancelled
                        currentlySwipedPost = null;
                        isSwipeInProgress = false;
                        if (listener != null) {
                            listener.onSwipeCancelled(position);
                        }
                    }
                });

        dialog.show(post, position);
    }

    private void pinPost(Post post, int currentPosition) {
        int newPosition = pinManager.pinPost(posts, post, currentPosition);

        // Clear swipe state before notifying
        currentlySwipedPost = null;
        isSwipeInProgress = false;

        if (listener != null) {
            listener.onPostPinned(currentPosition, newPosition);
        }
    }

    private void unpinPost(Post post, int currentPosition) {
        int newPosition = pinManager.unpinPostAndReposition(posts, post, currentPosition);

        // Clear swipe state before notifying
        currentlySwipedPost = null;
        isSwipeInProgress = false;

        if (listener != null) {
            listener.onPostUnpinned(currentPosition, newPosition);
        }
    }
    /**
     * Updates the posts list reference
     * Call this when the filtered list changes
     */
    public void updatePostsList(List<Post> newPosts) {
        this.posts = newPosts;
    }

    /**
     * Detaches the ItemTouchHelper from the RecyclerView
     */
    public void detach() {
        if (currentTouchHelper != null && recyclerView != null) {
            currentTouchHelper.attachToRecyclerView(null);
            currentTouchHelper = null;
        }
    }

}
