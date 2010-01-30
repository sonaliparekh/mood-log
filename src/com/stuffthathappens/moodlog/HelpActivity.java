package com.stuffthathappens.moodlog;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        WebView webView = (WebView) findViewById(R.id.helpWebView);
        String html = Utils.loadResToString(R.raw.help, this, "utf-8");
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);

    }

}
