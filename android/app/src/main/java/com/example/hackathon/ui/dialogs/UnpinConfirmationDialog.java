package com.example.hackathon.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import com.example.hackathon.dao.model.Post;

/**
 * Displays confirmation dialog for unpinning posts.
 * Similar to PinConfirmationDialog but for unpin action.
 */
public class UnpinConfirmationDialog {

    public interface OnUnpinConfirmationListener {
        void onUnpinConfirmed(Post post, int position);
        void onUnpinCancelled();
    }

    private Context context;
    private OnUnpinConfirmationListener listener;

    public UnpinConfirmationDialog(Context context, OnUnpinConfirmationListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Shows the unpin confirmation dialog
     * @param post The post to unpin
     * @param position The current position of the post
     */
    public void show(final Post post, final int position) {
        if (post == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Unpin Post");
        builder.setMessage("Do you want to unpin \"" + post.topic + "\"?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onUnpinConfirmed(post, position);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onUnpinCancelled();
                }
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) {
                    listener.onUnpinCancelled();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
