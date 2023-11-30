package com.example.letsgogolfing;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;

// Other imports that your class already uses

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import org.json.*;


public class CameraActivity extends AppCompatActivity {
    private static final int MY_CAMERA_PERMISSION_CODE = 420;
    private static final int GALLERY_PERMISSION_CODE = 240;
    private Uri imageUri;
    private SharedPreferences sharedPref;
    private String currentUsername;
    private FirestoreRepository firebase;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    public String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    public static final int MODE_PHOTO_CAMERA = 1;
    public static final int MODE_PHOTO_GALLERY = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        currentUsername = sharedPref.getString("username", null);
        firebase = new FirestoreRepository(currentUsername);

        int mode = getIntent().getIntExtra("mode", MODE_PHOTO_CAMERA);
        boolean BarcodeInfo = getIntent().getBooleanExtra("BarcodeInfo", false);
        Item item = (Item) getIntent().getSerializableExtra("item");

        activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = null;
                    if (mode == MODE_PHOTO_CAMERA) {
                        // This is a result from the camera
                        uri = imageUri;
                    } else if (mode == MODE_PHOTO_GALLERY && result.getData() != null) {
                        // This is a result from the gallery
                        uri = result.getData().getData();
                    }
        
                    if (BarcodeInfo && uri != null) {
                        /**Change back to processImageForBarcode(uri) when we have the barcode API working
                        processImageForBarcode(uri);*/
                        processImageForBarcodeMock();
                    } else if (uri != null) {
                        Log.d("Image URI", uri.toString());
        
                        // Handle the item intent content
                        handleItemIntentContent(item, uri);
                    } else {
                        // Uri is null
                        Toast.makeText(this, "No image was selected or captured!", Toast.LENGTH_SHORT).show();  
                }
            }
            }
        );
        try {
            launchCameraOrGallery(activityResultLauncher, mode); // Pass activityResultLauncher as a parameter
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void launchCameraOrGallery(ActivityResultLauncher<Intent> activityResultLauncher, int mode) throws IOException {
        if (mode == MODE_PHOTO_CAMERA) {
            launchCamera();
        } else if (mode == MODE_PHOTO_GALLERY) {
            openGallery();
        }
    }

    private void launchCamera() throws IOException {
         if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
        imageUri = createImageFile(); // Create a file to store the image
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activityResultLauncher.launch(cameraIntent);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(galleryIntent);
    }

    private void processImageForBarcode(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            processImageWithMLKit(this, bitmap);
        } catch (IOException e) {
            Log.e("CameraActivity", "Error processing barcode image", e);
        }
    }

    private void processImageForBarcodeMock() {
        // Get a reference to the image file in Firebase Storage
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("images/testImages/barcode.jpg");
    
        // Create a local file to store the image
        File localFile;
        try {
            localFile = File.createTempFile("barcode", "jpg");
        } catch (IOException e) {
            Log.e("CameraActivity", "Error creating local file", e);
            return;
        }
    
        // Download the image to the local file
        imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // The image has been downloaded to the local file
                // Now process the image
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                processImageWithMLKit(CameraActivity.this, bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e("CameraActivity", "Error downloading image", exception);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                activityResultLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Uri createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "Cliche" + timeStamp;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return imageUri;
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
                                    barcodeFetchInfo.fetchProductDetails(barcodeValue, item -> {
                                        handleItemIntentContent(item);
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

    private void handleItemIntentContent(Item item) {
        handleItemIntentContent(item, null);
    }

    private void handleItemIntentContent(Item item, Uri uri) {
        if (uri == null) {
            // If the uri is null, launch AddItemActivity with item made from BarcodeFetchInfo
            Intent intent = new Intent(this, AddItemActivity.class);
            intent.putExtra("item", item);
            startActivity(intent);
        } else {
            if (item == null) {
                // If the item is null, report error to log
                Log.e("CameraActivity", "Both item and Uri are null");
            } else {
                // Create the intent
                Intent intent;
                if (item.getId() == null) {
                    // If the item ID is null, launch AddItemActivity with the same item
                    intent = new Intent(CameraActivity.this, AddItemActivity.class);
                } else {
                    // If the item ID is not null, launch EditItemActivity with the same item
                    intent = new Intent(CameraActivity.this, EditItemActivity.class);
                }
                intent.putExtra("item", item);
                intent.putExtra("uri", uri.toString());
                // Use setResult to send the result back to the calling activity
                setResult(Activity.RESULT_OK, intent);
                finish(); // Finish CameraActivity to return to the calling activity
            }
        }
    }
}