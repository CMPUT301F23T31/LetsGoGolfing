package com.example.letsgogolfing;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;



import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
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
import java.util.List;
import static com.example.letsgogolfing.utils.Formatters.decimalFormat;
import java.util.Set;


/**
 * The main activity class that serves as the entry point for the application.
 * It handles the display and interaction with a grid of items, allowing the user to
 * select and delete items, as well as adding new ones and viewing their details.
 */
public class MainActivity extends AppCompatActivity {

    private Uri imageUri;

    private static final int CAMERA_REQUEST = 2104;
    private static final int MY_CAMERA_PERMISSION_CODE = 420;

    private TextView selectTextCancel; // Add this member variable for the TextView
    private static final String TAG = "MainActivity";
    private GridView itemGrid;
    private ItemAdapter itemAdapter; // You need to create this Adapter class.

    private boolean isSelectMode = false;
    private ImageButton selectButton;
    private ImageButton deleteButton;
    private ImageView scanItemButton;

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;

    ActivityResultLauncher<Intent> editItemActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // The item was added or updated, so refresh your list
                }
            });

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

        itemGrid = findViewById(R.id.itemGrid);
        String currentUsername = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("username", null);
        itemAdapter = new ItemAdapter(this, new ArrayList<>(), currentUsername);
        itemGrid.setAdapter(itemAdapter);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Item> items = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Item item = document.toObject(Item.class);
                    item.setId(document.getId()); // Make sure to set the document ID
                    items.add(item);
                }
                itemAdapter.updateItems(items); // Update your adapter with this list
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });

        itemGrid.setOnItemLongClickListener((parent, view, position, id) -> {
            Item item = itemAdapter.getItem(position);
            if (item != null && item.getId() != null) {
                // Proceed with deletion
                db.collection("items").document(item.getId()).delete()
                        .addOnSuccessListener(aVoid -> {
                            // Deletion successful, update UI
                            itemAdapter.removeItem(position); // You need to implement this method in your adapter
                            itemAdapter.notifyDataSetChanged();
                            updateTotalValue(itemAdapter.getItems());
                            Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Handle error
                            Toast.makeText(MainActivity.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Document ID is null, handle this case
                Toast.makeText(MainActivity.this, "Cannot delete item without an ID", Toast.LENGTH_SHORT).show();
            }

            return true; // True to indicate the long click was consumed
        });

        itemGrid.setOnItemClickListener((parent, view, position, id) -> {
            if (isSelectMode) {
                itemAdapter.toggleSelection(position); // Toggle item selection
            } else {
                // Existing code to show item details...
                Item item = itemAdapter.getItem(position);
                if( item != null && item.getId() != null) {
                    db.collection("items").document(item.getId()).get()
                            .addOnSuccessListener(aVoid -> {
                                Intent intent = new Intent(MainActivity.this, ViewDetailsActivity.class);
                                intent.putExtra("ITEM", item); // Make sure your Item class implements Serializable or Parcelable
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Error fetching item from database", Toast.LENGTH_SHORT).show();
                            });

                }
            }
        });




        ImageView addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            editItemActivityLauncher.launch(intent); // Use the launcher to start for result
        });

        Button manageTagsButton = findViewById(R.id.manage_tags_button);
        manageTagsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageTagsActivity.class);
            startActivity(intent);
        });


        selectTextCancel = findViewById(R.id.select_text_cancel);
        selectButton = findViewById(R.id.select_button);
        deleteButton = findViewById(R.id.delete_button);
        scanItemButton = findViewById(R.id.scan_item_button);

        deleteButton.setVisibility(View.GONE); // Hide delete button initially

        selectButton.setOnClickListener(v -> {
            isSelectMode = !isSelectMode; // Toggle select mode
            itemAdapter.setSelectModeEnabled(isSelectMode); // Inform the adapter
            deleteButton.setVisibility(isSelectMode ? View.VISIBLE : View.GONE); // Show or hide the delete button
            selectTextCancel.setText(isSelectMode ? "Cancel" : "Select"); // Update the text
        });

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


    /**
     * Fetches items from the Firestore database and updates the grid adapter.
     * It also updates the total value of all items displayed.
     */
    private void fetchItemsAndRefreshAdapter() {
        String currentUsername = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("username", null);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("items")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Item> newItems = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Item item = document.toObject(Item.class);
                            item.setId(document.getId());
                            if (item.getUsername() != null && item.getUsername().equals(currentUsername)) {
                                newItems.add(item);
                            }
                        }
                        itemAdapter.updateItems(newItems); // Update your adapter with the filtered list
                        updateTotalValue(newItems);
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }



    /**
     * Deletes the selected items from the Firestore database and updates the UI accordingly.
     * It clears the selection mode after deletion is completed.
     */
    private void deleteSelectedItems() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Use the getSelectedPositions method to get the set of selected item positions
        Set<Integer> selectedPositions = itemAdapter.getSelectedPositions();
        for (int position : selectedPositions) {
            Item item = itemAdapter.getItem(position);
            batch.delete(db.collection("items").document(item.getId()));
        }

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove items from the adapter and refresh
                List<Integer> positions = new ArrayList<>(selectedPositions); // Create a list from the set
                // Sort the positions in reverse order before removing items
                Collections.sort(positions, Collections.reverseOrder());
                for (int position : positions) {
                    itemAdapter.removeItem(position);
                }
                itemAdapter.clearSelection();
                itemAdapter.notifyDataSetChanged();
                updateTotalValue(itemAdapter.getItems());
                Toast.makeText(MainActivity.this, "Items deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Error deleting items", Toast.LENGTH_SHORT).show();
            }
            // Reset select mode
            isSelectMode = false;
            itemAdapter.setSelectModeEnabled(false);
            deleteButton.setVisibility(View.GONE);
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
        BarcodeScannerActivity barcodeScannerActivity = new BarcodeScannerActivity();
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



}
