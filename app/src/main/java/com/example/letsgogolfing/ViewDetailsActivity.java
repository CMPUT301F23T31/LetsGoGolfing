package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

// generate javadocs for ViewDetailsActivity
/**
 * Activity for viewing the details of an item in an Android application.
 * This activity allows users to view item details, edit the item, and view photos associated with the item.
 * It interacts with Firestore to fetch tags related to the item.
 */
public class ViewDetailsActivity extends AppCompatActivity {
    private LinearLayout tagsContainerView;
    private Button editButton, viewPhotoButton;
    private ImageButton backButton;
    private final List<String> tagList = new ArrayList<>(); // This should be populated from Firestore
    private List<String> selectedTags = new ArrayList<>();
    private Item item;
    private FirestoreRepository db;
    private String username;
    private static final String TAG = "ViewDetailsActivity";

    /**
     * Initializes the activity. This method sets up the user interface and initializes
     * the listeners for various UI components.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_details);

        InitializeUI();

        backButton.setOnClickListener(v -> {
            // takes back to home page main_activity
            Intent intent = new Intent(ViewDetailsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewDetailsActivity.this, EditItemActivity.class);
            item = (Item) getIntent().getSerializableExtra("ITEM");
            Log.d(TAG, "Item ID: " + item.getId());
            intent.putExtra("ITEM", item);
            startActivity(intent);
        });

        viewPhotoButton.setOnClickListener(v -> {
            // Implement View Photos
        });
    }

    /**
     * Initializes the user interface components of this activity.
     * This method retrieves the item details passed via Intent, sets up the text views,
     * buttons, and tag container view. It also populates the views with the item's data.
     */
        private void InitializeUI() {
            // Retrieve the item from the intent
            username = getIntent().getStringExtra("username");
            item = (Item) getIntent().getSerializableExtra("ITEM");

            // Instantiate TextViews
            TextView name = findViewById(R.id.name_view_text);
            TextView description = findViewById(R.id.description_view_text);
            TextView make = findViewById(R.id.make_view_text);
            TextView model = findViewById(R.id.model_view_text);
            TextView serial = findViewById(R.id.serial_view_text);
            TextView comment = findViewById(R.id.comment_view_text);
            TextView date = findViewById(R.id.date_view_text);
            TextView value = findViewById(R.id.value_view_text);

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

            // Connect to database
            db = new FirestoreRepository(username);


            // Set content of Tag Container
            loadTags();
        }

    /**
     * This method is responsible for displaying the tags associated with the item.
     * It dynamically creates TextViews for each tag and adds them to the tag container layout.
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
     * Loads tags from Firestore and updates the UI accordingly.
     * This method fetches a collection of tags from Firestore, processes the response,
     * and calls displayTags() to update the user interface.
     */
    private void loadTags() {
        db.fetchTags(new FirestoreRepository.OnTagsFetchedListener() {
            @Override
            public void onTagsFetched(List<String> tags) {
                tagList.clear();
                tagList.addAll(tags);
                selectedTags = new ArrayList<>(item.getTags()); // Use the tags from the item
                displayTags();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ViewDetailsActivity.this, "Failed to fetch tags", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error getting documents: ", e);
            }
        });
    }
}
