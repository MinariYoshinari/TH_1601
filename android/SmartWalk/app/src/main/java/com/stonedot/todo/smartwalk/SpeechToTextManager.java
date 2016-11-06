package com.stonedot.todo.smartwalk;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

/**
 * Created by komatsu on 2016/10/29.
 */

public class SpeechToTextManager {

    private Activity mActivity;
    private SpeechRecognizer mSTT;
    private SpeechToTextListenerImpl mSTTListener;

    public SpeechToTextManager(Activity activity, SpeechToTextListenerImpl.SpeechToTextListener listener) {
        mActivity = activity;
        mSTTListener = new SpeechToTextListenerImpl(activity, listener);
        mSTT = SpeechRecognizer.createSpeechRecognizer(activity);
        mSTT.setRecognitionListener(mSTTListener);
    }

    public void speechToText(final Guide guide) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,  mActivity.getString(R.string.input_speech));
                mSTTListener.setNextGuide(guide);
                mSTT.startListening(intent);
            }
        });
    }

    public void cancel() {
        if(mSTT == null) return;
        mSTT.cancel();
        mSTT.stopListening();
    }

    public void shutdown() {
        if(mSTT == null) return;
        cancel();
        mSTT.destroy();
    }
}
