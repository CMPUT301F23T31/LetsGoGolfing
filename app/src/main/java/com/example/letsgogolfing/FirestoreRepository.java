package com.example.letsgogolfing;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// so far this only handles fetching tags and adding new items - vedant
public class FirestoreRepository {

    private final FirebaseFirestore db;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches tags from Firestore and notifies it through a callback.
     *
     * @param listener The callback listener for tag fetching results.
     */
    public void fetchTags(OnTagsFetchedListener listener) {
        db.collection("tags").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> fetchedTags = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    fetchedTags.add(document.getString("name"));
                }
                listener.onTagsFetched(fetchedTags);
            } else {
                listener.onError(task.getException());
            }
        });
    }

    /**
     * Adds a new item to Firestore and notifies it through a callback.
     *
     * @param itemMap  The item data to be added to Firestore.
     * @param listener The callback listener for item addition results.
     */
    public void addItem(Map<String, Object> itemMap, OnItemAddedListener listener) {
        db.collection("items").add(itemMap)
                .addOnSuccessListener(documentReference -> listener.onItemAdded(documentReference.getId()))
                .addOnFailureListener(listener::onError);
    }

    /**
     * Callback interface for tag fetching results.
     */
    public interface OnTagsFetchedListener {
        void onTagsFetched(List<String> tags);
        void onError(Exception e);
    }

    /**
     * Callback interface for item addition results.
     */
    public interface OnItemAddedListener {
        void onItemAdded(String itemId);
        void onError(Exception e);
    }
}
