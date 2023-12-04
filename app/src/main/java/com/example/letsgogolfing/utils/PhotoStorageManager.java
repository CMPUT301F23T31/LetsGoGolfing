package com.example.letsgogolfing.utils;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.*;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

public class PhotoStorageManager {
    // private String username;
    private String itemID;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;


    public PhotoStorageManager(Context context) {
        this.context = context;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", context.MODE_PRIVATE); // fetch username from session
        // username = prefs.getString("username", "No name"); // "No name" is a default value.
    }

    public void uploadPhoto(Uri photoURI, UUID itemId) {
        // Check if the URI is not empty
        if (photoURI != null) {
            // Generate a unique filename for the photo
            String photoFileName = "photo_" + System.currentTimeMillis() + ".jpg";

            // Get a reference to the storage location
            // change to username + "/" + itemId.toString() + "/" + photoFileName if user profiles are implemented
            StorageReference photoRef = storageRef.child(itemId.toString() + "/" + photoFileName);

            // Upload the photo to Firebase Storage
            UploadTask uploadTask = photoRef.putFile(photoURI);

            // Register observers to listen for when the upload is done or if it fails
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Photo uploaded successfully
                    // You can get the download URL of the uploaded photo if needed
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            // Do something with the download URL if needed
                            String photoDownloadUrl = downloadUri.toString();
                            Log.d(TAG, "Photo download URL: " + photoDownloadUrl);
                            // Additional actions after successful upload
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle unsuccessful uploads
                    Log.e(TAG, "Photo upload failed: " + e.getMessage());
                    Toast.makeText(context, "Photo upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle the case where the URI is empty
            Log.e(TAG, "Photo URI is empty");
            Toast.makeText(context, "Photo URI is empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void retrievePhotos(UUID itemId) {
        // Get a reference to the storage location
        // change to username + "/" + itemId.toString() + "/" + photoFileName if user profiles are implemented
        StorageReference photoFolderRef = storageRef.child(itemId.toString() + "/");

        List<StorageReference> results = new ArrayList<>();

        // List items in the folder
        photoFolderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    // List of StorageReference items
                    List<StorageReference> items = listResult.getItems();

                    // Sort the items based on filenames
                    Collections.sort(items, new Comparator<StorageReference>() {
                        @Override
                        public int compare(StorageReference o1, StorageReference o2) {
                            // Extract filenames from StorageReferences
                            String fileName1 = o1.getName();
                            String fileName2 = o2.getName();

                            // Compare filenames
                            return fileName1.compareTo(fileName2);
                        }
                    });
                    // Now 'items' contains StorageReferences sorted by filename
                    for (StorageReference itemRef : items) {
                        // All the items under listRef.
                        Log.d(TAG, "Photo filename: " + itemRef.getName());
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure
                    Log.e(TAG, "Error retrieving photos: " + e.getMessage());
                });
    }

    public void deletePhoto(Uri photoURI, UUID itemId) {
        // Check if the URI is not empty
        if (photoURI != null) {
            // Get the filename from the photo URI
            String photoFileName = getFileNameFromUri(photoURI);

            // Get a reference to the storage location
            // change to username + "/" + itemId.toString() + "/" + photoFileName if user profiles are implemented
            StorageReference photoRef = storageRef.child(itemId.toString() + "/" + photoFileName);

            // Delete the photo from Firebase Storage
            photoRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        // Photo deleted successfully
                        Log.d(TAG, "Photo deleted successfully");
                        // Additional actions after successful deletion
                    })
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful deletion
                        Log.e(TAG, "Photo deletion failed: " + exception.getMessage());
                        Toast.makeText(context, "Photo deletion failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle the case where the URI is empty
            Log.e(TAG, "Photo URI is empty");
            Toast.makeText(context, "Photo URI is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        return path != null ? path.substring(path.lastIndexOf('/') + 1) : null;
    }




    /**
     * if using bitmap
     * add this to manifest
     * <uses-feature android:name="android.hardware.camera" />
     * <uses-feature android:name="android.hardware.camera.autofocus" />
     * <uses-permission android:name="android.permission.CAMERA" />
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * */
    private Bitmap imageBitmap;
    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private void uploadImageBitmap(byte[] imageData, UUID itemId) {
        // Upload image to Firebase Storage
        String photoFileName = "photo_" + System.currentTimeMillis() + ".jpg";

        // Get a reference to the storage location
        // change to username + "/" + itemId.toString() + "/" + photoFileName if user profiles are implemented
        StorageReference photoRef = storageRef.child(itemId.toString() + "/" + photoFileName);

        UploadTask uploadTask = storageRef.putBytes(imageData);

        // Register observers to listen for when the upload is done or if it fails
        uploadTask.addOnCompleteListener((Executor) this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Photo uploaded successfully");
                    // Image uploaded successfully
                } else {
                    Log.d(TAG, "Photo upload failed: ");
                    Toast.makeText(context, "Photo upload failed", Toast.LENGTH_SHORT).show();
                    // Handle error
                }
            }
        });
    }

}
