package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;
import static com.example.letsgogolfing.utils.Formatters.decimalFormat;

import com.example.letsgogolfing.utils.ImageFragment;
import com.example.letsgogolfing.utils.PhotoStorageManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

// generate javadocs for ViewDetailsActivity
/**
 * Activity for viewing the details of an item.
 */
public class ViewDetailsActivity extends AppCompatActivity {


    private String originalName;
    private String originalDescription;
    private String originalMake;
    private String originalModel;
    private String originalSerial;
    private String originalComment;
    private Date originalDate;

    private double originalValue;
    private String originalTags;
    EditText name;
    EditText description;
    EditText value;
    EditText make;
    EditText model;
    EditText serial;
    EditText comment;
    EditText date;

    Button editButton;
    Button viewPhotoButton;
    Button saveButton;
    Button cancelButton;
    Button addPhotoButton;
    ImageButton backButton;

    private List<String> tagList = new ArrayList<>(); // This should be populated from Firestore
    private List<String> selectedTags = new ArrayList<>();
    private Item item;
    private List<String> originalTagsList = new ArrayList<>();
    private static final String TAG = "ViewDetailsActivity";


    /**
     * onCreate method for the ViewDetailsActivity.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_details);

        // Retrieve the item from the intent
        item = (Item) getIntent().getSerializableExtra("ITEM");


        InitializeEditTextAndButtons(item);

        // MAYBE DON"T NEED
        // list of tags
        List<String> tags = item.getTags();
        //String tagsString = TextUtils.join(", ", tags);


        LinearLayout tagsContainerView = findViewById(R.id.tagsContainerView);
        tagsContainerView.removeAllViews(); // Clear all views/tags before adding new ones

        for (String tag : tags) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setBackgroundResource(R.drawable.tag_background); // Make sure this drawable exists
            // Set other TextView properties like padding, textAppearance, etc.

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0); // Adding some right margin between tags

            tagView.setLayoutParams(params);
            tagsContainerView.addView(tagView); // Add the TextView to your container
        }

        backButton.setOnClickListener(v -> {
            // takes back to home page main_activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> {

            TransitionToEdit(v);
        });

        cancelButton.setOnClickListener(v -> {
            selectedTags = new ArrayList<>(originalTagsList);
            displayTags();
            SetFieldsToOriginalValues(v);
            TransitionToViewItem(v);
        });

        Button addTagsButton = findViewById(R.id.add_tags_button_view);
        addTagsButton.setOnClickListener(v -> {
            showTagSelectionDialog();
        });

        loadTags();

        saveButton.setOnClickListener(v -> {
            // Extract the updated information from EditText fields
            String updatedName = name.getText().toString();
            String updatedDescription = description.getText().toString();
            String updatedMake = make.getText().toString();
            String updatedModel = model.getText().toString();
            String updatedSerialNumber = serial.getText().toString();
            String updatedComment = comment.getText().toString();
            String updatedDate = date.getText().toString();
            String updatedValueString = value.getText().toString();

            Date updatedDateOfPurchase = null;
            try {
                // Convert the date string back to a Date object
                updatedDateOfPurchase = dateFormat.parse(updatedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(ViewDetailsActivity.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert the value string to a double
            double updatedEstimatedValue;
            try {
                updatedEstimatedValue = Double.parseDouble(updatedValueString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(ViewDetailsActivity.this, "No Value Entered. Defaulted to 0.", Toast.LENGTH_SHORT).show();
                updatedEstimatedValue = 0;

                return;
            }

            // Create a Map for the updated values
            Map<String, Object> updatedValues = new HashMap<>();
            updatedValues.put("name", updatedName);
            updatedValues.put("description", updatedDescription);
            updatedValues.put("make", updatedMake);
            updatedValues.put("model", updatedModel);
            updatedValues.put("serialNumber", updatedSerialNumber);
            updatedValues.put("comment", updatedComment);
            updatedValues.put("dateOfPurchase", updatedDateOfPurchase != null ? new Timestamp(updatedDateOfPurchase) : null);
            updatedValues.put("estimatedValue", updatedEstimatedValue);
            updatedValues.put("tags", selectedTags);

            // Get the document ID from the item
            String documentId = item.getId(); // Assuming 'item' is an instance variable representing the current item

            // Update Firestore document
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("items").document(documentId)
                    .update(updatedValues)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ViewDetailsActivity.this, "Changes saved", Toast.LENGTH_SHORT).show();
                        // Update originalTagsList to reflect the newly saved tags
                        originalTagsList = new ArrayList<>(selectedTags);
                        TransitionToViewItem(v);

                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(ViewDetailsActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                    });
        });

        Button viewPhotoButton = findViewById(R.id.viewPhotoBtn);

        // Set an OnClickListener on the button
        viewPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start ViewPhotoActivity
                Intent intent = new Intent(ViewDetailsActivity.this, ViewPhotoActivity.class);
                
                // Pass the item ID to ViewPhotoActivity
                /**change once item id is included in item*/
                intent.putExtra("itemId", 0);
                
