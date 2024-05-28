package com.example.healthcare.ui.signup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthcare.R;

public class HintSpinnerAdapter extends ArrayAdapter<CharSequence> {

    private Context mContext;
    private int mResource;

    public HintSpinnerAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0; // The first item is the hint, disable it
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView tv = (TextView) view;
        if (position == 0) {
            tv.setTextColor(mContext.getResources().getColor(R.color.hint_color)); // Set hint color
        } else {
            tv.setTextColor(mContext.getResources().getColor(R.color.text_color)); // Set text color
        }
        return view;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView tv = (TextView) view;
        if (position == 0) {
            tv.setTextColor(mContext.getResources().getColor(R.color.hint_color)); // Set hint color
        } else {
            tv.setTextColor(mContext.getResources().getColor(R.color.text_color)); // Set text color
        }
        return view;
    }
}
