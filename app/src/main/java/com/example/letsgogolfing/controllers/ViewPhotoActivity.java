package com.example.letsgogolfing.controllers;

import static com.google.android.gms.vision.L.TAG;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letsgogolfing.R;
import com.example.letsgogolfing.models.FirestoreRepository;
import com.example.letsgogolfing.models.Item;
import com.example.letsgogolfing.views.ImageAdapter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing photos associated with an item.
 * This activity allows users to view photos associated with an item and delete photos from the item.
 * It interacts with Firebase Storage to delete photos.
 */
public class ViewPhotoActivity extends AppCompatActivity {
    private ImageAdapter imageAdapter;

    private List<String> imageUris; // Field to store image URI strings

    private FirestoreRepository firestoreRepository;

    private String itemId;

    private String username;

    /**
     * Initializes the activity. This method sets up the user interface and initializes
     * the listeners for various UI components.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        // Initialize the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(imageAdapter);

        username = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("username", null);

        // Get current user ID
        firestoreRepository = new FirestoreRepository(username);

        // Retrieve item ID and image URIs from the intent
        itemId = getIntent().getStringExtra("itemId");
        List<String> stringUris = getIntent().getStringArrayListExtra("imageUris");

        // log the photo uri's
        Log.d("ViewPhotoActivity", "Photo URI's: " + stringUris);
        Log.d("ViewPhotoActivity", "Item ID: " + itemId);

        if (itemId == null || stringUris == null) {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Convert String URIs to Uri objects and set them to the adapter
        List<Uri> uriList = new ArrayList<>();
        for (String uriStr : stringUris) {
            uriList.add(Uri.parse(uriStr));
        }
        imageAdapter.setImageUris(uriList);

        // Set delete listener for the adapter
        imageAdapter.setOnImageDeleteListener(imageUri -> {
            deleteImageFromFirebase(imageUri.toString());
        });

        Button back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> finish());
    }


    /**
     * Deletes an image from Firebase Storage.
     * @param imageUriString The URI of the image to delete.
     */
    private void deleteImageFromFirebase(String imageUriString) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUriString);
        photoRef.delete().addOnSuccessListener(aVoid -> {
            Log.d("ViewPhotoActivity", "Image deleted successfully");
            removeImageUriFromItem(imageUriString);
        }).addOnFailureListener(exception -> {
            Log.e("ViewPhotoActivity", "Error deleting image", exception);
        });
    }


    /**
     * Removes an image URI from an item in Firestore.
     * @param imageUriString The URI of the image to remove.
     */
    private void removeImageUriFromItem(String imageUriString) {
        // Assuming you have the item ID

        if (itemId == null) {
            Log.e("ViewPhotoActivity", "Item ID is null");
            // Handle the case appropriately
            return;
        }



        Log.d("ViewPhotoActivity", "Removing image URI from item " + itemId);
        firestoreRepository.deleteImageUriFromItem(itemId, imageUriString, new FirestoreRepository.OnImageUriDeletedListener() {
            @Override
            public void onImageUriDeleted() {
                // Image URI removed from Firestore, now update UI
                Log.d("ViewPhotoActivity", imageUriString + " removed from item " + itemId);
                refreshData();
            }
            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    /**
     * Refreshes the data in the adapter.
     */
    private void refreshData() {
        // Get the updated item from the database
        firestoreRepository.fetchItemById(itemId, new FirestoreRepository.OnItemFetchedListener() {
            @Override
            public void onItemFetched(Item item) {
                // Convert the imageUris list to actual Uris
                List<Uri> imageUris = new ArrayList<>();
                for (String uriString : item.getImageUris()) {
                    imageUris.add(Uri.parse(uriString));
                }
                // Update the list in the ImageAdapter
                imageAdapter.setImageUris(imageUris);
                // Notify the adapter
                imageAdapter.notifyDataSetChanged();
            }

            /**
             * Called when an error occurs while fetching the item.
             *
             * @param e The exception that occurred.
             */
            @Override
            public void onError(Exception e) {
                // Handle any errors
                Log.e(TAG, "onError: failed to load item " + itemId, e);
            }
        });
    }

}
