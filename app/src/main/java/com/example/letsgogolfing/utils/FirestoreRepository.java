package com.example.letsgogolfing.utils;
import android.net.Uri;
import android.util.Log;

import com.example.letsgogolfing.Item;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// so far this only handles fetching tags and adding new items - vedant
public class FirestoreRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String currentUserId;

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private DocumentReference newDocRef;

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
    
        if (item.getId() != null) {
            // If the item has an itemId, use set to maintain the itemId
            db.collection("users").document(currentUserId).collection("items").document(item.getId()).set(itemMap)
                .addOnSuccessListener(aVoid -> listener.onItemAdded(item.getId()))
                .addOnFailureListener(listener::onError);
        } else {
            // If the item doesn't have an itemId, use add to generate a new itemId
            db.collection("users").document(currentUserId).collection("items").add(itemMap)
                .addOnSuccessListener(documentReference -> listener.onItemAdded(documentReference.getId()))
                .addOnFailureListener(listener::onError);
        }
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

    public void addUser(String username, OnUserAddedListener listener) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);

        db.collection("users").document(username).set(user)
                .addOnSuccessListener(aVoid -> listener.onUserAdded(username))
                .addOnFailureListener(listener::onError);
    }

    // Callback interfaces
    public interface OnUserExistenceCheckedListener {
        void onUserExists();
        void onUserDoesNotExist();
        void onError(Exception e);
    }

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

    public interface OnItemDeletedListener {
        void OnItemsDeleted();
        void onError(Exception e);
    }

    public interface OnItemUpdatedListener {
        void onItemUpdated();
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

    public void uploadImage(Uri imageUri, Item item, OnImageUploadedListener listener) {
        // Create a reference to the file in Firebase Storage
        String photoFileName = currentUserId + "_" +item.getId() + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child("images/" + photoFileName);

        // Upload the file to Firebase Storage
        imageRef.putFile(imageUri)
        .addOnSuccessListener(taskSnapshot -> {
            // Get the download URL of the uploaded file
            imageRef.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> {
                    // Use the download URL as the document ID
                    String documentId = downloadUri.toString();

                    // Add the download URL to the item's ImageUri array list
                    item.getImageUris().add(documentId);

                    // Create a new document in the imageData collection with the ID set to the filename
                    Map<String, Object> imageData = new HashMap<>();
                    imageData.put("downloadUrl", documentId);
                    imageData.put("fileName", photoFileName);

                    db.collection("imageData").document(photoFileName).set(imageData)
                        .addOnSuccessListener(aVoid -> {
                            // Notify the listener that the image has been uploaded
                            listener.onImageUploaded(documentId);
                        })
                        .addOnFailureListener(listener::onError);
                })
                .addOnFailureListener(listener::onError);
        })
        .addOnFailureListener(listener::onError);
    }

    public interface OnImageUploadedListener {
        void onImageUploaded(String downloadUrl);
        void onError(Exception e);
    }

    public void deleteImage(String downloadUrl, OnImageDeletedListener listener) {
        // Extract the filename from the downloadUrl
        String encodedFileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1, downloadUrl.indexOf("?"));
        String fileName;

        // Check if the filename is URL encoded
        if (encodedFileName.contains("%2F")) {
            try {
                fileName = URLDecoder.decode(encodedFileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return;
            }
        } else {
            fileName = encodedFileName;
        }

        // Remove the "images/" prefix from the filename
        if (fileName.startsWith("images/")) {
            fileName = fileName.substring(7);
        }

        Log.d("FirestoreRepository", "Filename: " + fileName);

        // Extract the itemId from the filename
        String itemId = fileName.split("_")[1];
        Log.d("FirestoreRepository", "Item ID: " + itemId);

        // Create a reference to the file in Firebase Storage
        StorageReference imageRef = storageRef.child("images/" + fileName);
        Log.d("FirestoreRepository", "image path: " + imageRef);

        //variables in lambda can't be changed during methods, finalFileName effectively static
        String finalFileName = fileName;
        db.collection("users").document(currentUserId).collection("items").document(itemId).get()
            .addOnSuccessListener(itemDocumentSnapshot -> {
                // Get the ImageUris array from the item document
                List<String> imageUris = (List<String>) itemDocumentSnapshot.get("imageUris");
                Log.d("FirestoreRepository", "Image URIs: " + imageUris);

                // Remove the download URL from the ImageUris array
                imageUris.remove(downloadUrl);
                Log.d("FirestoreRepository", "Download URL: " + downloadUrl);

                // Update the item document
                db.collection("users").document(currentUserId).collection("items").document(itemId).update("imageUris", imageUris)
                    .addOnSuccessListener(aVoid -> {
                        // Delete the file
                        imageRef.delete()
                            .addOnSuccessListener(aVoid2 -> {
                                // Delete the document from the imageData collection
                                db.collection("imageData").document(finalFileName).delete()
                                    .addOnSuccessListener(aVoid3 -> {
                                        // Notify the listener that the image has been deleted
                                        listener.onImageDeleted();
                                    })
                                    .addOnFailureListener(listener::onError);
                            })
                            .addOnFailureListener(listener::onError);
                    })
                    .addOnFailureListener(listener::onError);
            })
            .addOnFailureListener(listener::onError);
    }

    public interface OnImageDeletedListener {
        void onImageDeleted();
        void onError(Exception e);
    }

    public void generateID(String collectionType, OnIDGeneratedListener listener) {
        switch (collectionType.toLowerCase()) {
            case "users":
                newDocRef = db.collection("users").document();
                break;
            case "items":
                newDocRef = db.collection("users").document(currentUserId).collection("items").document();
                break;
            case "tags":
                newDocRef = db.collection("tags").document();
                break;
            default:
                throw new IllegalArgumentException("Invalid collection type");
        }
        // Check if the "items" collection exists
        newDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // The collection exists, generate the ID
                String id = newDocRef.getId();
                Log.d("FirestoreRepository", "Generated ID: " + id);
                listener.onIDGenerated(id);
            } else {
                // The collection doesn't exist, notify the listener
                listener.onError(new Exception("The '" + collectionType + "' collection doesn't exist"));
            }
        });
    }

    public interface OnIDGeneratedListener {
        void onIDGenerated(String id);
        void onError(Exception e);
    }
}
