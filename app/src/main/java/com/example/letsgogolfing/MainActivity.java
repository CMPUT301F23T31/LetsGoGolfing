package com.example.letsgogolfing;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GridView itemGrid;
    private ItemAdapter itemAdapter; // You need to create this Adapter class.

    ActivityResultLauncher<Intent> editItemActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // The item was added or updated, so refresh your list
                    fetchItemsAndRefreshAdapter();
                }
            });

    // Inside MainActivity
    private void fetchItemsAndRefreshAdapter() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Item> newItems = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Item item = document.toObject(Item.class);
                    item.setId(document.getId()); // Make sure to set the document ID
                    newItems.add(item);
                }
                itemAdapter.updateItems(newItems); // Assuming your adapter has this method
            } else {
                Log.w(TAG, "Error getting documents: ", task.getException());
            }
        });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemGrid = findViewById(R.id.itemGrid);
        itemAdapter = new ItemAdapter(this, new ArrayList<>());
        itemGrid.setAdapter(itemAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Item> items = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Item item = document.toObject(Item.class);
                    item.setId(document.getId()); // Make sure to set the document ID
                    items.add(item);
                }
                itemAdapter.updateItems(items); // Update your adapter with this list
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });

        itemGrid.setOnItemLongClickListener((parent, view, position, id) -> {
            Item item = itemAdapter.getItem(position);
            if (item != null && item.getId() != null) {
                // Proceed with deletion
                db.collection("items").document(item.getId()).delete()
                        .addOnSuccessListener(aVoid -> {
                            // Deletion successful, update UI
                            itemAdapter.removeItem(position); // You need to implement this method in your adapter
                            itemAdapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Handle error
                            Toast.makeText(MainActivity.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Document ID is null, handle this case
                Toast.makeText(MainActivity.this, "Cannot delete item without an ID", Toast.LENGTH_SHORT).show();
            }
            return true; // True to indicate the long click was consumed
        });

        itemGrid.setOnItemClickListener((parent, view, position, id) -> {
            Item item = itemAdapter.getItem(position);
            Intent intent = new Intent(MainActivity.this, ViewDetailsActivity.class);
            intent.putExtra("ITEM", item); // Make sure your Item class implements Serializable or Parcelable
            startActivity(intent);
        });


        ImageView addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
            editItemActivityLauncher.launch(intent); // Use the launcher to start for result
        });
    }
}
