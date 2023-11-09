package com.example.letsgogolfing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class ItemAdapter extends BaseAdapter {

    private Context context;
    private List<Item> items;
    private LayoutInflater inflater;

    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            holder.nameTextView = convertView.findViewById(R.id.itemName); // Adjust this ID based on your grid_item.xml
            holder.descriptionTextView = convertView.findViewById(R.id.itemDescription); // Adjust this ID based on your grid_item.xml
            holder.valueTextView = convertView.findViewById(R.id.itemValue); // Adjust this ID based on your grid_item.xml
            // Add more views if necessary
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = getItem(position);
        holder.nameTextView.setText(item.getName());
        holder.descriptionTextView.setText(item.getDescription());
        holder.valueTextView.setText(context.getString(R.string.item_value, item.getEstimatedValue()));
        // Set other properties to the holder's views as needed

        return convertView;
    }

    public void updateItems(List<Item> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    // ViewHolder pattern to optimize performance
    private static class ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView valueTextView;
        // Add more views as needed
    }
}
