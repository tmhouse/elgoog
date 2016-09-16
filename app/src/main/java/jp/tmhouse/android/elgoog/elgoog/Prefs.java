package jp.tmhouse.android.elgoog.elgoog;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.inputmethod.InputMethodSubtype;

/**
 * Created by mutoh on 16/9/16.
 */
public class Prefs {
    private SharedPreferences m_sp;

    public Prefs(Context ctx) {
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

    /***
    public enum EInputMode {
        INPUT_MODE_TEXT(0),
        INPUT_MODE_NUMERIC(1);

        private int mode = 0;
        private EInputMode(int v) {
            mode = v;
        }
        public int getValue() {
            return(mode);
        }
        public static EInputMode get(int v) {
            if( INPUT_MODE_NUMERIC.getValue() == v ) {
                return(INPUT_MODE_NUMERIC);
            } else if( INPUT_MODE_TEXT.getValue() == v ) {
                return(INPUT_MODE_TEXT);
            }
            throw new RuntimeException("value is no defined");
        }
    }
     ***/
    public void saveInputMode(int inputType) {
        SharedPreferences.Editor e = m_sp.edit();
        e.putInt("inputType", inputType);
        e.commit();
    }
    public int getInputMode() {
        return(m_sp.getInt("inputType", InputType.TYPE_CLASS_TEXT));
    }
}
