package com.example.letsgogolfing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private Button loginButton;
    private Button signUpButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page); // Set the layout for the activity
        db = FirebaseFirestore.getInstance();

        // Initialize Firestore and UI elements
        usernameInput = findViewById(R.id.usernameInput);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        loginButton.setOnClickListener(v -> attemptLogin());
        signUpButton.setOnClickListener(v -> attemptSignUp());
    }

    private void attemptLogin() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String username = usernameInput.getText().toString().trim();
        if (!username.isEmpty()) {
            db.collection("users") // Assuming you have a 'users' collection
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (!task.getResult().isEmpty()) {
                                // User exists, proceed to login
                                proceedToMain(username);
                            } else {
                                // User does not exist, prompt to sign up
                                Toast.makeText(LoginActivity.this, "User does not exist, please sign up", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Error checking user", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
        }
    }

    private void attemptSignUp() {
        String username = usernameInput.getText().toString().trim();
        if (!username.isEmpty()) {
            db.collection("users") // Assuming you have a 'users' collection
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (task.getResult().isEmpty()) {
                                // Username does not exist, can create new user
                                addUserToDatabase(username);
                            } else {
                                // Username already exists, prompt to log in
                                Toast.makeText(LoginActivity.this, "Username already exists, please login", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Error checking for username", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
        }
    }

    private void proceedToMain(String username) {
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("username", username).apply();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void addUserToDatabase(String username) {
        // Create a new user with a username
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> proceedToMain(username))
                .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error adding user", Toast.LENGTH_SHORT).show());
    }
}
