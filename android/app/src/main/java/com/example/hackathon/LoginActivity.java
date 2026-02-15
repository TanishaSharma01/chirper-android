package com.example.hackathon;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathon.auth.Session;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.persistentdata.DataManager;
import com.example.hackathon.persistentdata.io.AndroidIOFactory;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton, goToRegisterButton;
    private CheckBox showPasswordCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PersistentBootstrap.ensureSeedUsers(this);

        DataManager dm = new DataManager(new AndroidIOFactory(this));
        try {
            dm.readAll();
        } catch (Exception ignored) {
        }

        UUID saved = Session.load(this);
        if (saved != null) {
            User u = UserDAO.getInstance().getByUUID(saved);
            if (u != null) UserDAO.getInstance().setCurrentUser(u);
        }

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        goToRegisterButton = findViewById(R.id.goToRegisterButton);
        showPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);

        showPasswordCheckbox.setOnCheckedChangeListener((button, checked) ->
                togglePasswordVisibility(checked));

        loginButton.setOnClickListener(v -> handleLogin());
        goToRegisterButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void togglePasswordVisibility(boolean show) {
        passwordInput.setTransformationMethod(show
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
        passwordInput.setSelection(passwordInput.getText().length());
    }

    private void handleLogin() {
        DataManager dm = new DataManager(new AndroidIOFactory(this));
        try {
            dm.readAll();
        } catch (Exception ignored) {
        }

        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        User user = UserDAO.getInstance().login(username, password);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (user != null) {
            UserDAO.getInstance().setCurrentUser(user);
            com.example.hackathon.auth.Session.save(this, user.id());
            Toast.makeText(this, "Welcome, " + user.username(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
}
