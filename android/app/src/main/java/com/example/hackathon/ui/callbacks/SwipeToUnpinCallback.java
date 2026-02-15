package com.example.hackathon.ui.callbacks;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Handles swipe gesture visualization for unpinning items.
 * Draws an orange/red background with unpin icon when user swipes left.
 */
public abstract class SwipeToUnpinCallback extends ItemTouchHelper.Callback {

    private Context mContext;
    private Paint mClearPaint;
    private ColorDrawable mBackground;
    private int backgroundColor;
    private Drawable unpinDrawable;
    private int intrinsicWidth;
    private int intrinsicHeight;

    public SwipeToUnpinCallback(Context context) {
        mContext = context;
        mBackground = new ColorDrawable();
        backgroundColor = Color.parseColor("#FF5722"); // Orange-red for unpin

        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // Use a different icon for unpin (you can change this to a custom icon)
        unpinDrawable = ContextCompat.getDrawable(mContext, android.R.drawable.ic_menu_close_clear_cancel);
        intrinsicWidth = unpinDrawable.getIntrinsicWidth();
        intrinsicHeight = unpinDrawable.getIntrinsicHeight();
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        if (isCancelled) {
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(),
                    (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        // Draw orange-red background for unpin
        mBackground.setColor(backgroundColor);
        mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                itemView.getRight(), itemView.getBottom());
        mBackground.draw(c);

        // Draw unpin icon
        int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int iconMargin = (itemHeight - intrinsicHeight) / 2;
        int iconLeft = itemView.getRight() - iconMargin - intrinsicWidth;
        int iconRight = itemView.getRight() - iconMargin;
        int iconBottom = iconTop + intrinsicHeight;

        unpinDrawable.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        unpinDrawable.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, mClearPaint);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.5f;
    }
}
