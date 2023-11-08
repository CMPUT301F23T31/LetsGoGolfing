package com.example.letsgogolfing.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.letsgogolfing.DataRepository;
import com.example.letsgogolfing.model.Item;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Item>> items;
    private final DataRepository repository;

    public HomeViewModel() {
        repository = DataRepository.getInstance();
        items = new MutableLiveData<>();
        List<Item> sampleItems = Arrays.asList(
                new Item("1",  "Laptop", new Date(), "Dell", "XPS 15", "123456", 1000.0, "My laptop", Arrays.asList("Electronics", "Office")),
                new Item("2",  "Phone", new Date(), "Apple", "iPhone 12", "789012", 800.0, "My phone", Arrays.asList("Electronics"))
        );

        items.setValue(sampleItems);
    }

    // Expose LiveData to observe the list of items.
    public LiveData<List<Item>> getItems() {
        return items;
    }

    // Update the list of items and notify observers.
    public void updateItems(List<Item> updatedItems) {
        items.setValue(updatedItems);
    }

    // Add a single item to the list and notify observers.
    public void addItem(Item item) {
        List<Item> currentItems = items.getValue();
        if (currentItems != null) {
            currentItems.add(item);
            items.setValue(currentItems);
        }
    }

    // Remove a single item from the list and notify observers.
    public void removeItem(Item item) {
        List<Item> currentItems = items.getValue();
        if (currentItems != null) {
            currentItems.remove(item);
            items.setValue(currentItems);
        }
    }

    // Load initial data or fetch data from a repository.
    private void loadInitialData() {
        // Replace this with actual data loading logic.
        // List<Item> initialItems = repository.loadItems();
        // items.setValue(initialItems);
    }
}