                // Start ViewPhotoActivity
                startActivity(intent);
            }
        });
    };


    /**
     * Transition to edit view.
     * @param v The view.
     */
    private void TransitionToEdit(View v) {
        saveButton.setVisibility(v.VISIBLE);
        cancelButton.setVisibility(v.VISIBLE);
        addPhotoButton.setVisibility(v.VISIBLE);
        viewPhotoButton.setVisibility(v.INVISIBLE);
        editButton.setVisibility(v.INVISIBLE);
        name.setEnabled(true);
        description.setEnabled(true);
        model.setEnabled(true);
        make.setEnabled(true);
        serial.setEnabled(true);
        comment.setEnabled(true);
        date.setEnabled(true);
        value.setEnabled(true);
        // make the add/edit tags button visible
        Button addTagsButton = findViewById(R.id.add_tags_button_view);
        addTagsButton.setVisibility(View.VISIBLE);
    }

    /**
     * Transition to view item.
     * @param v The view.
     */
    private void TransitionToViewItem(View v) {
        saveButton.setVisibility(v.INVISIBLE);
        cancelButton.setVisibility(v.INVISIBLE);
        addPhotoButton.setVisibility(v.INVISIBLE);
        viewPhotoButton.setVisibility(v.VISIBLE);
        editButton.setVisibility(v.VISIBLE);
        name.setEnabled(false);
        description.setEnabled(false);
        model.setEnabled(false);
        make.setEnabled(false);
        serial.setEnabled(false);
        comment.setEnabled(false);
        date.setEnabled(false);
        value.setEnabled(false);
        // Hide the add/edit tags button
        Button addTagsButton = findViewById(R.id.add_tags_button_view);
        addTagsButton.setVisibility(View.INVISIBLE);
    }


    /**
     * Set fields to original values.
     * @param v The view.
     */
    private void SetFieldsToOriginalValues(View v) {
        name.setText(originalName);
        description.setText(originalDescription);
        make.setText(originalMake);
        model.setText(originalModel);
        serial.setText(originalSerial);
        comment.setText(originalComment);
        date.setText(dateFormat.format(originalDate));
        value.setText(Double.toString(originalValue));
    }


    /**
     * Initialize edit text and buttons.
     * @param item The item.
     */
    private void InitializeEditTextAndButtons(Item item) {

        // Initialize EditTexts
        name = findViewById(R.id.nameField);
        description = findViewById(R.id.descriptionField);
        make = findViewById(R.id.makeField);
        model = findViewById(R.id.modelField);
        serial = findViewById(R.id.serialField);
        comment = findViewById(R.id.commentField);
        date = findViewById(R.id.dateField);
        value = findViewById(R.id.valueField);
        // Initialize Buttons
        saveButton = findViewById(R.id.saveBtn);
        editButton = findViewById(R.id.editInfoBtn);
        cancelButton = findViewById(R.id.cancel_edit_button);
        addPhotoButton = findViewById(R.id.add_photo_button);
        backButton = findViewById(R.id.backButton);
        viewPhotoButton = findViewById(R.id.viewPhotoBtn);

        // Set original values for when cancel is pressed
        originalName = item.getName();
        originalDescription = item.getDescription();
        originalMake = item.getMake();
        originalModel = item.getModel();
        originalSerial = item.getSerialNumber();
        originalComment = item.getComment();
        originalDate = item.getDateOfPurchase();
        originalValue = item.getEstimatedValue();
        originalTagsList = new ArrayList<>(item.getTags());


        // Set the EditTexts with the original values
        name.setText(originalName);
        description.setText(originalDescription);
        make.setText(originalMake);
        model.setText(originalModel);
        serial.setText(originalSerial);
        comment.setText(originalComment);
        date.setText(dateFormat.format(originalDate));
        value.setText(Double.toString(originalValue));

        // Set fields to not be editable at first
        name.setEnabled(false);
        description.setEnabled(false);
        model.setEnabled(false);
        make.setEnabled(false);
        serial.setEnabled(false);
        comment.setEnabled(false);
        date.setEnabled(false);
        value.setEnabled(false);
    }

    /**
     * Display tags.
     */
    private void displayTags() {
        LinearLayout tagsContainerView = findViewById(R.id.tagsContainerView);
        tagsContainerView.removeAllViews(); // Clear all views/tags before adding new ones

        for (String tag : selectedTags) {

            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setBackgroundResource(R.drawable.tag_background); // Make sure this drawable exists
            // Add LayoutParams, margins, etc., here
            tagsContainerView.addView(tagView); // Add the TextView to your container

        }
    }


    /**
     * Load tags.
     */
    private void loadTags() {
        // Assuming you have a method to fetch tags from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tags").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tagList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    tagList.add(document.getString("name"));
                }
                selectedTags = new ArrayList<>(item.getTags()); // Use the tags from the item
                displayTags();
            } else {
                Log.w(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    /**
     * Show tag selection dialog.
     */
    private void showTagSelectionDialog() {
        // Convert List to array for AlertDialog
        String[] tagsArray = tagList.toArray(new String[0]);
        boolean[] checkedTags = new boolean[tagList.size()];
        for (int i = 0; i < tagList.size(); i++) {
            checkedTags[i] = selectedTags.contains(tagList.get(i));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMultiChoiceItems(tagsArray, checkedTags, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // Add or remove the tag from the selected tags list based on whether the checkbox is checked
                String selectedTag = tagList.get(which);
                if (isChecked) {
                    selectedTags.add(selectedTag);
                } else {
                    selectedTags.remove(selectedTag);
                }
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            displayTags(); // Update the display with the selected tags
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
