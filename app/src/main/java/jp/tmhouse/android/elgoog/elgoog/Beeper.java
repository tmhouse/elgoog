package jp.tmhouse.android.elgoog.elgoog;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Created by mutoh on 16/9/16.
 */
public class Beeper {
    private ToneGenerator m_tone = new ToneGenerator(
            AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
    private Handler m_handler;
    private static int  MSG_PLAY_BEEP = 1;

    public Beeper() {
        HandlerThread handlerThread = new HandlerThread("Beeper");
        handlerThread.start();
        m_handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if( msg.what == MSG_PLAY_BEEP ) {
                    playBeep();
                }
            }
        };
    }

    public void beep() {
        m_handler.sendEmptyMessage(MSG_PLAY_BEEP);
    }

    private synchronized void playBeep() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }
        m_tone.startTone(ToneGenerator.TONE_PROP_BEEP, 200);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }
        m_tone.startTone(ToneGenerator.TONE_PROP_BEEP, 1000);
    }
}
