package com.example.letsgogolfing.controllers;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letsgogolfing.R;
import com.example.letsgogolfing.controllers.dialogs.TagDialogHelper;
import com.example.letsgogolfing.models.FirestoreRepository;
import com.example.letsgogolfing.models.Item;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.Manifest;

/**
 * Activity for adding a new item to the inventory.
 * <p>
 * This activity allows users to input information about a new item, including its name,
 * description, make, model, date of purchase, estimated value, and associated tags. Users can
 * select tags from the available list, and the information is then saved to Firestore.
 */
public class AddItemActivity extends AppCompatActivity {
    private static final String TAG = "EditItemActivity";
    private int uploadCounter = 0;
    private int totalUploadCount = 0;
    private AlertDialog loadingDialog;
    private ArrayList<String> tempUris = new ArrayList<>();
    private FirestoreRepository firestoreRepository;
    private List<String> tagList = new ArrayList<>();
    private List<String> selectedTags = new ArrayList<>();
    private Uri imageUri;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_GALLERY_PERMISSION_CODE = 101;

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Check if the result comes from the camera
                    if (imageUri != null) {
                        // The image is saved at imageUri
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

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.loading_dialog);
        builder.setCancelable(false); // Optional: make the dialog non-cancelable
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

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

        // Retrieve the current username from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentUsername = sharedPref.getString("username", null);

        // Initialize FirestoreRepository with the current username
        firestoreRepository = new FirestoreRepository(currentUsername);

        Item item = (Item) getIntent().getSerializableExtra("item");
        if (item != null) {
            ((EditText) findViewById(R.id.nameField)).setText(item.getName());
            ((EditText) findViewById(R.id.descriptionField)).setText(item.getDescription());
            ((EditText) findViewById(R.id.makeField)).setText(item.getMake());
            ((EditText) findViewById(R.id.modelField)).setText(item.getModel());
            ((EditText) findViewById(R.id.commentField)).setText(item.getComment());
            ((EditText) findViewById(R.id.serialField)).setText(item.getSerialNumber());

            // limit the number of decimals in the value field to 2
            double value = item.getEstimatedValue();
            String valueString = String.format("%.2f", value);


            ((EditText) findViewById(R.id.valueField)).setText(valueString);

            // set the date as todays current date in 'yyyy-mm-dd' format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            ((EditText) findViewById(R.id.dateField)).setText(sdf.format(new Date()));
        }

        // confirm button listener
        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(v -> saveItem());

        // cancel button listener
        Button cancel_button = findViewById(R.id.cancel_button_add_item);
        cancel_button.setOnClickListener(v -> finish());

