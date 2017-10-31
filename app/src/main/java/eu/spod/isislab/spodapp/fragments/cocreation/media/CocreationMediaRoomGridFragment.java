package eu.spod.isislab.spodapp.fragments.cocreation.media;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import java.util.Observable;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.ImageAdapter;
import eu.spod.isislab.spodapp.fragments.cocreation.CocreationRoomFragment;
import eu.spod.isislab.spodapp.services.SpodLocationService;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationMediaRoomGridFragment extends CocreationRoomFragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ImageAdapter gridAdapter;

    GridView grid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.media_gallery_screen_slider_fragment, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                rootView.findViewById(R.id.cocoreation_room_media_list_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        gridAdapter = new ImageAdapter(getActivity());
        grid = (GridView)rootView.findViewById(R.id.image_gridview);
        grid.setNumColumns(GridView.AUTO_FIT);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getSheetData(room.getId());
        //NetworkChannel.getInstance().connectCocreationWebSocket(room.getId());

        ((MainActivity)getActivity()).setToolbarTitle(room.getName());

        super.onActivityCreated(savedInstanceState);
    }


    public void refreshData() {
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getSheetData(this.room.getId());
    }

    @Override
    public void update(Observable o, Object arg) {
        super.update(o,arg);

         switch (NetworkChannel.getInstance().getCurrentService()){
            case NetworkChannel.SERVICE_SYNC_NOTIFICATION:

                //Log.e("SYNC_NOTIFICATION", arg.toString());

                break;
            case NetworkChannel.SERVICE_COCREATION_GET_SHEET_DATA:
                try {
                    gridAdapter.setData(response);
                    grid.setAdapter(gridAdapter);
                    gridAdapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        NetworkChannel.getInstance().addObserver(this);
    }

    @Override
    public void onPause() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        NetworkChannel.getInstance().deleteObserver(this);
        NetworkChannel.getInstance().closeWebSocket();
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottom_bar_room_list_add:
                boolean ls_available = SpodLocationService.isAvailable();
                Location currentL = SpodLocationService.getCurrentLocation();
                if(ls_available || currentL == null) {

                    GalleryAddItemFragment addItemFragment = new GalleryAddItemFragment();
                    addItemFragment.setSheetId(room.getSheetId());
                    addItemFragment.setCocreationRoomGridFragment(this);

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
