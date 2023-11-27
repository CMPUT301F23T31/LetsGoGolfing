package com.example.letsgogolfing;  

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 2104;
    private static final int MY_CAMERA_PERMISSION_CODE = 420;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.captured_image);
        try {
            imageUri = createImageFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageView.setImageURI(imageUri);
                        uploadImageBitmap(imageUri, UUID.randomUUID());
                        //processImageForGTIN(imageUri);
                    }
                }
        );

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraActivityResultLauncher.launch(cameraIntent);
        }
        downloadAndProcessImage("images/testImages/barcode.jpg");
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
    private void processImageForGTIN(Uri imageUri) {
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            processImageForGTIN(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void processImageForGTIN(Bitmap bitmap) {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();
    
        if(!barcodeDetector.isOperational()){
            Toast.makeText(getApplicationContext(), "Could not set up the detector!", Toast.LENGTH_SHORT).show();
            return;
        }
    
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
    
        for(int i = 0; i< barcodes.size(); i++){
            Barcode barcode = barcodes.valueAt(i);
            // Check the format of the barcode to ensure it's of type UPC or EAN (GTIN)
            if (barcode.format == Barcode.UPC_A || barcode.format == Barcode.UPC_E ||
                barcode.format == Barcode.EAN_8 || barcode.format == Barcode.EAN_13) {
                String gtin = barcode.rawValue;
                // Do something with the GTIN (barcode data)
                Log.d("GTIN", "Barcode data: " + gtin);
            }
        }
    }

    //for testing barcode detection
    private void downloadAndProcessImage(String imagePath) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            processImageForGTIN(bitmap);
        }).addOnFailureListener(exception -> {
            Log.e("Firebase Storage", "Failed to download image", exception);
        });
    }


}