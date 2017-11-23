package eu.spod.isislab.spodapp.fragments.cocreation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationWebContentFragment extends Fragment {

    public static final String TAG = "CocreationWebContentFragment";
    public static final String DATALETS_TEMPLATE = "datalets";
    public static final String METADATA_TEMPLATE = "metadata";

    String resourceUrl = null;
    String template    = null;
    String data        = null;
    String addon       = null;
    WebView webView    = null;

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public void setTemplate(String template, String data, String addon) {
        this.template = template;
        this.data     = data;
        this.addon    = addon;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.cocoreation_web_content_fragment, container, false);

        Glide.with(getActivity())
                .asGif()
                .load(R.drawable.jelly_fluid_loader)
                .into((ImageView)rootView.findViewById(R.id.cocoreation_web_content_loader_image));

        webView = (WebView)rootView.findViewById(R.id.cocoreation_web_content_webview);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                ((LinearLayout) rootView.findViewById(R.id.cocoreation_web_content_loader)).setLayoutParams(new AppBarLayout.LayoutParams(0,0));
                super.onPageFinished(view, url);
            }
        });

        if( resourceUrl == null ){
            webView.loadData(loadFromTemplate(), "text/html", null);
        }else if( resourceUrl != null ) {
            webView.loadUrl(resourceUrl);
        }

        return rootView;
    }

    private String loadFromTemplate(){
        String sourceCode = null;
        try {
            int template_resource_id = 0;
            switch(template){
                case METADATA_TEMPLATE:
                    template_resource_id = R.raw.metadata_template;
                    break;
                case DATALETS_TEMPLATE:
                    template_resource_id = R.raw.datalets_template;
                    break;
            }

            InputStream inputStream = getActivity().getResources().openRawResource(template_resource_id);
            byte[] code = new byte[inputStream.available()];
            inputStream.read(code);

            sourceCode = new String(code);
            sourceCode = sourceCode.replaceAll("#DEEP_ENDPOINT#", Consts.DEEP_ENDPOINT);
            sourceCode = sourceCode.replaceAll("#DATA#", data);
            sourceCode = sourceCode.replaceAll("#ADDON#", addon);

            inputStream.close();

        } catch (IOException e) {
            Log.e("LOAD_TEMPLATE", "Couldn't open template page for " + template, e);
        }

        return sourceCode;
    }
}
