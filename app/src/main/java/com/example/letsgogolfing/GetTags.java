package com.example.letsgogolfing;

import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

interface TagOkFunction{
    void apply(List<String> tags);
}
public class GetTags {
    private List<String> tagList = new ArrayList<>(); // This should be populated from the ManageTagsActivity
    private List<String> selectedTags = new ArrayList<>();
    private static final String TAG = "EditItemActivity";
    private Activity activity;
    public GetTags(Activity activity){
        this.activity = activity;

    }
    public void fetchTagsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the tags from Firestore
        db.collection("tags").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tagList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    tagList.add(document.getString("name"));
                }
                // Now that the tags are fetched, you can enable the 'Add Tags' button
            } else {
                Log.w(TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    /**
     * Displays a dialog for selecting tags.
     * <p>
     * This method presents a dialog that allows users to select tags from the available list.
     * The selected tags are updated in the UI, and the user can associate them with the item.
     * Tags are pre-checked based on the user's previous selections.
     */
    // You might need to pass the tags from MainActivity to here or retrieve them from persistent storage.


    public void showTagSelectionDialog(TagOkFunction tagOkFunction) {
        fetchTagsFromFirestore(); // Originally, this would only be called on the creation of the AddItemActivity class...
        // and not on every call of showTagSelectionDialog

        // Convert List to array for AlertDialog
        tagList.removeAll(Collections.singleton(null));
        String[] tagsArray = tagList.toArray(new String[0]);
        boolean[] checkedTags = new boolean[tagList.size()];


        try {
            // Pre-check the tags that have been previously selected
            for(int i = 0; i < tagList.size(); i++) {
                if(selectedTags.contains(tagList.get(i))) {
                    checkedTags[i] = true;
                }
            }

            // Show the dialog (little pop-up screen) checkbox list of tags
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMultiChoiceItems(tagsArray, checkedTags, (dialog, which, isChecked) -> {
                // Add or remove the tag from the selected tags list based on whether the checkbox is checked
                String selectedTag = tagList.get(which);
                if (isChecked) {
                    selectedTags.add(selectedTag);
                } else {
                    selectedTags.remove(selectedTag);
                }
            });

            // Add OK and Cancel buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                tagOkFunction.apply(selectedTags);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
