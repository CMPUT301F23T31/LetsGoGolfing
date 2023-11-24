package com.example.letsgogolfing;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.Timestamp;

/**
 * Activity for adding a new item to the inventory.
 * <p>
 * This activity allows users to input information about a new item, including its name,
 * description, make, model, date of purchase, estimated value, and associated tags. Users can
 * select tags from the available list, and the information is then saved to Firestore.
 */
public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "EditItemActivity";
    private List<String> tagList = new ArrayList<>(); // This should be populated from the ManageTagsActivity
    private List<String> selectedTags = new ArrayList<>();
    private FirestoreRepository firestoreRepository;


    private EditText nameField;
    private EditText descriptionField;
    private EditText makeField;
    private EditText modelField;
    private EditText commentField;
    private EditText dateField;
    private EditText valueField;


    /**
     * Called when the activity is starting. Responsible for initializing the activity.
     * <p>
     * This method sets up the UI components and initializes button click listeners.
     * The "Confirm" button triggers the {@link #saveItem()} method, and the "Cancel" button
     * finishes the activity. The "Add Tags" button invokes the {@link #showTagSelectionDialog()} method.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_page);
        // Initialize the FireStoreRepo
        firestoreRepository = new FirestoreRepository();

        nameField = findViewById(R.id.nameField);
        descriptionField = findViewById(R.id.descriptionField);
        makeField = findViewById(R.id.makeField);
        modelField = findViewById(R.id.modelField);
        commentField = findViewById(R.id.commentField);
        dateField = findViewById(R.id.dateField);
        valueField = findViewById(R.id.valueField);


        // confirm button listener
        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(v -> saveItem());

        // cancel button listener
        Button cancel_button = findViewById(R.id.cancel_button_add_item);
        cancel_button.setOnClickListener(v -> finish());

        // add tags button listener
        Button tagButton = findViewById(R.id.add_tags_button);
        tagButton.setOnClickListener(v -> showTagSelectionDialog());

        // Fetch the tags from Firestore
        fetchTagsFromFirestore();
    }


    /**
     * Fetches tags from Firestore.
     * <p>
     * This method queries the Firestore database to retrieve a list of available tags.
     * If the retrieval is successful, the tagList is cleared and populated with the names
     * of the fetched tags. This method is typically called during the initialization of the
     * activity to ensure that the tagList is up-to-date.
     */
    private void fetchTagsFromFirestore() {
        firestoreRepository.fetchTags(new FirestoreRepository.OnTagsFetchedListener() {
            @Override
            public void onTagsFetched(List<String> tags) { // this will make sure to fetch all tags from database into the tags list
                tagList.clear();
                tagList.addAll(tags);
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(AddItemActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
                // Handle error
            }
        });
    }


    /**
     * Displays a dialog for selecting tags.
     * <p>
     * This method presents a dialog that allows users to select tags from the available list.
     * The selected tags are updated in the UI, and the user can associate them with the item.
     * Tags are pre-checked based on the user's previous selections.
     */
    // You might need to pass the tags from MainActivity to here or retrieve them from persistent storage.
    private void showTagSelectionDialog() {
        // Convert List to array for AlertDialog
        tagList.removeAll(Collections.singleton(null));
        String[] tagsArray = tagList.toArray(new String[0]);
        boolean[] checkedTags = new boolean[tagList.size()];


        try {
            // Pre-check the tags that have been previously selected
            for(int i = 0; i < tagList.size(); i++) {
                if(selectedTags.contains(tagList.get(i))) {
                    checkedTags[i] = true;
                }
            }

            // Show the dialog (little pop-up screen) checkbox list of tags
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

            // Add OK and Cancel buttons
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
     * Saves a new item to the Firestore database.
     * <p>
     * This method creates a new {@link Item} object, sets its properties based on user input,
     * and adds it to the Firestore "items" collection. The item's name, description, make, model,
     * comment, date of purchase, estimated value, and tags are obtained from the corresponding
     * input fields in the UI. The date and value are parsed from the user's input, and tags are
     * fetched from the {@code selectedTags} list. The resulting item is added to Firestore, and
     * if successful, the activity finishes and notifies the calling activity with a success flag.
     * </p>
     * <p>
     * Note: This method relies on the {@link #convertItemToMap(Item)} method to convert the
     * {@link Item} object into a {@link Map} for Firestore storage.
     * </p>
     */

    private void saveItem() {
        // Create a new Item object
        Item newItem = new Item();

        // Set name, description, make, model, and comment directly on the Item object
        newItem.setName(nameField.getText().toString());
        newItem.setDescription(descriptionField.getText().toString());
        newItem.setMake(makeField.getText().toString());
        newItem.setModel(modelField.getText().toString());
        newItem.setComment(commentField.getText().toString());

        // Parse and set the date of purchase
        String dateString = dateField.getText().toString();
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

        // Convert Item object to Map for Firestore
        Map<String, Object> itemMap = convertItemToMap(newItem);

        firestoreRepository.addItem(itemMap, new FirestoreRepository.OnItemAddedListener() {
            @Override
            public void onItemAdded(String itemId) {
                Toast.makeText(AddItemActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                // we can now use the itemId if needed
                Intent data = new Intent();
                data.putExtra("item_added", true);
                setResult(RESULT_OK, data);
                finish();
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(AddItemActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
                // Handle error
            }
        });
    }

    /**
     * Converts an {@link Item} object to a {@link Map} for Firestore storage.
     * <p>
     * This method takes an {@code Item} object and creates a {@code Map} where each field
     * of the item is represented by a key-value pair. The keys are field names, and the values
     * are obtained from the corresponding getters in the {@code Item} class. The date of purchase
     * is converted to a {@link Timestamp} for Firestore storage. The resulting {@code Map} can be
     * used to store the item data in Firestore.
     * </p>
     *
     * @param item The {@code Item} object to be converted to a {@code Map}.
     * @return A {@code Map} representing the fields of the provided {@code Item} object.
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
