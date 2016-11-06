package com.stonedot.todo.smartwalk;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import static com.stonedot.todo.smartwalk.Guide.ConfirmReply;
import static com.stonedot.todo.smartwalk.Guide.ConfirmSend;
import static com.stonedot.todo.smartwalk.Guide.DecideReply;
import static com.stonedot.todo.smartwalk.Guide.Finish;
import static com.stonedot.todo.smartwalk.Guide.GetAnswerConfirmReply;
import static com.stonedot.todo.smartwalk.Guide.GetAnswerConfirmSend;
import static com.stonedot.todo.smartwalk.Guide.Notification;
import static com.stonedot.todo.smartwalk.Guide.RepeatReply;
import static com.stonedot.todo.smartwalk.Guide.RequestFriend;
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
    private ArrayDeque<String> notificationQueue = new ArrayDeque<>();
    private boolean isWorking = false;
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
        if(!isGuideContinuable(guide)) return;

        switch (guide) {
            case Notification:
                notificationQueue.add(text);
                mTTS.textToSpeech(text, RequestFriend);
                break;

            case RequestFriend:
                isFriendRequest(requestFriendCallback);
                break;

            case ConfirmReply:
                Log.d("Guidance", "ConfirmReply");
                notificationQueue.remove();
                Log.d("Guidance", notificationQueue.toString());
                if(!isNotificationQueueEmpty()) break;
                isWorking = true;
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
                isWorking = true;
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
                isWorking = false;
                break;

            case Reserve:
                isWorking = false;
                reserve();
                break;

            case Failed:
                mTTS.textToSpeech(t(R.string.guide_input_speech_failed), Finish);
                break;

            default:
                isWorking = false;
                break;
        }
    }

    public void nextGuide(Guide guide) {
        nextGuide(guide, "");
    }

    public boolean isGuideContinuable(Guide guide) {
        return isNotificationQueueEmpty()
                || guide == Notification
                || guide == RequestFriend
                || guide == ConfirmReply;
    }

    public boolean isNotificationQueueEmpty() {
        return notificationQueue.size() == 0;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void cancelGuide() {
        mTTS.cancel();
        mSTT.cancel();
        isWorking = false;
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

    private void isFriendRequest(HttpJSONClient.Responded callback) {
        URL url = null;
        try {
            url = new URL("https://smartwalk.stonedot.com/message/can_push");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        Map<String, String> sendData = new HashMap<String, String>() {
            {
                put("sender", UserDataStorage.getLineMid(mActivity.getBaseContext()));
                put("display_name", latestReservation.getSender());
            }
        };
        HttpJSONClient http = new HttpJSONClient(url, sendData);
        http.post(callback);
    }

    private HttpJSONClient.Responded requestFriendCallback = new HttpJSONClient.Responded() {
        @Override
        public void responded(int code, String statusMessage, String content) {
            try {
                Log.d("Guidance", "Responded");
                boolean canPush = new JSONObject(content).getBoolean("can_push");
                if(canPush) nextGuide(ConfirmReply);
                else {
                    notificationQueue.remove();
                    mTTS.textToSpeech(t(R.string.guide_be_friend_to_reply), Finish);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
