package jp.tmhouse.android.elgoog.elgoog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.webkit.WebView;

/**
 * Created by mutoh on 16/9/20.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        setupSummary();

        getSp(this).registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(
                            SharedPreferences sharedPreferences, String key) {
                        setupSummary();
                    }
                }
        );
    }

    private void setupSummary() {
        ListPreference lp = (ListPreference)
                getPreferenceScreen().findPreference(
                        getString(R.string.userAgentListKey));
        lp.setSummary(getUserAgent(getApplicationContext()));
    }

    private static SharedPreferences getSp(Context ctx) {
        return(PreferenceManager.getDefaultSharedPreferences(ctx));
    }
    public static String getUserAgent(Context ctx) {
        String key = ctx.getString(R.string.userAgentListKey);
        String val = getSp(ctx).getString(key, "0");
        if( val == null || val.equals("0") ) {
            return(MainActivity.getWebViewOriginalUserAgent());
        }
        return(val);
    }

    public static boolean getStringMatchSoundOn(Context ctx) {
        String key = ctx.getString(R.string.soundOnKey);
        boolean val = getSp(ctx).getBoolean(key, true);
        return(val);
    }
}
