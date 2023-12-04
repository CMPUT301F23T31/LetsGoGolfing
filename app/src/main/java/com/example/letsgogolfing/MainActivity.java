package com.example.letsgogolfing;


import static android.view.View.GONE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import androidx.fragment.app.DialogFragment;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letsgogolfing.controllers.AddItemActivity;
import com.example.letsgogolfing.controllers.dialogs.FilterDialogFragment;
import com.example.letsgogolfing.controllers.LoginActivity;
import com.example.letsgogolfing.controllers.dialogs.SortDialogFragment;
import com.example.letsgogolfing.controllers.ViewDetailsActivity;
import com.example.letsgogolfing.controllers.ViewProfileActivity;
import com.example.letsgogolfing.models.FirestoreRepository;
import com.example.letsgogolfing.models.Item;
import com.example.letsgogolfing.utils.BarcodeFetchInfo;
import com.example.letsgogolfing.utils.ItemComparator;
import com.example.letsgogolfing.controllers.dialogs.TagDialogHelper;
import com.example.letsgogolfing.views.DatePickerEditText;
import com.example.letsgogolfing.views.ItemAdapter;
import com.google.firebase.BuildConfig;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import java.util.Set;


/**
 * The main activity class that serves as the entry point for the application.
 * It handles the display and interaction with a grid of items, allowing the user to
 * select and delete items, as well as adding new ones and viewing their details.
 */
public class MainActivity extends AppCompatActivity implements SortDialogFragment.SortOptionListener, FilterDialogFragment.FilterDialogListener{

    private Uri imageUri;
    private static final int MY_CAMERA_PERMISSION_CODE = 420;

    // itemsFetched used for UI test to check if items are fetched from FireStore;
    public static boolean itemsFetched;
    private TextView selectTextCancel; // Add this member variable for the TextView
    private Button manageTagsButton;
    private static final String TAG = "MainActivity";
    private GridView itemGrid;
    private ItemAdapter itemAdapter; // You need to create this Adapter class.

    private FilterDialogFragment.FilterType selectedFilterType;
    private boolean isSelectMode = false;
    private ImageButton deleteButton;
    private ItemComparator comparator;
    private ImageView scanItemButton;
    private ArrayList<String> tagList;

    private FirestoreRepository firestoreRepository;

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private ImageButton filterButton;
    private DialogFragment sortDialog = new SortDialogFragment();
    private DatePickerEditText searchEditText;



    /**
     * compares two {@code Item} objects based on the specified sorting field and order.
     */
    @Override
    public void onSortOptionSelected(String selectedOption, boolean sortDirection) {
        itemAdapter.setSortCriteria(selectedOption, sortDirection);
    }



