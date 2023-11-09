package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditItemActivity extends AppCompatActivity {

    private static final String TAG = "EditItemActivity"; // logging purposes

    // Field for storing the item ID if we are editing
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_page);

        // Check if we're in edit mode
        itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId != null && !itemId.isEmpty()) {
            loadItemData(itemId);
        }

        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(v -> {
            if (itemId != null && !itemId.isEmpty()) {
                updateItem(itemId); // Update existing item
            } else {
                saveNewItem(); // Save new item
            }
        });
    }

    private void loadItemData(String itemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Item item = documentSnapshot.toObject(Item.class);
                    if (item != null) {
                        ((EditText) findViewById(R.id.nameField)).setText(item.getName());
                        ((EditText) findViewById(R.id.descriptionField)).setText(item.getDescription());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditItemActivity.this, "Error loading item", Toast.LENGTH_SHORT).show());
    }

    private void updateItem(String itemId) {
        Map<String, Object> item = getItemDataFromForm();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").document(itemId).set(item)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Item updated successfully");
                    Toast.makeText(EditItemActivity.this, "Item updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating item", e);
                    Toast.makeText(EditItemActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNewItem() {
        Map<String, Object> item = getItemDataFromForm();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").add(item)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Item added successfully");
                    Toast.makeText(EditItemActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding item", e);
                    Toast.makeText(EditItemActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> getItemDataFromForm() {
        Map<String, Object> item = new HashMap<>();
        item.put("name", ((EditText) findViewById(R.id.nameField)).getText().toString());
        item.put("description", ((EditText) findViewById(R.id.descriptionField)).getText().toString());
        item.put("dateOfPurchase", ((EditText) findViewById(R.id.dateField)).getText().toString()); // Consider converting this to a Date object or Timestamp
        item.put("make", ((EditText) findViewById(R.id.makeField)).getText().toString());
        item.put("model", ((EditText) findViewById(R.id.modelField)).getText().toString());
        item.put("serialNumber", ((EditText) findViewById(R.id.serialField)).getText().toString());
        item.put("comment", ((EditText) findViewById(R.id.commentField)).getText().toString());
        item.put("tags", ((EditText) findViewById(R.id.tagsField)).getText().toString()); // This should be a List<String>, not a single string

        // Parse the estimated value as a double
        try {
            double estimatedValue = Double.parseDouble(((EditText) findViewById(R.id.valueField)).getText().toString());
            item.put("estimatedValue", estimatedValue);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format for estimated value", Toast.LENGTH_LONG).show();
            // You might want to return or handle this case differently
        }

        // Convert the date string to a Date object or Firebase Timestamp before putting it into the map


        // Convert the tags string to a List<String> before putting it into the map

        return item;
    }

}
