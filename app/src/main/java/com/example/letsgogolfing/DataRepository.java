package com.example.letsgogolfing;

import com.google.firebase.firestore.FirebaseFirestore;

public class DataRepository {

    private static DataRepository instance;
    private static FirebaseFirestore db;

    private DataRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }
}