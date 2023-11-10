package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;
import static com.example.letsgogolfing.utils.Formatters.decimalFormat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import static com.example.letsgogolfing.utils.DataRepository.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewDetailsActivity extends AppCompatActivity {


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
        String tagsString = TextUtils.join(", ", tags);
        ((TextView) findViewById(R.id.tagsField)).setText(tagsString);



        // If you want the fields to be non-editable, make them TextViews or disable the EditTexts
        ((EditText) findViewById(R.id.nameField)).setEnabled(false);
        ((EditText) findViewById(R.id.descriptionField)).setEnabled(false);
        ((EditText) findViewById(R.id.modelField)).setEnabled(false);
        ((EditText) findViewById(R.id.makeField)).setEnabled(false);
        ((EditText) findViewById(R.id.serialField)).setEnabled(false);
        ((EditText) findViewById(R.id.commentField)).setEnabled(false);
        ((EditText) findViewById(R.id.dateField)).setEnabled(false);
        ((EditText) findViewById(R.id.valueField)).setEnabled(false);
        ((EditText) findViewById(R.id.tagsField)).setEnabled(false);


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
            ((EditText) findViewById(R.id.tagsField)).setEnabled(true);

            saveButton.setVisibility(v.VISIBLE);

        });

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
                        ((EditText) findViewById(R.id.tagsField)).setEnabled(false);
                        saveButton.setVisibility(View.INVISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(ViewDetailsActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
