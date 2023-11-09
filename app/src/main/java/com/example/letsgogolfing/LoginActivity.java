package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page); // Set the layout for the activity

        usernameInput = findViewById(R.id.usernameInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            if (!username.isEmpty()) {
                // Assuming no actual authentication is needed and any username is accepted
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close LoginActivity after moving to MainActivity
            } else {
                // No username entered, show an error
                Toast.makeText(LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
