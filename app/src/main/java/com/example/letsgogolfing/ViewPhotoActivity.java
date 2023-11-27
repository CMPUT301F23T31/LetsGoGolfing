package com.example.letsgogolfing;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

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

        // Initialize the RecyclerView with an empty adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(imageAdapter);

        // Retrieve the itemId as an Integer
        /** fix this once item ID is properly used */
        int itemId = getIntent().getIntExtra("itemId", 0);

        // Convert the itemId to a String
        String itemIdString = Integer.toString(itemId);

        // Check if the user is signed in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            displayImagesFromFirebase();
        } else {
            // Handle the case where the user is not signed in
            Log.e("Firebase Auth", "User is not signed in");
            displayImagesFromFirebase();
        }
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
