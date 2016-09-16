package jp.tmhouse.android.elgoog.elgoog;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
}
