package com.example.letsgogolfing.views;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Custom EditText for date input, which displays a DatePickerDialog when touched.
 * This class extends AppCompatEditText and is designed to handle date input
 * in a user-friendly way, by showing a date picker instead of the standard keyboard.
 */
public class DatePickerEditText extends AppCompatEditText {
    private Context context;

    /**
     * Constructor for DatePickerEditText.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public DatePickerEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setFocusableInTouchMode(false); // Disable keyboard
        setFocusable(false); // Make view non-focusable
    }

    /**
     * Handles the touch event by displaying a DatePickerDialog.
     * When the user touches the EditText, a DatePickerDialog will be shown
     * instead of the standard keyboard. If the EditText already contains a valid date,
     * the dialog will open with that date selected. Otherwise, it will open with
     * the current date.
     *
     * @param event The MotionEvent that triggered the touch event.
     * @return Returns the result of the superclass's onTouchEvent method.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Calendar calendar = Calendar.getInstance();

            // Check if there's already a date in the EditText
            String existingDate = getText().toString();
            if (!existingDate.isEmpty()) {
                try {
                    calendar.setTime(dateFormat.parse(existingDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                    // If parsing fails, calendar remains set to the current date
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        setText(dateFormat.format(selectedDate.getTime()));
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        return super.onTouchEvent(event);
    }
}
