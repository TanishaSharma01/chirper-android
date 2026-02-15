package com.example.hackathon;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.dao.PostDAO;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.managers.PostPersistenceManager;
import com.example.hackathon.persistentdata.DataManager;
import com.example.hackathon.persistentdata.io.AndroidIOFactory;

import java.util.Iterator;
import java.util.UUID;

/**
 * Activity for adding a new message to a post.
 */
public class AddMessageActivity extends BaseActivity {

    private EditText editTextMessageContent;
    private Button buttonSubmit;
    private Button buttonCancel;
    private UUID postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message);

        // Get post ID from intent
        String postIdString = getIntent().getStringExtra("POST_UUID");
        if (postIdString != null) {
            postId = UUID.fromString(postIdString);
        } else {
            Toast.makeText(this, "Error: Post not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupButtons();
        setupBottomNavigation();
    }

    @Override
    protected void navigateToHome() {
        // When in AddMessage, "Home" should go back to the post viewer
        finish(); // Just close this activity and return to PostViewerActivity
    }


    private void initializeViews() {
        editTextMessageContent = findViewById(R.id.editTextMessageContent);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonCancel = findViewById(R.id.buttonCancel);
    }

    private void setupButtons() {
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateField()) {
                    createNewMessage();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavigation.setSelectedItemId(R.id.navigation_home);
                finish();
            }
        });
    }

    /**
     * Validates the message content field
     */
    private boolean validateField() {
        String content = editTextMessageContent.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            editTextMessageContent.setError("Message cannot be empty");
            editTextMessageContent.requestFocus();
            return false;
        }

        if (content.length() < 3) {
            editTextMessageContent.setError("Message must be at least 3 characters");
            editTextMessageContent.requestFocus();
            return false;
        }

        editTextMessageContent.setError(null);
        return true;
    }

    /**
     * Creates a new message and adds it to the post
     */
    private void createNewMessage() {
        String content = editTextMessageContent.getText().toString().trim();

        // Get a random user as the message author
//        User currentUser = getRandomUser();
//
//        if (currentUser == null) {
//            Toast.makeText(this, "Error: No user available", Toast.LENGTH_SHORT).show();
//            return;
//        }

        User currentUser = UserDAO.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the post
        Post post = PostDAO.getInstance().get(new Post(postId));

        if (post == null) {
            Toast.makeText(this, "Error: Post not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            // Create new message
            // Message(UUID id, UUID poster, UUID thread, long timestamp, String message)
            Message newMessage = new Message(
                    UUID.randomUUID(),              // id
                    currentUser.getUUID(),          // poster
                    postId,                         // thread (post UUID)
                    System.currentTimeMillis(),     // timestamp
                    content                         // message content
            );

            // Add message to post
            post.messages.insert(newMessage);

            // Show success message
            Toast.makeText(this, "Message added successfully!", Toast.LENGTH_SHORT).show();

            // Return to PostViewerActivity with result
            Intent resultIntent = new Intent();
            resultIntent.putExtra("NEW_MESSAGE_ADDED", true);
            setResult(RESULT_OK, resultIntent);
//            PostPersistenceManager.getInstance(this).savePosts();
            DataManager dm = new DataManager(new AndroidIOFactory(this));
            dm.writeAll();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error adding message: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("E", "Error adding message");
        }
    }

    /**
     * Gets a random user from UserDAO
     */
    private User getRandomUser() {
        Iterator<User> userIterator = UserDAO.getInstance().getAll();
        if (userIterator.hasNext()) {
            return userIterator.next();
        }
        return null;
    }

    @Override
    protected void setBottomNavigationSelectedItem() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_add);
        }
    }

}
