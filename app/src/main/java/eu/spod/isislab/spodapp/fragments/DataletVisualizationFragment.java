package eu.spod.isislab.spodapp.fragments;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import eu.spod.isislab.spodapp.R;


public class DataletVisualizationFragment extends Fragment {
    private static final String DATALET_SRC_PARAM = "datalet_src";
    private static final String TAG = "DataletVisualizationFra";

    private String dataletSrc;
    private WebView dataletVebView;

    private int lastScreenOrientation = -1;


    public DataletVisualizationFragment() {

    }

    public static DataletVisualizationFragment newInstance(String dataletSrc) {
        DataletVisualizationFragment fragment = new DataletVisualizationFragment();
        Bundle args = new Bundle();
        args.putString(DATALET_SRC_PARAM, dataletSrc);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            dataletSrc = getArguments().getString(DATALET_SRC_PARAM);
        }


        Log.d(TAG, "onCreate: "+dataletSrc);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation != lastScreenOrientation){
            Log.d(TAG, "onConfigurationChanged: " + newConfig.orientation);
            doDataletLayout();
            lastScreenOrientation = newConfig.orientation;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_datalet_visualization, container, false);

        dataletVebView = (WebView) view.findViewById(R.id.datalet_webview);


        Log.d(TAG, "onCreateView: ua="+ dataletVebView.getSettings().getUserAgentString());
        String newUA= "SPODua";

        dataletVebView.getSettings().setUserAgentString(newUA);
        //dataletVebView.getSettings().setUseWideViewPort(true);
        //dataletVebView.getSettings().setLoadWithOverviewMode(true);
        //dataletVebView.getSettings().setSupportZoom(true);
        //dataletVebView.getSettings().setBuiltInZoomControls(true);
        dataletVebView.getSettings().setJavaScriptEnabled(true);
        //dataletVebView.getSettings().setGeolocationEnabled(false);
        //dataletVebView.getSettings().setLoadsImagesAutomatically(true);



        /*dataletVebView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                webViewHeight = dataletVebView.getHeight();

                Log.d(TAG, "onGlobalLayout: height="+webViewHeight);
                Log.d(TAG, "onGlobalLayout: h="+h + " d="+d + " den="+den);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    dataletVebView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else {
                    dataletVebView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });*/


        dataletVebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "onPageFinished: " + view.getUrl());
                doDataletLayout();
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                startActivity(browserIntent);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }
        });

        /*dataletVebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                Log.d(TAG, "onConsoleMessage: " + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }

        });*/
        return view;
    }

    private void doDataletLayout(){
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        int h = dm.heightPixels;
        int dpi = dm.densityDpi;
        float den = dm.density;

        int heightDensityRatio = Math.round(h/den);
        Log.d(TAG, "doDataletLayout: " + h + " " + dpi);

        //int dataletHeight = Math.round((heightDensityRatio * 640)/677);
        float dataletHeight =(h * 150)/dpi;
        String css = String.format("(function(){var a = document.getElementById('datalet_container'); a.style.height='%spx';})()", dataletHeight);

        Log.d(TAG, "doDataletLayout: "+css);
        dataletVebView.loadUrl("javascript: " + css);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        dataletVebView.loadUrl(dataletSrc);

        // Log.d(TAG, "onResume: "+ dataletVebView.getUrl());
    }
}
