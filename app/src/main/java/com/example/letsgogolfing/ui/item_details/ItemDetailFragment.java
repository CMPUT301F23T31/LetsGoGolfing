package com.example.letsgogolfing.ui.item_details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.letsgogolfing.databinding.FragmentItemDetailBinding;
import com.example.letsgogolfing.model.Item;

public class ItemDetailFragment extends Fragment {
    private FragmentItemDetailBinding binding;
    private ItemDetailViewModel viewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ItemDetailViewModel.class);

        viewModel.setSelectedItem(item);

        viewModel.getSelectedItem().observe(getViewLifecycleOwner(), this::displayItemDetails);
    }

    private void displayItemDetails(Item item) {
        binding.nameText.setText(item.getName());
        binding.descriptionText.setText((item.getDescription()));
        binding.dateText.setText(item.getDateOfPurchase());
        binding.modelText.setText(item.getModel());
        binding.makeText.setText(item.getMake());
        binding.valueText.setText(item.getEstimatedValue());
        binding.serialText.setText(item.getSerialNumber());
        binding.commentText.setText(item.getComment());
        binding.tagsText.setText(item.getTags());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
