package eu.spod.isislab.spodapp.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.GridView;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.adapters.MediaGallaeryGridAdaper;
import eu.spod.isislab.spodapp.services.SpodLocationServices;
import eu.spod.isislab.spodapp.utils.MediaGalleryItem;
import eu.spod.isislab.spodapp.R;

public class MediaGalleryScreenSliderFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener
{

    private MediaGallaeryGridAdaper gridAdapter;
    private String sheetId;

    public void setItems(ArrayList<MediaGalleryItem> items) {
        this.items = items;
    }

    ArrayList<MediaGalleryItem> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.media_gallery_screen_slider_fragment, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                rootView.findViewById(R.id.room_list_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        gridAdapter = new MediaGallaeryGridAdaper(getActivity());
        gridAdapter.setItems(items);

        GridView grid = (GridView)rootView.findViewById(R.id.image_gridview);
        grid.setAdapter(this.gridAdapter);

        return rootView;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottom_bar_room_list_add:
                if(SpodLocationServices.isAvailable()) {

                    GalleryAddItemFragment addItemFragment = new GalleryAddItemFragment();
                    addItemFragment.setSheetId(sheetId);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, addItemFragment)
                            .addToBackStack("gallery_add_item_fragment")
                            .commit();

                }else{
                    Snackbar.make(getActivity().findViewById(R.id.container), "Please make GPS available to add new geolocalized item", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;

        }
        return true;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }
}
