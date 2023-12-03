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

import java.util.Calendar;

public class FilterDialogFragment extends DialogFragment {
    private static FilterType lastSelectedFilterType = null;
    private FilterType selectedFilterType;

    private static long selectedDateTimestamp = -1;

    public interface FilterDialogListener {
        void onFilterSelected(FilterType filterType);
        void onDateFilterSelected(long selectedDateTimestamp); // Add this to handle date filtering
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

        calendarView.setVisibility(View.GONE);// Initially hide the calendar view

        RadioButton radioButtonDescriptor = view.findViewById(R.id.radio_button_descriptor);
        RadioButton radioButtonTags = view.findViewById(R.id.radio_button_tags);
        RadioButton radioButtonMake = view.findViewById(R.id.radio_button_make);
        RadioButton radioButtonDate = view.findViewById(R.id.radio_button_date);

        if (lastSelectedFilterType != null) {
            if (lastSelectedFilterType == FilterType.BY_DESCRIPTOR) {
                radioButtonDescriptor.setChecked(true);
            } else if (lastSelectedFilterType == FilterType.BY_TAGS) {
                radioButtonTags.setChecked(true);
            } else if (lastSelectedFilterType == FilterType.BY_MAKE) {
                radioButtonMake.setChecked(true);
            } else if (lastSelectedFilterType == FilterType.BY_DATE) {
                radioButtonDate.setChecked(true);
                calendarView.setVisibility(View.VISIBLE); // Show calendar if date filter was last selected
            }
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Reset calendar visibility at the beginning
            calendarView.setVisibility(View.GONE);

            if (checkedId == R.id.radio_button_descriptor) {
                selectedFilterType = FilterType.BY_DESCRIPTOR;
            } else if (checkedId == R.id.radio_button_tags) {
                selectedFilterType = FilterType.BY_TAGS;
            } else if (checkedId == R.id.radio_button_make) {
                selectedFilterType = FilterType.BY_MAKE;
            } else if (checkedId == R.id.radio_button_date) {
                selectedFilterType = FilterType.BY_DATE;
                calendarView.setVisibility(View.VISIBLE); // Show calendar for date filter
            } else {
                selectedFilterType = null;
            }

            lastSelectedFilterType = selectedFilterType; // Update the last selected filter
        });

        // Handle CalendarView date selection
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDateTimestamp = calendar.getTimeInMillis(); // Save the selected date in milliseconds
        });

        builder.setView(view)
                .setPositiveButton("Apply", (dialog, id) -> {
                    if (listener != null) {
                        if (selectedFilterType == FilterType.BY_DATE && selectedDateTimestamp != -1) {
                            // If the date filter type is selected and a date has been picked, call onDateFilterSelected
                            listener.onDateFilterSelected(selectedDateTimestamp);
                        } else {
                            // For other filter types, call the existing onFilterSelected
                            listener.onFilterSelected(selectedFilterType);
                        }
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
            throw new ClassCastException(context + " must implement FilterDialogListener");
        }
    }
}
