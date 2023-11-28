package com.example.letsgogolfing;  

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

// Other imports that your class already uses

import com.bumptech.glide.Glide;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import org.json.*;


public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 2104;
    private static final int MY_CAMERA_PERMISSION_CODE = 420;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private Uri imageUri;

    public static final String MODE_KEY = "CameraMode";
    public static final int MODE_BARCODE = 0;
    public static final int MODE_PHOTO = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.captured_image);
        try {
            imageUri = createImageFile(); // Create a file to store the image
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int mode = getIntent().getIntExtra(MODE_KEY, -1);

        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (mode == MODE_PHOTO) {
                            // Handle the result for photo mode
                            imageView.setImageURI(imageUri);
                            Log.d("Image URI: FROM PHOTO ADD", imageUri.toString());

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("imageUri", imageUri.toString());
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else if (mode == MODE_BARCODE) {
                            // Handle the result for barcode mode
                            processImageForBarcode(imageUri);
                        }
                    }
                }
        );

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            launchCamera(mode); // Launch camera based on mode
        }
    }

    private void launchCamera(int mode) {
        if (mode == MODE_PHOTO || mode == MODE_BARCODE) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraActivityResultLauncher.launch(cameraIntent);
        }
    }

    private void processImageForBarcode(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            processImageWithMLKit(this, bitmap);
        } catch (IOException e) {
            Log.e("CameraActivity", "Error processing barcode image", e);
        }
    }

    private void uploadImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            String photoFileName = "photo_" + System.currentTimeMillis() + ".jpg";
            StorageReference imagesRef = storageRef.child("images/testImages/" + photoFileName);

            UploadTask uploadTask = imagesRef.putBytes(imageData);
            uploadTask.addOnFailureListener(exception -> {
                Log.e("Firebase Upload", "Upload failed", exception);
                Toast.makeText(this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
                // Here you can also update the newItem object with the URL of the uploaded image, if needed
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraActivityResultLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                Glide.with(this)
                        .load(imageUri)
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
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

    private void uploadImageBitmap(Uri imageURI, UUID itemId) {
        try {
            // Get the image data from the URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Convert the Bitmap to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // Create a reference to 'images/mountains.jpg'
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            String photoFileName = "photo_" + System.currentTimeMillis() + ".jpg";
            StorageReference imagesRef = storageRef.child("images/testImages/" + photoFileName);

            // Upload the byte array to Firebase Storage
            UploadTask uploadTask = imagesRef.putBytes(imageData);
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                Log.e("Firebase Upload", "Upload failed", exception);
                Toast.makeText(CameraActivity.this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                StorageMetadata metadata = taskSnapshot.getMetadata();
                Toast.makeText(CameraActivity.this, "Upload successful: " + metadata.getPath(), Toast.LENGTH_SHORT).show();
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //2 options for processing image for GTIN: Uri or Bitmap


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

    //for testing barcode detection
    private void downloadAndProcessImage(Context context, String imagePath) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            processImageWithMLKit(context, bitmap);
        }).addOnFailureListener(exception -> {
            Log.e("Firebase Storage", "Failed to download image", exception);
        });
    }

    //Rainforest API call, too messy json to get field values
    private void getProductInfo(String gtin) {
        OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS) // connect timeout
        .writeTimeout(20, TimeUnit.SECONDS) // write timeout
        .readTimeout(30, TimeUnit.SECONDS) // read timeout
        .build();

        String url = "https://api.rainforestapi.com/request?api_key=83D3CF6386FE423289847177DF2D3BDC&amazon_domain=amazon.com&type=product&gtin=" + gtin;

        Request request = new Request.Builder()
            .url(url)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    // Print the response to the log
                    Log.d("API Response", myResponse);
                }
            }
        });
    }

    //upcitemdb API call, works
    public void fetchProductDetails(String upc) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS) // connect timeout
        .writeTimeout(20, TimeUnit.SECONDS) // write timeout
        .readTimeout(30, TimeUnit.SECONDS) // read timeout
        .build();

        Request request = new Request.Builder()
                .url("https://api.upcitemdb.com/prod/trial/lookup?upc=" + upc)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    Log.d("JSON Data", jsonData);
                    try {
                        JSONObject Jobject = new JSONObject(jsonData);
                        JSONArray Jarray = Jobject.getJSONArray("items");

                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject object = Jarray.getJSONObject(i);
                            String brand = object.getString("brand");
                            String model = object.getString("model");
                            String category = object.getString("category");
                            List<String> tags = new ArrayList<>();
                            String[] categoryParts = category.split(" > ");
                            if (categoryParts.length == 1) {
                                tags.add(categoryParts[0]);
                            } else if (categoryParts.length > 1) {
                                tags.add(categoryParts[0]);
                                tags.add(categoryParts[categoryParts.length - 1]);
                            }
                            String title = object.getString("title");    
                            String upc = object.getString("upc");
                            String description = object.getString("description");
                            double lowestPrice = object.getDouble("lowest_recorded_price");
                            double highestPrice = object.getDouble("highest_recorded_price");
                            double averagePrice = (lowestPrice + highestPrice) / 2;

                            Log.d("Product Details", "Title: " + title + "\nBrand: " + brand + "\nModel: " + model + "\nAverage Price: " + averagePrice + "\nTags: " + tags + "\nUPC: " + upc + "\nDescription: " + description);
                            JSONArray offers = object.getJSONArray("offers");
                            for (int j = 0; j < offers.length(); j++) {
                                JSONObject offer = offers.getJSONObject(j);
                                Log.d("Offer Details", offer.getString("domain") + "\t" + offer.getString("title") + "\t" + offer.getString("price"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}