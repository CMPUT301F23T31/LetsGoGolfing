package com.example.letsgogolfing;

import static com.google.android.gms.vision.L.TAG;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ViewPhotoActivity extends AppCompatActivity {
    private ImageAdapter imageAdapter;

    private List<String> imageUris; // Field to store image URI strings

    private FirestoreRepository firestoreRepository;

    private String itemId;

    private String username;

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


    private void deleteImageFromFirebase(String imageUriString) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUriString);
        photoRef.delete().addOnSuccessListener(aVoid -> {
            Log.d("ViewPhotoActivity", "Image deleted successfully");
            removeImageUriFromItem(imageUriString);
        }).addOnFailureListener(exception -> {
            Log.e("ViewPhotoActivity", "Error deleting image", exception);
        });
    }


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

            @Override
            public void onError(Exception e) {
                // Handle any errors
                Log.e(TAG, "onError: failed to load item " + itemId, e);
            }
        });
    }

}
