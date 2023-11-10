package com.example.letsgogolfing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.letsgogolfing.utils.FirestoreHelper;
import static com.example.letsgogolfing.utils.FirestoreHelper.db;

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

    private static final String collectionName = "users";
    //Change to "userTestOnly" for testing

    public String toastSucks;

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
        toastSucks = "toastSucks";

        // Initialize UI elements
        usernameInput = findViewById(R.id.usernameInput);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        loginButton.setOnClickListener(v -> attemptLogin(collectionName));
        signUpButton.setOnClickListener(v -> attemptSignUp(collectionName));
    }


    /**
     * Attempts to log in the user with the entered username.
     * This method checks if the username entered in the usernameInput field exists in the Firestore 'users' collection.
     * If the user exists, it calls proceedToMain to navigate to the MainActivity.
     * If the user does not exist, it displays a message prompting the user to sign up.
     */
    private void attemptLogin(String collectionName){
        String username = usernameInput.getText().toString().trim();
        if (!username.isEmpty()) {
            db.collection(collectionName) // Assuming you have a 'users' collection
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (!task.getResult().isEmpty()) {
                                // User exists, proceed to login
                                proceedToMain(username);
                            } else {
                                // User does not exist, prompt to sign up
                                toastSucks = "User does not exist, please sign up";
                                Toast.makeText(LoginActivity.this, "User does not exist, please sign up", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            toastSucks = "Error checking user";
                            Toast.makeText(LoginActivity.this, "Error checking user", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            toastSucks = "Please enter a username";
            Toast.makeText(LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Attempts to sign up a new user with the entered username.
     * This method checks if the username entered in the usernameInput field is unique within the Firestore 'users' collection.
     * If the username does not exist, it calls addUserToDatabase to create a new user.
     * If the username already exists, it displays a message indicating the username is taken and prompting to log in.
     */
    private void attemptSignUp(String collectionName) {
        String username = usernameInput.getText().toString().trim();
        if (!username.isEmpty()) {
            db.collection(collectionName) // Assuming you have a 'users' collection
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (task.getResult().isEmpty()) {
                                // Username does not exist, can create new user
                                addUserToDatabase(username, collectionName);
                            } else {
                                // Username already exists, prompt to log in
                                toastSucks = "Username already exists, please login";
                                Toast.makeText(LoginActivity.this, "Username already exists, please login", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            toastSucks = "Error checking for username";
                            Toast.makeText(LoginActivity.this, "Error checking for username", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            toastSucks = "Please enter a username";
            Toast.makeText(LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Proceeds to the main activity of the application.
     * This method saves the provided username in SharedPreferences under the key "username" and starts MainActivity.
     * It also finishes the current LoginActivity, removing it from the back stack.
     *
     * @param username A String representing the username of the user who has logged in or signed up.
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
     * If the operation fails, it displays an error message to the user.
     *
     * @param username A String representing the username to be added to the database.
     */
    private void addUserToDatabase(String username, String collectionName) {
        // Create a new user with a username
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        // Add a new document with a generated ID
        db.collection(collectionName)
                .add(user)
                .addOnSuccessListener(documentReference -> proceedToMain(username))
                .addOnFailureListener(e -> {
                        // Handle the failure
                        toastSucks = "Error adding user";
                        Toast.makeText(LoginActivity.this, "Error adding user", Toast.LENGTH_SHORT).show();});
    }
}
