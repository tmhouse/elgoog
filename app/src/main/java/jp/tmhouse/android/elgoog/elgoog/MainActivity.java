package jp.tmhouse.android.elgoog.elgoog;

import android.app.Activity;
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
    private Button      m_clear;
    private ImageButton      m_mic;
    private TmContinuousSpeechRecognizer  m_csr;
    private TextFinder m_textFinder = new TextFinder();
    private Beeper      m_beeper = new Beeper();
    private Prefs       m_prefs;

    /**
     * 文字列配列のどれかをwebviewのページ内から探してhiglightする.
     */
    private class TextFinder {
        private ArrayList<String> m_lastSpeechTextArray;
        private int m_doFindTextArrayCount = 0;
        private String m_curFindText = null;

        private void doFindTextArray(ArrayList<String> arr) {
            // trimしよう
            ArrayList<String> trimedArr = new ArrayList<String>(20);
            int size = arr.size();
            for( int i = 0; i < size; i++ ) {
                String org = arr.get(i);
                String trimed = org.trim().replace(" ", "").replace("　", "");
                if( ! org.equals(trimed) ) {
                    trimedArr.add(trimed);
                    Log.d("doFindTextArray", "trimed added. org=" + org + ", trimed=" + trimed);
                } else {
                    Log.d("doFindTextArray", "trimed not added. org=" + org);
                }
            }
            arr.addAll(trimedArr);
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
                String msg = null;
                if( m_lastSpeechTextArray.size() > 1 ) {
                    msg = concatArrText() + "\nは全部見つかりません。";
                } else {
                    msg = concatArrText() + "\nは見つかりません。";
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                stopFindText();
                m_beeper.beep();
                return;
            }
            m_doFindTextArrayCount++;
        }

        private String concatArrText() {
            StringBuilder sb = new StringBuilder();
            int len = m_lastSpeechTextArray.size();
            for( int i = 0; i < len; i++ ) {
                sb.append("- ");
                String s = m_lastSpeechTextArray.get(i);
                sb.append(s);
                if( i < len ) {
                    sb.append("\n");
                }
            }
            return(sb.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("app", "test");
        m_prefs = new Prefs(this);
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
                String curText = m_textFinder.getCurrentText();
                Log.i("app", "curText=" + curText +
                        ", activeMatchOrdinal=" + activeMatchOrdinal +
                        ", numberOfMatches=" + numberOfMatches +
                        ", isDoneCounting=" + Boolean.toString(isDoneCounting));
                if( isDoneCounting && (curText != null) ) {
                    if( numberOfMatches > 0 ) {
                        Log.i("find text", "found text:" + curText);
                        m_textFinder.stopFindText();
                        setFindTextView(curText, false);
                    } else {
                        Log.i("find text", "not found:" + curText);
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
        m_searchText.addTextChangedListener(m_textWatcher);
        m_searchText.requestFocus();

        m_go = (Button) findViewById(R.id.go);
        m_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_webview.loadUrl(m_url.getText().toString());
            }
        });

        m_clear = (Button)findViewById(R.id.clear);
        m_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFindTextView("", false);
            }
        });

        init(m_webview);
    }

    private void setFindTextView(String str, boolean fireTextWatcher) {
        String cur = m_searchText.getText().toString();
        if( cur != null && cur.equals(str) ) {
            return;
        }

        if( fireTextWatcher ) {
            m_searchText.setText(str);
        } else {
            m_searchText.removeTextChangedListener(m_textWatcher);
            m_searchText.setText(str);
            m_searchText.setSelection(0, str.length());
            m_searchText.addTextChangedListener(m_textWatcher);
        }
    }

    private TextWatcher m_textWatcher = new TextWatcher() {
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
    };

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

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                m_prefs.saveLastUrl(url);
            }
        });

        loadUrl(m_prefs.getLastUrl());
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
