package eu.spod.isislab.spodapp.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.AddressSolver;

public class ItemMapFragment extends Fragment {

    public static final String TAG = "ItemMapFragment";

    private String title;
    private String description;
    private String image;
    private Location location;
    private LatLng selectedPoint;

    public void setData(String title, String description, String image,Location location) {
        this.title       = title;
        this.description = description;
        this.image       = image;
        this.location    = location;
    }

    View asView = null;

    public ItemMapFragment(){ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.map_fragment, container, false);
        ((TextView) asView.findViewById(R.id.map_title)).setText(title);
        ((TextView) asView.findViewById(R.id.map_description)).setText(description);
        new AddressSolver((TextView) asView.findViewById(R.id.map_location), getActivity()).execute(location);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if(mapFragment == null){
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {

                LatLng myLocation = new LatLng(location.getLatitude(),
                        location.getLongitude());

                googleMap.addMarker(new MarkerOptions()
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.logo))
                        .anchor(0.0f, 1.0f)
                        .position(myLocation));

                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,16));

               /* googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().anchor(0.0f, 1.0f).position(point));


                    }
                });*/


            }
        });

        return asView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }

}
