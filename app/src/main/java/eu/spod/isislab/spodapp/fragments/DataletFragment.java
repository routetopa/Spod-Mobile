package eu.spod.isislab.spodapp.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.Comment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class DataletFragment extends Fragment {

    public static final String TAG = "DataletFragment";

    Comment comment;

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View asView = inflater.inflate(R.layout.datalet_fragment, container, false);
        ((TextView) asView.findViewById(R.id.datalet_owner_name)).setText(comment.getUsername());
        ((WebView) asView.findViewById(R.id.datalet_comment_body)).loadDataWithBaseURL("", comment.getComment(), "text/html", "UTF-8", "");
        ((TextView) asView.findViewById(R.id.datalet_comment_date)).setText(comment.getTimestamp());

        final ImageView ownerImage = (ImageView) asView.findViewById(R.id.datalet_owner_image);
        Glide.with(getActivity())
                .load(comment.getAvatar_url())
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(ownerImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ownerImage.setImageDrawable(circularBitmapDrawable);
                    }
                });

        WebView webView = (WebView)asView.findViewById(R.id.datalet_webview);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.79 Safari/537.36");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.loadUrl(NetworkChannel.getInstance().getDataletStaticUrl(comment.getDatalet_id()));

        return asView;
    }
}
