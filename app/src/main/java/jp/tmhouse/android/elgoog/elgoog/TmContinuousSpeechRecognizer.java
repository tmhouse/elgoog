package jp.tmhouse.android.elgoog.elgoog;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * 連続してSpeechRecognizerを稼働させるクラス.
 */
public class TmContinuousSpeechRecognizer {
    private SpeechRecognizer m_sr;
    private Activity m_act;
    private OnRecognizedCB      m_cb;
    private boolean     m_isListening = false;

    public TmContinuousSpeechRecognizer(Activity act) {
        m_act = act;

        checkPermission();

        if (m_sr == null) {
            m_sr = SpeechRecognizer.createSpeechRecognizer(m_act);
            if (!SpeechRecognizer.isRecognitionAvailable(m_act)) {
                Toast.makeText(m_act, "音声認識が使えません",
                        Toast.LENGTH_LONG).show();
                throw new RuntimeException("speech recognizer not available");
            }
            m_sr.setRecognitionListener(new listener());
        }
    }

    public interface OnRecognizedCB {
        public void onRecognized(ArrayList<String> results);
    }
    public void setOnResultListener(OnRecognizedCB cb) {
        m_cb = cb;
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(m_act, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    m_act,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    0);
            //status_view.setText("Requesting RECORD_AUDIO Permission...");
            return;
        }
    }

    // 音声認識を開始する
    public void startListening() {
        if( m_sr == null ) {
            Toast.makeText(m_act, "音声認識が使えません",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if( m_isListening ) {
            stopListening();
        }

        // インテントの作成
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // 言語モデル指定
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech please");
        m_sr.startListening(intent);
        m_isListening = true;
    }

    public void stopListening() {
        if( m_sr != null ) {
            m_sr.stopListening();
        }
        m_isListening = false;
    }

    public void destroy() {
        stopListening();
        if (m_sr != null) m_sr.destroy();
        m_sr = null;
    }

    // 音声認識を再開する
    //public void restartListeningService() {
    //    stopListening();
    //    startListening();
    //}

    // RecognitionListenerの定義
    // 中が空でも全てのメソッドを書く必要がある
    private class listener implements RecognitionListener {
        // 話し始めたときに呼ばれる
        public void onBeginningOfSpeech() {
            /*Toast.makeText(getApplicationContext(), "onBeginningofSpeech",
                    Toast.LENGTH_SHORT).show();*/
        }


        // 結果に対する反応などで追加の音声が来たとき呼ばれる
        // しかし呼ばれる保証はないらしい
        public void onBufferReceived(byte[] buffer) {
        }

        // 話し終わった時に呼ばれる
        public void onEndOfSpeech() {
            /*Toast.makeText(getApplicationContext(), "onEndofSpeech",
                    Toast.LENGTH_SHORT).show();*/
        }

        // ネットワークエラーか認識エラーが起きた時に呼ばれる
        public void onError(int error) {
            String reason = "";
            switch (error) {
                // Audio recording error
                case SpeechRecognizer.ERROR_AUDIO:
                    reason = "ERROR_AUDIO";
                    break;
                // Other client side errors
                case SpeechRecognizer.ERROR_CLIENT:
                    reason = "ERROR_CLIENT";
                    break;
                // Insufficient permissions
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    reason = "ERROR_INSUFFICIENT_PERMISSIONS";
                    break;
                // 	Other network related errors
                case SpeechRecognizer.ERROR_NETWORK:
                    reason = "ERROR_NETWORK";
                    /* ネットワーク接続をチェックする処理をここに入れる */
                    break;
                // Network operation timed out
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    reason = "ERROR_NETWORK_TIMEOUT";
                    break;
                // No recognition result matched
                case SpeechRecognizer.ERROR_NO_MATCH:
                    reason = "ERROR_NO_MATCH";
                    break;
                // RecognitionService busy
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    reason = "ERROR_RECOGNIZER_BUSY";
                    break;
                // Server sends error status
                case SpeechRecognizer.ERROR_SERVER:
                    reason = "ERROR_SERVER";
                    /* ネットワーク接続をチェックをする処理をここに入れる */
                    break;
                // No speech input
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    reason = "ERROR_SPEECH_TIMEOUT";
                    break;
            }
            Toast.makeText(m_act, reason, Toast.LENGTH_SHORT).show();
            //restartListeningService();
            stopListening();
        }

        // 将来の使用のために予約されている
        public void onEvent(int eventType, Bundle params) {
        }

        // 部分的な認識結果が利用出来るときに呼ばれる
        // 利用するにはインテントでEXTRA_PARTIAL_RESULTSを指定する必要がある
        public void onPartialResults(Bundle partialResults) {
        }

        // 音声認識の準備ができた時に呼ばれる
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(m_act, "Speech now",
                    Toast.LENGTH_SHORT).show();
        }

        // 認識結果が準備できた時に呼ばれる
        public void onResults(Bundle results) {
            // 結果をArrayListとして取得
            ArrayList<String> results_array = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            // 取得した文字列を結合
            //String resultsString = "";
            //for (int i = 0; i < results.size(); i++) {
            //resultsString += results_array.get(i) + ";";
            //}
            // トーストを使って結果表示
            //Toast.makeText(m_act, resultsString, Toast.LENGTH_LONG).show();
            //restartListeningService();

            if( m_cb != null ) {
                m_cb.onRecognized(results_array);
            }
            m_isListening = false;
        }

        // サウンドレベルが変わったときに呼ばれる
        // 呼ばれる保証はない
        public void onRmsChanged(float rmsdB) {
        }
    }
}
