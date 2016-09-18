package jp.tmhouse.android.elgoog.elgoog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.InputMethodSubtype;
import android.webkit.WebView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by mutoh on 16/9/16.
 */
public class Prefs {
    private SharedPreferences m_sp;
    private Context     m_ctx;

    public Prefs(Context ctx) {
        m_ctx = ctx;
        m_sp = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    public void saveLastUrl(String url) {
        SharedPreferences.Editor e = m_sp.edit();
        e.putString("lastUrl", url);
        e.commit();
    }
    public String getLastUrl() {
        return(m_sp.getString("lastUrl", "http://www.google.com"));
    }

    public void saveInputMode(int inputType) {
        SharedPreferences.Editor e = m_sp.edit();
        e.putInt("inputType", inputType);
        e.commit();
    }
    public int getInputMode() {
        return(m_sp.getInt("inputType", InputType.TYPE_CLASS_TEXT));
    }

    public void saveWebState(WebView webView) {
        Bundle b = new Bundle();
        webView.saveState(b);

        byte[] barr = b.getByteArray(c_WEBVIEW_CHROMIUM_STATE);
        if( barr == null ) {
            Log.e("saveWebState", "no data");
            return;
        }

        BufferedOutputStream out = null;
        try {
            FileOutputStream file = m_ctx.openFileOutput(c_WEBSTATE_FILE, Context.MODE_PRIVATE);
            out = new BufferedOutputStream(file);
            out.write(barr);
            out.flush();
            out.close();
            Log.v("saveWebState", "file write completed.");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static final String c_WEBSTATE_FILE = "webstate.bin";
    private static final String c_WEBVIEW_CHROMIUM_STATE = "WEBVIEW_CHROMIUM_STATE";

    public Bundle getWebState() {
        byte[] barr = null;
        try {
            InputStream is = m_ctx.openFileInput(c_WEBSTATE_FILE);
            barr = new byte[is.available()];
            is.read(barr);
            Log.v("getWebState", "file read completed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
             e.printStackTrace();
        }

        Bundle b = new Bundle();
        b.putByteArray(c_WEBVIEW_CHROMIUM_STATE, barr);
        return(b);
    }
}
