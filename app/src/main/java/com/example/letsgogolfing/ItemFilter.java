package com.example.letsgogolfing;

import android.widget.Filter;

import com.example.letsgogolfing.ItemAdapter;
import com.example.letsgogolfing.Item;
import java.util.ArrayList;
import java.util.List;

public class ItemFilter extends Filter {
    private final ItemAdapter adapter;
    private final List<Item> originalList;
    private final List<Item> filteredList;

    public ItemFilter(ItemAdapter adapter, List<Item> originalList) {
        this.adapter = adapter;
        this.originalList = new ArrayList<>(originalList);
        this.filteredList = new ArrayList<>();
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        filteredList.clear();
        final FilterResults results = new FilterResults();

        if (constraint == null || constraint.length() == 0) {
            filteredList.addAll(originalList);
        } else {
            final String filterPattern = constraint.toString().toLowerCase().trim();

            for (final Item item : originalList) {
                switch (adapter.currentFilterType) {
                    case BY_DESCRIPTOR:
                        if (item.getDescription().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                        break;
                    case BY_TAGS:
                        // Logic to filter by tags
                        for (String tag : item.getTags()) {
                            if (tag.toLowerCase().contains(filterPattern)) {
                                filteredList.add(item);
                                break; // Break to avoid adding the same item multiple times
                            }
                        }
                    case BY_MAKE:
                        // Add logic to filter by make
                        if (item.getMake() != null && item.getMake().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    case BY_DATE:
                        // Add logic to filter by date
                        break;
                }
            }
        }

        if (filteredList.isEmpty()) {
            results.values = new ArrayList<Item>(); // return an empty list instead of null
        } else {
            results.values = filteredList;
        }
        results.count = filteredList.size();
        return results;
    }



    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.clear();
        if (results.values != null) {
            adapter.addAll((ArrayList<Item>) results.values);
        }
        adapter.notifyDataSetChanged();
    }
}

