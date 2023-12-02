package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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

    public static class ItemFilter extends Filter {
        private final ItemAdapter adapter;
        private final List<Item> originalList;
        private final List<Item> filteredList;

        public ItemFilter(ItemAdapter adapter, List<Item> originalList) {
            this.adapter = adapter;
            this.originalList = new ArrayList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final Item item : originalList) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.clear();
            adapter.addAll((ArrayList<Item>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}
