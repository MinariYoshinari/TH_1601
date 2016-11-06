package com.stonedot.todo.smartwalk;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by komatsu on 2016/10/24.
 */

public class TextToSpeechManager implements TextToSpeech.OnInitListener {

    private Context mContext;
    private TextToSpeechProgressListener mTTSListener;
    private TextToSpeech mTTS;
    private boolean mInitCompletedFlag = false;

    public TextToSpeechManager(Context context, TextToSpeechProgressListener.TextToSpeechListener ttsListener) {
        mContext = context;
        mTTSListener = new TextToSpeechProgressListener(ttsListener);
        mTTS = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            Toast.makeText(mContext, R.string.tts_error_init, Toast.LENGTH_SHORT).show();
            return;
        }

        if(mTTSListener != null) {
            mTTS.setOnUtteranceProgressListener(mTTSListener);
        }

        Locale locale = Locale.JAPANESE;
        if (mTTS.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
            mTTS.setLanguage(locale);
            mInitCompletedFlag = true;
        }
        else Toast.makeText(mContext, R.string.tts_error_language, Toast.LENGTH_SHORT).show();
    }

    public void textToSpeech(String text, Guide guide) {
        if (mTTS == null || text.length() <= 0 || !mInitCompletedFlag) return;
        HashMap<String, String> parameter = new HashMap<String, String>();
        parameter.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, guide.toString());
        mTTS.speak(TextManager.extractSpeakableChars(text), TextToSpeech.QUEUE_ADD, parameter);
    }

    public void cancel() {
        if (mTTS != null) mTTS.stop();
    }

    public void shutdown() {
        if(mTTS == null) return;
        mTTS.stop();
        mTTS.shutdown();
        mTTS = null;
    }
}
