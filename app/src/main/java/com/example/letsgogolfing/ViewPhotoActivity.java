package com.example.letsgogolfing;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        // Initialize the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(imageAdapter);

        // Retrieve the list of image URIs passed from ViewDetailsActivity
        List<String> imageUris = getIntent().getStringArrayListExtra("imageUris");
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


    public void displayImagesFromFirebase() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("images/testImages/");
    
        imagesRef.listAll()
            .addOnSuccessListener(listResult -> {
                Log.d("Firebase Storage", "Number of items: " + listResult.getItems().size());
                List<Task<Uri>> tasks = new ArrayList<>();
                for (StorageReference item : listResult.getItems()) {
                    Task<Uri> task = item.getDownloadUrl();
                    task.addOnFailureListener(exception -> {
                        Log.e("Firebase Storage", "Failed to get download URL", exception);
                    });
                    tasks.add(task);
                }
    
                Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
                    List<Uri> imageUris = new ArrayList<>();
                    for (Object object : objects) {
                        Uri uri = (Uri) object;
                        Log.d("Firebase Storage", "Image URI: " + uri.toString());
                        imageUris.add(uri);
                    }
    
                    // Update the adapter with the image URIs
                    imageAdapter.setImageUris(imageUris);
                    imageAdapter.notifyDataSetChanged();
                    Log.d("ImageAdapter", "Item count: " + imageAdapter.getItemCount());
                });
            })
            .addOnFailureListener(exception -> {
                // Handle any errors
                Log.e("Firebase Storage", "Failed to list files", exception);
            });
    }
}
