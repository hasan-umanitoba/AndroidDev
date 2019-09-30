package uofm.hasan.bullsexchange;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class JobsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){ //changes color of actionBar
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.mygradient));

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);

        WebView webView = (WebView) findViewById(R.id.webView); //finds webView

        webView.getSettings().setJavaScriptEnabled(true); //enabling javaScript for navigation


        webView.setWebViewClient(new WebViewClient());

        Intent intent = getIntent();

        String url = intent.getStringExtra("content") ; //loads URL inside the jobPosting and navigates there
        webView.loadUrl(url);
    }
}
