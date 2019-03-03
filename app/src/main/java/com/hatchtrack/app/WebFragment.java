package com.hatchtrack.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebFragment extends Fragment {
    private static final String TAG = HatchListFragment.class.getSimpleName();

    private WebView webView;
    private String url;

    public WebFragment() {
        Log.i(TAG, "WebFragment(): new");
    }

    public static WebFragment newInstance() {
        WebFragment fragment = new WebFragment();
        return(fragment);
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        String url = args.getString(Globals.KEY_URL);
        if(url != null){
            this.url = url;
            if(this.webView != null){
                this.webView.loadUrl(this.url);
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_web_view, container, false);
        this.webView = rootView.findViewById(R.id.webViewId);
        WebSettings webSettings = this.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // Force links and redirects to open in the WebView instead of in a browser
        this.webView.setWebViewClient(new WebViewClient());
        if(this.url != null){
            this.webView.loadUrl(this.url);
        }
        return(rootView);
    }
}
