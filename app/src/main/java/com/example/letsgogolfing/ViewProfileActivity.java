package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.DataRepository.db;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.text.DecimalFormat;

public class ViewProfileActivity extends AppCompatActivity {

    private static final String TAG = "ViewProfileActivity";

    private TextView totalItems;
    private TextView totalCost;
    private TextView userName;
    private final DecimalFormat df = new DecimalFormat("#,###.##");

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
                totalCost = findViewById(R.id.totalItemValue);
                totalCost.setText(this.getApplicationContext().getString(R.string.item_value , df.format(totalItemValue)));
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE); // fetch username from session
                String username = prefs.getString("username", "No name"); // "No name" is a default value.
                userName.setText(username);
            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
                Toast.makeText(ViewProfileActivity.this, "Failed to fetch data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
