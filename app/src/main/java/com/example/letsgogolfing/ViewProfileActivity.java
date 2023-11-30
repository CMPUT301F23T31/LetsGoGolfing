package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/**
 * Activity for viewing the user's profile.
 */
public class ViewProfileActivity extends AppCompatActivity {

    private TextView totalItems;
    private TextView totalCost;
    private TextView userName;
    private FirestoreRepository firestoreRepository;


    /**
     * onCreate method for the ViewProfileActivity.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        totalItems = findViewById(R.id.totalItemCount);
        totalCost = findViewById(R.id.totalItemValue);
        userName = findViewById(R.id.nameLabel);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentUsername = prefs.getString("username", null);

        firestoreRepository = new FirestoreRepository(currentUsername);
        fetchProfileData();

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            finish();
        });

        Button logout_button = findViewById(R.id.logout_button);

        logout_button.setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void logoutUser() {
        clearUserData();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void clearUserData() {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("username");
        editor.apply();
    }


    /**
     * Fetches the user's profile data from Firestore.
     */
    private void fetchProfileData() {
        firestoreRepository.fetchItems(new FirestoreRepository.OnItemsFetchedListener() {
            @Override
            public void onItemsFetched(List<Item> items) {
                int totalItemCount = items.size();
                double totalItemValue = 0;
                for (Item item : items) {
                    totalItemValue += item.getEstimatedValue(); // Assuming getEstimatedValue() returns the item value
                }
                totalItems.setText(String.valueOf(totalItemCount));
                totalCost.setText(String.format(getString(R.string.cost_formatting), totalItemValue));
                userName.setText(firestoreRepository.getCurrentUserId());
            }

            @Override
            public void onError(Exception e) {
                // Handle the error
            }
        });
    }
}
