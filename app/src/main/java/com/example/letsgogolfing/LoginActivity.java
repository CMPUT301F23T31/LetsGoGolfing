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

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private Button loginButton;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

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
