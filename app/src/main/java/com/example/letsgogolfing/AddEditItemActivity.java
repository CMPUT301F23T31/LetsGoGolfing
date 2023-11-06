package com.example.letsgogolfing;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.example.letsgogolfing.model.Item;

public class AddEditItemActivity extends AppCompatActivity {

    private EditText nameField, descriptionField, dateField, modelField, makeField, valueField, commentField, serialField, tagsField;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_page);

        // Initialize EditTexts
        nameField = findViewById(R.id.nameField);
        descriptionField = findViewById(R.id.descriptionField);
        dateField = findViewById(R.id.dateField);
        modelField = findViewById(R.id.modelField);
        makeField = findViewById(R.id.makeField);
        valueField = findViewById(R.id.valueField);
        commentField = findViewById(R.id.commentField);
        serialField = findViewById(R.id.serialField);
        tagsField = findViewById(R.id.tagsField);

        db = FirebaseFirestore.getInstance();

        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();

                // go back to mainpage
                finish();
            }
        });
    }

    private void addItem() {
        String name = nameField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        String dateString = dateField.getText().toString().trim();
        String model = modelField.getText().toString().trim();
        String make = makeField.getText().toString().trim();
        String value = valueField.getText().toString().trim();
        String comment = commentField.getText().toString().trim();
        String serial = serialField.getText().toString().trim();
        String tags = tagsField.getText().toString().trim();

        Date date;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            dateField.setError("Invalid date");
            dateField.requestFocus();
            return;
        }

        // Parse and validate other fields
        // For example, checking if the value field is a valid number
        double valueDouble;
        try {
            valueDouble = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            valueField.setError("Value must be a number");
            valueField.requestFocus();
            return;
        }


        String[] tagsArray = tagsField.getText().toString().trim().split(",");
        List<String> tagsList = new ArrayList<>(Arrays.asList(tagsArray));
        // Create an Item object
        // Assuming the Item class constructor matches the order of these parameters
        Item item = new Item(name, description, date, make, model, serial, valueDouble, comment, tagsList);

        // Add item to Firestore
        db.collection("items").add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddEditItemActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> Toast.makeText(AddEditItemActivity.this, "Error adding item", Toast.LENGTH_SHORT).show());
    }


}