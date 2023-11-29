package com.example.letsgogolfing;

import static com.example.letsgogolfing.CameraActivity.MODE_PHOTO_CAMERA;
import static com.example.letsgogolfing.utils.Formatters.dateFormat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * Activity for editing the details of an item in an Android application.
 * This activity allows users to edit item details, add photos, and manage tags associated with the item.
 * It interacts with Firestore to update item details and fetch tags.
 */
public class EditItemActivity extends AppCompatActivity {
    private EditText name, description, value, make, model, serial, comment, date;
    private LinearLayout tagsContainerView;
    private Button saveButton, cancelButton, addPhotoButton, addTagsButton;
    private ImageButton backButton;
    private final List<String> tagList = new ArrayList<>(); // This should be populated from Firestore
    private List<String> selectedTags = new ArrayList<>();
    private Item item;
    private FirestoreRepository db;
    private String username;
    private List<String> tempUris = new ArrayList<>();
    private static final String TAG = "EditItemActivity";



    /**
     * Initializes the activity. This method sets up the user interface and initializes
     * the listeners for various UI components.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);

        SharedPreferences sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", null);
        db = new FirestoreRepository(username);


        item = (Item) getIntent().getSerializableExtra("item");

        // Retrieve the item from the intent
        if (item == null) {
            // If the Item object is null, log an error and finish the activity
            Log.e("EditItemActivity", "Item is null");
            finish();
            return;
        }
    
        InitializeUI(item);

        backButton.setOnClickListener(v -> {
            // takes back to home page main_activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewDetailsActivity.class);
            intent.putExtra("item", item);
            startActivity(intent);
        });

        addTagsButton.setOnClickListener(v -> showTagSelectionDialog());

        addPhotoButton.setOnClickListener(v -> {
            updateItem();
            Intent photoIntent = new Intent(this, CameraActivity.class);
            photoIntent.putExtra("mode", MODE_PHOTO_CAMERA);
            photoIntent.putExtra("BarcodeInfo", false);
            photoIntent.putExtra("item", item);
            startActivity(photoIntent);
        });

        saveButton.setOnClickListener(v -> {
            if (item != null && item.getId() != null) {
                updateItem();
                Log.d("EditItemActivity", "Item ID before update: " + item.getId());
                db.updateItem(item.getId(), item, new FirestoreRepository.OnItemUpdatedListener() {
                    @Override
                    public void onItemUpdated() {
                        Log.d("EditItemActivity", "Item ID after update: " + item.getId());
                        // Get the URI passed through the intent from CameraActivity
                        String uriString = getIntent().getStringExtra("uri");
                        Uri uri = Uri.parse(uriString);
            
                        // Upload the image with the updated item and the URI
                        db.uploadImage(uri, item, new FirestoreRepository.OnImageUploadedListener() {
                            @Override
                            public void onImageUploaded(String downloadUrl) {
                                Log.d("EditItemActivity", "Item ID after image upload: " + item.getId());
                                Log.d("EditItemActivity", "Image uploaded, download URL: " + downloadUrl);
            
                                // Move to ViewDetailsActivity
                                Intent intent = new Intent(EditItemActivity.this, ViewDetailsActivity.class);
                                intent.putExtra("item", item);
                                startActivity(intent);
                            }
            
                            @Override
                            public void onError(Exception e) {
                                Log.e("EditItemActivity", "Error uploading image", e);
                            }
                        });
                    }
            
                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(EditItemActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("EditItemActivity", "Item or item ID is null");
            }
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
        name = findViewById(R.id.name_edit_text);
        description = findViewById(R.id.description_edit_text);
        make = findViewById(R.id.make_edit_text);
        model = findViewById(R.id.model_edit_text);
        serial = findViewById(R.id.serial_edit_text);
        comment = findViewById(R.id.comment_edit_text);
        date = findViewById(R.id.date_edit_text);
        value = findViewById(R.id.value_edit_text);
        tagsContainerView = findViewById(R.id.tags_linear_layout);
        // Initialize Buttons
        addTagsButton = findViewById(R.id.add_tags_button_view);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_edit_button);
        addPhotoButton = findViewById(R.id.add_photo_button);
        backButton = findViewById(R.id.back_button);
        // Set the EditTexts with the original values
        name.setText(item.getName());
        description.setText(item.getDescription());
        make.setText(item.getMake());
        model.setText(item.getModel());
        serial.setText(item.getSerialNumber());
        comment.setText(item.getComment());
        date.setText(dateFormat.format(item.getDateOfPurchase()));
        value.setText(Double.toString(item.getEstimatedValue()));
        tempUris = item.getImageUris();
        loadTags();
    }

    /**
     * This is a new thing i added to handle updating items with new images
     */
// Call this method after the image URI list is updated
    private void updateItemInFirestore() {
        db.updateItem(item.getId(), item, new FirestoreRepository.OnItemUpdatedListener() {
            @Override
            public void onItemUpdated() {
                // Notify user of success
                Toast.makeText(EditItemActivity.this, "Item updated with new image", Toast.LENGTH_SHORT).show();
                // Refresh the UI here if necessary
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditItemActivity.this, "Error updating item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Displays the tags associated with the item in the user interface.
     * Dynamically creates TextViews for each tag and adds them to the tags container layout.
     */
    private void displayTags() {
        tagsContainerView.removeAllViews(); // Clear all views/tags before adding new ones

        for (String tag : selectedTags) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setBackgroundResource(R.drawable.tag_background); // Make sure this drawable exists

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, 0, 16, 0); // Adding some right margin between tags

            tagView.setLayoutParams(params);
            // Add the TextView to your container
            tagsContainerView.addView(tagView);

        }
    }


    /**
     * Loads tags from Firestore and updates the UI accordingly.
     * Fetches a list of tags from Firestore, processes the response, and calls displayTags() to update the user interface.
     */
    private void loadTags() {
        db.fetchTags(new FirestoreRepository.OnTagsFetchedListener() {
            @Override
            public void onTagsFetched(List<String> tags) {
                tagList.clear();
                tagList.addAll(tags);
                selectedTags = new ArrayList<>(item.getTags()); // Use the tags from the item
                displayTags();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditItemActivity.this, "Failed to fetch tags", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error getting documents: ", e);
            }
        });
    }

    /**
     * Updates the item object with the new values from the EditText fields.
     * Converts and validates user input before updating the item object.
     */
    private void showTagSelectionDialog() {
        // Convert List to array for AlertDialog
        String[] tagsArray = tagList.toArray(new String[0]);
        boolean[] checkedTags = new boolean[tagList.size()];
        for (int i = 0; i < tagList.size(); i++) {
            checkedTags[i] = selectedTags.contains(tagList.get(i));
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
            displayTags(); // Update the display with the selected tags
            item.setTags(new ArrayList<>(selectedTags));
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateItem() {
        try {
            // Convert the date string back to a Date object
            item.setDateOfPurchase(dateFormat.parse(date.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(EditItemActivity.this, "Invalid date format", Toast.LENGTH_SHORT).show();
        }

        // Convert the value string to a double
        try {
            item.setEstimatedValue(Double.parseDouble(value.getText().toString()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(EditItemActivity.this, "No Value Entered. Defaulted to 0.", Toast.LENGTH_SHORT).show();
            item.setEstimatedValue(0);
        }
        item.setName(name.getText().toString());
        item.setDescription(description.getText().toString());
        item.setModel(model.getText().toString());
        item.setMake(make.getText().toString());
        item.setSerialNumber(serial.getText().toString());
        item.setComment(comment.getText().toString());
        item.setTags(selectedTags);
    }
}
