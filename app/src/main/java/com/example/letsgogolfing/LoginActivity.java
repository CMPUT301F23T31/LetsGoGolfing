package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        usernameInput = findViewById(R.id.usernameInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (!username.isEmpty()) {
                checkUnique(username);
                // the following lines store the username so that it can be accessed at any point of the session
                // this is a common practice in android app design - vt
                getSharedPreferences("AppPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("username", username)
                        .apply();
            } else {
                Toast.makeText(LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks whether the given username is unique within the "users" collection in Firestore.
     * If unique, it proceeds to add the user to the database, otherwise it prompts the user
     * to choose a different username.
     *
     * @param username A String representing the username to be checked for uniqueness.
     */
    private void checkUnique(String username) {
        db.collection("users") // Assuming you have a 'users' collection
            .whereEqualTo("username", username)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().isEmpty()) {
                        // Username does not exist, can create new user
                        addUserToDatabase(username);
                    } else {
                        // Username already exists
                        Toast.makeText(LoginActivity.this, "Username already exists, choose another", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Error checking for username", Toast.LENGTH_SHORT).show();
                }
            });
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
            .addOnSuccessListener(documentReference -> {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(LoginActivity.this, "Error adding user", Toast.LENGTH_SHORT).show();
            });
    }
}
