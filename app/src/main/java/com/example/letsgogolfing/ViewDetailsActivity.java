package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;
import static com.example.letsgogolfing.utils.Formatters.decimalFormat;

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

public class ViewDetailsActivity extends AppCompatActivity {

    private List<String> tagList = new ArrayList<>(); // This should be populated from Firestore
    private List<String> selectedTags = new ArrayList<>();
    private Item item;

    private static final String TAG = "ViewDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_details);

        // Retrieve the item from the intent
        Item item = (Item) getIntent().getSerializableExtra("ITEM");

        // Now populate the fields with item details
        ((EditText) findViewById(R.id.nameField)).setText(item.getName());
        ((EditText) findViewById(R.id.descriptionField)).setText(item.getDescription());
        ((EditText) findViewById(R.id.makeField)).setText(item.getMake());
        ((EditText) findViewById(R.id.modelField)).setText(item.getModel());
        ((EditText) findViewById(R.id.serialField)).setText(item.getSerialNumber());
        ((EditText) findViewById(R.id.commentField)).setText(item.getComment());


        String dateString = dateFormat.format(item.getDateOfPurchase());
        ((EditText) findViewById(R.id.dateField)).setText(dateString);

// For the double value, you can use String.format to control the formatting
// For example, "%.2f" will format the double to two decimal places
        String valueString = decimalFormat.format(item.getEstimatedValue());
        ((EditText) findViewById(R.id.valueField)).setText(valueString);

        // list of tags
        List<String> tags = item.getTags();
        //String tagsString = TextUtils.join(", ", tags);



        // If you want the fields to be non-editable, make them TextViews or disable the EditTexts
        ((EditText) findViewById(R.id.nameField)).setEnabled(false);
        ((EditText) findViewById(R.id.descriptionField)).setEnabled(false);
        ((EditText) findViewById(R.id.modelField)).setEnabled(false);
        ((EditText) findViewById(R.id.makeField)).setEnabled(false);
        ((EditText) findViewById(R.id.serialField)).setEnabled(false);
        ((EditText) findViewById(R.id.commentField)).setEnabled(false);
        ((EditText) findViewById(R.id.dateField)).setEnabled(false);
        ((EditText) findViewById(R.id.valueField)).setEnabled(false);

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


        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // takes back to home page main_activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        Button saveButton = findViewById(R.id.saveBtn);
        Button editButton = findViewById(R.id.editInfoBtn);
        editButton.setOnClickListener(v -> {
            // sets all of the fields to enabled
            ((EditText) findViewById(R.id.nameField)).setEnabled(true);
            ((EditText) findViewById(R.id.descriptionField)).setEnabled(true);
            ((EditText) findViewById(R.id.modelField)).setEnabled(true);
            ((EditText) findViewById(R.id.makeField)).setEnabled(true);
            ((EditText) findViewById(R.id.serialField)).setEnabled(true);
            ((EditText) findViewById(R.id.commentField)).setEnabled(true);
            ((EditText) findViewById(R.id.dateField)).setEnabled(true);
            ((EditText) findViewById(R.id.valueField)).setEnabled(true);

            saveButton.setVisibility(v.VISIBLE);

        });

        Button addTagsButton = findViewById(R.id.add_tags_button_view);
        addTagsButton.setOnClickListener(v -> {
            // Only allow tag editing when in edit mode
            if (saveButton.getVisibility() == View.VISIBLE) {
                showTagSelectionDialog();
            } else {
                Toast.makeText(this, "You must be in edit mode to modify tags.", Toast.LENGTH_SHORT).show();
            }
        });

        loadTags();

        saveButton.setOnClickListener(v -> {
            // Extract the updated information from EditText fields
            String updatedName = ((EditText) findViewById(R.id.nameField)).getText().toString();
            String updatedDescription = ((EditText) findViewById(R.id.descriptionField)).getText().toString();
            String updatedMake = ((EditText) findViewById(R.id.makeField)).getText().toString();
            String updatedModel = ((EditText) findViewById(R.id.modelField)).getText().toString();
            String updatedSerialNumber = ((EditText) findViewById(R.id.serialField)).getText().toString();
            String updatedComment = ((EditText) findViewById(R.id.commentField)).getText().toString();
            String updatedDate = ((EditText) findViewById(R.id.dateField)).getText().toString();
            String updatedValueString = ((EditText) findViewById(R.id.valueField)).getText().toString();

            // Convert the date string back to a Date object
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date updatedDateOfPurchase = null;
            try {
                updatedDateOfPurchase = sdf.parse(updatedDate);
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

            // Get the document ID from the item
            String documentId = item.getId(); // Assuming 'item' is an instance variable representing the current item

            // Update Firestore document
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("items").document(documentId)
                    .update(updatedValues)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ViewDetailsActivity.this, "Changes saved", Toast.LENGTH_SHORT).show();
                        // Disable EditTexts and hide the save button again
                        ((EditText) findViewById(R.id.nameField)).setEnabled(false);
                        ((EditText) findViewById(R.id.descriptionField)).setEnabled(false);
                        ((EditText) findViewById(R.id.modelField)).setEnabled(false);
                        ((EditText) findViewById(R.id.makeField)).setEnabled(false);
                        ((EditText) findViewById(R.id.serialField)).setEnabled(false);
                        ((EditText) findViewById(R.id.commentField)).setEnabled(false);
                        ((EditText) findViewById(R.id.dateField)).setEnabled(false);
                        ((EditText) findViewById(R.id.valueField)).setEnabled(false);
                        saveButton.setVisibility(View.INVISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(ViewDetailsActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void displayTags() {
        LinearLayout tagsContainerView = findViewById(R.id.tagsContainerView);
        tagsContainerView.removeAllViews(); // Clear all views/tags before adding new ones

        for (String tag : selectedTags) {
            // ... Existing code to create and add TextViews for each tag ...
        }
    }

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
