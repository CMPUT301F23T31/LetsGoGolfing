package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditItemActivity extends AppCompatActivity {

    private static final String TAG = "EditItemActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_page);

        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        Map<String, Object> item = new HashMap<>();

        // Parse the date string to a Date object and then to a Timestamp
        String dateString = ((EditText) findViewById(R.id.dateField)).getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            if (date != null) {
                item.put("dateOfPurchase", new Timestamp(date));
            } else {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_LONG).show();
                return; // Exit the method if the date format is invalid
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Failed to parse date", Toast.LENGTH_LONG).show();
            return; // Exit the method if there's an error parsing the date
        }

        // Parse the estimated value as a double
        try {
            double estimatedValue = Double.parseDouble(((EditText) findViewById(R.id.valueField)).getText().toString());
            item.put("estimatedValue", estimatedValue);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format for estimated value", Toast.LENGTH_LONG).show();
            return; // Exit the method if the number format is invalid
        }

        // Collect other fields
        item.put("name", ((EditText) findViewById(R.id.nameField)).getText().toString());
        item.put("description", ((EditText) findViewById(R.id.descriptionField)).getText().toString());
        item.put("make", ((EditText) findViewById(R.id.makeField)).getText().toString());
        item.put("model", ((EditText) findViewById(R.id.modelField)).getText().toString());
        item.put("comment", ((EditText) findViewById(R.id.commentField)).getText().toString());

        // Convert the tags string to a List<String>
        String tagsString = ((EditText) findViewById(R.id.tagsField)).getText().toString();
        List<String> tagsList = new ArrayList<>(Arrays.asList(tagsString.split("\\s*,\\s*")));
        item.put("tags", tagsList);

        // Add the item to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(EditItemActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditItemActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
                });
    }

}
