package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
    private final List<String> tagList = new ArrayList<>(); // This should be populated from Firestore
    private List<String> selectedTags = new ArrayList<>();
    private Item item;
    private FirestoreRepository db;
    private String username;
    private Uri imageUri;

    private List<String> tempUris = new ArrayList<>();

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private static final String TAG = "EditItemActivity";
    private static final int MY_CAMERA_PERMISSION_CODE = 100;



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

        // Set Layout
        setContentView(R.layout.edit_item);

        // Retrieve the item from the intent
        username = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("username", null);

        // Get repository for user
        db = new FirestoreRepository(username);

        InitializeUI();

        cancelButton.setOnClickListener(v -> {
            finish();
        });

        addTagsButton.setOnClickListener(v -> showTagSelectionDialog());

        addPhotoButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            } else {
                launchCamera();
            }
        });

        saveButton.setOnClickListener(v -> {
            updateItem();
            db.updateItem(item.getId(), item, new FirestoreRepository.OnItemUpdatedListener() {
                @Override
                public void onItemUpdated() {
                    Toast.makeText(EditItemActivity.this, "Successfully updated item", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(EditItemActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                }
            });
        });

        cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK && imageUri != null) {
                                uploadImage(imageUri); // This will start the upload and then update the item
                            }
                        });
    }


    /**
     * Initializes EditText fields and buttons with item data.
     * Retrieves the item details passed via Intent and sets up the user interface components.
     *
     */
    private void InitializeUI() {
        item = (Item) getIntent().getSerializableExtra("ITEM");
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

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = createImageFile();
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(cameraIntent);
    }

    // Implement the createImageFile method
    private Uri createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "Cliche" + timeStamp;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return imageUri;
    }

    // Implement the uploadImage method
    // After capturing the image, upload it
    private void uploadImage(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String photoFileName = "photo_" + System.currentTimeMillis() + ".jpg";
        StorageReference imagesRef = storageRef.child("images/" + photoFileName);

        UploadTask uploadTask = imagesRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imagesRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                // Update the item's image URI list
                ArrayList<String> imageUris = item.getImageUris();
                if (imageUris == null) {
                    imageUris = new ArrayList<>();
                }
                imageUris.add(downloadUri.toString());
                item.setImageUris(imageUris);

                // Update the item in Firestore
                updateItemInFirestore();
            });
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.e("Firebase Upload", "Upload failed", exception);
            Toast.makeText(this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
