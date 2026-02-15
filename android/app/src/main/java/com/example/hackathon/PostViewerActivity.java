package com.example.hackathon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.hackathon.dao.MessageComparator;
import com.example.hackathon.helpers.ProfilePictureHelper;

import java.util.Collections;

import com.example.hackathon.dao.PostDAO;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.managers.PinMessageManager;
import com.example.hackathon.ui.handlers.MessageSwipeHandler;
import com.example.hackathon.ui.handlers.MessageLongPressHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class PostViewerActivity extends BaseActivity {

    private TextView textViewPostTitle;
    private TextView textViewPostAuthor;
    private ImageView imageViewAuthorProfile;  // Add this
    private Button buttonBack;
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private Post currentPost;
    private ArrayList<Message> messages;
    private PinMessageManager pinMessageManager;

    private FloatingActionButton fabAddMessage;
    private ActivityResultLauncher<Intent> addMessageLauncher;

    private void registerAddMessageLauncher() {
        addMessageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Refresh messages when new message is added
                        refreshMessages();
                    }
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_viewer);
        registerAddMessageLauncher();

        // Apply window insets to AppBarLayout to fix status bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });

//        pinMessageManager = PinMessageManager.getInstance();
        pinMessageManager = PinMessageManager.getInstance(this);  // Add context here

        initializeViews();
        getPostFromIntent();
        displayPostAndMessages();
        setupBackButton();
        setupAddMessageFab();
        setupMessageSwipeToPin();
        setupMessageLongPress();
        setupBottomNavigation();
    }

    private void initializeViews() {
        textViewPostTitle = findViewById(R.id.textViewPostTitle);
        textViewPostAuthor = findViewById(R.id.textViewPostAuthor);
        imageViewAuthorProfile = findViewById(R.id.imageViewAuthorProfile);  // Add this
        buttonBack = findViewById(R.id.buttonBack);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        fabAddMessage = findViewById(R.id.fabAddMessage);
    }

    private void getPostFromIntent() {
        String postUUIDString = getIntent().getStringExtra("POST_UUID");

        if (postUUIDString != null) {
            UUID postUUID = UUID.fromString(postUUIDString);
            currentPost = PostDAO.getInstance().get(new Post(postUUID));
        } else {
            currentPost = PostDAO.getInstance().getRandom();
        }
    }

    private void displayPostAndMessages() {
        if (currentPost != null) {
            if (textViewPostTitle != null) {
                textViewPostTitle.setText(currentPost.topic);
            }

            if (textViewPostAuthor != null) {
                User author = UserDAO.getInstance().getByUUID(currentPost.poster);
                String authorName = author != null ? author.username() : "Unknown User";
                textViewPostAuthor.setText("by " + authorName);

                // Load author profile picture using helper
                if (author != null && imageViewAuthorProfile != null) {
                    ProfilePictureHelper.loadProfilePicture(this, author.getUUID(), imageViewAuthorProfile);

                    // Make profile picture clickable
                    imageViewAuthorProfile.setOnClickListener(v -> openUserProfile(author.getUUID()));
                }

                // Make author name clickable
                final UUID authorUUID = currentPost.poster;
                textViewPostAuthor.setOnClickListener(v -> openUserProfile(authorUUID));

                // Add visual feedback that it's clickable
                textViewPostAuthor.setPaintFlags(textViewPostAuthor.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            }

            loadMessages();
        }
    }

    private void openUserProfile(UUID userUUID) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("USER_UUID", userUUID.toString());
        startActivity(intent);
    }

    private void loadMessages() {
        if (currentPost != null && currentPost.messages != null) {
            Iterator<Message> messageIterator = currentPost.messages.getAll();

            messages = new ArrayList<>();
            messageIterator.forEachRemaining(messages::add);

            // Sort to show pinned messages first
            pinMessageManager.sortWithPinnedFirst(currentPost.id, messages);

            messageAdapter = new MessageAdapter(messages, currentPost.id);

            // Set up author click listener
            messageAdapter.setOnAuthorClickListener(new MessageAdapter.OnAuthorClickListener() {
                @Override
                public void onAuthorClick(UUID authorUUID) {
                    openUserProfile(authorUUID);
                }
            });

            // Set up message click listener
            messageAdapter.setOnClickListener((position, message) -> {
                // Handle message click if needed
            });

            recyclerViewMessages.setAdapter(messageAdapter);
        }
    }

    private void setupMessageSwipeToPin() {
        if (currentPost == null || messages == null) {
            return;
        }

        MessageSwipeHandler swipeHandler = new MessageSwipeHandler(
                this,
                messages,
                recyclerViewMessages,
                currentPost.id,
                new MessageSwipeHandler.OnMessagePinnedListener() {
                    @Override
                    public void onMessagePinned(int fromPosition, int toPosition) {
                        messageAdapter = new MessageAdapter(messages, currentPost.id);
                        messageAdapter.setOnAuthorClickListener(authorUUID -> openUserProfile(authorUUID));
                        recyclerViewMessages.setAdapter(messageAdapter);
                        recyclerViewMessages.smoothScrollToPosition(toPosition);
                    }

                    @Override
                    public void onMessageUnpinned(int fromPosition, int toPosition) {
                        messageAdapter = new MessageAdapter(messages, currentPost.id);
                        messageAdapter.setOnAuthorClickListener(authorUUID -> openUserProfile(authorUUID));
                        recyclerViewMessages.setAdapter(messageAdapter);
                        recyclerViewMessages.smoothScrollToPosition(toPosition);
                    }

                    @Override
                    public void onSwipeCancelled(int position) {
                        messageAdapter = new MessageAdapter(messages, currentPost.id);
                        messageAdapter.setOnAuthorClickListener(authorUUID -> openUserProfile(authorUUID));
                        recyclerViewMessages.setAdapter(messageAdapter);
                    }
                });

        swipeHandler.attach();
    }

    private void setupMessageLongPress() {
        if (currentPost == null || messages == null) {
            return;
        }

        MessageLongPressHandler longPressHandler = new MessageLongPressHandler(
                this,
                recyclerViewMessages,
                (view, position) -> handleMessageLongPress(position)
        );

        recyclerViewMessages.addOnItemTouchListener(longPressHandler);
    }

    private void handleMessageLongPress(final int position) {
        if (position < 0 || position >= messages.size()) {
            return;
        }

        final Message message = messages.get(position);
        boolean isPinned = pinMessageManager.isPinned(currentPost.id, message);

        // Show action dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message Actions");

        String[] options;
        if (isPinned) {
            options = new String[]{"Unpin Message", "Cancel"};
        } else {
            options = new String[]{"Pin Message", "Cancel"};
        }

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (isPinned) {
                    unpinMessage(message, position);
                } else {
                    pinMessage(message, position);
                }
            }
            dialog.dismiss();
        });

        builder.create().show();
    }

    private void pinMessage(Message message, int currentPosition) {
        pinMessageManager.pinMessage(currentPost.id, message);
        messages.remove(currentPosition);
        messages.add(0, message);

        messageAdapter = new MessageAdapter(messages, currentPost.id);
        messageAdapter.setOnAuthorClickListener(authorUUID -> openUserProfile(authorUUID));
        recyclerViewMessages.setAdapter(messageAdapter);
        recyclerViewMessages.smoothScrollToPosition(0);
    }

    private void unpinMessage(Message message, int currentPosition) {
        pinMessageManager.unpinMessage(currentPost.id, message);

        // Find position after all pinned messages
        int newPosition = 0;
        for (Message msg : messages) {
            if (msg.equals(message)) {
                continue;
            }
            if (pinMessageManager.isPinned(currentPost.id, msg)) {
                newPosition++;
            } else {
                break;
            }
        }

        messages.remove(currentPosition);
        messages.add(newPosition, message);

        messageAdapter = new MessageAdapter(messages, currentPost.id);
        messageAdapter.setOnAuthorClickListener(authorUUID -> openUserProfile(authorUUID));
        recyclerViewMessages.setAdapter(messageAdapter);
        recyclerViewMessages.smoothScrollToPosition(newPosition);
    }

    private void setupBackButton() {
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> finish());
        }
    }

    private void setupAddMessageFab() {
        fabAddMessage.setOnClickListener(v -> openAddMessageActivity());
    }

    private void openAddMessageActivity() {
        Intent intent = new Intent(PostViewerActivity.this, AddMessageActivity.class);
        intent.putExtra("POST_UUID", currentPost.id.toString());
        addMessageLauncher.launch(intent);
    }

    private void refreshMessages() {
        if (currentPost != null && currentPost.messages != null) {
            messages.clear();

            Iterator<Message> messageIterator = currentPost.messages.getAll();
            messageIterator.forEachRemaining(messages::add);

            pinMessageManager.sortWithPinnedFirst(currentPost.id, messages);

            messageAdapter = new MessageAdapter(messages, currentPost.id);
            messageAdapter.setOnAuthorClickListener(authorUUID -> openUserProfile(authorUUID));
            recyclerViewMessages.setAdapter(messageAdapter);

            // Scroll to the top to show newest message
            if (!messages.isEmpty()) {
                recyclerViewMessages.smoothScrollToPosition(0);
            }
        }
    }

    @Override
    protected void setBottomNavigationSelectedItem() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(0);
        }
    }
}
