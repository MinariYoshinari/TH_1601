package com.stonedot.todo.smartwalk;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by komatsu on 2016/10/29.
 */

public class SpeechToTextListenerImpl implements RecognitionListener {

    public interface SpeechToTextListener {
        void onGetTextFromSpeech(String text, Guide guide);
        void onGetTextFromSpeechFailed(Guide guide);
    }
    private SpeechToTextListener mListener;

    private Context mContext;

    private Guide nextGuide = Guide.Guide;

    public SpeechToTextListenerImpl(Context context, SpeechToTextListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Toast.makeText(mContext, R.string.stt_start_input_speech, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        Toast.makeText(mContext, R.string.stt_finish_input_speech, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                errorToast(R.string.stt_error_audio);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                errorToast(R.string.stt_error_client);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                errorToast(R.string.stt_error_insufficient_permissions);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                // ネットワークエラー(その他)
                errorToast(R.string.stt_error_network);
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                // ネットワークタイムアウトエラー
                errorToast(R.string.stt_error_network_timeout);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                // 音声認識結果無し
                errorToast(R.string.stt_error_no_match);
                if(mListener != null) mListener.onGetTextFromSpeechFailed(nextGuide);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                errorToast(R.string.stt_error_recognizer_busy);
                break;
            case SpeechRecognizer.ERROR_SERVER:
                errorToast(R.string.stt_error_server);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                // 音声入力無し
                errorToast(R.string.stt_error_speech_timeout);
                if(mListener != null) mListener.onGetTextFromSpeechFailed(nextGuide);
                break;
            default:
                break;
        }
    }

    private void errorToast(int stringId) {
        Toast.makeText(mContext, stringId, Toast.LENGTH_SHORT).show();
    }

    // Bundleが新しくなってしまったもので、Guideを受け取れないので、
    // ListenerImplだが特例でsetメソッド設置
    public void setNextGuide(Guide guide) {
        nextGuide = guide;
    }

    @Override
    public void onResults(Bundle bundle) {
        // とりあえず第1候補を使う
        ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(mListener != null) mListener.onGetTextFromSpeech(results.get(0), nextGuide);
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}
