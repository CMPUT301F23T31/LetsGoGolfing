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

    public static void handleUserCollections(String[] names, boolean isTest) {
        if (isTest) {
            handleTestCollections(new String[]{testCollectionName[0]}, names);
        } else {
            handleCollections(new String[]{collectionName[0]}, names);
        }
    }

//    public static void handleItemCollections() {
//        handleTestCollections(new String[]{testCollectionName[1]});
//        handleCollections(new String[]{collectionName[1]});
//    }
//
//    public static void handleTagCollections() {
//        handleTestCollections(new String[]{testCollectionName[2]});
//        handleCollections(new String[]{collectionName[2]});
//    }

    public static void handleAllCollections(String[] names, boolean isTest) {
        if (isTest) {
            handleTestCollections(testCollectionName, names);
        } else {
            handleCollections(collectionName, names);
        }
    }


    private static void handleTestCollections(String[] testCollectionName, String[] names) {
        for (String testCollection : testCollectionName) {
            CollectionReference userTestCollection = db.collection(testCollection);

            userTestCollection.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Collection exists, delete its contents
                        deleteCollectionContents(userTestCollection);
                        createNewUserTestCollection(userTestCollection, names);
                    } else {
                        // Collection does not exist, create a new one with an array of usernames

                        createNewUserTestCollection(userTestCollection, names);
                    }
                } else {
                    // Handle the error
                }
            });
        }
    }

    private static void handleCollections(String[] collectionName, String[] names) {

        for (String testCollection : collectionName) {
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
                            createNewUserTestCollection(userTestCollection, names);
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

    private static void createNewUserTestCollection(CollectionReference collectionReference, String[] usernames) {
        for (String name : usernames) {
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