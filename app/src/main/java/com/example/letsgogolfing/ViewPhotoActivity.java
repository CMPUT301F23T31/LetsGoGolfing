package com.example.letsgogolfing;

import static com.google.android.gms.vision.L.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letsgogolfing.utils.FirestoreRepository;
import com.example.letsgogolfing.utils.ImageAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPhotoActivity extends AppCompatActivity {
    private ImageAdapter imageAdapter;
    private Item item;
    private List<String> imageUris;

    private SharedPreferences sharedPref;
    private String currentUsername;
    private FirestoreRepository firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        currentUsername = sharedPref.getString("username", null);
        firebase = new FirestoreRepository(currentUsername);

        // Initialize the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(new ArrayList<>(), this, new ImageAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                // Get the downloadUrl of the image
                String downloadUrl = imageUris.get(position);
                Log.d("ViewPhotoActivity", "onDeleteClick: " + downloadUrl);

                // Delete the image from Firestore
                firebase.deleteImage(downloadUrl, new FirestoreRepository.OnImageDeletedListener() {
                    @Override
                    public void onImageDeleted() {
                        // Remove the downloadUrl from the list
                        imageUris.remove(position);
                        // Update the list in the ImageAdapter
                        imageAdapter.updateImageUris(imageUris);
                        // Notify the adapter
                        imageAdapter.notifyItemRemoved(position);
                        imageAdapter.notifyItemRangeChanged(position, imageUris.size());
                    }

                    @Override
                    public void onError(Exception e) {
                        // Handle any errors
                        Log.e(TAG, "onError: failed to delete image " + downloadUrl, e);
                    }
                });
            }
        });

        recyclerView.setAdapter(imageAdapter);

        // Get the item from the Intent
        Intent intent = getIntent();
        item = (Item) intent.getSerializableExtra("item");

        // Initialize imageUris with the image URIs from the item
        imageUris = item.getImageUris();

        if (imageUris != null) {
            // Convert String URIs to Uri objects
            List<Uri> uriList = new ArrayList<>();
            for (String uriString : imageUris) {
                uriList.add(Uri.parse(uriString));
            }

            // Update the adapter with the image URIs
            imageAdapter.setImageUris(uriList);
            imageAdapter.notifyDataSetChanged();
        } else {
            // Handle the case where no image URIs are passed
            Log.e("ViewPhotoActivity", "No image URIs received");
        }

        Button back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the data
        refreshData();
    }

    private void refreshData() {
        // Get the updated item from the database
        firebase.fetchItemById(item.getId(), new FirestoreRepository.OnItemFetchedListener() {
            @Override
            public void onItemFetched(Item item) {
                // Update the imageUris list
                imageUris = item.getImageUris();
                // Update the list in the ImageAdapter
                imageAdapter.updateImageUris(imageUris);
                // Notify the adapter
                imageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                // Handle any errors
                Log.e(TAG, "onError: failed to load item " + item.getId(), e);
            }
        });
    }

}
