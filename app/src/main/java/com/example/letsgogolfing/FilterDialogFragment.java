package com.example.letsgogolfing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class FilterDialogFragment extends DialogFragment {

    private FilterType selectedFilterType;

    public interface FilterDialogListener {
        void onFilterSelected(FilterType filterType);
    }

    public enum FilterType {
        BY_DESCRIPTOR, BY_TAGS, BY_MAKE, BY_DATE
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

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_button1) {
                selectedFilterType = FilterType.BY_DESCRIPTOR;
            } else if (checkedId == R.id.radio_button2) {
                selectedFilterType = FilterType.BY_TAGS;
            } else if (checkedId == R.id.radio_button3) {
                selectedFilterType = FilterType.BY_MAKE;
            } else if (checkedId == R.id.radio_button4) {
                selectedFilterType = FilterType.BY_DATE;
                calendarView.setVisibility(View.VISIBLE);
            } else {
                selectedFilterType = null;
            }
            calendarView.setVisibility(View.GONE);
        });

        builder.setView(view)
                .setPositiveButton("Apply", (dialog, id) -> {
                    if (listener != null) {
                        listener.onFilterSelected(selectedFilterType);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                });

        return builder.create();
    }

    public void setFilterDialogListener(FilterDialogListener listener) {
        this.listener = listener;
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
