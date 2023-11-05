package com.example.letsgogolfing;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Retrieve the item
        Item item = (Item) getIntent().getSerializableExtra("ITEM");

        // Display the item details
        // For example, if you have a TextView for item description:
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView nameTextView = findViewById(R.id.nameTextView);
        TextView dateTextView = findViewById(R.id.dateTextView);
        TextView modelTextView = findViewById(R.id.modelTextView);
        TextView makeTextView = findViewById(R.id.makeTextView);
        TextView valueTextView = findViewById(R.id.valueTextView);
        TextView commentTextView = findViewById(R.id.commentTextView);
        TextView serialTextView = findViewById(R.id.serialTextView);
        TextView tagsTextView = findViewById(R.id.tagsTextView);
        descriptionTextView.setText(item.getDescription());
        nameTextView.setText(item.getName());
        dateTextView.setText(item.getDateOfPurchase().toString());
        modelTextView.setText(item.getModel());
        makeTextView.setText(item.getMake());
        valueTextView.setText(String.valueOf(item.getEstimatedValue()));
        commentTextView.setText(item.getComment());
        serialTextView.setText(item.getSerialNumber());
        tagsTextView.setText(item.getTags().toString());


        // Similarly, set the image and other details...
    }
}

