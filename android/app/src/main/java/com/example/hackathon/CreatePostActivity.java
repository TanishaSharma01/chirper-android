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
import com.example.hackathon.dao.PostDAO;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.managers.PostPersistenceManager;
import com.example.hackathon.persistentdata.DataManager;
import com.example.hackathon.persistentdata.io.AndroidIOFactory;

import java.util.Iterator;
import java.util.UUID;

/**
 * Activity for creating a new discussion post.
 * Validates input and creates post in the database.
 */
public class CreatePostActivity extends BaseActivity {

    private EditText editTextPostTitle;
    private EditText editTextPostDescription;
    private Button buttonSubmit;
    private Button buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        initializeViews();
        setupButtons();
        setupBottomNavigation();
    }

    private void initializeViews() {
        editTextPostTitle = findViewById(R.id.editTextPostTitle);
        editTextPostDescription = findViewById(R.id.editTextPostDescription);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonCancel = findViewById(R.id.buttonCancel);
    }


    private void setupButtons() {
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    createNewPost();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHome();
                finish();
            }
        });
    }

    /**
     * Validates all input fields
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateFields() {
        String title = editTextPostTitle.getText().toString().trim();
        String description = editTextPostDescription.getText().toString().trim();

        // Check if title is empty
        if (TextUtils.isEmpty(title)) {
            editTextPostTitle.setError("Title is required");
            editTextPostTitle.requestFocus();
            return false;
        }

        // Check if title is too short
        if (title.length() < 5) {
            editTextPostTitle.setError("Title must be at least 5 characters");
            editTextPostTitle.requestFocus();
            return false;
        }

        // Description validation (optional - remove if not needed)
        if (TextUtils.isEmpty(description)) {
            editTextPostDescription.setError("Description is required");
            editTextPostDescription.requestFocus();
            return false;
        }

        // Clear any previous errors
        editTextPostTitle.setError(null);
        editTextPostDescription.setError(null);

        return true;
    }

    /**
     * Creates a new post and adds it to the database
     */
    private void createNewPost() {
        User currentUser = UserDAO.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = editTextPostTitle.getText().toString().trim();
        String description = editTextPostDescription.getText().toString().trim();

        try {
            // Create new post using the correct constructor
            UUID postId = UUID.randomUUID();
            Post newPost = new Post(
                    postId,                      // id
                    currentUser.getUUID(),       // poster
                    title                        // topic
            );

            // Add the description as the first message in the post
            if (!TextUtils.isEmpty(description)) {
                Message firstMessage = new Message(
                        UUID.randomUUID(),           // id
                        currentUser.getUUID(),       // poster
                        postId,                      // thread (the post's UUID)
                        System.currentTimeMillis(),  // timestamp
                        description                  // message
                );
                newPost.messages.insert(firstMessage);
            }

            // Add post to DAO
            PostDAO.getInstance().add(newPost);

            // Show success message
            Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();

            // Return to MainActivity with result
            Intent resultIntent = new Intent();
            resultIntent.putExtra("NEW_POST_CREATED", true);
            setResult(RESULT_OK, resultIntent);


//            PostPersistenceManager.getInstance(this).savePosts();
            // Save to CSV using DataManager
            DataManager dm = new DataManager(new AndroidIOFactory(this));
            dm.writeAll();
            finish();

        } catch (Exception e) {
            // Handle any errors during post creation
            Toast.makeText(this, "Error creating post: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("E", "Error creating post");
        }
    }

    /**
     * Gets a random user from UserDAO
     * In real app, you would get the currently logged-in user
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
