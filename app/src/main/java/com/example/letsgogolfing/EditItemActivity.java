package com.example.letsgogolfing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_page);

        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        String name = ((EditText) findViewById(R.id.nameField)).getText().toString();
        String description = ((EditText) findViewById(R.id.descriptionField)).getText().toString();
        // Continue for all fields...

        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("description", description);
        // Continue putting other fields...

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(EditItemActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity, returning to MainActivity
                })
                .addOnFailureListener(e -> Toast.makeText(EditItemActivity.this, "Error adding item", Toast.LENGTH_SHORT).show());
    }
}
