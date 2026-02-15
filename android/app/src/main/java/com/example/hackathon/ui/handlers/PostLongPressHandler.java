package com.example.hackathon.ui.handlers;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Detects long press gestures on RecyclerView items.
 * Acts like a "right-click detector" for mobile.
 */
public class PostLongPressHandler implements RecyclerView.OnItemTouchListener {

    public interface OnItemLongPressListener {
        void onItemLongPress(View view, int position);
    }

    private OnItemLongPressListener listener;
    private GestureDetector gestureDetector;

    public PostLongPressHandler(Context context, final RecyclerView recyclerView,
                                OnItemLongPressListener listener) {
        this.listener = listener;

        // GestureDetector detects the long press motion
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                // Find which item was pressed
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if (childView != null && listener != null) {
                    int position = recyclerView.getChildAdapterPosition(childView);
                    listener.onItemLongPress(childView, position);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // Not needed for long press
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // Not needed for long press
    }
}
