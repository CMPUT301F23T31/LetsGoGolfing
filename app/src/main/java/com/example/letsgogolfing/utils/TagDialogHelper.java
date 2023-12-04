package com.example.letsgogolfing.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.letsgogolfing.R;

import java.util.ArrayList;
import java.util.List;

public class TagDialogHelper {

    public interface OnTagsSelectedListener {
        void onTagsSelected(List<String> selectedTags);

        void onNewTagAdded(String newTag);
    }

    public static void showTagSelectionDialog(Context context, List<String> allTags,
                                              List<String> selectedTags,
                                              OnTagsSelectedListener listener) {
        // Build and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Tags");

        // Main layout for the dialog
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // ScrollView for tags
        ScrollView scrollView = new ScrollView(context);
        // Limiting the height of ScrollView
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                context.getResources().getDimensionPixelSize(R.dimen.tag_scroll_height))); // Adjust this value as needed in your dimens.xml


        LinearLayout tagsLayout = new LinearLayout(context);
        tagsLayout.setOrientation(LinearLayout.VERTICAL);


        // Populate tagsLayout with CheckBoxes
        List<CheckBox> checkBoxes = new ArrayList<>();
        for (String tag : allTags) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(tag);
            checkBox.setChecked(selectedTags.contains(tag));
            tagsLayout.addView(checkBox);
            checkBoxes.add(checkBox);
            // Disable unchecked checkboxes if the limit is reached
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getSelectedCount(checkBoxes) >= 4) {
                    for (CheckBox cb : checkBoxes) {
                        if (!cb.isChecked()) {
                            cb.setEnabled(false);
                        }
                    }
                } else {
                    for (CheckBox cb : checkBoxes) {
                        cb.setEnabled(true);
                    }
                }
            });
        }
        scrollView.addView(tagsLayout);
        mainLayout.addView(scrollView);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Add new tag");
        mainLayout.addView(input);

        final Button addTagButton = new Button(context);
        addTagButton.setText("Add Tag");
        mainLayout.addView(addTagButton);

        builder.setView(mainLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Update selectedTags based on CheckBoxes
            selectedTags.clear();
            for (int i = 0; i < tagsLayout.getChildCount(); i++) {
                CheckBox checkBox = (CheckBox) tagsLayout.getChildAt(i);
                if (checkBox.isChecked()) {
                    selectedTags.add(checkBox.getText().toString());
                }
            }
            listener.onTagsSelected(selectedTags);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        addTagButton.setOnClickListener(v -> {
            String newTag = input.getText().toString().trim();
            if (!newTag.isEmpty() && !allTags.contains(newTag)) {
                listener.onNewTagAdded(newTag);
                input.setText("");
                allTags.add(newTag);
                // Refresh the dialog to include the new tag
                dialog.dismiss();
                showTagSelectionDialog(context, allTags, selectedTags, listener); // Re-open the dialog with updated tag list
            }
        });

        dialog.show();
    }




    private static int getSelectedCount(List<CheckBox> checkBoxes) {
        int count = 0;
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                count++;
            }
        }
        return count;
    }
}