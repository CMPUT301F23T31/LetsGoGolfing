package com.example.letsgogolfing;

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

public class SortDialogFragment extends DialogFragment {
    private int checkedItem = -1; // No item is selected by default
    private SortOptionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (SortOptionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SortOptionsListener");
        }
    }
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
        for (int i = 0; i < sortOptions.length; i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(sortOptions[i]);
            radioButton.setId(View.generateViewId());
            sortOptionsGroup.addView(radioButton);

            if (i == checkedItem) {
                radioButton.setChecked(true);
            }
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

    private void handleConfirmClick(AlertDialog dialog) {
        RadioGroup sortOptionsGroup = dialog.findViewById(R.id.sort_options_group);
        RadioGroup sortDirectionGroup = dialog.findViewById(R.id.sort_direction_group);

        int selectedOptionId = sortOptionsGroup.getCheckedRadioButtonId();
        int selectedDirectionId = sortDirectionGroup.getCheckedRadioButtonId();

        // Default sort direction to descending if not selected
        String selectedDirection = selectedDirectionId != -1 ?
                ((RadioButton) dialog.findViewById(selectedDirectionId)).getText().toString().toLowerCase():
                getString(R.string.descending).toLowerCase();

        // If a sort option is not selected do not dismiss dialog
        if (selectedOptionId == -1) {
            Toast.makeText(getContext(), "Please select a sort option.", Toast.LENGTH_SHORT).show();
        } else { // If a sort option is selected call listener and dismiss dialog
            String selectedOption = ((RadioButton) dialog.findViewById(selectedOptionId)).getText().toString().toLowerCase();
            mListener.onSortOptionSelected(selectedOption, selectedDirection);

            // Dismiss the dialog only if a sort option is selected
            dialog.dismiss();
        }
    }

    public interface SortOptionListener {
        void onSortOptionSelected(String selectedOption, String selectedDirection);
    }
}

