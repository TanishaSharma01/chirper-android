package com.example.hackathon.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import com.example.hackathon.dao.model.Post;

/**
 * Shows action options for a post (Pin/Unpin).
 * This is displayed when user long-presses on a post.
 */
public class PostActionDialog {

    public interface OnActionSelectedListener {
        void onPinSelected(Post post, int position);
        void onUnpinSelected(Post post, int position);
    }

    private Context context;
    private OnActionSelectedListener listener;

    public PostActionDialog(Context context, OnActionSelectedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Shows action dialog with appropriate options
     * @param post The post to perform action on
     * @param position Position in the list
     * @param isPinned Whether the post is currently pinned
     */
    public void show(final Post post, final int position, boolean isPinned) {
        if (post == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Post Actions");

        // Different options based on pin status
        String[] options;
        if (isPinned) {
            options = new String[]{"Unpin Post", "Cancel"};
        } else {
            options = new String[]{"Pin Post", "Cancel"};
        }

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener == null) {
                    return;
                }

                if (isPinned) {
                    // For pinned posts: Unpin or Cancel
                    if (which == 0) {
                        listener.onUnpinSelected(post, position);
                    }
                } else {
                    // For unpinned posts: Pin or Cancel
                    if (which == 0) {
                        listener.onPinSelected(post, position);
                    }
                }
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
