package com.example.letsgogolfing.controllers.dialogs;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;

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

import com.example.letsgogolfing.R;

import java.util.Calendar;
import java.util.Date;

/**
 * DialogFragment for selecting a filter to apply to the list of items.
 */
public class FilterDialogFragment extends DialogFragment {
    private static FilterType lastSelectedFilterType = null;
    private FilterType selectedFilterType;

    /**
     * Interface for the callback to be invoked when a filter is selected.
     */
    public interface FilterDialogListener {
        void onFilterSelected(FilterType filterType);
    }

    /**
     * Enum representing the different types of filters that can be applied.
     */
    public enum FilterType {
        BY_DESCRIPTOR, BY_TAGS, BY_MAKE, BY_DATE, BY_NAME, CLEAR
    }

    private FilterDialogListener listener;

    /**
     * Creates a new instance of FilterDialogFragment.
     *
     * @return A new instance of FilterDialogFragment.
     */
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

        builder.setView(view)
                .setPositiveButton("Apply", (dialog, id) -> {
                    if (listener != null) {
                            listener.onFilterSelected(selectedFilterType);

                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                })
                .setNeutralButton("Clear Filter", (dialog, id) -> {
                    listener.onFilterSelected(FilterType.CLEAR);
                });

        return builder.create();
    }

    /**
     * Sets the listener for the dialog.
     *
     * @param listener The listener to be set.
     */
    public void setFilterDialogListener(FilterDialogListener listener) {
        this.listener = listener;
    }

    /**
     * Called when the fragment is attached to the activity.
     *
     * @param context The context of the activity.
     */
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
