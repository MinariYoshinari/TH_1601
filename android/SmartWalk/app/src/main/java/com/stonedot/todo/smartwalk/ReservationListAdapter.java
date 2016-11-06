package com.stonedot.todo.smartwalk;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by komatsu on 2016/10/30.
 */

public class ReservationListAdapter extends ArrayAdapter<Reservation> {

    private View mConvertView;
    private LayoutInflater mInflater;

    public ReservationListAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        setConvertView(convertView, parent);
        setItem(getItem(position));
        Log.d("ReservationListAdapter", "GetView");
        return mConvertView;
    }

    private void setConvertView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            mConvertView = mInflater.inflate(R.layout.reservation_list_item, parent, false);
            return;
        }
        mConvertView = convertView;
    }

    private void setItem(Reservation reservation) {
        ((TextView) mConvertView.findViewById(R.id.reservation_sender)).setText(reservation.getSender());
        ((TextView) mConvertView.findViewById(R.id.reservation_content)).setText(reservation.getContent());
        ((TextView) mConvertView.findViewById(R.id.reservation_time))
                .setText(reservation.getTime(getContext().getString(R.string.notification_time_format)));
    }
}
