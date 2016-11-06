package com.stonedot.todo.smartwalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements
        SpeechToTextListenerImpl.SpeechToTextListener,
        TextToSpeechProgressListener.TextToSpeechListener,
        SmartWalkGuidance.GuidanceListener,
        ReservationListFragment.ReservationListListener {

    private TextToSpeechManager mTTS;
    private SpeechToTextManager mSTT;

    private SmartWalkGuidance mGuidance;

    private FragmentManager mFM;
    private ReservationListFragment mReservationListFragment;

    private LINEBroadcastReceiver mLINEReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // フラグメント関係
        mFM = getSupportFragmentManager();
        mReservationListFragment = (ReservationListFragment) mFM.findFragmentById(R.id.fragment_reservation_list);

        // 音声関連のマネージャー
        mTTS = new TextToSpeechManager(this, this);
        mSTT = new SpeechToTextManager(this, this);

        // ガイダンス
        mGuidance = new SmartWalkGuidance(this, this, mTTS, mSTT);

        // SNS通知関係
        NotificationServiceAccess.showNotificationAccessSettingMenu(this);

        mLINEReceiver = new LINEBroadcastReceiver(this, mGuidance);
    }

    @Override
    public void onTextToSpeechFinished(Guide guide) {

        mGuidance.nextGuide(guide);
    }

    @Override
    public void onGetTextFromSpeech(String text, Guide guide) {
        mGuidance.nextGuide(guide, text);
    }

    @Override
    public void onGetTextFromSpeechFailed(Guide guide) {
        mGuidance.nextGuide(guide);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTTS.shutdown();
        mSTT.shutdown();
    }

    @Override
    public void onReserve(Reservation reservation) {
        mReservationListFragment.add(reservation);
    }

    @Override
    public void onItemClicked(Reservation reservation) {
        mGuidance.setLatestReservation(reservation);
        mGuidance.nextGuide(Guide.DecideReply, getString(R.string.decide_reply_word));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.line_login:
                new LINELoginPage(this).openLoginPage();
                break;
            case R.id.line_friend_list:
                Intent intent = new Intent(getApplicationContext(), LINEFriendListActivity.class);
                startActivity(intent);
                break;
            case R.id.about:
                new AboutDialogFragment().show(mFM, getString(R.string.app_name));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
