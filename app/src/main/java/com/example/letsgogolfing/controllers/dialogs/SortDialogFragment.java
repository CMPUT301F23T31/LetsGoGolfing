package com.example.letsgogolfing.controllers.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.letsgogolfing.R;

/**
 * A dialog fragment that provides options for sorting items.
 * This fragment presents a list of sorting options and directions (ascending or descending)
 * to the user in the form of radio buttons. The user's selection is communicated back to
 * the hosting activity or fragment through the {@code SortOptionListener} interface.
 */
public class SortDialogFragment extends DialogFragment {
    private int checkedItem = -1; // No item is selected by
    private static int lastSelectedOption = -1; // Persist last checked sort option
    private static boolean lastSelectedDirection = true; // Persist last sort direction, default to true (ascending)
    private SortOptionListener mListener;

    /**
     * Called when the fragment is first attached to its context.
     * Ensures that the hosting activity or fragment implements the {@code SortOptionListener} interface.
     * @param context The context to which this fragment is attached.
     * @throws ClassCastException if the context does not implement {@code SortOptionListener}.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (SortOptionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SortOptionsListener");
        }
    }

    /**
     * Called to create the dialog and its content.
     * Inflates the layout for the dialog, dynamically adds radio buttons for sort options,
     * and sets up the positive and negative buttons.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     * @return The newly created dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Inflate and set the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.sort_dialog, null);

        final RadioGroup sortOptionsGroup = view.findViewById(R.id.sort_options_group);
        final RadioGroup sortDirectionGroup = view.findViewById(R.id.sort_direction_group);

        // Dynamically add radio buttons for sort options
        String[] sortOptions = getResources().getStringArray(R.array.sort_options);
        RadioButton lastSelectedRadioButton = null;
        for (int i = 0; i < sortOptions.length; i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(sortOptions[i]);
            radioButton.setId(View.generateViewId());
            sortOptionsGroup.addView(radioButton);

            if (i == lastSelectedOption) {
                radioButton.setChecked(true);
            }

        }

        // Assuming the first child in sortDirectionGroup is Ascending and the second is Descending
        if (sortDirectionGroup.getChildCount() >= 2) {
            ((RadioButton) sortDirectionGroup.getChildAt(0)).setChecked(lastSelectedDirection);
            ((RadioButton) sortDirectionGroup.getChildAt(1)).setChecked(!lastSelectedDirection);
        }

        builder.setView(view)
                .setTitle(R.string.dialog_sort)
                .setPositiveButton(R.string.confirm, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }

    /**
     * Called when the fragment's dialog is started.
     * Overrides the behavior of the positive button in the dialog to prevent automatic dismissal
     * upon clicking and to handle the click event manually.
     */
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleConfirmClick(dialog);
                }
            });
        }
    }

    /**
     * Handles the click event of the confirm button in the dialog.
     * Checks which sorting option and direction are selected and notifies the listener.
     * If no sorting option is selected, a toast message is displayed and the dialog is not dismissed.
     * @param dialog The dialog where the sorting options are presented.
     */
    private void handleConfirmClick(AlertDialog dialog) {
        RadioGroup sortOptionsGroup = dialog.findViewById(R.id.sort_options_group);
        RadioGroup sortDirectionGroup = dialog.findViewById(R.id.sort_direction_group);

        int selectedOptionId = sortOptionsGroup.getCheckedRadioButtonId();
        int selectedDirectionId = sortDirectionGroup.getCheckedRadioButtonId();

        boolean sortDirection = false;
        if (selectedDirectionId != -1) {
            String directionText = ((RadioButton) dialog.findViewById(selectedDirectionId)).getText().toString().toLowerCase();
            sortDirection = directionText.equals("ascending");
        }
        // Save the index of the selected sort option for next time
        for (int i = 0; i < sortOptionsGroup.getChildCount(); i++) {
            if (((RadioButton)sortOptionsGroup.getChildAt(i)).isChecked()) {
                lastSelectedOption = i;
                break;
            }
        }
        // Save the selected sort direction
        lastSelectedDirection = sortDirection;

        // If a sort option is not selected do not dismiss dialog
        if (selectedOptionId == -1) {
            Toast.makeText(getContext(), "Please select a sort option.", Toast.LENGTH_SHORT).show();
        } else { // If a sort option is selected call listener and dismiss dialog
            String selectedOption = ((RadioButton) dialog.findViewById(selectedOptionId)).getText().toString().toLowerCase();
            mListener.onSortOptionSelected(selectedOption, sortDirection);

            // Dismiss the dialog only if a sort option is selected
            dialog.dismiss();
        }
    }

    /**
     * Interface definition for a callback to be invoked when a sorting option is selected.
     */
    public interface SortOptionListener {
        /**
         * Called when a sort option is selected in the dialog.
         * @param selectedOption The sorting option chosen by the user.
         * @param sortDirection The sorting direction, {@code true} for ascending, {@code false} for descending.
         */
        void onSortOptionSelected(String selectedOption, boolean sortDirection);
    }
}

