package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Timestamp;

import static com.example.letsgogolfing.utils.DataRepository.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    private Item item;
    private static final String TAG = "EditItemActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_page);

        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(v -> saveItem());

        Button cancel_button = findViewById(R.id.cancel_button_add_item);
        cancel_button.setOnClickListener(v -> finish());
    }

    private void saveItem() {
        // Create a new Item object
        Item newItem = new Item();

        // Set name, description, make, model, and comment directly on the Item object
        newItem.setName(((EditText) findViewById(R.id.nameField)).getText().toString());
        newItem.setDescription(((EditText) findViewById(R.id.descriptionField)).getText().toString());
        newItem.setMake(((EditText) findViewById(R.id.makeField)).getText().toString());
        newItem.setModel(((EditText) findViewById(R.id.modelField)).getText().toString());
        newItem.setComment(((EditText) findViewById(R.id.commentField)).getText().toString());

        // Parse and set the date of purchase
        String dateString = ((EditText) findViewById(R.id.dateField)).getText().toString();
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
        String tagsString = ((EditText) findViewById(R.id.tagsField)).getText().toString();
        List<String> tagsList = new ArrayList<>(Arrays.asList(tagsString.split("\\s*,\\s*")));
        newItem.setTags(tagsList);

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
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddItemActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
                });
    }

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
