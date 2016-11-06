package com.stonedot.todo.smartwalk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LatestNotificationFragment extends Fragment {

    private View mFragment;
    private TextView mNoLatestNotification;
    private ImageView mSNS;
    private TextView mSender;
    private TextView mContent;
    private TextView mTime;
    private String mTimeFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_latest_notification, container, false);
        mTimeFormat = getActivity().getString(R.string.notification_time_format);
        findViews();
        return mFragment;
    }

    private void findViews() {
        mNoLatestNotification = (TextView) mFragment.findViewById(R.id.no_latest_notification);
        mSNS = (ImageView) mFragment.findViewById(R.id.latest_sns_image);
        mSender = (TextView) mFragment.findViewById(R.id.latest_sender);
        mContent = (TextView) mFragment.findViewById(R.id.latest_content);
        mTime = (TextView) mFragment.findViewById(R.id.latest_time);
    }

    public void displayLatestNotification(SNS sns, String sender, String content) {
        mNoLatestNotification.setVisibility(View.INVISIBLE);
        mSNS.setImageResource(snsIcon(sns));
        mSender.setText(sender);
        mContent.setText(content);
        mTime.setText(formattedTime());
    }

    private int snsIcon(SNS sns) {
        switch (sns) {
            case LINE: return R.mipmap.line_icon;
            default: return 0;
        }
    }

    private String formattedTime() {
        return new SimpleDateFormat(mTimeFormat).format(new Date());
    }
}
