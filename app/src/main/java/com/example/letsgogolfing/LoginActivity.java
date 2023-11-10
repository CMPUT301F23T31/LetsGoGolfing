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
     * Attempts to log in a user by checking the existence of the provided username in the Firestore
     * 'users' collection. Retrieves the username from the input field, performs a query to check if
     * the user exists, and takes appropriate actions based on the result:
     * <ul>
     *   <li>If the username is not empty and a user with the provided username is found, the method
     *   proceeds to the main activity by calling {@link #proceedToMain(String)}.</li>
     *   <li>If the username is empty, displays a Toast message instructing the user to enter a
     *   username.</li>
     *   <li>If the username is not empty but no user with the provided username is found, displays a
     *   Toast message indicating that the user does not exist and prompts the user to sign up.</li>
     *   <li>If an error occurs during the user-checking process, displays a Toast message indicating
     *   that an error occurred while checking for the user.</li>
     * </ul>
     */
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


    /**
     * Attempts to sign up a new user by checking the existence of the provided username in the Firestore
     * 'users' collection. Retrieves the username from the input field, performs a query to check if
     * the username already exists, and takes appropriate actions based on the result:
     * <ul>
     *   <li>If the username is not empty and no user with the provided username is found, the method
     *   proceeds to add the new user to the database by calling {@link #addUserToDatabase(String)}.</li>
     *   <li>If the username is empty, displays a Toast message instructing the user to enter a
     *   username.</li>
     *   <li>If the username is not empty but a user with the provided username already exists, displays
     *   a Toast message indicating that the username is taken and prompts the user to log in.</li>
     *   <li>If an error occurs during the username-checking process, displays a Toast message indicating
     *   that an error occurred while checking for the username.</li>
     * </ul>
     */
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


    /**
     * Proceeds to the main activity after a successful login. Stores the provided username in the
     * SharedPreferences under the key "username" in the "AppPrefs" file. Creates an intent to start
     * the {@link MainActivity}, starts the activity, and finishes the current login activity.
     *
     * @param username The username of the logged-in user to be stored in SharedPreferences.
     */
    private void proceedToMain(String username) {
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("username", username).apply();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();


    }

    /**
     * Adds a new user with the provided username to the "users" collection in Firestore.
     * Upon successful addition, it navigates to the MainActivity and finishes the current activity.
     *
     * @param username A String representing the username to be added to the database.
     */
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
