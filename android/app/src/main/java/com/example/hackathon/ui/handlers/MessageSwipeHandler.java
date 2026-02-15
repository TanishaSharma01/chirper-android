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
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.managers.PinMessageManager;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Handles swipe-to-pin/unpin functionality for messages.
 * Uses different visual callbacks for pin (green) and unpin (orange-red).
 */
public class MessageSwipeHandler {

    public interface OnMessagePinnedListener {
        void onMessagePinned(int fromPosition, int toPosition);
        void onMessageUnpinned(int fromPosition, int toPosition);
        void onSwipeCancelled(int position);
    }

    private Context context;
    private ArrayList<Message> messages;
    private RecyclerView recyclerView;
    private OnMessagePinnedListener listener;
    private PinMessageManager pinManager;
    private UUID threadId;
    private ItemTouchHelper currentTouchHelper;

    // Icons for pin and unpin
    private Drawable pinIcon;
    private Drawable unpinIcon;

    public MessageSwipeHandler(Context context, ArrayList<Message> messages,
                               RecyclerView recyclerView, UUID threadId,
                               OnMessagePinnedListener listener) {
        this.context = context;
        this.messages = messages;
        this.recyclerView = recyclerView;
        this.threadId = threadId;
        this.listener = listener;
        this.pinManager = PinMessageManager.getInstance();

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
                handleSwipe(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < messages.size()) {
                    Message message = messages.get(position);
                    boolean isPinned = pinManager.isPinned(threadId, message);

                    // Use different callback based on pin status
                    if (isPinned) {
                        drawUnpinBackground(c, viewHolder, dX, isCurrentlyActive);
                    } else {
                        drawPinBackground(c, viewHolder, dX, isCurrentlyActive);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        currentTouchHelper = new ItemTouchHelper(swipeCallback);
        currentTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Draws green background with pin icon for unpinned messages
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
     * Draws orange-red background with unpin icon for pinned messages
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
        if (position < 0 || position >= messages.size()) {
            return;
        }

        final Message message = messages.get(position);
        boolean isPinned = pinManager.isPinned(threadId, message);

        if (isPinned) {
            showUnpinConfirmationDialog(message, position);
        } else {
            showPinConfirmationDialog(message, position);
        }
    }

    private void showPinConfirmationDialog(final Message message, final int position) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Pin Message");
        builder.setMessage("Do you want to pin this message to the top?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            pinMessage(message, position);
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            if (listener != null) {
                listener.onSwipeCancelled(position);
            }
            dialog.dismiss();
        });

        builder.setOnCancelListener(dialog -> {
            if (listener != null) {
                listener.onSwipeCancelled(position);
            }
        });

        builder.create().show();
    }

    private void showUnpinConfirmationDialog(final Message message, final int position) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Unpin Message");
        builder.setMessage("Do you want to unpin this message?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            unpinMessage(message, position);
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            if (listener != null) {
                listener.onSwipeCancelled(position);
            }
            dialog.dismiss();
        });

        builder.setOnCancelListener(dialog -> {
            if (listener != null) {
                listener.onSwipeCancelled(position);
            }
        });

        builder.create().show();
    }

    private void pinMessage(Message message, int currentPosition) {
        pinManager.pinMessage(threadId, message);
        messages.remove(currentPosition);
        messages.add(0, message);

        if (listener != null) {
            listener.onMessagePinned(currentPosition, 0);
        }
    }

    private void unpinMessage(Message message, int currentPosition) {
        pinManager.unpinMessage(threadId, message);

        int newPosition = 0;
        for (Message msg : messages) {
            if (msg.equals(message)) {
                continue;
            }
            if (pinManager.isPinned(threadId, msg)) {
                newPosition++;
            } else {
                break;
            }
        }

        messages.remove(currentPosition);
        messages.add(newPosition, message);

        if (listener != null) {
            listener.onMessageUnpinned(currentPosition, newPosition);
        }
    }
}
