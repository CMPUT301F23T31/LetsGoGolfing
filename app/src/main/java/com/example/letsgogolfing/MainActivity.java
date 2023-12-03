package com.example.letsgogolfing;


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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import static com.example.letsgogolfing.utils.Formatters.decimalFormat;

import java.util.Map;
import java.util.Set;


/**
 * The main activity class that serves as the entry point for the application.
 * It handles the display and interaction with a grid of items, allowing the user to
 * select and delete items, as well as adding new ones and viewing their details.
 */
public class MainActivity extends AppCompatActivity implements SortDialogFragment.SortOptionListener {

    private Uri imageUri;

    private String currentUsername;

    private static final int CAMERA_REQUEST = 2104;
    private static final int MY_CAMERA_PERMISSION_CODE = 420;

    // itemsFetched used for UI test to check if items are fetched from FireStore;
    public static boolean itemsFetched;
    private TextView selectTextCancel; // Add this member variable for the TextView
    private static final String TAG = "MainActivity";
    private GridView itemGrid;
    private ItemAdapter itemAdapter; // You need to create this Adapter class.

    private boolean isSelectMode = false;
    private ImageButton deleteButton;
    private ImageButton cancelButton;
    private ItemComparator comparator;
    private ImageView scanItemButton;
    private ArrayList<String>tagList;

    private FirestoreRepository firestoreRepository;

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private DialogFragment sortDialog = new SortDialogFragment();

    @Override
    public void onSortOptionSelected(String selectedOption, boolean sortDirection) {
        comparator = new ItemComparator(selectedOption, sortDirection);
        sortArrayAdapter(comparator);
    }

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

        initForTesting();
//        if (isDebugMode() || isRunningEspressoTest()) {
//            // Bypass login and directly initialize components
//            // Initialize with test or default data
//            initForTesting();
//        } else {
//            SharedPreferences sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//            String currentUsername = sharedPref.getString("username", null);
//            if (currentUsername == null || currentUsername.isEmpty()) {
//                // Redirect to LoginActivity
//                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(intent);
//                Toast.makeText(MainActivity.this, "Crashed in known location", Toast.LENGTH_SHORT).show();
//                finish();
//                return;
//            }
//
//            // Initialize FirestoreRepository with the current username
//            firestoreRepository = new FirestoreRepository(currentUsername);
//        }

        GetTags getTags = new GetTags(this, firestoreRepository);
        itemGrid = findViewById(R.id.itemGrid);
        itemAdapter = new ItemAdapter(this, new ArrayList<>());
        itemGrid.setAdapter(itemAdapter);

        fetchItemsAndRefreshAdapter();
        cancelButton = findViewById(R.id.cancel_button);

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
                    cancelButton.setVisibility(View.VISIBLE);
                }
            } else {
                // Document ID is null, handle this case
                Toast.makeText(MainActivity.this, "Cannot select item without an ID", Toast.LENGTH_SHORT).show();
            }

            return true; // True to indicate the long click was consumed
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelection();
            }
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

        ImageView addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        Button manageTagsButton = findViewById(R.id.manage_tags_button);
        manageTagsButton.setOnClickListener(v -> {
            if (isSelectMode) {
                getTags.showTagSelectionDialog(selectedTags -> {
                    Map<String, Object> update = new HashMap<>();
                    for(Item item : itemAdapter.getSelectedItems()) {
                        item.addTags(selectedTags);
                        firestoreRepository.updateItem(item.getId(), item, new FirestoreRepository.OnItemUpdatedListener() {
                                    @Override
                                    public void onItemUpdated() {
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                    }
                                }
                        );
                    }
                    clearSelection();
                }, tagList);
                return;
            }
            Intent intent = new Intent(MainActivity.this, ManageTagsActivity.class);
            startActivity(intent);
        });

        ImageButton sortButton = findViewById(R.id.sort_button);
        sortButton.setOnClickListener(v -> {
                    sortDialog.show(getSupportFragmentManager(), "SortDialogFragment");
                });

        selectTextCancel = findViewById(R.id.select_text_cancel);
        deleteButton = findViewById(R.id.delete_button);

        deleteButton.setVisibility(View.GONE); // Hide delete button initially
        deleteButton.setOnClickListener(v -> deleteSelectedItems());

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchItemsAndRefreshAdapter();
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
        totalValueTextView.setText(this.getApplicationContext().getString(R.string.item_value , decimalFormat.format(totalValue)));
    }

    private boolean isRunningEspressoTest() {
        // Check for a system property that you will set in your test setup
        return "true".equals(System.getProperty("isRunningEspressoTest"));
    }

    private void initForTesting() {
        // Initialize components as needed for testing
        // This might include setting up dummy data or mocks
        // For example:
        firestoreRepository = new FirestoreRepository("test");
        itemGrid = findViewById(R.id.itemGrid);
        itemAdapter = new ItemAdapter(this, new ArrayList<>()); // Use a test adapter if necessary
        itemGrid.setAdapter(itemAdapter);
        // Other initializations...
    }



    private boolean isDebugMode() {
        return BuildConfig.DEBUG;
    }

    /**
     * Fetches items from the Firestore database and updates the grid adapter.
     * It also updates the total value of all items displayed.
     */
    private void fetchItemsAndRefreshAdapter() {
        // I changed this so that we use the FirestoreRepo class to handle the database - (vedant)
        firestoreRepository.fetchItems(new FirestoreRepository.OnItemsFetchedListener() {
            @Override
            public void onItemsFetched(List<Item> items) {
                itemAdapter.updateItems(items);
                sortArrayAdapter(comparator);
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
            // Reset select mode
//            isSelectMode = false;
//            itemAdapter.setSelectModeEnabled(false);
//            deleteButton.setVisibility(View.GONE);

        });
    }

    public void processImageForBarcode(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            processImageWithMLKit(this, bitmap);
        } catch (IOException e) {
            Log.e("CameraActivity", "Error processing barcode image", e);
        }
    }

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
        deleteButton.setVisibility(View.GONE);
        selectTextCancel.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
    }

}
