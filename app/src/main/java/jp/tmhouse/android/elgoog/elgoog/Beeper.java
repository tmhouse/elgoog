package jp.tmhouse.android.elgoog.elgoog;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.HashMap;

/**
 * Created by mutoh on 16/9/16.
 */
public class Beeper {
    private SoundPool   m_spool;
    private Handler m_handler;

    private static int  MSG_PLAY_HAZURE = 1;
    private static int  MSG_PLAY_ATARI = 2;
    private static int  MSG_PLAY_HATENA = 3;

    private static Beeper   s_instance = null;
    private HashMap<Integer, Integer> m_table = new HashMap<Integer, Integer>(127);

    public static Beeper getInstance(Context ctx) {
        if( s_instance == null ) {
            s_instance = new Beeper(ctx);
        }
        return(s_instance);
    }

    private Beeper(Context ctx) {
        m_spool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        m_table.put(MSG_PLAY_HAZURE, m_spool.load(ctx, R.raw.bubuuu, 0));
        m_table.put(MSG_PLAY_ATARI, m_spool.load(ctx, R.raw.pirororin, 0));
        m_table.put(MSG_PLAY_HATENA, m_spool.load(ctx, R.raw.pipo_hatena, 0));

        HandlerThread handlerThread = new HandlerThread("Beeper");
        handlerThread.start();
        m_handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Integer id = m_table.get(msg.what);
                if( id != null ) {
                    m_spool.play(id, 0.6f, 0.6f, 0, 0, 1.0f);
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

    public void playHatena() {
        m_handler.removeMessages(MSG_PLAY_HATENA);
        m_handler.sendEmptyMessage(MSG_PLAY_HATENA);
    }
}
