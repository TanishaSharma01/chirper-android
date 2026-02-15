package com.example.hackathon.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import com.example.hackathon.dao.model.Post;

/**
 * Displays confirmation dialog for pinning posts.
 * Handles user response through callback interface.
 */
public class PinConfirmationDialog {

    /**
     * Callback interface for pin confirmation response
     */
    public interface OnPinConfirmationListener {
        void onPinConfirmed(Post post, int position);
        void onPinCancelled(int position);
    }

    private Context context;
    private OnPinConfirmationListener listener;

    public PinConfirmationDialog(Context context, OnPinConfirmationListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Shows the pin confirmation dialog
     * @param post The post to pin
     * @param position The position of the post in the list
     */
    public void show(final Post post, final int position) {
        if (post == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Pin Post");
        builder.setMessage("Do you want to pin \"" + post.topic + "\" to the top?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onPinConfirmed(post, position);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onPinCancelled(position);
                }
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) {
                    listener.onPinCancelled(position);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
