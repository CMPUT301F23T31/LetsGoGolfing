package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// generate javadocs for ViewDetailsActivity
/**
 * Activity for viewing the details of an item.
 */
public class ViewDetailsActivity extends AppCompatActivity {
    private TextView name;
    private TextView description;
    private TextView value;
    private TextView make;
    private TextView model;
    private TextView serial;
    private TextView comment;
    private TextView date;
    private LinearLayout tagsContainerView;

    private Button editButton;
    private Button viewPhotoButton;
    private ImageButton backButton;

    private List<String> tagList = new ArrayList<>(); // This should be populated from Firestore
    private List<String> selectedTags = new ArrayList<>();
    private Item item;
    private static final String TAG = "ViewDetailsActivity";


    /**
     * onCreate method for the ViewDetailsActivity.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_details);

        InitializeUI();

        backButton.setOnClickListener(v -> {
            // takes back to home page main_activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditItemActivity.class);
            intent.putExtra("ITEM", item);
            startActivity(intent);
        });

        viewPhotoButton.setOnClickListener(v -> {
            // Implement View Photos
        });
    }

        private void InitializeUI() {
            // Retrieve the item from the intent
            item = (Item) getIntent().getSerializableExtra("ITEM");

            // Instantiate TextViews
            name = findViewById(R.id.name_view_text);
            description = findViewById(R.id.description_view_text);
            make = findViewById(R.id.make_view_text);
            model = findViewById(R.id.model_view_text);
            serial = findViewById(R.id.serial_view_text);
            comment = findViewById(R.id.comment_view_text);
            date = findViewById(R.id.date_view_text);
            value = findViewById(R.id.value_view_text);

            // Instantiate Buttons
            editButton = findViewById(R.id.edit_item_button);
            backButton = findViewById(R.id.back_button);
            viewPhotoButton = findViewById(R.id.view_photo_button);

            // Instantiate Tag Container
            tagsContainerView = findViewById(R.id.tags_linear_layout);

            // Set content TextViews
            name.setText(item.getName());
            description.setText(item.getDescription());
            make.setText(item.getMake());
            model.setText(item.getModel());
            serial.setText(item.getSerialNumber());
            comment.setText(item.getComment());
            date.setText(dateFormat.format(item.getDateOfPurchase()));
            value.setText(Double.toString(item.getEstimatedValue()));

            // Set content of Tag Container
            loadTags();
        }

    /**
     * Display tags.
     */
    private void displayTags() {
        tagsContainerView = findViewById(R.id.tags_linear_layout);
        tagsContainerView.removeAllViews(); // Clear all views/tags before adding new ones

        for (String tag : selectedTags) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setBackgroundResource(R.drawable.tag_background); // Make sure this drawable exists

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0); // Adding some right margin between tags

            tagView.setLayoutParams(params);
            tagsContainerView.addView(tagView); // Add the TextView to your container

        }
    }


    /**
     * Load tags.
     */
    private void loadTags() {
        // Assuming you have a method to fetch tags from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tags").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tagList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    tagList.add(document.getString("name"));
                }
                selectedTags = new ArrayList<>(item.getTags()); // Use the tags from the item
                displayTags();
            } else {
                Log.w(TAG, "Error getting documents: ", task.getException());
            }
        });
    }
}