        // add tags button listener
        Button tagButton = findViewById(R.id.add_tags_button);
        tagButton.setOnClickListener(v -> {
            TagDialogHelper.showTagSelectionDialog(
                    AddItemActivity.this,
                    tagList, // This is your existing list of all tags
                    selectedTags, // This is your existing list of currently selected tags
                    new TagDialogHelper.OnTagsSelectedListener() {
                        @Override
                        public void onTagsSelected(List<String> newSelectedTags) {
                            // Update the UI with the selected tags
                            updateTagsUI(newSelectedTags);
                            // Optionally, store the selected tags as needed
                            selectedTags = newSelectedTags;
                        }

                        @Override
                        public void onNewTagAdded(String newTag) {
                            // Handle the addition of the new tag, e.g., update Firestore and tagList
                            firestoreRepository.addTag(newTag, new FirestoreRepository.OnTagAddedListener() {
                                @Override
                                public void onTagAdded() {
                                    tagList.add(newTag); // Add the new tag to the local list
                                    // Optionally, refresh the tag selection dialog to include the new tag
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
        add_photo_button.setOnClickListener(v -> showImageSourceDialog());
    }

    private void updateTagsUI(List<String> selectedTags) {
        LinearLayout tagsContainer = findViewById(R.id.tagsContainer);

        if (tagsContainer != null) {
            tagsContainer.removeAllViews(); // Clear all views/tags before adding new ones

            for (String tag : selectedTags) {
                TextView tagView = new TextView(this);
                tagView.setText(tag);
                tagView.setBackgroundResource(R.drawable.tag_background); // Ensure this drawable resource exists
                // Set other properties and layout parameters for tagView as required
                tagsContainer.addView(tagView); // Add the TextView to the container
            }
        } else {
            Log.e(TAG, "tagsContainer is null");
        }
    }


    /**
     * Displays a dialog for selecting an image source.
     */
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Photo Source")
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
     * Launches the camera to take a photo.
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
     * Launches the camera to take a photo.
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
     * Creates a file for storing the image.
     *
     * @return The Uri of the image file.
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
     * Checks if the camera permission is granted.
     * <p>
     * This method checks if the camera permission is granted. If it is, the {@link #launchCamera()}
     * method is called. If not, the permission is requested from the user.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MY_GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Gallery permission is required to access photos", Toast.LENGTH_SHORT).show();
            }
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
     * Checks if a given date string is valid.
     * <p>
     * This method checks if a given date string is valid. The date string must be in the format
     * "yyyy-MM-dd", and the date must be a valid date (e.g., 2021-02-29 is not valid).
     * </p>
     *
     * @param dateString The date string to be checked.
     * @return True if the date string is valid, false otherwise.
     */
    public boolean isValidDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false); // This will make sure SimpleDateFormat doesn't adjust dates on its own
        try {
            Date date = sdf.parse(dateString);
            if (date == null) {
                return false;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            if (year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR)) {
                return false; // Year is out of range
            }

            if (month < 1 || month > 12) {
                return false; // Month is out of range
            }

            if (day < 1 || day > getDaysInMonth(month, year)) {
                return false; // Day is out of range
            }

            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Returns the number of days in a given month and year.
     *
     * @param month The month (1-12).
     * @param year The year.
     * @return The number of days in the month.
     */
    private int getDaysInMonth(int month, int year) {
        switch (month) {
            case 2: // February
                if (isLeapYear(year)) {
                    return 29;
                } else {
                    return 28;
                }
            case 4: case 6: case 9: case 11: // April, June, September, November
                return 30;
            default:
                return 31;
        }
    }

    /**
     * Checks if a given year is a leap year.
     *
     * @param year The year.
     * @return True if it's a leap year, false otherwise.
     */
    private boolean isLeapYear(int year) {
        if (year % 4 != 0) {
            return false;
        } else if (year % 100 != 0) {
            return true;
        } else return year % 400 == 0;
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
        newItem.setName(((EditText) findViewById(R.id.nameField)).getText().toString());
        newItem.setDescription(((EditText) findViewById(R.id.descriptionField)).getText().toString());
        newItem.setMake(((EditText) findViewById(R.id.makeField)).getText().toString());
        newItem.setModel(((EditText) findViewById(R.id.modelField)).getText().toString());
        newItem.setComment(((EditText) findViewById(R.id.commentField)).getText().toString());

        String currentUsername = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("username", null);
        newItem.setUsername(currentUsername); // Set the username before saving

        newItem.setImageUris(tempUris);

        // Parse and set the date of purchase
        String dateString = ((EditText) findViewById(R.id.dateField)).getText().toString();

        if (!isValidDate(dateString)) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_LONG).show();
            return; // Exit the method if the date is not valid
        }

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
            newItem.setEstimatedValue(estimatedValue);
        }

        // Parse and set the tags
        newItem.setTags(selectedTags);


        // Use FirestoreRepository to add the new item
        firestoreRepository.addItem(newItem, new FirestoreRepository.OnItemAddedListener() {
            @Override
            public void onItemAdded(String itemId) {
                Toast.makeText(AddItemActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AddItemActivity.this, "Error adding item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            String photoFileName = "photo_" + System.currentTimeMillis() + ".jpg";
            StorageReference imagesRef = storageRef.child("images/" + photoFileName);

            UploadTask uploadTask = imagesRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // ... existing code ...
                imagesRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    tempUris.add(downloadUri.toString());
                    uploadCounter++;
                    if (uploadCounter == totalUploadCount) {
                        hideLoadingDialog(); // Hide the dialog when all images are processed
                    }
                });
            }).addOnFailureListener(exception -> {
                Log.e("Firebase Upload", "Upload failed", exception);
                Toast.makeText(this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();

                uploadCounter++;
                if (uploadCounter == totalUploadCount) {
                    hideLoadingDialog(); // Hide the dialog when all images are processed
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            uploadCounter++;
            if (uploadCounter == totalUploadCount) {
                hideLoadingDialog(); // Hide the dialog when all images are processed
            }
        }
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
