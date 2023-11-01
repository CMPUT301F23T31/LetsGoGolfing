package com.example.letsgogolfing.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.letsgogolfing.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;

/**
 * This Custom ArrayAdapter manages an ArrayList of Items and updates the TextViews in content.xml
 */
public class ItemAdapter extends ArrayAdapter<Item> {
    private ArrayList<Item> items;
    private Context context;
    private static final DecimalFormat df = new DecimalFormat("#,###.##");
    private static final DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * This initializes the ItemAdapter
     * @param context
     * @param items Contains the Items of the ArrayList this Adapter manages
     */
    public ItemAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
        this.items = items;
        this.context = context;
    }

    /**
     * This updates the TextViews in content.xml for an item at index = position in the ArrayList
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return The updated view
     */
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.content, parent, false);
        }

        Item item = items.get(position);

        TextView itemName = view.findViewById(R.id.item_name_text);
        TextView itemDescription = view.findViewById(R.id.item_desc_text);
        TextView itemDateOfPurchase = view.findViewById(R.id.item_date_of_purchase_text);
        TextView itemMake = view.findViewById(R.id.item_make_text);
        TextView itemModel = view.findViewById(R.id.item_model_text);
        TextView itemSerialNumber = view.findViewById(R.id.item_serial_number_text);
        TextView itemEstimatedValue = view.findViewById(R.id.item_estimated_value_text);
        TextView itemComment = view.findViewById(R.id.item_comment_text);
        TextView itemTags = view.findViewById(R.id.item_tags_text);

        itemName.setText(item.getName());
        itemDescription.setText(item.getDescription());
        itemDateOfPurchase.setText(dtf.format(item.getDateOfPurchase()));
        itemMake.setText(item.getMake());
        itemModel.setText(item.getModel());
        itemSerialNumber.setText(item.getSerialNumber());
        itemEstimatedValue.setText("$"+df.format(item.getEstimatedValue()));
        itemComment.setText(item.getComment());
        itemTags.setText(String.join(", ", item.getTags()));

        return view;
    }
}
