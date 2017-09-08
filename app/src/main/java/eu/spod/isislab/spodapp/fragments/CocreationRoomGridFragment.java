package eu.spod.isislab.spodapp.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import java.util.Observable;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.ImageAdapter;
import eu.spod.isislab.spodapp.services.SpodLocationServices;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationRoomGridFragment extends CocreationRoomFragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ImageAdapter gridAdapter;

    String roomName;
    String roomId;
    String sheetId;
    GridView grid;

    public void setRoom(String roomName, String roomId, String sheetId){

        this.roomName = roomName;
        this.roomId   = roomId;
        this.sheetId  = sheetId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.media_gallery_screen_slider_fragment, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                rootView.findViewById(R.id.room_list_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        gridAdapter = new ImageAdapter(getActivity());
        grid = (GridView)rootView.findViewById(R.id.image_gridview);
        grid.setNumColumns(GridView.AUTO_FIT);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getSheetData(roomId);

        ((MainActivity)getActivity()).setToolbarTitle(roomName);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void update(Observable o, Object arg) {
        super.update(o,arg);
        try {
            gridAdapter.setData(response);
            grid.setAdapter(gridAdapter);
        }catch (Exception e){
            e.printStackTrace();
        }
        NetworkChannel.getInstance().deleteObserver(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottom_bar_room_list_add:
                boolean ls_available = SpodLocationServices.isAvailable();
                Location currentL = SpodLocationServices.getCurrentLocation();
                if(ls_available || currentL == null) {

                    GalleryAddItemFragment addItemFragment = new GalleryAddItemFragment();
                    addItemFragment.setSheetId(sheetId);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, addItemFragment)
                            .addToBackStack("gallery_add_item_fragment")
                            .commit();

                }else{
                    Snackbar.make(getActivity().findViewById(R.id.container), getString(R.string.cocreation_gps_not_available), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
        }
        return true;
    }
}
