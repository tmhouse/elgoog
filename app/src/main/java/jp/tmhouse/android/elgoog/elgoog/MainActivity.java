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
    private Button      m_find;
    private Button      m_clear;
    private ImageButton      m_mic;
    private TmContinuousSpeechRecognizer  m_csr;
    private ArrayList<String>   m_lastSpeechTextArray;
    private int                 m_doFindTextArrayCount = 0;

    private void doFindTextArray(ArrayList<String> arr) {
        //if( m_lastSpeechTextArray == null ) {
            m_lastSpeechTextArray = arr;
            m_doFindTextArrayCount = 0;
        //}

        try {
            String str = m_lastSpeechTextArray.get(m_doFindTextArrayCount);
            m_searchText.setText(str);
        } catch (IndexOutOfBoundsException e) {
            // end
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
                doFindTextArray(results);
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
                    //m_csr.stopListening();
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
                Log.d("afterTextChanged", "do findAllAsync:" + s.toString());
                m_webview.findAllAsync(s.toString());
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
