package com.example.letsgogolfing.controllers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letsgogolfing.R;
import com.example.letsgogolfing.models.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;


/**
 * Activity for managing tags.
 * This activity allows the user to add new tags and delete existing tags.
 */
public class ManageTagsActivity extends AppCompatActivity {
    private ArrayAdapter<String> tagsAdapter;
    private List<String> tagsList = new ArrayList<>();
    private FirestoreRepository firestoreRepository;

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
        setContentView(R.layout.activity_manage_tags);

        // Retrieve the current username from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentUsername = sharedPref.getString("username", null);

        // Initialize FirestoreRepository with the current username
        firestoreRepository = new FirestoreRepository(currentUsername);

        ListView tagsListView = findViewById(R.id.tagsListView);
        tagsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tagsList);
        tagsListView.setAdapter(tagsAdapter);

        fetchTags();

        EditText newTagEditText = findViewById(R.id.newTagEditText);
        Button addTagButton = findViewById(R.id.addTagButton);
        Button doneButton = findViewById(R.id.doneButton);

        addTagButton.setOnClickListener(v -> {
            String newTag = newTagEditText.getText().toString().trim();
            if (!newTag.isEmpty() && !tagsList.contains(newTag)) {
                // Add the tag using FirestoreRepository
                firestoreRepository.addTag(newTag, new FirestoreRepository.OnTagAddedListener() {
                    @Override
                    public void onTagAdded() {
                        tagsList.add(newTag);
                        tagsAdapter.notifyDataSetChanged();
                        Toast.makeText(ManageTagsActivity.this, "Tag added", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(ManageTagsActivity.this, "Error adding tag", Toast.LENGTH_SHORT).show();
                    }
                });
                newTagEditText.setText("");
            } else {
                Toast.makeText(this, "Tag is empty or already exists.", Toast.LENGTH_SHORT).show();
            }
        });

        doneButton.setOnClickListener(v -> finish());
    }

    /**
     * Fetches the list of tags from FirestoreRepository and updates the adapter.
     */
    private void fetchTags() {
        firestoreRepository.fetchTags(new FirestoreRepository.OnTagsFetchedListener() {
            @Override
            public void onTagsFetched(List<String> tags) {
                tagsList.clear();
                tagsList.addAll(tags);
                tagsAdapter.notifyDataSetChanged();
            }

            /**
             * Called when an error occurs while fetching tags.
             *
             * @param e The exception that occurred.
             */
            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageTagsActivity.this, "Error fetching tags", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
