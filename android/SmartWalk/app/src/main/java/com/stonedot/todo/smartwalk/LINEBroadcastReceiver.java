package com.stonedot.todo.smartwalk;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Formatter;

/**
 * Created by komatsu on 2016/10/29.
 */

public class LINEBroadcastReceiver {

    public final String SENDER_NAME_LINE = "LINE";
    public final String SENDER_NAME_SMART_WALK = "SmartWalk";
    public final String SMART_WALK_SEPARATOR = ":";

    private AppCompatActivity mActivity;
    private SmartWalkGuidance mGuidance;
    private LatestNotificationFragment mLINEFragment;

    private String mLastText = "";
    private String mSender = "";
    private String mContent = "";

    public LINEBroadcastReceiver(AppCompatActivity activity, SmartWalkGuidance guidance) {
        mActivity = activity;
        mGuidance = guidance;
        FragmentManager fm = mActivity.getSupportFragmentManager();
        mLINEFragment = (LatestNotificationFragment) fm.findFragmentById(R.id.fragment_line);
        notificationServiceStart();
    }

    private void notificationServiceStart() {
        Intent intent = new Intent(mActivity, LINENotificationService.class);
        mActivity.startService(intent);
        IntentFilter filter = new IntentFilter(LINENotificationService.PACKAGE_NAME);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(onNotice, filter);
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSender = intent.getStringExtra(LINENotificationService.KEY_SENDER);
            mContent = intent.getStringExtra(LINENotificationService.KEY_CONTENT);
            receiveNotification();
        }
    };

    private void receiveNotification() {
        if(mSender.equals(SENDER_NAME_SMART_WALK)) receiveSmartWalkMessage();
        if(mSender.equals(SENDER_NAME_LINE)) return;

        Formatter fm = new Formatter();
        fm.format(mActivity.getString(R.string.line_sender_format) + mContent, mSender);
        String text = fm.toString();

        if(isSameNotification(text)) return;

        mGuidance.setLatestReservation(new Reservation(SNS.LINE, mSender, mContent, new Date()));
        if(mGuidance.isWorking()) mGuidance.cancelGuide();
        mGuidance.nextGuide(Guide.Notification, text);

        mLINEFragment.displayLatestNotification(SNS.LINE, mSender, mContent);
    }

    // Notification2回以上呼ばれるので対策
    private boolean isSameNotification(String text) {
        if(text.equals(mLastText))
        {
            mLastText = text;
            return true;
        }
        mLastText = text;
        return false;
    }

    private void receiveSmartWalkMessage() {
        String[] split = mContent.split(SMART_WALK_SEPARATOR);
        StringBuilder builder = new StringBuilder();
        mSender = split[0];
        split[0] = "";
        for(String str : split) builder.append(str);
        mContent = builder.toString();
    }
}
