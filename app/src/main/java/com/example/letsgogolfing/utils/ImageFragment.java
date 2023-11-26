package com.example.letsgogolfing.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.letsgogolfing.R;

public class ImageFragment extends Fragment {

    private ImageView imageView;

    public static ImageFragment newInstance(byte[] imageData) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putByteArray("imageData", imageData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        imageView = view.findViewById(R.id.imageView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve the byte array from the arguments
        byte[] imageData = getArguments().getByteArray("imageData");

        // Decode the byte array into a Bitmap
        new LoadImageTask().execute(imageData);
    }

    private class LoadImageTask extends AsyncTask<byte[], Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(byte[]... params) {
            byte[] imageData = params[0];
            if (imageData != null) {
                // Decode the byte array into a Bitmap
                return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                // Set the Bitmap to the ImageView
                imageView.setImageBitmap(bitmap);
            } else {
                // Handle the case where the Bitmap is null (failed to decode)
                // You might want to show a placeholder image or an error message
            }
        }
    }
}