package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Activity for displaying user profile information, including total item count, total item value, and username.
 * <p>
 * This activity fetches data from the Firestore database, calculates the total item count and value, and displays
 * the information on the user interface. The user's username is retrieved from shared preferences and displayed as well.
 * <p>
 * Note: Make sure to replace placeholder values, such as "R.layout.profile_page" and "R.string.cost_formatting",
 * with the actual resource identifiers from your project.
 */
public class ViewProfileActivity extends AppCompatActivity {

    private TextView totalItems;
    private TextView totalCost;
    private TextView userName;

    /**
     * Called when the activity is first created. Initializes the user interface and fetches profile data from Firestore.
     *
     * @param savedInstanceState A Bundle containing the data most recently supplied in onSaveInstanceState(Bundle).
     *                           May be null if saved state is not available.
     */
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

    /**
     * Fetches user profile data from Firestore and updates the UI with the total item count, total item value, and username.
     * <p>
     * This method queries the Firestore database for items, calculates the total item count and value, retrieves the username
     * from shared preferences, and updates the corresponding TextViews in the UI with the calculated values.
     */
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
