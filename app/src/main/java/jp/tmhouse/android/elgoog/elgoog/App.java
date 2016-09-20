package jp.tmhouse.android.elgoog.elgoog;

import android.app.Application;
import android.preference.PreferenceManager;

/**
 * Created by mutoh on 16/9/19.
 */
public class App extends Application {
    public static boolean DBG = BuildConfig.DEBUG;

    public App() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(getBaseContext(),
                R.xml.settings, false);
    }
}
