package com.example.letsgogolfing;

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
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
import static com.example.letsgogolfing.FirestoreRepository.convertItemToMap;


/**
 * Activity for adding a new item to the inventory.
 * <p>
 * This activity allows users to input information about a new item, including its name,
 * description, make, model, date of purchase, estimated value, and associated tags. Users can
 * select tags from the available list, and the information is then saved to Firestore.
 */
public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "EditItemActivity";

    private List<String> tempUris = new ArrayList<>();


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

    private Uri imageUri;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && imageUri != null) {
                    // Handle the taken photo
                    uploadImage(imageUri);
                }
            });


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
        tagButton.setOnClickListener(v -> showTagSelectionDialog());

        Button addPhotoBtn = findViewById(R.id.addPhotoBtn);
        addPhotoBtn.setOnClickListener(v -> dispatchTakePictureIntent());

        // Fetch the tags from Firestore
        fetchTagsFromFirestore();

        // add photo button listener

        ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getStringExtra("imageUri") != null) {
                            String imageUriString = data.getStringExtra("imageUri");
                            Uri imageUri = Uri.parse(imageUriString);
                            uploadImage(imageUri); // Call the upload method here
                        }
                    }
                }
        );

        Button add_photo_button = findViewById(R.id.addPhotoBtn);
        add_photo_button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            } else {
                launchCamera();
            }
        });
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = createImageFile();
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(cameraIntent);
    }

    private Uri createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "Cliche" + timeStamp;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return imageUri;
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
     * Note: This method relies on the 'convertItemToMap' method to convert the
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

        String currentUsername = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("username", null);
        newItem.setUsername(currentUsername); // Set the username before saving

        newItem.setImageUris(tempUris);

        // Parse and set the date of purchase
        String dateString = ((EditText) findViewById(R.id.dateField)).getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            if (date != null) {
                newItem.setDateOfPurchase(date);
            } catch (ParseException e) {
                // This should not happen as you have already checked the date
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_LONG).show();
            return;
        }

        // Parse and set the estimated value
        try {
            double estimatedValue = Double.parseDouble(valueField.getText().toString());
            newItem.setEstimatedValue(estimatedValue);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format for value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse and set the tags
        newItem.setTags(selectedTags);

        // Add the item to Firestore
        firestoreRepository.addItem(newItem, new FirestoreRepository.OnItemAddedListener() {
            @Override
            public void onItemAdded(String itemId) {
                Toast.makeText(AddItemActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                // Handle success
                finishWithResult(true);
            }
        });     
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
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddItemActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
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
            uploadTask.addOnFailureListener(exception -> {
                Log.e("Firebase Upload", "Upload failed", exception);
                Toast.makeText(this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                // Get the download URL
                imagesRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    tempUris.add(downloadUri.toString());
                    Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
                });
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        itemMap.put("username", item.getUsername());

        return itemMap;
    }


    public boolean isValidDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false); // This will make sure SimpleDateFormat doesn't adjust dates on its own

        try {
            Date date = sdf.parse(dateString);

            // Extract the year, month, and day to perform additional checks
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // Month is 0-based in Calendar
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Check if month and day are in valid ranges
            if (month > 12 || day > 31) {
                return false;
            }

            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
