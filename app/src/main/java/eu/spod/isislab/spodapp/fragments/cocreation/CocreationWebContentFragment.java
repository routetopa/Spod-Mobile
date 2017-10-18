package eu.spod.isislab.spodapp.fragments.cocreation;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationWebContentFragment extends Fragment {

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

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.cocoreation_dataset_fragment, container, false);

        webView = (WebView)rootView.findViewById(R.id.cocoreation_dataset_webview);
        webView.getSettings().setJavaScriptEnabled(true);

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
                case "metadata":
                    template_resource_id = R.raw.metadata_template;
                    break;
                case "datalets":
                    template_resource_id = R.raw.datalets_template;
                    break;
            }

            InputStream inputStream = getActivity().getResources().openRawResource(template_resource_id);
            byte[] code = new byte[inputStream.available()];
            inputStream.read(code);

            sourceCode = new String(code);
            sourceCode = sourceCode.replaceAll("#DEEP_ENDPOINT#", NetworkChannel.DEEP_ENDPOINT);
            sourceCode = sourceCode.replaceAll("#DATA#", data);
            sourceCode = sourceCode.replaceAll("#ADDON#", addon);

            inputStream.close();

        } catch (IOException e) {
            Log.e("LOAD_TEMPLATE", "Couldn't open template page for " + template, e);
        }

        return sourceCode;
    }
}
