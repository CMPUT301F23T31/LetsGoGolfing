package com.example.letsgogolfing;

import static com.example.letsgogolfing.CameraActivity.MODE_PHOTO_CAMERA;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for adding a new item to the inventory.
 * <p>
 * This activity allows users to input information about a new item, including its name,
 * description, make, model, date of purchase, estimated value, and associated tags. Users can
 * select tags from the available list, and the information is then saved to Firestore.
 */
public class AddItemActivity extends AppCompatActivity {

    private EditText name, description, value, make, model, serial, comment, date;
    private Item item;
    private static final String TAG = "EditItemActivity";

    // IS THE FOLLOWING ARRAYLIST EVEN BEING USED? idk
    private ArrayList<String> tempUris = new ArrayList<>();

    private FirestoreRepository firestoreRepository;


    private List<String> tagList = new ArrayList<>(); // This should be populated from the ManageTagsActivity
    private List<String> selectedTags = new ArrayList<>();


    /**
     * Called when the activity is starting. Responsible for initializing the activity.
     * <p>
     * This method sets up the UI components and initializes button click listeners.
     * The "Confirm" button triggers the {@link #updateItem()} method, and the "Cancel" button
     * finishes the activity. The "Add Tags" button invokes the {@link #showTagSelectionDialog()} method.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_page);

        // Retrieve the current username from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentUsername = sharedPref.getString("username", null);

        // Initialize FirestoreRepository with the current username
        firestoreRepository = new FirestoreRepository(currentUsername);

        item = (Item) getIntent().getSerializableExtra("item");
        if (item == null) {
            item = new Item("", "", new Date(), "", "", "", 0.0, "", new ArrayList<>(), new ArrayList<>());
        }
        InitializeUI(item);

        // confirm button listener - ISSUE?
        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(v -> {
            updateItem(); // THIS COULD BE COOKED
            if (item != null) {
                // Generate the item ID
                firestoreRepository.generateID("items", new FirestoreRepository.OnIDGeneratedListener() {
                    @Override
                    public void onIDGenerated(String itemId) {
                        // Set the item ID
                        item.setId(itemId);
                        addItemToFirestore();
                    }
                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(AddItemActivity.this, "Error generating item ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("AddItemActivity", "Item is null");
            }
        });

        // cancel button listener
        Button cancel_button = findViewById(R.id.cancel_button_add_item);
        cancel_button.setOnClickListener(v -> finish());

        // add tags button listener
        Button tagButton = findViewById(R.id.add_tags_button);
        tagButton.setOnClickListener(v -> showTagSelectionDialog());

        // Fetch tags using FirestoreRepository
        firestoreRepository.fetchTags(new FirestoreRepository.OnTagsFetchedListener() {
            @Override
            public void onTagsFetched(List<String> tags) {
                tagList.clear();
                tagList.addAll(tags);
                // Now that the tags are fetched, you can enable the 'Add Tags' button
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Error getting tags: ", e);
            }
        });


        Button add_photo_button = findViewById(R.id.addPhotoBtn);
        //COULD BE ISSUE - (saveitem();)
        add_photo_button.setOnClickListener(v -> {
                updateItem();
                Intent photoIntent = new Intent(this, CameraActivity.class);
                photoIntent.putExtra("mode", MODE_PHOTO_CAMERA);
                photoIntent.putExtra("BarcodeInfo", false);
                photoIntent.putExtra("item", item);
                startActivity(photoIntent);
        });
    }

    /**
     * Initializes EditText fields and buttons with item data.
     * Retrieves the item details passed via Intent and sets up the user interface components.
     *
     * @param item The item to be edited, passed through the Intent.
     */
    private void InitializeUI(Item item) {
        // Initialize EditTexts
        name = findViewById(R.id.nameField);
        description = findViewById(R.id.descriptionField);
        make = findViewById(R.id.makeField);
        model = findViewById(R.id.modelField);
        comment = findViewById(R.id.commentField);
        serial = findViewById(R.id.serialField);
        value = findViewById(R.id.valueField);
        date = findViewById(R.id.dateField);

        // If the item is not null, set the values
        if (item != null) {
            name.setText(item.getName());
            description.setText(item.getDescription());
            make.setText(item.getMake());
            model.setText(item.getModel());
            comment.setText(item.getComment());
            serial.setText(item.getSerialNumber());

            // Limit the number of decimals in the value field to 2
            String valueString = String.format("%.2f", item.getEstimatedValue());
            value.setText(valueString);

            // Set the date as today's current date in 'yyyy-MM-dd' format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            date.setText(sdf.format(item.getDateOfPurchase()));
        }
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

    private void updateItem() {
        item.setItemProperties(
                AddItemActivity.this,
                date.getText().toString(),
                value.getText().toString(),
                name.getText().toString(),
                description.getText().toString(),
                model.getText().toString(),
                make.getText().toString(),
                serial.getText().toString(),
                comment.getText().toString(),
                selectedTags,
                tempUris
        );
    }

        // FIX THIS - DONT NESTTT
        private void addItemToFirestore() {
            // Add the item to Firestore
            firestoreRepository.addItem(item, new FirestoreRepository.OnItemAddedListener() {
                @Override
                public void onItemAdded(String itemId) {
                    Log.d("AddItemActivity", "Item added, ID: " + itemId);
                    // Get the URI passed through the intent from CameraActivity
                    String uriString = getIntent().getStringExtra("uri");

                    if (uriString != null) {
                        Uri uri = Uri.parse(uriString);

                        // Upload the image with the item and the URI
                        firestoreRepository.uploadImage(uri, item, new FirestoreRepository.OnImageUploadedListener() {
                            @Override
                            public void onImageUploaded(String downloadUrl) {
                                Log.d("AddItemActivity", "Image uploaded, download URL: " + downloadUrl);
                                firestoreRepository.updateItem(itemId, item, new FirestoreRepository.OnItemUpdatedListener() {
                                    @Override
                                    public void onItemUpdated() {
                                        Log.d("AddItemActivity", "Item updated, ID: " + item.getId() + ", Name: " + item.getName() + ", Estimated Value: " + item.getEstimatedValue());
                                        // Navigate to MainActivity after the item has been updated
                                        navigateToMainActivity();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("AddItemActivity", "Error updating item", e);
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("AddItemActivity", "Error uploading image", e);
                            }
                        });
                    } else {
                        // If the URI is null, navigate to MainActivity after adding the item
                        navigateToMainActivity();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(AddItemActivity.this, "Error adding item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AddItemActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close the current activity
    }

    /**
     * Updates the tagList with the provided list of tags.
     * <p>
     * This method updates the tagList with the provided list of tags. This method is typically
     * called from the {@link ManageTagsActivity} when the user adds or removes tags.
     *
     * @param newTags A List of Strings representing the updated list of tags.
     */
    public void updateTagList(List<String> newTags) {
        tagList.clear();
        tagList.addAll(newTags);
        // If needed, update the UI or other elements that depend on the tagList
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
     * @throws NullPointerException If the provided {@code Item} object is {@code null}.
     * @see Item
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
        itemMap.put("imageUris", item.getImageUris());
        return itemMap;
    }


}
