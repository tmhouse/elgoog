package jp.tmhouse.android.elgoog.elgoog;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private WebView m_webview;
    private EditText m_url;
    private EditText m_searchText;
    private Button      m_go;
    private Button      m_find;
    private Button      m_clear;
    private ImageButton      m_mic;
    private TmContinuousSpeechRecognizer  m_csr;
    private TextFinder m_textFinder = new TextFinder();
    private Beeper      m_beeper = new Beeper();

    /**
     * 文字列配列のどれかをwebviewのページ内から探してhiglightする.
     */
    private class TextFinder {
        private ArrayList<String> m_lastSpeechTextArray;
        private int m_doFindTextArrayCount = 0;
        private String m_curFindText = null;

        private void doFindTextArray(ArrayList<String> arr) {
            for (String s : arr) {
                Log.d("doFindTextArray", "str=" + s);
            }
            m_lastSpeechTextArray = arr;
            m_doFindTextArrayCount = 0;
            findNextText();
        }
        private void doFindText(String str) {
            if( str == null || str.isEmpty() ) {
                return;
            }
            ArrayList<String> arr = new ArrayList<String>(1);
            arr.add(str);
            doFindTextArray(arr);
        }

        private String getCurrentText() {
            return(m_curFindText);
        }

        private void stopFindText() {
            m_curFindText = null;
            m_lastSpeechTextArray = null;
            m_doFindTextArrayCount = 0;
        }

        private void findNextText() {
            if (m_lastSpeechTextArray == null) {
                return;
            }
            try {
                m_curFindText = m_lastSpeechTextArray.get(m_doFindTextArrayCount);
                if( m_curFindText == null || m_curFindText.isEmpty() ) {
                    throw new RuntimeException("why find null string?");
                }
                m_webview.findAllAsync(m_curFindText);
            } catch (IndexOutOfBoundsException e) {
                // end
                Log.w("findNextText", "all text were not found");
                stopFindText();
                m_beeper.beep();
                return;
            }
            m_doFindTextArrayCount++;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("app", "test");
        m_csr = new TmContinuousSpeechRecognizer(this);
        m_csr.setOnResultListener(new TmContinuousSpeechRecognizer.OnRecognizedCB() {
            @Override
            public void onRecognized(ArrayList<String> results) {
                m_textFinder.doFindTextArray(results);
            }
        });

        m_webview = (WebView)findViewById(R.id.webView);
        m_webview.setFindListener(new WebView.FindListener() {
            @Override
            public void onFindResultReceived(
                    int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
                Log.i("app", "activeMatchOrdinal=" + activeMatchOrdinal +
                        ", numberOfMatches=" + numberOfMatches +
                        ", isDoneCounting=" + Boolean.toString(isDoneCounting));
                if( isDoneCounting && (m_textFinder.getCurrentText() != null) ) {
                    if( numberOfMatches > 0 ) {
                        Log.i("find text", "found text:" + m_textFinder.getCurrentText());
                        m_textFinder.stopFindText();
                    } else {
                        Log.i("find text", "not found:" + m_textFinder.getCurrentText());
                        m_textFinder.findNextText();
                    }
                }
            }
        });
        m_url = (EditText) findViewById(R.id.url);

        m_mic = (ImageButton) findViewById(R.id.mic);
        m_mic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                //Log.d("mic onTouch", "action=" + event.getAction());
                if( action == MotionEvent.ACTION_DOWN ) {
                    m_csr.startListening();
                } else if( action == MotionEvent.ACTION_UP ) {
                    m_csr.stopListening();
                }
                return false;
            }
        });

        m_searchText = (EditText) findViewById(R.id.searchText);
        m_searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("afterTextChanged", "find text:" + s.toString());
                m_textFinder.doFindText(s.toString());
            }
        });

        m_go = (Button) findViewById(R.id.go);
        m_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_webview.loadUrl(m_url.getText().toString());
            }
        });

        m_find = (Button) findViewById(R.id.find);
        m_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = m_searchText.getText().toString();
                m_webview.findAllAsync(s);
            }
        });

        m_clear = (Button)findViewById(R.id.clear);
        m_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_searchText.setText("");
            }
        });

        init(m_webview);
    }

    private void init(WebView webview) {
        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                //activity.setProgress(progress * 1000);
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        loadUrl("http://www.google.com");
    }

    private void loadUrl(String url) {
        if( !( m_url.getText() != null && m_url.getText().equals(url)) ) {
            m_url.setText(url);
        }
        m_webview.loadUrl(url);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        m_csr.destroy();
        m_webview.destroy();

        super.onDestroy();
    }

}
