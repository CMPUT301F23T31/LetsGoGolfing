package com.example.letsgogolfing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;


import androidx.appcompat.app.AppCompatActivity;


import com.example.letsgogolfing.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private GridView gridView;
    private ItemAdapter itemAdapter;
    private List<Item> itemsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.main_page);

        db = FirebaseFirestore.getInstance();
        gridView = findViewById(R.id.itemGrid);
        itemAdapter = new ItemAdapter(this, itemsList);
        gridView.setAdapter(itemAdapter);

        // click listener for add item
        Button addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditItemActivity.class);
                startActivity(intent);
            }
        });

        // Fetch items from Firebase Firestore
        db.collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Item item = document.toObject(Item.class);
                    itemsList.add(item);
                }
                itemAdapter.notifyDataSetChanged(); // Notify the adapter about the data change
            } else {
                // Handle the error
            }
        });


    }

}   // click handler for the "Add Item" button