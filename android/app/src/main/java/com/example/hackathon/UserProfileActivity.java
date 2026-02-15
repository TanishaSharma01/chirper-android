package com.example.hackathon;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hackathon.auth.Session;
import com.example.hackathon.dao.PostDAO;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.helpers.ProfilePictureHelper;
import com.example.hackathon.managers.PinPostManager;
import com.example.hackathon.managers.ProfilePictureManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Activity for displaying user profile with their posts and statistics
 */
public class UserProfileActivity extends BaseActivity implements PostListAdapter.OnPostClickListener {

    private ImageView imageViewProfilePicture;
    private ImageView imageViewCameraIcon;
    private TextView textViewUsername;
    private TextView textViewPostCount;
    private TextView textViewMessageCount;
    private Button buttonBack;
    private Button buttonLogout;
    private RecyclerView recyclerViewUserPosts;
    private TextView textViewEmptyState;
    private LinearLayout emptyStateContainer;
    private TextView textViewPostsHeader;

    private PostListAdapter postAdapter;
    private User currentUser;
    private List<Post> userPosts;
    private PinPostManager pinManager;
    private ProfilePictureManager profilePictureManager;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        pinManager = PinPostManager.getInstance(this);
        profilePictureManager = ProfilePictureManager.getInstance(this);

        registerPhotoPickerLauncher();
        registerPermissionLauncher();

