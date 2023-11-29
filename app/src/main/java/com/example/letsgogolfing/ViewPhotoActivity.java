package com.example.letsgogolfing;

import static com.google.android.gms.vision.L.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

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

                // Delete the image from Firestore
                firebase.deleteImage(downloadUrl, new FirestoreRepository.OnImageDeletedListener() {
                    @Override
                    public void onImageDeleted() {
                        // Remove the downloadUrl from the list
                        imageUris.remove(position);
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

        // Retrieve the list of image URIs passed from ViewDetailsActivity
        imageUris = getIntent().getStringArrayListExtra("imageUris");
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
        back_button.setOnClickListener(v -> {
            finish();
        });

    }


}
