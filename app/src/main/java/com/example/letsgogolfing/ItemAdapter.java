package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.Formatters.decimalFormat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemAdapter extends BaseAdapter {

    private Context context;
    private List<Item> items;
    private LayoutInflater inflater;

    private String currentUsername;


    private boolean isSelectModeEnabled = false;
    private Set<Integer> selectedItems = new HashSet<>();

    public Set<Integer> getSelectedPositions() {
        return new HashSet<>(selectedItems);
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    // generate javadocs

    /**
     * Constructor for the ItemAdapter class.
     *
     * @param context The context of the activity that is using the adapter.
     * @param items   The list of items to be displayed.
     */
    public ItemAdapter(Context context, List<Item> items, String currentUsername) {
        this.context = context;
        this.items = items;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.currentUsername = currentUsername;
    }


    // generate javadocs for setSelectModeEnabled

    /**
     * Sets whether or not the adapter should be in select mode.
     *
     * @param enabled True if the adapter should be in select mode, false otherwise.
     */
    public void setSelectModeEnabled(boolean enabled) {
        isSelectModeEnabled = enabled;
        selectedItems.clear(); // Clear selections when toggling mode
        notifyDataSetChanged();
    }


    // generate javadocs for toggleSelection
    /**
     * Toggles the selection of the item at the given position.
     *
     * @param position The position of the item to toggle.
     */

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyDataSetChanged();
    }




    // get all items
    /**
     * Retrieves the list of items.
     *
     * @return The list of items.
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Get count of items.
     *
     */
    @Override
    public int getCount() {
        return items.size();
    }

    /**
     * Get item at position.
     *
     * @param position The position of the item to retrieve.
     */
    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    /**
     * Get item id at position.
     *
     * @param position The position of the item to retrieve the id of.
     */
    @Override
    public long getItemId(int position) {
        return position; // Assuming items don't have unique IDs
    }

    /**
     * Get view at position.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.itemName);
            holder.descriptionTextView = convertView.findViewById(R.id.itemDescription);
            holder.valueTextView = convertView.findViewById(R.id.itemValue);
            holder.imageView = convertView.findViewById(R.id.itemImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = getItem(position);
        holder.nameTextView.setText(item.getName());
        holder.descriptionTextView.setText(item.getDescription());

        holder.valueTextView.setText(context.getString(R.string.item_value, decimalFormat.format(item.getEstimatedValue())));

        // Set other properties to the holder's views as needed

        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUri())
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.default_image); // Set a default image if no URI is available
        }

        // Change background color if selected
        if (selectedItems.contains(position)) {
            convertView.setBackgroundColor(Color.parseColor("#5E716A")); // color for selected items
        } else {
            convertView.setBackgroundColor(Color.parseColor("#88CEB4")); // Original background color
        }

        return convertView;
    }


    /**
     * Update items.
     *
     * @param newItems The new list of items to update the adapter with.
     */
    public void updateItems(List<Item> newItems) {
        List<Item> filteredItems = new ArrayList<>();
        for (Item item : newItems) {
            if (item.getUsername() != null && item.getUsername().equals(currentUsername)) {
                filteredItems.add(item);
            }
        }
        this.items.clear();
        this.items.addAll(filteredItems);
        notifyDataSetChanged();
    }


    /**
     * Remove item at position.
     *
     * @param position The position of the item to remove.
     */
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
        }
    }


    // ViewHolder pattern to optimize performance
    private static class ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView valueTextView;
        ImageView imageView;
        // Add more views as needed
    }
}
