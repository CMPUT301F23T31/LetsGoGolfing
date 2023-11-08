package com.example.letsgogolfing.ui.item;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.letsgogolfing.model.Item;

public class ItemViewModel extends ViewModel {

    private final MutableLiveData<Item> item;

    public ItemViewModel() {
        item = new MutableLiveData<>();
    }

    // Expose LiveData to observe the item data.
    public LiveData<Item> getItem() {
        return item;
    }

    // Update the item data and notify observers.
    public void updateItem(Item updatedItem) {
        item.setValue(updatedItem);
    }

    // Load initial data or fetch data from a repository.
    private void loadInitialData() {
        // Replace this with your actual data loading logic.
        // For example, you can fetch data from a repository or database.
        // Item initialItem = repository.loadItem();
        // item.setValue(initialItem);
    }
}