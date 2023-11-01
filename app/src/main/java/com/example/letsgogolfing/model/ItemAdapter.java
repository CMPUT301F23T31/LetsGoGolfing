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
 *
 */
public class ItemAdapter extends ArrayAdapter<Item> {
    private ArrayList<Item> items;
    private Context context;
    private static final DecimalFormat df = new DecimalFormat("#,###.##");
    private static final DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");

    public ItemAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
        this.items = items;
        this.context = context;
    }

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
        itemTags.setText(String.join(",", item.getTags()));

        return view;
    }


}
