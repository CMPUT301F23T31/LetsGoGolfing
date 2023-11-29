package com.example.letsgogolfing;

import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
    private List<String> selectedTags = new ArrayList<>();
    private static final String TAG = "EditItemActivity";
    private Activity activity;
    private FirestoreRepository repository;
    public GetTags(Activity activity, FirestoreRepository repository){
        this.activity = activity;
        this.repository = repository;

    }


    /**
     * Displays a dialog for selecting tags.
     * <p>
     * This method presents a dialog that allows users to select tags from the available list.
     * The selected tags are updated in the UI, and the user can associate them with the item.
     * Tags are pre-checked based on the user's previous selections.
     */
    // You might need to pass the tags from MainActivity to here or retrieve them from persistent storage.


    public void showTagSelectionDialog(TagOkFunction tagOkFunction, ArrayList<String> tagList) {
        // Convert List to array for AlertDialog
        //adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_multiple_choice, itemList);
        tagList.removeAll(Collections.singleton(null));
        String[] tagsArray = tagList.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,  android.R.layout.simple_list_item_multiple_choice, tagList);
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
            View customView = createListView(adapter);
            builder.setView(customView);
            // Add OK and Cancel buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                tagOkFunction.apply(selectedTags);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            EditText editText = customView.findViewById(R.id.addTagText);
            editText.setHint("Enter your text"); // Optional: Set a hint for the input
            editText.setOnKeyListener((view, keyCode, keyEvent) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    Toast.makeText(activity, "test", Toast.LENGTH_SHORT).show();
                    String tagString = editText.getText().toString();
                    repository.addTag(tagString, new FirestoreRepository.OnTagAddedListener() {
                        @Override
                        public void onTagAdded() {
                            tagList.add(tagString);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    }
                    );
                    editText.setText("");
                    return true; // Consume the event
                }
                return false; // Allow other listeners to handle the event
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private View createListView(ArrayAdapter<String> adapter) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_custom_multi_choice_items, null);

        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);

        return view;
    }

}
