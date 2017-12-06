package eu.spod.isislab.spodapp.fragments.cocreation.media;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.UserManager;
import eu.spod.isislab.spodapp.fragments.ItemMapFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class GalleryItemFragment extends Fragment implements View.OnClickListener, Observer{

    public static final String TAG = "GalleryItemFragment";

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

        final ImageView imageView = (ImageView) asView.findViewById(R.id.item_image);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(getActivity())
                .load(this.image)
                .into(imageView);

        ((TextView) asView.findViewById(R.id.item_title)).setText(title);
        ((TextView) asView.findViewById(R.id.item_description)).setText(description);
        ((TextView) asView.findViewById(R.id.item_username)).setText(username);
        ((TextView) asView.findViewById(R.id.item_date)).setText(date);
        ((ImageView) asView.findViewById(R.id.item_location)).setOnClickListener(this);
         NetworkChannel.getInstance().addObserver(this);
         NetworkChannel.getInstance().getUserInfo("", /*UserManager.getInstance().getUsername()*/username);

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
                .addToBackStack(ItemMapFragment.TAG)
                .commit();
    }

    @Override
    public void update(Observable o, Object response) {
        try {
            JSONObject res = new JSONObject((String) response);
            Boolean status = res.getBoolean("status");
            if (status) {
                JSONObject user = new JSONObject(res.getString("user"));

                final ImageView imageView = (ImageView) asView.findViewById(R.id.item_avatar);

                Glide.with(getActivity())
                        .load(user.getString("image"))
                        .apply(new RequestOptions()
                                .centerCrop()
                                .circleCrop()
                                .timeout(10000)
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(imageView);

            }
            NetworkChannel.getInstance().deleteObserver(this);
        }catch(JSONException e){
            e.printStackTrace();
        }

    }
}
