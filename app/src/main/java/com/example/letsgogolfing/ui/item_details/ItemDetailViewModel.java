package com.example.letsgogolfing.ui.item_details;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.letsgogolfing.model.Item;

public class ItemDetailViewModel extends ViewModel {
    private final MutableLiveData<Item> selectedItem = new MutableLiveData<>();

    public void setSelectedItem(Item item) {
        selectedItem.setValue(item);
    }

    public LiveData<Item> getSelectedItem() {
        return selectedItem;
    }

    public void onViewPhoto() {

    }

    public void onEditItem() {

    }

    public void onBack() {

    }
}
