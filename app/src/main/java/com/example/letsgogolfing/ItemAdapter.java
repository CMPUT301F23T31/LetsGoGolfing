package com.example.letsgogolfing;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;


import java.util.List;

public class ItemAdapter extends BaseAdapter {
    private Context context;
    private List<Item> items;
    private LayoutInflater inflater;

    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.itemImage);
            holder.descriptionView = convertView.findViewById(R.id.itemDescription);
            holder.valueView = convertView.findViewById(R.id.itemValue);
            holder.tagContainer = convertView.findViewById(R.id.tagContainer);
            holder.editButton = convertView.findViewById(R.id.editButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item currentItem = items.get(position);

        // Set item description and value
        holder.descriptionView.setText(currentItem.getDescription());
        holder.valueView.setText("Value: $" + currentItem.getEstimatedValue());

        // Set the image for the item (Placeholder for now)
        //holder.imageView.setImageResource(R.drawable.ic_placeholder);

        // Create tags dynamically
        holder.tagContainer.removeAllViews();
        for (String tag : currentItem.getTags()) {
            TextView tagView = new TextView(context);
            tagView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tagView.setText(tag);
            tagView.setTextColor(Color.WHITE);
            // Add more styling to tagView as required
            holder.tagContainer.addView(tagView);
        }

        // Set click listener for edit button if needed
        holder.editButton.setOnClickListener(view -> {
            // Implement edit functionality
        });

        return convertView;
    }

    // ViewHolder for caching views
    private static class ViewHolder {
        ImageView imageView;
        TextView descriptionView;
        TextView valueView;
        LinearLayout tagContainer;
        ImageView editButton;
    }
}

