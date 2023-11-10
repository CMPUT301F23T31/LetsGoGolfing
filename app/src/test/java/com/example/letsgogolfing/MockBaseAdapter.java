package com.example.letsgogolfing;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MockBaseAdapter extends BaseAdapter {
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public void notifyDataSetChanged() {
        // Do nothing or provide your own implementation
    }
}
