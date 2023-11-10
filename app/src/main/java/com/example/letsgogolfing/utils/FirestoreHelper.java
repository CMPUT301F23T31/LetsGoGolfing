package com.example.letsgogolfing.utils;

import android.util.Log;
import android.widget.Toast;

import com.example.letsgogolfing.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirestoreHelper {

    private static final String[] testCollectionName = {"userTestOnly", "itemsTestOnly", "tagTestOnly"};
    private static final String[] collectionName = {"users", "items", "tags"};

    public static FirebaseFirestore db = FirebaseFirestore.getInstance();


    public static void handleUserTestCollections() {

        for (String testCollection : testCollectionName) {
            CollectionReference userTestCollection = db.collection(testCollection);

            userTestCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Collection exists, delete its contents
                            deleteCollectionContents(userTestCollection);
                        } else {
                            // Collection does not exist, create a new one
                            createNewUserTestCollection(userTestCollection);
                        }
                    } else {
                        // Handle the error
                    }
                }
            });
        }
    }

    public static void handleUserCollections() {

        for (String testCollection : testCollectionName) {
            CollectionReference userTestCollection = db.collection(testCollection);

            userTestCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Collection exists, log a message
                            Log.d("DatabaseSetup", "Collection " + testCollection + " already exists.");
                        } else {
                            // Collection does not exist, create a new one
                            createNewUserTestCollection(userTestCollection);
                        }
                    } else {
                        // Handle the error
                        Log.e("DatabaseSetup", "Error checking for collection " + testCollection, task.getException());
                    }
                }
            });
        }
    }



    private static void deleteCollectionContents(CollectionReference collectionReference) {
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                try {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                    } else {
                        // Handle the error
                        throw Objects.requireNonNull(task.getException());
                    }
                } catch (Exception e) {
                    // Log the error
                    Log.e("DatabaseSetup", "Failed to delete existing user documents", e);
                }
            }
        });
    }

    private static void createNewUserTestCollection(CollectionReference collectionReference) {
        // Add dummy users
        String[] namesTest = {"testLogin", "nonUniqueUser"};
        for (String name : namesTest) {
            // Create a new user with a username
            Map<String, Object> user = new HashMap<>();
            user.put("username", name);
            // Add a new document with a generated ID
            collectionReference.add(user)
                    .addOnFailureListener(e -> {
                        // Handle the failure
                        Log.e("DatabaseSetup", "Failed to add user document", e);
                    });
        }
    }
}