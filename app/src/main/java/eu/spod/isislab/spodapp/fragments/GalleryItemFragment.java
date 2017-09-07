package eu.spod.isislab.spodapp.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Network;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.AddressSolver;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.User;

public class GalleryItemFragment extends Fragment implements View.OnClickListener, Observer{

    private String title;
    private String description;
    private String image;
    private Location location;
    private String date;
    private String username;

    public void setData(String title, String description, String image, Location location, String date, String username) {
        this.title       = title;
        this.description = description;
        this.image       = image;
        this.location    = location;
        this.date        = date;
        this.username    = username;
    }

    View asView = null;

    public GalleryItemFragment(){ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.gallery_item_fragment, container, false);

        /*DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;*/

        ImageView imageView = (ImageView) asView.findViewById(R.id.item_image);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(getActivity())
                .load(image)
                .into(imageView);

        ((TextView) asView.findViewById(R.id.item_title)).setText(title);
        ((TextView) asView.findViewById(R.id.item_description)).setText(description);
        ((TextView) asView.findViewById(R.id.item_username)).setText(username);
        ((TextView) asView.findViewById(R.id.item_date)).setText(date);
        ((ImageView) asView.findViewById(R.id.item_location)).setOnClickListener(this);
         NetworkChannel.getInstance().addObserver(this);
         NetworkChannel.getInstance().getUserInfo("", User.getInstance().getUsername());

        /*TextView address = (TextView) asView.findViewById(R.id.item_location);
        address.setOnClickListener(this);

        new AddressSolver(address, getActivity()).execute(location);*/

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

    @Override
    public void update(Observable o, Object response) {
        try {
            JSONObject res = new JSONObject((String) response);
            Boolean status = res.getBoolean("status");
            if (status) {
                JSONObject user = new JSONObject(res.getString("user"));

                Glide.with(getActivity())
                        .load(user.getString("image"))
                        .into((ImageView) asView.findViewById(R.id.item_avatar));

            }
            NetworkChannel.getInstance().deleteObserver(this);
        }catch(JSONException e){
            e.printStackTrace();
        }

    }
}
