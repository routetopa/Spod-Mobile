package eu.spod.isislab.spodapp.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import android.widget.TextView;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.AddressSolver;

public class GalleryItemFragment extends Fragment implements View.OnClickListener{

    private String title;
    private String description;
    private String image;
    private Location location;

    public void setData(String title, String description, String image,Location location) {
        this.title       = title;
        this.description = description;
        this.image       = image;
        this.location    = location;
    }

    View asView = null;

    public GalleryItemFragment(){ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.gallery_item_fragment, container, false);

       /* DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        WebView iwv = (WebView) asView.findViewById(R.id.item_webview);

        iwv.getSettings().setLoadWithOverviewMode(true);
        iwv.getSettings().setUseWideViewPort(true);
        iwv.setLayoutParams(new LinearLayout.LayoutParams( (int)dpWidth , (int)dpHeight - 100 ));*/

        ((WebView) asView.findViewById(R.id.item_webview)).loadUrl(image);
        ((TextView) asView.findViewById(R.id.item_title)).setText(title);
        ((TextView) asView.findViewById(R.id.item_description)).setText(description);

        TextView address = (TextView) asView.findViewById(R.id.item_location);
        address.setOnClickListener(this);

        new AddressSolver(address, getActivity()).execute(location);

        return asView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        ItemMapFragment mapFragment = new ItemMapFragment();
        mapFragment.setData(title, description, image, location);
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mapFragment)
                .addToBackStack("map_fragment")
                .commit();


    }
}
