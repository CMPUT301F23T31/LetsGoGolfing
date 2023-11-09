package com.example.letsgogolfing;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemAdapter extends BaseAdapter {

    private Context context;
    private List<Item> items;
    private LayoutInflater inflater;

    private boolean isSelectModeEnabled = false;
    private Set<Integer> selectedItems = new HashSet<>();

    public Set<Integer> getSelectedPositions() {
        return new HashSet<>(selectedItems);
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setSelectModeEnabled(boolean enabled) {
        isSelectModeEnabled = enabled;
        selectedItems.clear(); // Clear selections when toggling mode
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyDataSetChanged();
    }




    // get all items
    public List<Item> getItems() {
        return items;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; // Assuming items don't have unique IDs
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.itemName);
            holder.descriptionTextView = convertView.findViewById(R.id.itemDescription);
            holder.valueTextView = convertView.findViewById(R.id.itemValue);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = getItem(position);
        holder.nameTextView.setText(item.getName());
        holder.descriptionTextView.setText(item.getDescription());
        holder.valueTextView.setText(context.getString(R.string.item_value, item.getEstimatedValue()));

        // Change background color if selected
        if (selectedItems.contains(position)) {
            convertView.setBackgroundColor(Color.parseColor("#FFCDD2")); // Pink color for selected items
        } else {
            convertView.setBackgroundColor(Color.parseColor("#88CEB4")); // Original background color
        }

        return convertView;
    }


    public void updateItems(List<Item> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

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
        // Add more views as needed
    }
}
