package com.example.hackathon;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.persistentdata.DataManager;
import com.example.hackathon.persistentdata.io.AndroidIOFactory;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput, confirmPasswordInput;
    private CheckBox showPasswordCheckbox, showConfirmPasswordCheckbox;
    private Button registerButton, backToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        PersistentBootstrap.ensureSeedUsers(this);

        DataManager dm = new DataManager(new AndroidIOFactory(this));
        try { dm.readAll(); } catch (Exception ignored) {}

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        showPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);
        showConfirmPasswordCheckbox = findViewById(R.id.showConfirmPasswordCheckbox);
        registerButton = findViewById(R.id.registerButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);

        showPasswordCheckbox.setOnCheckedChangeListener((b, v) -> togglePassword(passwordInput, v));
        showConfirmPasswordCheckbox.setOnCheckedChangeListener((b, v) -> togglePassword(confirmPasswordInput, v));

        registerButton.setOnClickListener(v -> handleRegister());
        backToLoginButton.setOnClickListener(v -> finish());
    }

    private void togglePassword(EditText et, boolean visible) {
        et.setTransformationMethod(visible
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
        et.setSelection(et.getText().length());
    }

//    private void handleRegister() {
//
//        DataManager dm = new DataManager(new AndroidIOFactory(this));
//        try {
//            dm.readAll();
//        } catch (Exception ignored) {}
//
//        String username = usernameInput.getText().toString().trim();
//        String password = passwordInput.getText().toString().trim();
//        String confirm = confirmPasswordInput.getText().toString().trim();
//
//        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
//            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (!password.equals(confirm)) {
//            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        User newUser = UserDAO.getInstance().register(username, password);
//        if (newUser == null) {
//            Toast.makeText(this, "Username invalid or already exists", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Persist everything back to CSVs
//        try {
//            dm.writeAll();
//        } catch (Exception ignored) {
//            Log.d("D", "database issue");
//        }
//    }

    private void handleRegister() {
        DataManager dm = new DataManager(new AndroidIOFactory(this));
        try {
            dm.readAll();
        } catch (Exception ignored) {}

        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirm = confirmPasswordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Password should not contain spaces and be at least 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = UserDAO.getInstance().register(username, password);
        if (newUser == null) {
            Toast.makeText(this, "Username invalid or already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            dm.writeAll();
        } catch (Exception e) {
            Log.e("RegisterActivity", "Failed to save user data", e);
            Toast.makeText(this, "Error saving user. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "User created successfully!", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("USERNAME", username);  // Optional pre-fill username in login
        intent.putExtra("REGISTRATIONSUCCESS", true); // Indicate success in login activity
        startActivity(intent);
        finish();
    }


}
