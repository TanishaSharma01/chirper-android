package com.example.hackathon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hackathon.helpers.PostSearchHelper;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.managers.PinPostManager;
import com.example.hackathon.managers.ThemeManager;
import com.example.hackathon.ui.dialogs.PinConfirmationDialog;
import com.example.hackathon.ui.dialogs.UnpinConfirmationDialog;
import com.example.hackathon.ui.dialogs.PostActionDialog;
import com.example.hackathon.ui.handlers.PostSwipeHandler;
import com.example.hackathon.ui.handlers.PostLongPressHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SearchActivity extends BaseActivity implements PostListAdapter.OnPostClickListener {

    private RecyclerView recyclerViewPosts;
    private PostListAdapter postAdapter;
    private TextView textViewNoResults;
    private SearchView searchViewPosts;
    private List<Post> posts;
    private List<Post> filteredPosts;
    private PinPostManager pinManager;
    private PostSearchHelper searchHelper;
    private PostSwipeHandler swipeHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.getInstance(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        pinManager = PinPostManager.getInstance(this);
        searchHelper = new PostSearchHelper(pinManager);

        initializeViews();
        setupSearchView();
        loadPosts();
        setupLongPressForUnpin();
        setupBottomNavigation();
    }

    private void initializeViews() {
        textViewNoResults = findViewById(R.id.textViewNoResults);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        searchViewPosts = findViewById(R.id.searchViewPosts);

        searchViewPosts.setIconified(false);
        searchViewPosts.requestFocus();
    }

    private void setupSearchView() {
        searchViewPosts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPosts(newText);
                return true;
            }
        });

        searchViewPosts.setOnCloseListener(() -> {
            searchViewPosts.setQuery("", false);
            filterPosts("");
            searchViewPosts.clearFocus();
            return true;
        });
    }

    private void loadPosts() {
        posts = searchHelper.loadAllPosts();
        filteredPosts = new ArrayList<>(posts);
        updatePostList();
    }

    private void filterPosts(String query) {
        if (posts == null) {
            return;
        }

        filteredPosts = searchHelper.filterPosts(posts, query);
        pinManager.sortWithPinnedFirst(filteredPosts);

        updatePostList();

        if (filteredPosts.isEmpty()) {
            recyclerViewPosts.setVisibility(View.GONE);
            textViewNoResults.setVisibility(View.VISIBLE);
        } else {
            recyclerViewPosts.setVisibility(View.VISIBLE);
            textViewNoResults.setVisibility(View.GONE);
        }
    }

    private void updatePostList() {
        // Create new adapter
        postAdapter = new PostListAdapter(filteredPosts, this, pinManager);
        postAdapter.setOnAuthorClickListener(this::openUserProfile);
        recyclerViewPosts.setAdapter(postAdapter);

        // Update the swipe handler's list reference OR recreate it
        if (swipeHandler != null) {
            swipeHandler.detach();
        }
        setupSwipeToPin();
    }

    /**
     * Sets up swipe to pin/unpin functionality
     * MUST be called after filteredPosts is updated
     */


    private void setupSwipeToPin() {
        swipeHandler = new PostSwipeHandler(this, filteredPosts, recyclerViewPosts,
                new PostSwipeHandler.OnPostPinnedListener() {
                    @Override
                    public void onPostPinned(int fromPosition, int toPosition) {
                        pinManager.sortWithPinnedFirst(posts);
                        pinManager.sortWithPinnedFirst(filteredPosts);
                        updatePostList();
                        recyclerViewPosts.smoothScrollToPosition(toPosition);
                    }

                    @Override
                    public void onPostUnpinned(int fromPosition, int toPosition) {
                        pinManager.sortWithPinnedFirst(posts);
                        pinManager.sortWithPinnedFirst(filteredPosts);
                        updatePostList();
                        recyclerViewPosts.smoothScrollToPosition(toPosition);
                    }

                    @Override
                    public void onSwipeCancelled(int position) {
                        updatePostList();
                    }
                });

        swipeHandler.attach();
    }

    private void setupLongPressForUnpin() {
        PostLongPressHandler longPressHandler = new PostLongPressHandler(
                this,
                recyclerViewPosts,
                new PostLongPressHandler.OnItemLongPressListener() {
                    @Override
                    public void onItemLongPress(View view, int position) {
                        handlePostLongPress(position);
                    }
                }
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
                    }
                });

        dialog.show(post, position);
    }

//    private void pinPost(Post post, int currentPosition) {
//        int newPosition = pinManager.pinPost(filteredPosts, post, currentPosition);
//        pinManager.sortWithPinnedFirst(posts);
//
//        postAdapter.notifyItemMoved(currentPosition, newPosition);
//        postAdapter.notifyItemChanged(newPosition);
//        recyclerViewPosts.smoothScrollToPosition(newPosition);
//    }
//
//    private void unpinPost(Post post, int currentPosition) {
//        int newPosition = pinManager.unpinPostAndReposition(filteredPosts, post, currentPosition);
//        pinManager.sortWithPinnedFirst(posts);
//        if (currentPosition != newPosition) {
//            postAdapter.notifyItemMoved(currentPosition, newPosition);
//        }
//        postAdapter.notifyItemChanged(newPosition);
//        recyclerViewPosts.smoothScrollToPosition(newPosition);
//    }


    private void pinPost(Post post, int currentPosition) {
        int newPosition = pinManager.pinPost(filteredPosts, post, currentPosition);
        pinManager.sortWithPinnedFirst(posts);

        // Recreate adapter for instant update (no animation)
        postAdapter = new PostListAdapter(filteredPosts, SearchActivity.this, pinManager);
        postAdapter.setOnAuthorClickListener(this::openUserProfile);
        recyclerViewPosts.setAdapter(postAdapter);
        recyclerViewPosts.smoothScrollToPosition(newPosition);
    }

    private void unpinPost(Post post, int currentPosition) {
        int newPosition = pinManager.unpinPostAndReposition(filteredPosts, post, currentPosition);
        pinManager.sortWithPinnedFirst(posts);

        // Recreate adapter for instant update (no animation)
        postAdapter = new PostListAdapter(filteredPosts, SearchActivity.this, pinManager);
        postAdapter.setOnAuthorClickListener(this::openUserProfile);
        recyclerViewPosts.setAdapter(postAdapter);
        recyclerViewPosts.smoothScrollToPosition(newPosition);
    }

    @Override
    public void onPostClick(Post post) {
        Intent intent = new Intent(SearchActivity.this, PostViewerActivity.class);
        intent.putExtra("POST_UUID", post.getUUID().toString());
        startActivity(intent);
    }

    private void openUserProfile(UUID userUUID) {
        Intent intent = new Intent(SearchActivity.this, UserProfileActivity.class);
        intent.putExtra("USER_UUID", userUUID.toString());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        posts = searchHelper.loadAllPosts();
        pinManager.sortWithPinnedFirst(posts);

        String currentQuery = searchViewPosts.getQuery().toString();
        filterPosts(currentQuery);

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_search);
        }
    }

    @Override
    protected void setBottomNavigationSelectedItem() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_search);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // Check if we need to refresh
        if (intent.getBooleanExtra("REFRESH", false)) {
            refreshFromNavigation();
        }
    }
    private void refreshFromNavigation() {
        posts = searchHelper.loadAllPosts();
        String currentQuery = searchViewPosts.getQuery().toString();
        filterPosts(currentQuery);
    }
}
