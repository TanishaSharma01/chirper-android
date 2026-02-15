package com.example.hackathon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathon.dao.RandomContentGenerator;
import com.example.hackathon.dao.PostDAO;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.managers.PinPostManager;
import com.example.hackathon.managers.PostPersistenceManager;
import com.example.hackathon.persistentdata.DataManager;
import com.example.hackathon.persistentdata.io.AndroidIOFactory;
import com.example.hackathon.ui.dialogs.PinConfirmationDialog;
import com.example.hackathon.ui.dialogs.UnpinConfirmationDialog;
import com.example.hackathon.ui.dialogs.PostActionDialog;
import com.example.hackathon.ui.handlers.PostSwipeHandler;
import com.example.hackathon.ui.handlers.PostLongPressHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.hackathon.helpers.PostSearchHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.example.hackathon.managers.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Main activity displaying list of discussion posts with pin/unpin, search, and create functionality
 */
public class MainActivity extends BaseActivity implements PostListAdapter.OnPostClickListener {

    private RecyclerView recyclerViewPosts;
    private PostListAdapter postAdapter;
    private TextView textViewTitle;
    private TextView textViewNoResults;
    private SearchView searchViewPosts;

    private List<Post> posts;  // All posts
    private List<Post> filteredPosts;  // Filtered posts for display

    private PostSwipeHandler swipeHandler;
    private PinPostManager pinManager;
    private FloatingActionButton fabCreatePost;
    private PostSearchHelper searchHelper;

    private SwitchMaterial switchTheme;
    private ThemeManager themeManager;
    private PostPersistenceManager postPersistenceManager;

    private ActivityResultLauncher<Intent> createPostLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        pinManager = PinPostManager.getInstance(this);
        postPersistenceManager = PostPersistenceManager.getInstance(this);
        searchHelper = new PostSearchHelper(pinManager);

        registerCreatePostLauncher();

        initializeViews();
        setupThemeToggle();
        initializeDataOnce();
        loadPosts();
        setupSwipeToPin();
        setupLongPressForUnpin();
        setupBottomNavigation();
    }

    private void registerCreatePostLauncher() {
        createPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshPostList();
                    }
                }
        );
    }

    private void initializeViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewNoResults = findViewById(R.id.textViewNoResults);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        switchTheme = findViewById(R.id.switchTheme);
    }

    private void setupThemeToggle() {
        switchTheme.setChecked(themeManager.isDarkMode());
        updateThemeSwitchText();

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeManager.setDarkMode(isChecked);
            updateThemeSwitchText();
        });
    }

    private void updateThemeSwitchText() {
        if (themeManager.isDarkMode()) {
            switchTheme.setText("☀️");
        } else {
            switchTheme.setText("🌙");
        }
    }

