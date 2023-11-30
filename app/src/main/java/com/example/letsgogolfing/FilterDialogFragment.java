package com.example.letsgogolfing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class FilterDialogFragment extends DialogFragment {

    public interface FilterDialogListener {
        void onFilterSelected(boolean option1, boolean option2, boolean option3, boolean option4);
    }

    private FilterDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_dialog, null);

        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        final CalendarView calendarView = view.findViewById(R.id.calendarView);
        RadioButton radioButton1 = view.findViewById(R.id.radio_button1);
        RadioButton radioButton2 = view.findViewById(R.id.radio_button2);
        RadioButton radioButton3 = view.findViewById(R.id.radio_button3);
        RadioButton radioButton4 = view.findViewById(R.id.radio_button4);

        // Initially hide the calendar view
        calendarView.setVisibility(View.GONE);

        // Add listener to RadioGroup to toggle CalendarView visibility
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Log.d("FilterDialog", "Checked ID: " + checkedId);
            if (checkedId == R.id.radio_button4) {
                Log.d("FilterDialog", "Showing calendar view");
                calendarView.setVisibility(View.VISIBLE);
            } else {
                Log.d("FilterDialog", "Hiding calendar view");
                calendarView.setVisibility(View.GONE);
            }
        });


        builder.setView(view)
                .setPositiveButton("Apply", (dialog, id) -> {
                    boolean option1 = radioButton1.isChecked();
                    boolean option2 = radioButton2.isChecked();
                    boolean option3 = radioButton3.isChecked();
                    boolean option4 = radioButton4.isChecked();
                    listener.onFilterSelected(option1, option2, option3, option4);
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