        initializeViews();
        getUserFromIntent();
        displayUserProfile();
        loadUserPosts();
        setupBackButton();
        setupLogoutButton();
        setupProfilePictureClick();
        setupBottomNavigation();
        setupBottomNavigationListener();
    }

    private void registerPhotoPickerLauncher() {
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        updateProfilePicture(uri);
                    }
                }
        );
    }

    private void registerPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean anyGranted = false;
                    for (Boolean granted : permissions.values()) {
                        if (granted) {
                            anyGranted = true;
                            break;
                        }
                    }

                    if (anyGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "Permission denied. Cannot access photos.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void initializeViews() {
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);
        imageViewCameraIcon = findViewById(R.id.imageViewCameraIcon);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewPostCount = findViewById(R.id.textViewPostCount);
        textViewMessageCount = findViewById(R.id.textViewMessageCount);
        buttonBack = findViewById(R.id.buttonBack);
        buttonLogout = findViewById(R.id.buttonLogout);
        recyclerViewUserPosts = findViewById(R.id.recyclerViewUserPosts);
        textViewEmptyState = findViewById(R.id.textViewEmptyState);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        textViewPostsHeader = findViewById(R.id.textViewPostsHeader);
        recyclerViewUserPosts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupProfilePictureClick() {
        UUID sessionUserUUID = Session.load(this);

        if (currentUser != null && sessionUserUUID != null &&
                currentUser.getUUID().equals(sessionUserUUID)) {
            imageViewProfilePicture.setOnClickListener(v -> checkPermissionsAndOpenPicker());
            if (imageViewCameraIcon != null) {
                imageViewCameraIcon.setVisibility(View.VISIBLE);
            }
        } else {
            imageViewProfilePicture.setClickable(false);
            if (imageViewCameraIcon != null) {
                imageViewCameraIcon.setVisibility(View.GONE);
            }
        }
    }

    private void checkPermissionsAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (hasPhotoPickerSupport()) {
                openImagePicker();
            } else {
                requestPhotosPermission();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPhotosPermission();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPhotosPermission();
            }
        }
    }

    private boolean hasPhotoPickerSupport() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    private void requestPhotosPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES
            });
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }

    private void openImagePicker() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void updateProfilePicture(Uri imageUri) {
        if (currentUser == null) {
            return;
        }

        try {
            String savedImagePath = saveImageToInternalStorage(imageUri);

            if (savedImagePath != null) {
                profilePictureManager.saveProfilePicture(currentUser.getUUID(), savedImagePath);

                Glide.with(this)
                        .load(new File(savedImagePath))
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .circleCrop()
                        .into(imageViewProfilePicture);

                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save profile picture", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating profile picture: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            android.util.Log.e("UserProfile", "Error saving profile picture", e);
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            String filename = "profile_" + currentUser.getUUID().toString() + ".jpg";

            File directory = new File(getFilesDir(), "profile_pictures");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File imageFile = new File(directory, filename);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            android.util.Log.e("UserProfile", "Error saving image to internal storage", e);
            return null;
        }
    }

    private void getUserFromIntent() {
        String userUUIDString = getIntent().getStringExtra("USER_UUID");

        if (userUUIDString != null) {
            UUID userUUID = UUID.fromString(userUUIDString);
            currentUser = UserDAO.getInstance().getByUUID(userUUID);
        } else {
            UUID sessionUUID = Session.load(this);
            if (sessionUUID != null) {
                currentUser = UserDAO.getInstance().getByUUID(sessionUUID);
            } else {
                Iterator<User> userIterator = UserDAO.getInstance().getAll();
                if (userIterator.hasNext()) {
                    currentUser = userIterator.next();
                }
            }
        }
    }

    private void displayUserProfile() {
        if (currentUser == null) {
            textViewUsername.setText("Unknown User");
            imageViewProfilePicture.setImageResource(R.drawable.default_profile);
            buttonLogout.setVisibility(View.GONE);
            textViewPostsHeader.setVisibility(View.GONE);
            return;
        }

        textViewUsername.setText(currentUser.username());
        ProfilePictureHelper.loadProfilePicture(this, currentUser.getUUID(), imageViewProfilePicture);

        UUID sessionUUID = Session.load(this);
        if (sessionUUID != null && currentUser.getUUID().equals(sessionUUID)) {
            buttonLogout.setVisibility(View.VISIBLE);
//            textViewPostsHeader.setText("Posts by You");
        } else {
            buttonLogout.setVisibility(View.GONE);
//            textViewPostsHeader.setText("Posts by @" + currentUser.username());
        }

        textViewPostsHeader.setVisibility(View.VISIBLE);
        calculateUserStats();
    }



    private void calculateUserStats() {
        if (currentUser == null) {
            return;
        }

        int postCount = 0;
        int messageCount = 0;

        Iterator<Post> postIterator = PostDAO.getInstance().getAll();
        while (postIterator.hasNext()) {
            Post post = postIterator.next();

            if (post.poster.equals(currentUser.getUUID())) {
                postCount++;
            }

            if (post.messages != null) {
                Iterator<Message> messageIterator = post.messages.getAll();
                while (messageIterator.hasNext()) {
                    Message message = messageIterator.next();
                    if (message.poster().equals(currentUser.getUUID())) {
                        messageCount++;
                    }
                }
            }
        }

        textViewPostCount.setText(String.valueOf(postCount));
        textViewMessageCount.setText(String.valueOf(messageCount));
    }

    private void loadUserPosts() {
        if (currentUser == null) {
            return;
        }

        userPosts = new ArrayList<>();

        Iterator<Post> postIterator = PostDAO.getInstance().getAll();
        while (postIterator.hasNext()) {
            Post post = postIterator.next();
            if (post.poster.equals(currentUser.getUUID())) {
                userPosts.add(post);
            }
        }

        pinManager.sortWithPinnedFirst(userPosts);

        if (userPosts.isEmpty()) {
            recyclerViewUserPosts.setVisibility(View.GONE);
            textViewPostsHeader.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerViewUserPosts.setVisibility(View.VISIBLE);
            textViewPostsHeader.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);

            postAdapter = new PostListAdapter(userPosts, this, pinManager);
            recyclerViewUserPosts.setAdapter(postAdapter);
        }
    }

    private void setupBackButton() {
        buttonBack.setOnClickListener(v -> finish());
    }

    private void setupLogoutButton() {
        buttonLogout.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Session.clear(UserProfileActivity.this);
                    Toast.makeText(UserProfileActivity.this,
                            "Logged out successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onPostClick(Post post) {
        Intent intent = new Intent(UserProfileActivity.this, PostViewerActivity.class);
        intent.putExtra("POST_UUID", post.getUUID().toString());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getUserFromIntent();
        calculateUserStats();
        loadUserPosts();
        displayUserProfile();
        updateBottomNavigationSelection();
    }

    @Override
    protected void setBottomNavigationSelectedItem() {
        updateBottomNavigationSelection();
    }

    private void updateBottomNavigationSelection() {
        if (bottomNavigation == null) {
            return;
        }

        UUID sessionUserUUID = Session.load(this);
        boolean isOwnProfile = currentUser != null &&
                sessionUserUUID != null &&
                currentUser.getUUID().equals(sessionUserUUID);

        bottomNavigation.setOnItemSelectedListener(null);

        if (isOwnProfile) {
            bottomNavigation.setSelectedItemId(R.id.navigation_profile);
        } else {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }

        setupBottomNavigationListener();
    }

    private void setupBottomNavigationListener() {
        if (bottomNavigation == null) {
            return;
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                navigateToHome();
                return true;
            } else if (itemId == R.id.navigation_search) {
                navigateToSearch();
                return true;
            } else if (itemId == R.id.navigation_add) {
                navigateToCreate();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                navigateToProfile();
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.getBooleanExtra("REFRESH", false)) {
            getUserFromIntent();
            displayUserProfile();
            loadUserPosts();
        }
    }
}
