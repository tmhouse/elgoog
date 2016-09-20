package jp.tmhouse.android.elgoog.elgoog;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Created by mutoh on 16/9/16.
 */
public class Beeper {
    private SoundPool   m_spool;
    private Handler m_handler;
    private int     m_hazureId;
    private int     m_atariId;

    private static int  MSG_PLAY_HAZURE = 1;
    private static int  MSG_PLAY_ATARI = 2;

    public Beeper(Context ctx) {
        m_spool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        m_hazureId = m_spool.load(ctx, R.raw.bubuuu, 0);
        m_atariId = m_spool.load(ctx, R.raw.pirororin, 0);

        HandlerThread handlerThread = new HandlerThread("Beeper");
        handlerThread.start();
        m_handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if( msg.what == MSG_PLAY_HAZURE ) {
                    m_spool.play(m_hazureId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
                if( msg.what == MSG_PLAY_ATARI ) {
                    m_spool.play(m_atariId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }
        };
    }

    public void playHazure() {
        m_handler.removeMessages(MSG_PLAY_HAZURE);
        m_handler.sendEmptyMessage(MSG_PLAY_HAZURE);
    }

    public void playAtari() {
        m_handler.removeMessages(MSG_PLAY_ATARI);
        m_handler.sendEmptyMessage(MSG_PLAY_ATARI);
    }
}
