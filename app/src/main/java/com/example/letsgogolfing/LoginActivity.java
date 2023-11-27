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

/**
 * LoginActivity is responsible for handling the user login process.
 * It provides a user interface for username input and communicates with Firebase Firestore
 * to check for username uniqueness and add new users to the database.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private Button loginButton;
    private Button signUpButton;
    private FirebaseFirestore db;

    /**
     * Called when the activity is starting. This is where most initialization should go:
     * calling setContentView(int) to inflate the activity's UI, using findViewById(int)
     * to programmatically interact with widgets in the UI, setting up listeners, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
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


    /**
     * Attempts to log in the user with the provided username.
     * If the username is not empty and exists in the database, it proceeds to the MainActivity.
     * Otherwise, it displays a Toast message indicating that the user does not exist.
     */
    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        if (!username.isEmpty()) {
            db.collection("users").document(username).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().exists()) {
                                // User exists, proceed to login
                                proceedToMain(username);
                            } else {
                                // User does not exist, prompt to sign up
                                Toast.makeText(this, "User does not exist, please sign up", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Error checking user
                            Toast.makeText(this, "Error checking user", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Attempts to sign up the user with the provided username.
     * If the username is not empty and does not exist in the database, it adds the user to the database
     * and proceeds to the MainActivity. Otherwise, it displays a Toast message indicating that the
     * username already exists.
     */
    private void attemptSignUp() {
        String username = usernameInput.getText().toString().trim();
        if (!username.isEmpty()) {
            db.collection("users").document(username).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().exists()) {
                                // Username does not exist, can create new user
                                addUserToDatabase(username);
                            } else {
                                // Username already exists, prompt to log in
                                Toast.makeText(this, "Username already exists, please login", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Error checking for username
                            Toast.makeText(this, "Error checking for username", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds a new user with the provided username to the "users" collection in Firestore.
     * Upon successful addition, it navigates to the MainActivity and finishes the current activity.
     *
     * @param username A String representing the username to be added to the database.
     */
    private void addUserToDatabase(String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username); // This might be optional since the username is the document ID

        db.collection("users").document(username).set(user)
                .addOnSuccessListener(documentReference -> proceedToMain(username))
                .addOnFailureListener(e -> Toast.makeText(this, "Error adding user", Toast.LENGTH_SHORT).show());
    }

    /**
     * Navigates to the MainActivity and finishes the current activity.
     *
     * @param username A String representing the username to be passed to the MainActivity.
     */
    private void proceedToMain(String username) {
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("username", username).apply();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
