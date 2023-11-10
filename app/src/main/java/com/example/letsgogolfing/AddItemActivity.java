package com.example.letsgogolfing;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for adding a new item to the inventory.
 */
public class AddItemActivity extends AppCompatActivity {

    private Item item;
    private static final String TAG = "EditItemActivity";

    // This should be populated from the ManageTagsActivity
    private List<String> tagList = new ArrayList<>();
    private List<String> selectedTags = new ArrayList<>();

    /**
     * Called when the activity is starting. Responsible for initializing the activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                            shut down, this Bundle contains the data it most recently supplied
     *                            in onSaveInstanceState(Bundle). Note: Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_page);

        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(v -> saveItem());

        Button cancel_button = findViewById(R.id.cancel_button_add_item);
        cancel_button.setOnClickListener(v -> finish());

        Button tagButton = findViewById(R.id.add_tags_button);
        tagButton.setOnClickListener(v -> showTagSelectionDialog());

        fetchTagsFromFirestore();
    }

    /**
     * Fetches tags from Firestore to populate the tagList.
     */
    private void fetchTagsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tags").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tagList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    tagList.add(document.getString("name"));
                }
                // Now that the tags are fetched, you can enable the 'Add Tags' button
            } else {
                Log.w(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    /**
     * Displays a dialog for selecting tags from the available tagList. The method converts the tagList
     * to an array, initializes the checked tags array, and pre-checks the tags that have been previously selected.
     * The selected tags are displayed in a LinearLayout within the dialog.
     *
     * @throws Exception If an error occurs while creating or showing the dialog.
     */
    private void showTagSelectionDialog() {
        // Convert List to array for AlertDialog
        tagList.removeAll(Collections.singleton(null));
        String[] tagsArray = tagList.toArray(new String[0]);
        boolean[] checkedTags = new boolean[tagList.size()];
        try {
            // ... existing dialog code ...
            // Pre-check the tags that have been previously selected
            for(int i = 0; i < tagList.size(); i++) {
                if(selectedTags.contains(tagList.get(i))) {
                    checkedTags[i] = true;
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMultiChoiceItems(tagsArray, checkedTags, (dialog, which, isChecked) -> {
                // Add or remove the tag from the selected tags list based on whether the checkbox is checked
                String selectedTag = tagList.get(which);
                if (isChecked) {
                    selectedTags.add(selectedTag);
                } else {
                    selectedTags.remove(selectedTag);
                }
            });
            builder.setPositiveButton("OK", (dialog, which) -> {
                LinearLayout tagsContainer = findViewById(R.id.tagsContainer);
                if (tagsContainer != null) {
                    tagsContainer.removeAllViews(); // Clear all views/tags before adding new ones

                    for (String tag : selectedTags) {
                        TextView tagView = new TextView(this);
                        tagView.setText(tag);
                        tagView.setBackgroundResource(R.drawable.tag_background); // Make sure this drawable exists
                        // Add LayoutParams, margins, etc., here
                        tagsContainer.addView(tagView); // Add the TextView to your container
                    }
                } else {
                    Log.e(TAG, "tagsContainer is null");
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Saves the entered information as a new item in the inventory. Retrieves user input from various
     * EditText fields, validates and parses the data, creates a new Item object, and stores it in the
     * Firestore database using the Firestore API. Displays Toast messages for validation errors or
     * successful item addition.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Creates a new {@link Item} object.</li>
     *   <li>Sets the name, description, make, model, and comment on the Item object.</li>
     *   <li>Parses and sets the date of purchase.</li>
     *   <li>Parses and sets the estimated value.</li>
     *   <li>Sets the tags from the selectedTags list.</li>
     *   <li>Uses the Firestore API to add the Item object to the "items" collection.</li>
     *   <li>Handles success and failure cases, displaying appropriate Toast messages.</li>
     * </ol>
     * </p>
     */
    private void saveItem() {
        // Create a new Item object
        Item newItem = new Item();

        // Set name, description, make, model, and comment directly on the Item object
        newItem.setName(((EditText) findViewById(R.id.nameField)).getText().toString());
        newItem.setDescription(((EditText) findViewById(R.id.descriptionField)).getText().toString());
        newItem.setMake(((EditText) findViewById(R.id.makeField)).getText().toString());
        newItem.setModel(((EditText) findViewById(R.id.modelField)).getText().toString());
        newItem.setComment(((EditText) findViewById(R.id.commentField)).getText().toString());

        // Parse and set the date of purchase
        String dateString = ((EditText) findViewById(R.id.dateField)).getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            if (date != null) {
                newItem.setDateOfPurchase(date);
            } else {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Failed to parse date", Toast.LENGTH_LONG).show();
            return;
        }

        // Parse and set the estimated value
        try {
            double estimatedValue = Double.parseDouble(((EditText) findViewById(R.id.valueField)).getText().toString());
            newItem.setEstimatedValue(estimatedValue);
        } catch (NumberFormatException e) {
            Toast.makeText(AddItemActivity.this, "No Value Entered. Defaulted to 0.", Toast.LENGTH_SHORT).show();
            double estimatedValue = 0;
        }

        // Parse and set the tags
        newItem.setTags(selectedTags);

        // Now, use the Firestore API to add the Item object
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Convert Item object to Map for Firestore
        Map<String, Object> itemMap = convertItemToMap(newItem);

        db.collection("items").add(itemMap)
                .addOnSuccessListener(documentReference -> {
                    // Optionally, save the document ID in the Item object
                    newItem.setId(documentReference.getId());

                    Toast.makeText(AddItemActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                    Intent data = new Intent();
                    data.putExtra("item_added", true);
                    setResult(RESULT_OK, data);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddItemActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the tagList with a new list of tags. Clears the existing tags in the tagList and adds
     * all tags from the newTags list. If needed, this method can be used to update the UI or other
     * elements that depend on the tagList.
     *
     * @param newTags The new list of tags to update the tagList with.
     */
    public void updateTagList(List<String> newTags) {
        tagList.clear();
        tagList.addAll(newTags);
        // If needed, update the UI or other elements that depend on the tagList
    }

    /**
     * Converts an {@link Item} object into a {@link Map} for Firestore. Each field of the Item object
     * is mapped to a corresponding key-value pair in the resulting Map.
     *
     * @param item The Item object to be converted.
     * @return A Map representation of the Item for Firestore.
     */
    // Helper method to convert an Item object into a Map for Firestore
    private Map<String, Object> convertItemToMap(Item item) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", item.getName());
        itemMap.put("description", item.getDescription());
        itemMap.put("dateOfPurchase", new Timestamp(item.getDateOfPurchase()));
        itemMap.put("make", item.getMake());
        itemMap.put("model", item.getModel());
        itemMap.put("serialNumber", item.getSerialNumber());
        itemMap.put("estimatedValue", item.getEstimatedValue());
        itemMap.put("comment", item.getComment());
        itemMap.put("tags", item.getTags());
        return itemMap;
    }


}
