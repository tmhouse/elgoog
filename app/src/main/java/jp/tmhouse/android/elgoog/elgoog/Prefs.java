package jp.tmhouse.android.elgoog.elgoog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mutoh on 16/9/16.
 */
public class Prefs {
    private SharedPreferences m_sp;
    private Context     m_ctx;
    private LocalDB     m_db;
    private static final String c_WEBSTATE_FILE = "webstate.bin";
    private static final String c_WEBVIEW_CHROMIUM_STATE = "WEBVIEW_CHROMIUM_STATE";

    public Prefs(Context ctx) {
        m_ctx = ctx;
        m_sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        m_db = new LocalDB(m_ctx);
    }

    public String getUserAgent() {
        String key = m_ctx.getString(R.string.userAgentListKey);
        String val = m_sp.getString(key, "0");
        if( val == null || val.equals("0") ) {
            return(MainActivity.getWebViewOriginalUserAgent());
        }
        return(val);
    }

    public boolean getStringMatchSoundOn() {
        String key = m_ctx.getString(R.string.soundOnKey);
        boolean val = m_sp.getBoolean(key, true);
        return(val);
    }

    public void saveInputMode(int inputType) {
        SharedPreferences.Editor e = m_sp.edit();
        e.putInt("inputType", inputType);
        e.commit();
    }
    public int getInputMode() {
        return(m_sp.getInt("inputType", InputType.TYPE_CLASS_TEXT));
    }

    public void clearWebState() {
        m_ctx.deleteFile(c_WEBSTATE_FILE);
        if(App.DBG) Log.i("saveWebState", "file clear completed.");
    }

    public void saveWebState(WebView webView) {
        // clearCacheしないとファイルが巨大化する
        webView.clearCache(true);
        webView.clearMatches();
        webView.destroyDrawingCache();

        Bundle b = new Bundle();
        WebBackForwardList list = webView.saveState(b);

        // debug
        if( BuildConfig.DEBUG ) {
            int size = list.getSize();
            for( int i = 0; i < size; i++ ) {
                WebHistoryItem item = list.getItemAtIndex(i);
                if(App.DBG) Log.d("saveWebState", "save hist url=" + item.getUrl());
            }
        }

        byte[] barr = b.getByteArray(c_WEBVIEW_CHROMIUM_STATE);
        if( barr == null ) {
            if(App.DBG) Log.e("saveWebState", "no data");
            return;
        }

        BufferedOutputStream out = null;
        try {
            FileOutputStream file = m_ctx.openFileOutput(c_WEBSTATE_FILE, Context.MODE_PRIVATE);
            out = new BufferedOutputStream(file);
            out.write(barr);
            out.flush();
            out.close();
            Log.i("saveWebState", "file write completed. size=" + barr.length);
            Toast.makeText(m_ctx, "web state saved. size=" + barr.length,
                    Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public boolean restoreWebState(WebView webView) {
        try {
            InputStream is = m_ctx.openFileInput(c_WEBSTATE_FILE);
            int size = is.available();
            byte[] barr = new byte[size];
            is.read(barr);
            Bundle b = new Bundle();
            b.putByteArray(c_WEBVIEW_CHROMIUM_STATE, barr);
            webView.restoreState(b);
            if(App.DBG) Log.i("getWebState", "file read completed. size=" + size);
            return(true);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return(false);
    }

    public void addBookmark(String url, String title) {
        m_db.addBookmark(url, title);
    }

    public LocalDB.Bookmark[] getAllBookmarks() {
        return(m_db.getAllBookmarks());
    }

    public void updateBookmark(LocalDB.Bookmark bkmark) {
        m_db.updateBookmark(bkmark);
    }

    public void deleteBookmark(int id) {
        m_db.deleteBookmark(id);
    }
}
