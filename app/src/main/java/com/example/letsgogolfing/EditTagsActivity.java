package com.example.letsgogolfing;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.example.letsgogolfing.model.TagAdapter;
import com.example.letsgogolfing.ui.item.ItemViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditTagsActivity extends AppCompatActivity {
    private EditText editTagsEditText;
    private TagAdapter tagsAdapter;
    private List<String> tagsList;
    private RecyclerView tagsRecyclerView;

    private ItemViewModel itemViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_tag);

        tagsRecyclerView = findViewById(R.id.tagsRecyclerView);
        tagsList = new ArrayList<>();
        tagsAdapter = new TagAdapter(this, tagsList);

        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tagsRecyclerView.setAdapter(tagsAdapter);

        // Initialize your ItemViewModel
        itemViewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        // Observe changes in the item data
        itemViewModel.getItem().observe(this, item -> {
            if (item != null) {
                // Prepopulate the tags from the item
                tagsList.clear();
                tagsList.addAll(item.getTags());
                tagsAdapter.notifyDataSetChanged();
            }
        });

        // Implement logic for adding, editing, and removing tags using tagsAdapter
        TagAdapter.OnItemClickListener itemClickListener = new TagAdapter.OnItemClickListener() {

            @Override
            public void onEditClick(int position, String editedTag) {
                // Implement logic to edit the tag in tagsList
                tagsList.set(position, editedTag);
                tagsAdapter.notifyItemChanged(position);
            }

            @Override
            public void onDeleteClick(int position) {
                // Implement logic to remove the tag from tagsList
                tagsList.remove(position);
                tagsAdapter.notifyItemRemoved(position);
            }
        };
    }

    private void editTag(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Tag");

        // Create an EditText view to allow tag editing
        final EditText input = new EditText(this);
        input.setText(tagsList.get(position)); // Set the initial text
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String editedTag = input.getText().toString();
            tagsList.set(position, editedTag);
            tagsAdapter.notifyItemChanged(position); // Notify the adapter that the item has changed
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel(); // Cancel the editing process
        });

        builder.show();
    }

    private void removeTag(int position) {
        tagsList.remove(position);
        tagsAdapter.notifyItemRemoved(position);
    }
    Button saveTags = findViewById(R.id.saveTagsButton);
}
