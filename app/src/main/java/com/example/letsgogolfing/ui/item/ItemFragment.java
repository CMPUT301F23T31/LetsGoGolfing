package com.example.letsgogolfing.ui.item;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.letsgogolfing.EditTagsActivity;
import com.example.letsgogolfing.R;
import com.example.letsgogolfing.databinding.ViewItemBinding;
import com.example.letsgogolfing.model.Item;

/**
 * This displays the information managed by ItemViewModel
 */
public class ItemFragment extends Fragment {

    private ViewItemBinding binding;
    private ItemViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        binding = ViewItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Find the TextViews in the layout
        TextView itemNameTextView = root.findViewById(R.id.itemNameTextView);
        TextView itemDescriptionTextView = root.findViewById(R.id.itemDescriptionTextView);
        TextView itemTagsTextView = root.findViewById(R.id.itemTagsTextView);

        // Observe changes in the ItemViewModel
        viewModel.getItem().observe(getViewLifecycleOwner(), item -> {
            // Update the TextViews with item fields from the selected item
            if (item != null) {
                itemNameTextView.setText(item.getName());
                itemDescriptionTextView.setText(item.getDescription());
                itemTagsTextView.setText(String.join(", ", item.getTags()));
            }
        });

        binding.editTagsButton.setOnClickListener(v -> {
            // Navigate to the EditTagsActivity
            Intent intent = new Intent(getActivity(), EditTagsActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}






