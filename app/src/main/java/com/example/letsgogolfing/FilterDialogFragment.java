package com.example.letsgogolfing;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class FilterDialogFragment extends DialogFragment {



    public interface FilterDialogListener {
        void onFilterSelected(/* parameters to pass back selected filter options */boolean option1, boolean option2, boolean option3);
    }

    private FilterDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_dialog, null);

        // You can now find your views by ID
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        RadioButton radioButton1 = view.findViewById(R.id.radio_button1);
        RadioButton radioButton2 = view.findViewById(R.id.radio_button2);
        RadioButton radioButton3 = view.findViewById(R.id.radio_button3);

        // ... rest of your code

        builder.setView(view)
                .setPositiveButton("Apply", (dialog, id) -> {
                    // User clicked Apply button
                    boolean option1 = radioButton1.isChecked();
                    boolean option2 = radioButton2.isChecked();
                    boolean option3 = radioButton3.isChecked();
                    listener.onFilterSelected(option1, option2, option3);
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                });

        return builder.create();
    }



    // Override the onAttach to ensure that the host activity implements the callback interface
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (FilterDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FilterDialogListener");
        }
    }


}

