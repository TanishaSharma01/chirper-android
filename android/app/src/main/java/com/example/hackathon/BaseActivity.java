package com.example.hackathon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathon.auth.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.UUID;

/**
 * Base Activity with common bottom navigation functionality.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Call this after setContentView() in child activities
     */
    protected void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation == null) {
            return;
        }

        // Let child class set which item is selected
        setBottomNavigationSelectedItem();

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

    /**
     * Each child activity overrides this to set which item is selected
     */
    protected void setBottomNavigationSelectedItem(){
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
    }

    protected void navigateToHome() {
        if (!this.getClass().getSimpleName().equals("MainActivity")) {
            Intent intent = new Intent(this, MainActivity.class);
            // Use NEW_TASK to bring MainActivity to front and refresh it
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("REFRESH", true); // Signal to refresh
            startActivity(intent);
        }
    }

    protected void navigateToSearch() {
        if (!this.getClass().getSimpleName().equals("SearchActivity")) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("REFRESH", true); // Signal to refresh
            startActivity(intent);
        }
    }



    protected void navigateToCreate() {
        if (!this.getClass().getSimpleName().equals("CreatePostActivity")) {
            Intent intent = new Intent(this, CreatePostActivity.class);
            startActivity(intent);
        }
    }

    protected void navigateToProfile() {
        UUID currentUserUUID = Session.load(this);

        if (currentUserUUID != null) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            // Pass the session user UUID explicitly to load YOUR profile
            intent.putExtra("USER_UUID", currentUserUUID.toString());

            // Use CLEAR_TOP to remove other UserProfileActivity instances
            // And add NEW_TASK to force recreation
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }





    // Helper method to get current user UUID
// You need to store this when user logs in
    private UUID getCurrentUserUUID() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String uuidString = prefs.getString("CURRENT_USER_UUID", null);
        if (uuidString != null) {
            return UUID.fromString(uuidString);
        }
        return null;
    }
}
