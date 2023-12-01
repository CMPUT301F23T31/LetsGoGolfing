package com.example.letsgogolfing;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letsgogolfing.utils.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class ManageTagsActivity extends AppCompatActivity {
    private ArrayAdapter<String> tagsAdapter;
    private List<String> tagsList = new ArrayList<>();
    private FirestoreRepository firestoreRepository;

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

    private void fetchTags() {
        firestoreRepository.fetchTags(new FirestoreRepository.OnTagsFetchedListener() {
            @Override
            public void onTagsFetched(List<String> tags) {
                tagsList.clear();
                tagsList.addAll(tags);
                tagsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageTagsActivity.this, "Error fetching tags", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
