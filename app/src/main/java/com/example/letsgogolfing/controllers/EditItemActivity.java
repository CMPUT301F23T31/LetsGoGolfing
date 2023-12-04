package com.example.letsgogolfing.controllers;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.letsgogolfing.R;
import com.example.letsgogolfing.controllers.dialogs.TagDialogHelper;
import com.example.letsgogolfing.models.FirestoreRepository;
import com.example.letsgogolfing.models.Item;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private static final int MY_GALLERY_PERMISSION_CODE = 101;

    private int uploadCounter = 0;
    private int totalUploadCount = 0;
    private AlertDialog loadingDialog;



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


        initializeActivityResultLauncher();

        // Set Layout
        setContentView(R.layout.activity_edit_item);

        // Retrieve the item from the intent
        username = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("username", null);

        // Get repository for user
        db = new FirestoreRepository(username);

        InitializeUI();

        cancelButton.setOnClickListener(v -> {
            finish();
        });

        addTagsButton.setOnClickListener(v -> {
            TagDialogHelper.showTagSelectionDialog(
                EditItemActivity.this, tagList, selectedTags, new TagDialogHelper.OnTagsSelectedListener() {
                @Override
                public void onTagsSelected(List<String> newSelectedTags) {
                    // Update the UI with the selected tags
                    displayTags(newSelectedTags);
                    // Update selectedTags list
                    selectedTags = newSelectedTags;
                }
                @Override
                public void onNewTagAdded(String newTag) {
                    // Handle the addition of the new tag
                    // Update Firestore and tagList
                    db.addTag(newTag, new FirestoreRepository.OnTagAddedListener() {
                        @Override
                        public void onTagAdded() {
                            tagList.add(newTag);
                            // Optionally, refresh the dialog or handle UI updates
                        }
                        @Override
                        public void onError(Exception e) {
                            // Handle error in adding tag
                        }
                    });
                }
            }
        );
        });

        addPhotoButton.setOnClickListener(v -> showImageSourceDialog());

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

    }

    /**
     * Initializes the ActivityResultLauncher for the camera and gallery intents.
     * This method is called in onCreate().
     */
    private void initializeActivityResultLauncher() {
        cameraActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Check if the result comes from the camera
                        if (imageUri != null) {
                            // The image is saved at imageUri
                            totalUploadCount = 1;
                            uploadCounter = 0; // Reset counter
                            showLoadingDialog();
                            uploadImage(imageUri);
                        } else if (result.getData() != null && result.getData().getClipData() != null) {
                            // Multiple images selected from the gallery
                            int count = result.getData().getClipData().getItemCount();
                            totalUploadCount = count;
                            uploadCounter = 0; // Reset counter
                            showLoadingDialog();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                uploadImage(imageUri);
                            }
                        }
                    }
                });
    }


    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.loading_dialog);
        builder.setCancelable(false); // Make dialog non-cancelable if desired
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }


    /**
     * Displays a dialog for selecting the image source.
     * This method is called when the user clicks the add photo button.
     */
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else {
                        launchGallery();
                    }
                })
                .show();
    }

    /**
     * Launches the gallery to select images.
     * This method is called when the user selects the "Choose from Gallery" option in the image source dialog.
     */
    private void launchGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, MY_GALLERY_PERMISSION_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        cameraActivityResultLauncher.launch(galleryIntent);
    }


    /**
     * Initializes EditText fields and buttons with item data.
     * Retrieves the item details passed via Intent and sets up the user interface components.
     *
     */
    private void InitializeUI() {
        item = (Item) getIntent().getSerializableExtra("item");
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

    /**
     * Launches the camera to take a photo.
     * This method is called when the user selects the "Take Photo" option in the image source dialog.
     */
    private void launchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = createImageFile();
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(cameraIntent);
    }


    /**
     * Creates an image file in the external storage.
     * This method is called when the user selects the "Take Photo" option in the image source dialog.
     *
     * @return The URI of the image file.
     */
    private Uri createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "Cliche" + timeStamp;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return imageUri;
    }

    /**
     * Uploads an image to Firebase Storage.
     * This method is called when the user selects an image from the gallery or takes a photo with the camera.
     *
     * @param imageUri The URI of the image to upload.
     */
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

                // Increment the upload counter and hide the loading dialog if all uploads are done
                uploadCounter++;
                if (uploadCounter == totalUploadCount) {
                    hideLoadingDialog();
                }
            });
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.e("Firebase Upload", "Upload failed", exception);
            Toast.makeText(this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();

            // Increment the upload counter and hide the loading dialog if all uploads are done
            uploadCounter++;
            if (uploadCounter == totalUploadCount) {
                hideLoadingDialog();
            }
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
    private void displayTags(List<String> tags) {
        tagsContainerView.removeAllViews(); // Clear all views/tags before adding new ones

        for (String tag : tags) {
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
                displayTags(selectedTags);
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

    /**
     * Displays a dialog for selecting the image source.
     * This method is called when the user clicks the add photo button.
     */
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