    /**
     * Sorts the item adapter based on the specified comparator.
     *
     * @param comparator The comparator to use for sorting.
     */
    private void sortArrayAdapter(Comparator<Item> comparator) {
        if (itemAdapter != null) {
            ArrayList<Item> itemList = new ArrayList<>();
            for (int i = 0; i < itemAdapter.getCount(); i++) {
                itemList.add(itemAdapter.getItem(i));
            }

            itemList.sort(comparator);
            itemAdapter.clear();
            itemAdapter.addAll(itemList);
            itemAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Initializes the activity with the required layout and sets up the item grid adapter.
     * It also configures click listeners for the item grid and other UI components.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the most recent data,
     *                           or null if it is the first time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isDebugMode() || isRunningEspressoTest()) {
            // Bypass login and directly initialize components
            // Initialize with test or default data
            initForTesting();
        } else {
            SharedPreferences sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String currentUsername = sharedPref.getString("username", null);
            if (currentUsername == null || currentUsername.isEmpty()) {
                // Redirect to LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "Crashed in known location", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize FirestoreRepository with the current username
            firestoreRepository = new FirestoreRepository(currentUsername);
        }
        itemGrid = findViewById(R.id.itemGrid);
        itemAdapter = new ItemAdapter(this, new ArrayList<>());

        itemGrid.setAdapter(itemAdapter);

        fetchItemsAndRefreshAdapter();
        itemGrid.setOnItemLongClickListener((parent, view, position, id) -> {
            Item item = itemAdapter.getItem(position);
            if (item != null && item.getId() != null) {
                firestoreRepository.fetchTags(new FirestoreRepository.OnTagsFetchedListener() {
                    @Override
                    public void onTagsFetched(List<String> tags) {
                        tagList = (ArrayList<String>) tags;
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                }); // this is necessary for the tags menu to not be empty...
                if(isSelectMode == false){
                    isSelectMode = true;
                    deleteButton.setVisibility(View.VISIBLE);
                    itemAdapter.toggleSelection(position);
                    selectTextCancel.setVisibility(View.VISIBLE);
                    manageTagsButton.setVisibility(View.VISIBLE);
                }
            } else {
                // Document ID is null, handle this case
                Toast.makeText(MainActivity.this, "Cannot select item without an ID", Toast.LENGTH_SHORT).show();
            }

            return true; // True to indicate the long click was consumed
        });

        itemGrid.setOnItemClickListener((parent, view, position, id) -> {
            if (isSelectMode) {
                itemAdapter.toggleSelection(position); // Toggle item selection
            } else {
                Item item = itemAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, ViewDetailsActivity.class);
                intent.putExtra("item", item);
                startActivity(intent);
            }
        });

        // Inside onCreate method or appropriate initialization method
        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.enableDatePicker(false);
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here for this context
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed here for this context
            }

            @Override
            public void afterTextChanged(Editable s) {
                // As the user types or clears text in the EditText
                itemAdapter.getFilter().filter(s.toString());
            }
        });

        ImageView addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        manageTagsButton = findViewById(R.id.manage_tags_button);
        manageTagsButton.setOnClickListener(v -> {
            if (isSelectMode) {
                TagDialogHelper.showTagSelectionDialog(this, tagList, new ArrayList<>(), // Empty list for pre-selected tags
                        new TagDialogHelper.OnTagsSelectedListener() {
                            @Override
                            public void onTagsSelected(List<String> selectedTags) {
                                // Update each selected item with these tags
                                for(Item item : itemAdapter.getSelectedItems()) {
                                    Set<String> mergedTags = new HashSet<>(item.getTags());
                                    mergedTags.addAll(selectedTags); // Add all selected tags, duplicates are automatically handled
                                    item.setTags(new ArrayList<>(mergedTags)); // Convert back to List
                                    firestoreRepository.updateItem(item.getId(), item, new FirestoreRepository.OnItemUpdatedListener() {
                                        @Override
                                        public void onItemUpdated() {
                                        }
                                        @Override
                                        public void onError(Exception e) {
                                        }
                                    });
                                }
                                clearSelection();
                            }
                            @Override
                            public void onNewTagAdded(String newTag) {
                                // Handle the addition of the new tag, e.g., update Firestore
                                firestoreRepository.addTag(newTag, new FirestoreRepository.OnTagAddedListener() {
                                    @Override
                                    public void onTagAdded() {
                                        tagList.add(newTag); // Add the new tag to the local list
                                    }
                                    @Override
                                    public void onError(Exception e) {
                                    }
                                });
                            }
                        });
            }
        });

        ImageButton sortButton = findViewById(R.id.sort_button);
        sortButton.setOnClickListener(v -> {
                    sortDialog.show(getSupportFragmentManager(), "SortDialogFragment");
                });

        selectTextCancel = findViewById(R.id.select_text_cancel);
        selectTextCancel.setOnClickListener(v -> {
                    clearSelection();
        });


        deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> {
            deleteSelectedItems();
        });


        manageTagsButton.setVisibility(GONE);
        selectTextCancel.setVisibility(GONE);
        deleteButton.setVisibility(GONE); // Hide delete button initially

        // Clicking the profile button
        ImageView profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ViewProfileActivity.class);
            startActivity(intent);
        });

        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Image captured successfully
                        if (imageUri != null) {
                            processImageForBarcode(imageUri);
                        }
                    }
                });



        scanItemButton = findViewById(R.id.scan_item_button);
        scanItemButton.setOnClickListener(v -> {
            // Check for camera permission before launching the camera
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                imageUri = createImageFile(); // Ensure this method returns a valid Uri
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraActivityResultLauncher.launch(cameraIntent);
            } else {
                // Request camera permission if not granted
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            }
        });

        ImageView filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(v -> showFilterDialog());

    }

    /**
     * Called when the activity is resumed.
     * It fetches the items from the database and refreshes the adapter.
     */
    @Override
    protected void onResume() {
        super.onResume();
        fetchItemsAndRefreshAdapter();
    }




    /**
     * Displays the filter dialog fragment.
     */
    public void showFilterDialog() {
        FilterDialogFragment dialogFragment = new FilterDialogFragment();
        dialogFragment.setFilterDialogListener(this);
        dialogFragment.show(getSupportFragmentManager(), "FilterDialogFragment");
    }

    /**
     * Updates the total value text view with the sum of estimated values of all items.
     *
     * @param items The list of items whose values are to be summed.
     */
    private void updateTotalValue(List<Item> items) {
        double totalValue = 0;
        for (Item item : items) {
            totalValue += item.getEstimatedValue(); // Assuming getEstimatedValue() returns a double
        }

        TextView totalValueTextView = findViewById(R.id.totalValue);
        totalValueTextView.setText(this.getApplicationContext().getString(R.string.total_value, totalValue));
    }

    private boolean isRunningEspressoTest() {
        // Check for a system property that you will set in your test setup
        return "true".equals(System.getProperty("isRunningEspressoTest"));
    }

    /**
     * Initializes the activity with the required layout and sets up the item grid adapter.
     * It also configures click listeners for the item grid and other UI components.
     */
    private void initForTesting() {
        // Initialize components as needed for testing
        // This might include setting up dummy data or mocks
        // For example:
        firestoreRepository = new FirestoreRepository("testUser");
        itemGrid = findViewById(R.id.itemGrid);
        itemAdapter = new ItemAdapter(this, new ArrayList<>()); // Use a test adapter if necessary
        itemGrid.setAdapter(itemAdapter);
        // Other initializations...
    }

    /**
     * Checks if the application is running in debug mode.
     *
     * @return True if the application is running in debug mode, false otherwise.
     */
    private boolean isDebugMode() {
        return BuildConfig.DEBUG;
    }

    /**
     * Proceeds to the main activity after successful login.
     */
    private void fetchItemsAndRefreshAdapter() {
        // I changed this so that we use the FirestoreRepo class to handle the database - (vedant)
        firestoreRepository.fetchItems(new FirestoreRepository.OnItemsFetchedListener() {
            @Override
            public void onItemsFetched(List<Item> items) {
                itemAdapter.clear();
                itemAdapter.addAll(items);

                // Reapply filters and sorts
                itemAdapter.reapplyFilterAndSort();

                updateTotalValue(items);
                itemsFetched = true;
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Error getting documents: ", e);
            }
        });
    }


        /**
         * Deletes the selected items from the Firestore database and updates the UI accordingly.
         * It clears the selection mode after deletion is completed.
         */
    private void deleteSelectedItems() {
        Set<Integer> selectedPositions = itemAdapter.getSelectedPositions();
        List<String> itemIdsToDelete = new ArrayList<>();
        for (int position : selectedPositions) {
            Item item = itemAdapter.getItem(position);
            itemIdsToDelete.add(item.getId());
        }
        firestoreRepository.deleteItems(itemIdsToDelete, new FirestoreRepository.OnItemDeletedListener() {
            @Override
            public void OnItemsDeleted() {
                // Remove items from the adapter and refresh
                List<Integer> positions = new ArrayList<>(selectedPositions);
                Collections.sort(positions, Collections.reverseOrder());
                for (int position : positions) {
                    itemAdapter.removeItem(position);
                }
                clearSelection();
                itemAdapter.notifyDataSetChanged();
                updateTotalValue(itemAdapter.getItems());
                Toast.makeText(MainActivity.this, "Items deleted", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Error deleting items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Processes the image for barcodes using ML Kit.
     *
     * @param imageUri The URI of the image to process.
     */
    public void processImageForBarcode(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            processImageWithMLKit(this, bitmap);
        } catch (IOException e) {
            Log.e("CameraActivity", "Error processing barcode image", e);
        }
    }

    /**
     * Called when the user responds to a permission request.
     * It launches the camera if the permission is granted.
     *
     * @param requestCode  The request code passed in requestPermissions()
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, launch the camera
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                imageUri = createImageFile();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraActivityResultLauncher.launch(cameraIntent);
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Processes the image for barcodes using ML Kit.
     *
     * @param context The context of the activity.
     * @param bitmap  The bitmap of the image to process.
     */
    private void processImageWithMLKit(Context context, Bitmap bitmap) {
        BarcodeFetchInfo barcodeFetchInfo = new BarcodeFetchInfo();
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build();

            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        // Check if list of barcodes is not empty
                        if (!barcodes.isEmpty()) {
                            // Iterate through the barcodes
                            for (Barcode barcode : barcodes) {
                                // Get raw value of the barcode
                                String barcodeValue = barcode.getRawValue();
                                // Log or print the barcode value
                                Log.d("Barcode Value", "Barcode: " + barcodeValue);
                                try{
                                    barcodeFetchInfo.fetchProductDetails(barcodeValue, new BarcodeFetchInfo.OnProductFetchedListener() {
                                        @Override
                                        public void onProductFetched(Item item) {
                                            Intent intent = new Intent(context, AddItemActivity.class);
                                            intent.putExtra("item", item); // Assuming Item is Serializable
                                            context.startActivity(intent);
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e("Barcode Fetch", "Error fetching barcode", e);
                                }

                                // You can also handle the barcode value as needed
                                // For example, updating UI, calling a method, etc.
                            }
                        } else {
                            Log.d("Barcode Processing", "No barcodes found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors during processing
                        Log.e("Barcode Processing", "Error processing barcode", e);
                    });
        } catch (Exception e) {
            Log.e("Image Processing", "Error processing image", e);
        }
    }

    /**
     * Creates an image file in the external storage directory.
     *
     * @return The URI of the image file.
     */
    private Uri createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "Cliche" + timeStamp;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return imageUri;
    }


    /**
     * Initializes the activity with the required layout and sets up the item grid adapter.
     * It also configures click listeners for the item grid and other UI components.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the most recent data,
     *                           or null if it is the first time.
     */

    /**
     * Clears the relevant buttons whenever the selection needs to be cleared
     * including when the user explicity presses "cancel" on the selection
     */
    private void clearSelection(){
        isSelectMode = !isSelectMode; // Toggle select mode
        itemAdapter.clearSelection(); // Inform the adapter
        deleteButton.setVisibility(GONE);
        selectTextCancel.setVisibility(GONE);
        manageTagsButton.setVisibility(GONE);
    }

    /**
     * Called when the user selects a filter type in the filter dialog fragment.
     *
     * @param filterType The selected filter type.
     */
    @Override
    public void onFilterSelected(FilterDialogFragment.FilterType filterType) {
        // Set the current filter type in the adapter
        itemAdapter.setFilterType(filterType);

        if (filterType == FilterDialogFragment.FilterType.CLEAR) {
            itemAdapter.clearFilter();
            searchEditText.setText("");
            searchEditText.enableDatePicker(false);
        } else if (filterType == FilterDialogFragment.FilterType.BY_DATE) {
            searchEditText.enableDatePicker(true);
            CharSequence currentSearchText = searchEditText.getText();
            // Apply the filter with the current search text
            itemAdapter.getFilter().filter(currentSearchText.toString());
        } else {
            searchEditText.enableDatePicker(false);
            CharSequence currentSearchText = searchEditText.getText();
            // Apply the filter with the current search text
            itemAdapter.getFilter().filter(currentSearchText.toString());
        }
    }


}

