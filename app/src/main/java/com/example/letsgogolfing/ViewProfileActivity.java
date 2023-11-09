package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ViewProfileActivity extends AppCompatActivity {

    private TextView totalItems;
    private TextView totalCost;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        totalItems = findViewById(R.id.totalItemCount);
        totalCost = findViewById(R.id.totalItemValue);
        userName = findViewById(R.id.nameLabel);
        fetchProfileData();
        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void fetchProfileData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalItemCount = 0;
                double totalItemValue = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Item item = document.toObject(Item.class);
                    totalItemCount++;
                    totalItemValue += item.getEstimatedValue(); // Replace with your method to get item value
                }
                totalItems.setText(String.valueOf(totalItemCount));
                totalCost.setText(String.format(getString(R.string.cost_formatting), totalItemValue));
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE); // fetch username from session
                String username = prefs.getString("username", "No name"); // "No name" is a default value.
                userName.setText(username);
            } else {
                // Handle the error
            }
        });
    }
}