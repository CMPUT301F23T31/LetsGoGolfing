package com.example.letsgogolfing;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for managing tags, allowing users to view, add, and save tags to Firestore.
 * <p>
 * This activity displays a list of tags from Firestore. Users can add new tags,
 * and the added tags are both updated in the UI and saved to Firestore. The final tags list can be
 * saved to persistent storage or passed back to the calling activity.
 */
public class ManageTagsActivity extends AppCompatActivity {
    private ArrayAdapter<String> tagsAdapter;
    private List<String> tagsList = new ArrayList<>();

    /**
     * Called when the activity is first created. Initializes the user interface, sets up the tags list,
     * and handles button clicks for adding tags and finishing the activity.
     *
     * @param savedInstanceState A Bundle containing the data most recently supplied in onSaveInstanceState(Bundle).
     *                           May be null if saved state is not available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_tags);

        ListView tagsListView = findViewById(R.id.tagsListView);
        tagsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tagsList);
        tagsListView.setAdapter(tagsAdapter);

        EditText newTagEditText = findViewById(R.id.newTagEditText);
        Button addTagButton = findViewById(R.id.addTagButton);
        Button doneButton = findViewById(R.id.doneButton);

        addTagButton.setOnClickListener(v -> {
            String newTag = newTagEditText.getText().toString().trim();
            if (!newTag.isEmpty() && !tagsList.contains(newTag)) {
                tagsList.add(newTag);
                tagsAdapter.notifyDataSetChanged();
                newTagEditText.setText("");

                // Save to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> tagMap = new HashMap<>();
                tagMap.put("name", newTag);
                db.collection("tags").add(tagMap)
                        .addOnSuccessListener(documentReference -> Toast.makeText(this, "Tag added to Firestore", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Error adding tag to Firestore", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Tag is empty or already exists.", Toast.LENGTH_SHORT).show();
            }
        });


        doneButton.setOnClickListener(v -> {
            // Save the tags list to persistent storage or pass back to MainActivity
            // For this example, we're just finishing the activity
            finish();
        });
    }
}