//    private void initializeDataOnce() {
//        boolean postsLoaded = postPersistenceManager.loadPosts();
//
//        if (!postsLoaded) {
//            android.util.Log.d("MainActivity", "No saved posts, generating random data");
//            RandomContentGenerator.populateRandomData();
//            postPersistenceManager.savePosts();
//            android.util.Log.d("MainActivity", "Generated posts saved");
//        } else {
//            android.util.Log.d("MainActivity", "Loaded existing posts from storage");
//        }
//    }

    private void initializeDataOnce() {
        DataManager dm = new DataManager(new AndroidIOFactory(this));

        try {
            dm.readAll();

            // Check if we have any posts
            int postCount = 0;
            Iterator<Post> iterator = PostDAO.getInstance().getAll();
            while (iterator.hasNext()) {
                iterator.next();
                postCount++;
            }

            if (postCount == 0) {
                android.util.Log.d("MainActivity", "No saved posts, generating random data");
                RandomContentGenerator.populateRandomData();
                dm.writeAll();
                android.util.Log.d("MainActivity", "Generated posts saved");
            } else {
                android.util.Log.d("MainActivity", "Loaded " + postCount + " posts from storage");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error loading data", e);
            // Generate random data as fallback
            RandomContentGenerator.populateRandomData();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save all data when pausing
        if (!isChangingConfigurations()) {
            DataManager dm = new DataManager(new AndroidIOFactory(this));
            try {
                dm.writeAll();
                android.util.Log.d("MainActivity", "Saved all data");
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error saving data", e);
            }
        }
    }

    private void loadPosts() {
        posts = searchHelper.loadAllPosts();
        pinManager.sortWithPinnedFirst(posts);
        filteredPosts = new ArrayList<>(posts);

        postAdapter = new PostListAdapter(filteredPosts, this, pinManager);
        postAdapter.setOnAuthorClickListener(this::openUserProfile);
        recyclerViewPosts.setAdapter(postAdapter);
    }

    private void setupSwipeToPin() {
        if (filteredPosts == null || filteredPosts.isEmpty()) {
            return;
        }

        // Detach old handler if exists
        if (swipeHandler != null) {
            swipeHandler.detach();
        }

        swipeHandler = new PostSwipeHandler(this, filteredPosts, recyclerViewPosts,
                new PostSwipeHandler.OnPostPinnedListener() {
                    @Override
                    public void onPostPinned(int fromPosition, int toPosition) {
                        pinManager.sortWithPinnedFirst(posts);
                        pinManager.sortWithPinnedFirst(filteredPosts);

                        // Recreate adapter and reattach handlers
                        recreateAdapterAndHandlers();
                        recyclerViewPosts.smoothScrollToPosition(toPosition);
                    }

                    @Override
                    public void onPostUnpinned(int fromPosition, int toPosition) {
                        pinManager.sortWithPinnedFirst(posts);
                        pinManager.sortWithPinnedFirst(filteredPosts);

                        // Recreate adapter and reattach handlers
                        recreateAdapterAndHandlers();
                        recyclerViewPosts.smoothScrollToPosition(toPosition);
                    }

                    @Override
                    public void onSwipeCancelled(int position) {
                        // Recreate adapter to reset visual state
                        postAdapter = new PostListAdapter(filteredPosts, MainActivity.this, pinManager);
                        postAdapter.setOnAuthorClickListener(MainActivity.this::openUserProfile);
                        recyclerViewPosts.setAdapter(postAdapter);
                    }
                });

        swipeHandler.attach();
    }

    /**
     * Recreates adapter and reattaches all handlers after data changes
     */
    private void recreateAdapterAndHandlers() {
        // Recreate adapter
        postAdapter = new PostListAdapter(filteredPosts, MainActivity.this, pinManager);
        postAdapter.setOnAuthorClickListener(this::openUserProfile);
        recyclerViewPosts.setAdapter(postAdapter);

        // Reattach swipe handler with new adapter
        setupSwipeToPin();
    }

    private void setupLongPressForUnpin() {
        PostLongPressHandler longPressHandler = new PostLongPressHandler(
                this,
                recyclerViewPosts,
                (view, position) -> handlePostLongPress(position)
        );

        recyclerViewPosts.addOnItemTouchListener(longPressHandler);
    }

    private void handlePostLongPress(final int position) {
        if (position < 0 || position >= filteredPosts.size()) {
            return;
        }

        final Post post = filteredPosts.get(position);
        boolean isPinned = pinManager.isPinned(post);

        PostActionDialog actionDialog = new PostActionDialog(this,
                new PostActionDialog.OnActionSelectedListener() {
                    @Override
                    public void onPinSelected(Post post, int position) {
                        showPinConfirmation(post, position);
                    }

                    @Override
                    public void onUnpinSelected(Post post, int position) {
                        showUnpinConfirmation(post, position);
                    }
                });

        actionDialog.show(post, position, isPinned);
    }

    private void showPinConfirmation(final Post post, final int position) {
        PinConfirmationDialog dialog = new PinConfirmationDialog(this,
                new PinConfirmationDialog.OnPinConfirmationListener() {
                    @Override
                    public void onPinConfirmed(Post post, int position) {
                        pinPost(post, position);
                    }

                    @Override
                    public void onPinCancelled(int position) {
                        // Do nothing
                    }
                });

        dialog.show(post, position);
    }

    private void showUnpinConfirmation(final Post post, final int position) {
        UnpinConfirmationDialog dialog = new UnpinConfirmationDialog(this,
                new UnpinConfirmationDialog.OnUnpinConfirmationListener() {
                    @Override
                    public void onUnpinConfirmed(Post post, int position) {
                        unpinPost(post, position);
                    }

                    @Override
                    public void onUnpinCancelled() {
                        // Do nothing
                    }
                });

        dialog.show(post, position);
    }


    private void pinPost(Post post, int currentPosition) {
        int newPosition = pinManager.pinPost(filteredPosts, post, currentPosition);
        pinManager.sortWithPinnedFirst(posts);

        // Recreate adapter and handlers
        recreateAdapterAndHandlers();
        recyclerViewPosts.smoothScrollToPosition(newPosition);
    }

    private void unpinPost(Post post, int currentPosition) {
        int newPosition = pinManager.unpinPostAndReposition(filteredPosts, post, currentPosition);
        pinManager.sortWithPinnedFirst(posts);

        // Recreate adapter and handlers
        recreateAdapterAndHandlers();
        recyclerViewPosts.smoothScrollToPosition(newPosition);
    }

    private void refreshPostList() {
        // Reload posts from DAO
        posts = searchHelper.loadAllPosts();
        pinManager.sortWithPinnedFirst(posts);
        filteredPosts = new ArrayList<>(posts);

        // Recreate adapter and handlers
        recreateAdapterAndHandlers();

        // Scroll to first unpinned post
        int pinnedCount = 0;
        for (Post post : filteredPosts) {
            if (pinManager.isPinned(post)) {
                pinnedCount++;
            } else {
                break;
            }
        }

        if (pinnedCount < filteredPosts.size()) {
            recyclerViewPosts.smoothScrollToPosition(pinnedCount);
        }
    }

    @Override
    public void onPostClick(Post post) {
        Intent intent = new Intent(MainActivity.this, PostViewerActivity.class);
        intent.putExtra("POST_UUID", post.getUUID().toString());
        startActivity(intent);
    }

    private void openUserProfile(UUID userUUID) {
        Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
        intent.putExtra("USER_UUID", userUUID.toString());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload posts from DAO (includes newly created posts)
        posts = searchHelper.loadAllPosts();
        pinManager.sortWithPinnedFirst(posts);
        filteredPosts = new ArrayList<>(posts);

        // Recreate adapter and handlers to show new posts
        recreateAdapterAndHandlers();

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }

        android.util.Log.d("MainActivity", "Resumed with " + posts.size() + " posts");
    }

    @Override
    protected void setBottomNavigationSelectedItem() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.getBooleanExtra("REFRESH", false)) {
            posts = searchHelper.loadAllPosts();
            pinManager.sortWithPinnedFirst(posts);
            filteredPosts = new ArrayList<>(posts);
            recreateAdapterAndHandlers();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PostPersistenceManager.getInstance(this).savePosts();
    }
}
