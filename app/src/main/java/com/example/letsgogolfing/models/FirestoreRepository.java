package com.example.letsgogolfing.models;
import android.util.Log;

import com.example.letsgogolfing.models.Item;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Repository class for interacting with the Firestore database.
 * <p>
 * This class provides methods for performing CRUD operations on the Firestore database.
 * It also provides callback interfaces for handling the results of these operations.
 * </p>
 */
public class FirestoreRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String currentUserId;

    public FirestoreRepository(String userId) {
        this.currentUserId = userId;
    }

    /**
     * Gets the current user's ID (username).
     *
     * @return The current user's ID (username).
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Fetches all items from the Firestore database.
     * This method retrieves a collection of items stored in Firestore. Each document is converted into an {@link Item} object.
     * The method populates a list of {@link Item} objects with data from the Firestore documents and passes this list to the {@link OnItemsFetchedListener} upon successful retrieval.
     * In case of a failure during the fetch operation, the listener is notified with the exception.
     *
     * @param listener The {@link OnItemsFetchedListener} callback for handling the results of the fetch operation.
     *                 It receives a list of {@link Item} objects on successful data retrieval or an exception on failure.
     */
    public void fetchItems(OnItemsFetchedListener listener) {
        db.collection("users").document(currentUserId).collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Item> items = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Item item = document.toObject(Item.class);
                    item.setId(document.getId());
                    items.add(item);
                }
                listener.onItemsFetched(items);
            } else {
                listener.onError(task.getException());
            }
        });
    }

    /**
     * Fetches a single item from the Firestore database.
     * This method retrieves a single item from Firestore using the provided item ID.
     * The document is converted into an {@link Item} object and passed to the {@link OnItemFetchedListener} upon successful retrieval.
     * In case of a failure during the fetch operation, the listener is notified with the exception.
     *
     * @param itemId The ID ({@link String}) of the item to be fetched.
     * @param listener The {@link OnItemFetchedListener} callback for handling the results of the fetch operation.
     *                 It receives an {@link Item} object on successful data retrieval or an exception on failure.
     */
    public void fetchItemById(String itemId, OnItemFetchedListener listener) {
        db.collection("users").document(currentUserId).collection("items").document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Item item = documentSnapshot.toObject(Item.class);
                        if (item != null) {
                            item.setId(documentSnapshot.getId());
                            listener.onItemFetched(item);
                        } else {
                            listener.onError(new Exception("Error parsing item."));
                        }
                    } else {
                        listener.onError(new Exception("Item not found."));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    public interface OnItemFetchedListener {
        void onItemFetched(Item item);
        void onError(Exception e);
    }

    /**
     * Adds a new tag to Firestore and notifies through a callback.
     *
     * @param tag The tag to be added to Firestore.
     * @param listener The callback listener for tag addition results.
     */
    public void addTag(String tag, OnTagAddedListener listener) {
        Map<String, Object> tagMap = new HashMap<>();
        tagMap.put("name", tag);

        db.collection("tags").add(tagMap)
                .addOnSuccessListener(documentReference -> listener.onTagAdded())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Callback interface for tag addition results.
     */
    public interface OnTagAddedListener {
        void onTagAdded();
        void onError(Exception e);
    }

    /**
     * Adds a new item to Firestore and notifies it through a callback.
     *
     * @param item  The item data to be added to Firestore.
     * @param listener The callback listener for item addition results.
     */
    public void addItem(Item item, OnItemAddedListener listener) {
        Map<String, Object> itemMap = convertItemToMap(item);
        // Ensure image URIs are included
        itemMap.put("imageUris", item.getImageUris());
        db.collection("users").document(currentUserId).collection("items").add(itemMap)
                .addOnSuccessListener(documentReference -> listener.onItemAdded(documentReference.getId()))
                .addOnFailureListener(listener::onError);
    }

    /**
     * Deletes a list of items from the Firestore database using a batch operation.
     * This method iterates over a list of item IDs and adds each delete operation to a Firestore batch.
     * Upon successful deletion of all items in the batch, the provided {@link OnItemDeletedListener} callback is notified.
     * In case of failure, the callback is notified with the exception.
     *
     * @param itemIds A list of item IDs ({@link String}) to be deleted.
     * @param listener The {@link OnItemDeletedListener} callback for handling the result of the deletion operation.
     */
    public void deleteItems(List<String> itemIds, OnItemDeletedListener listener) {
        WriteBatch batch = db.batch();
        for (String id : itemIds) {
            batch.delete(db.collection("users").document(currentUserId).collection("items").document(id));
        }
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.OnItemsDeleted();
            } else {
                listener.onError(task.getException());
            }
        });
    }

    /**
     * Updates an existing item in the Firestore database.
     * This method converts the {@link Item} object into a {@link Map} and then sets the updated data in the Firestore document with the specified item ID.
     * Upon successful update, the provided {@link OnItemUpdatedListener} callback is notified.
     * In case of failure, the callback is notified with the exception.
     *
     * @param itemId The ID ({@link String}) of the item to be updated.
     * @param item The {@link Item} object containing the updated data.
     * @param listener The {@link OnItemUpdatedListener} callback for handling the result of the update operation.
     */

    public void updateItem(String itemId, Item item, OnItemUpdatedListener listener) {
        Map<String, Object> itemMap = convertItemToMap(item);
        db.collection("users").document(currentUserId).collection("items").document(itemId).set(itemMap)
                .addOnSuccessListener(aVoid -> listener.onItemUpdated())
                .addOnFailureListener(listener::onError);
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
     * Checks if a user exists in Firestore and notifies it through a callback.
     *
     * @param username The username of the user to be checked.
     * @param listener The callback listener for user existence checking results.
     */
    public void checkUserExists(String username, OnUserExistenceCheckedListener listener) {
        db.collection("users").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onUserExists();
                    } else {
                        listener.onUserDoesNotExist();
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Adds a new user to Firestore and notifies it through a callback.
     *
     * @param username The username of the user to be added.
     * @param listener The callback listener for user addition results.
     */
    public void addUser(String username, OnUserAddedListener listener) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);

        db.collection("users").document(username).set(user)
                .addOnSuccessListener(aVoid -> listener.onUserAdded(username))
                .addOnFailureListener(listener::onError);
    }



    /**
     * Callback interface for tag addition results.
     */
    public interface OnUserExistenceCheckedListener {
        void onUserExists();
        void onUserDoesNotExist();
        void onError(Exception e);
    }

    /**
     * Callback interface for user addition results.
     */
    public interface OnUserAddedListener {
        void onUserAdded(String userId);
        void onError(Exception e);
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

    /**
     * Callback interface for item fetching results.
     */
    public interface OnItemsFetchedListener {
        void onItemsFetched(List<Item> items);
        void onError(Exception e);
    }

    /**
     * Callback interface for item deletion results.
     */
    public interface OnItemDeletedListener {
        void OnItemsDeleted();
        void onError(Exception e);
    }

    /**
     * Callback interface for item update results.
     */
    public interface OnItemUpdatedListener {
        void onItemUpdated();
        void onError(Exception e);
    }

    /**
     * Adds an image URI to an item in Firestore.
     * This method adds the provided image URI to the imageUris array of the item with the specified item ID.
     * Upon successful addition, the provided {@link OnImageUriAddedListener} callback is notified.
     * In case of failure, the callback is notified with the exception.
     *
     * @param itemId The ID ({@link String}) of the item to which the image URI is to be added.
     * @param imageUri The image URI ({@link String}) to be added to the item.
     * @param listener The {@link OnImageUriAddedListener} callback for handling the result of the addition operation.
     */
    public void deleteImageUriFromItem(String itemId, String imageUri, OnImageUriDeletedListener listener) {
        if (itemId == null || imageUri == null) {
            Log.e("FirestoreRepository", "Item ID or Image URI is null");
            listener.onError(new Exception("Item ID or Image URI is null"));
            return;
        }

        Log.d("FirestoreRepository", "Deleting image URI " + imageUri + " from item " + itemId);

        // Log the currentUserId and itemId for debugging
        Log.d("FirestoreRepository", "currentUserId: " + currentUserId);
        Log.d("FirestoreRepository", "itemId: " + itemId);

        DocumentReference itemRef = db.collection("users").document(currentUserId).collection("items").document(itemId);
        Log.d("FirestoreRepository", "Deleting URI from: " + itemRef.getPath());

        itemRef.update("imageUris", FieldValue.arrayRemove(imageUri))
                .addOnSuccessListener(aVoid -> listener.onImageUriDeleted())
                .addOnFailureListener(listener::onError);
    }


    public interface OnImageUriDeletedListener {
        void onImageUriDeleted();
        void onError(Exception e);
    }
    /**
     * Converts an {@link Item} object to a {@link Map} for Firestore storage.
     * <p>
     * This method takes an {@code Item} object and creates a {@code Map} where each field
     * of the item is represented by a key-value pair. The keys are field names, and the values
     * are obtained from the corresponding getters in the {@code Item} class. The date of purchase
     * is converted to a {@link Timestamp} for Firestore storage. The resulting {@code Map} can be
     * used to store the item data in Firestore.
     * </p>
     *
     * @param item The {@code Item} object to be converted to a {@code Map}.
     * @return A {@code Map} representing the fields of the provided {@code Item} object.
     */
    // Helper method to convert an Item object into a Map for Firestore
    // ALSO NEW: this now is a centralized function, so that whenever you want to add an item it'll automatically turn it
    // into a map then process the data before its put into firestore.
    public static Map<String, Object> convertItemToMap(Item item) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", item.getName());
        itemMap.put("description", item.getDescription());
        itemMap.put("dateOfPurchase", new Timestamp(item.getDateOfPurchase()));
        itemMap.put("make", item.getMake());
        itemMap.put("model", item.getModel());
        itemMap.put("serialNumber", item.getSerialNumber());
        itemMap.put("estimatedValue", item.getEstimatedValue());
        itemMap.put("comment", item.getComment());
        itemMap.put("tags", item.getTags());
        itemMap.put("imageUris", item.getImageUris());
        return itemMap;
    }
}
