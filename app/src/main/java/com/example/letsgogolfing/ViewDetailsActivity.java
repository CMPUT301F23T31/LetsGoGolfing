package com.example.letsgogolfing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

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

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(item.getDateOfPurchase());
        ((EditText) findViewById(R.id.dateField)).setText(dateString);

// For the double value, you can use String.format to control the formatting
// For example, "%.2f" will format the double to two decimal places
        String valueString = String.format(Locale.getDefault(), "%.2f", item.getEstimatedValue());
        ((EditText) findViewById(R.id.valueField)).setText(valueString);


        // If you want the fields to be non-editable, make them TextViews or disable the EditTexts
        ((EditText) findViewById(R.id.nameField)).setEnabled(false);
        ((EditText) findViewById(R.id.descriptionField)).setEnabled(false);
        ((EditText) findViewById(R.id.modelField)).setEnabled(false);
        ((EditText) findViewById(R.id.makeField)).setEnabled(false);
        ((EditText) findViewById(R.id.serialField)).setEnabled(false);
        ((EditText) findViewById(R.id.commentField)).setEnabled(false);
        ((EditText) findViewById(R.id.dateField)).setEnabled(false);
        ((EditText) findViewById(R.id.valueField)).setEnabled(false);


        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // takes back to home page main_activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });




    }
}
