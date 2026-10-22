package com.epochs.game;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebResourceError;
import android.net.Uri;
import android.content.Intent;
public class MainActivity extends Activity {
 private WebView view; private boolean fallbackLoaded=false;
 private static final String OFFLINE="file:///android_asset/index.html";
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
  view=new WebView(this);setContentView(view);
  WebSettings s=view.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);
  s.setAllowFileAccess(true);s.setAllowContentAccess(false);
  s.setJavaScriptCanOpenWindowsAutomatically(false);
  s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  final Uri allowed=Uri.parse(BuildConfig.GAME_URL);
  view.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest req){
    Uri u=req.getUrl();
    if(u.toString().equals(OFFLINE))return false;
    if("https".equals(u.getScheme())&&u.getHost()!=null&&u.getHost().equals(allowed.getHost()))return false;
    if("https".equals(u.getScheme()))startActivity(new Intent(Intent.ACTION_VIEW,u));
    return true;
   }
   @Override public void onReceivedError(WebView v,WebResourceRequest req,WebResourceError e){
    if(req.isForMainFrame()&&!fallbackLoaded){fallbackLoaded=true;v.loadUrl(OFFLINE);}
   }
  });
  if("https".equals(allowed.getScheme())&&allowed.getHost()!=null&&!"example.invalid".equals(allowed.getHost()))view.loadUrl(BuildConfig.GAME_URL);
  else{fallbackLoaded=true;view.loadUrl(OFFLINE);}
 }
 @Override public void onBackPressed(){if(view.canGoBack())view.goBack();else super.onBackPressed();}
 @Override public void onDestroy(){view.destroy();super.onDestroy();}
}