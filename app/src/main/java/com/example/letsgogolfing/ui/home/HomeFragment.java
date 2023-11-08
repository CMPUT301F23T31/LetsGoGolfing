package com.example.letsgogolfing.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.letsgogolfing.EditTagsActivity;
import com.example.letsgogolfing.R;
import com.example.letsgogolfing.databinding.FragmentHomeBinding;
import com.example.letsgogolfing.model.Item;
import com.example.letsgogolfing.model.ItemAdapter;
import com.example.letsgogolfing.ui.item.ItemFragment;
import com.example.letsgogolfing.ui.item.ItemViewModel;

import java.util.ArrayList;

/**
 * This displays the information managed by HomeViewModel
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModelHome;
    private ItemViewModel viewModelItem;
    private ItemAdapter adapter;

    /**
     * When the Fragment is displayed it inflates the view and observes the data from HomeViewModel
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view updated with information managed by HomeViewModel
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModelHome = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModelItem = new ViewModelProvider(this).get(ItemViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        adapter = new ItemAdapter(getContext(), new ArrayList<>());
        binding.listHome.setAdapter(adapter);
        viewModelHome.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.clear();
            adapter.addAll(items);
            adapter.notifyDataSetChanged();
        });

        binding.listHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item clickedItem = (Item) parent.getItemAtPosition(position);

                // Set the selected item in the ViewModel
                viewModelItem.updateItem(clickedItem);

                // Use the NavController to navigate to ItemFragment
                Navigation.findNavController(view).navigate(R.id.itemFragment);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}