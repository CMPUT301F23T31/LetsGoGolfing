package com.example.letsgogolfing.ui.additem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.letsgogolfing.databinding.EditItemPageBinding;
import com.example.letsgogolfing.databinding.FragmentNotificationsBinding;
import com.example.letsgogolfing.databinding.FragmentProfileBinding;
import com.example.letsgogolfing.ui.notifications.NotificationsViewModel;


public class AddItemFragment extends Fragment {

    private EditItemPageBinding binding;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the edit_item_page.xml layout with the correct binding class
        binding = EditItemPageBinding.inflate(inflater, container, false);
        // You can setup your UI components here if needed
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
