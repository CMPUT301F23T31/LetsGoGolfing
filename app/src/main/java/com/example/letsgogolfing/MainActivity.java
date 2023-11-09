package com.example.letsgogolfing;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView itemGrid;
    private ItemAdapter itemAdapter; // You need to create this Adapter class.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemGrid = findViewById(R.id.itemGrid);
        itemAdapter = new ItemAdapter(this, new ArrayList<>());
        itemGrid.setAdapter(itemAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // clear persistence
        db.clearPersistence(); // IMPORTANT, TESTING USE ONLY

        db.collection("items").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(MainActivity.this, "Error loading items", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Item> items = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                items.add(doc.toObject(Item.class)); // Assuming you have an Item class
            }

            itemAdapter.updateItems(items);
        });

        // Inside onCreate after setting the adapter
        itemGrid.setOnItemClickListener((parent, view, position, id) -> {
            Item item = itemAdapter.getItem(position);
            Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
            intent.putExtra("EDIT_ITEM", true); // Flag to indicate editing
            intent.putExtra("ITEM_ID", item.getId()); // Pass the unique Firestore document ID
            // ... pass other item properties
            startActivity(intent);
        });


        ImageView addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
            startActivity(intent);
        });
    }
}
