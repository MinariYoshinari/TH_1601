package com.stonedot.todo.smartwalk;

import android.app.Activity;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import static com.stonedot.todo.smartwalk.Guide.ConfirmReply;
import static com.stonedot.todo.smartwalk.Guide.ConfirmSend;
import static com.stonedot.todo.smartwalk.Guide.DecideReply;
import static com.stonedot.todo.smartwalk.Guide.Finish;
import static com.stonedot.todo.smartwalk.Guide.GetAnswerConfirmReply;
import static com.stonedot.todo.smartwalk.Guide.GetAnswerConfirmSend;
import static com.stonedot.todo.smartwalk.Guide.RepeatReply;
import static com.stonedot.todo.smartwalk.Guide.Reserve;
import static com.stonedot.todo.smartwalk.Guide.Send;
import static com.stonedot.todo.smartwalk.Guide.StartReply;

/**
 * Created by komatsu on 2016/10/29.
 */

public class SmartWalkGuidance {

    public interface GuidanceListener {
        void onReserve(Reservation reservation);
    }
    private GuidanceListener mListener;

    private Activity mActivity;
    private TextToSpeechManager mTTS;
    private SpeechToTextManager mSTT;
    private Reservation latestReservation;
    private String mMessage;

    public SmartWalkGuidance(Activity activity, GuidanceListener listener, TextToSpeechManager tts, SpeechToTextManager sst) {
        mActivity = activity;
        mListener = listener;
        mTTS = tts;
        mSTT = sst;
    }

    public void setLatestReservation(Reservation reservation) { latestReservation = reservation; }

    public void nextGuide(Guide guide, String text) {
        if(guide == null) return;
        Log.d("nextGuide", guide.toString());
        switch (guide) {
            case Notification:
                mTTS.textToSpeech(text, ConfirmReply);
                break;

            case ConfirmReply:
                // 通知を最後まで読み切っていること
                mTTS.textToSpeech(t(R.string.guide_confirm_reply), GetAnswerConfirmReply);
                break;

            case GetAnswerConfirmReply:
                mSTT.speechToText(DecideReply);
                break;

            case DecideReply:
                if(!text.equals(t(R.string.decide_reply_word))) {
                    mTTS.textToSpeech(t(R.string.guide_reserve), Reserve);
                    break;
                }
                mTTS.textToSpeech(t(R.string.guide_input_message), StartReply);
                break;

            case StartReply:
                mSTT.speechToText(RepeatReply);
                break;

            case RepeatReply:
                if(text == null || text.isEmpty()) {
                    mTTS.textToSpeech(t(R.string.guide_input_message_failed), ConfirmReply);
                    break;
                }
                mMessage = text;
                Formatter fm = new Formatter();
                fm.format(t(R.string.guide_input_message_repeat_format), mMessage);
                mTTS.textToSpeech(fm.toString(), ConfirmSend);
                break;

            case ConfirmSend:
                mTTS.textToSpeech(t(R.string.guide_confirm_send), GetAnswerConfirmSend);
                break;

            case GetAnswerConfirmSend:
                mSTT.speechToText(Send);
                break;

            case Send:
                if(!text.equals(t(R.string.decide_send_word)) || mMessage == null || mMessage.isEmpty()) {
                    mTTS.textToSpeech(t(R.string.guide_input_message_again), StartReply);
                    break;
                }
                if(!send()) {
                    mTTS.textToSpeech(t(R.string.guide_send_failed), Finish);
                    break;
                }
                mTTS.textToSpeech(t(R.string.guide_send_message), Finish);
                break;

            case Finish:
                break;

            case Reserve:
                reserve();
                break;

            case Failed:
                mTTS.textToSpeech(t(R.string.guide_input_speech_failed), Finish);
                break;

            default:
                break;
        }
    }

    public void nextGuide(Guide guide) {
        nextGuide(guide, "");
    }

    public void cancelGuide() {
        mTTS.cancel();
        mSTT.cancel();
    }

    public void notificationWhileGuidance() {

    }

    private String t(int stringId) {
        return mActivity.getString(stringId);
    }

    private void reserve() {
        if(mListener == null) return;
        mListener.onReserve(latestReservation);
    }

    private boolean send() {
        URL url = null;
        try {
            url = new URL("https://smartwalk.stonedot.com/message/push");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        Map<String, String> sendData = new HashMap<String, String>() {
            {
                put("display_name", latestReservation.getSender());
                put("sender", UserDataStorage.getLineMid(mActivity.getBaseContext()));
                put("message", mMessage);
            }
        };
        HttpJSONClient http = new HttpJSONClient(url, sendData);
        http.post(null);
        return true;
    }
}
