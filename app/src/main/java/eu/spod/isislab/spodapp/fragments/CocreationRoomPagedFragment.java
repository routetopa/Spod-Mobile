package eu.spod.isislab.spodapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.MediaGalleryScreenSlidePagerAdapter;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.ZoomOutPageTransformer;

public class CocreationRoomPagedFragment extends CocreationRoomFragment {

    private View asView = null;
    private ViewPager mPager;
    private MediaGalleryScreenSlidePagerAdapter mPagerAdapter;

    String roomName;
    String roomId;
    String sheetId;

    public CocreationRoomPagedFragment(){}

    public void setRoom(String roomName, String roomId, String sheetId){

        this.roomName = roomName;
        this.roomId   = roomId;
        this.sheetId  = sheetId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.cocreation_room_fragment, container, false);

        mPager = (ViewPager) asView.findViewById(R.id.pager);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());

        return asView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getSheetData(roomId);

        ((MainActivity)getActivity()).setToolbarTitle(this.roomName);
        mPagerAdapter = new MediaGalleryScreenSlidePagerAdapter(getActivity().getSupportFragmentManager());

        super.onActivityCreated(savedInstanceState);
    }

    public void refreshData(){
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getSheetData(roomId);
    }

    @Override
    public void onPause() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        NetworkChannel.getInstance().getSheetData(roomId);
        super.onResume();
    }

    @Override
    public void update(Observable o, Object arg) {
        super.update(o,arg);
        try {
            mPagerAdapter.setData(response, sheetId);
            mPager.setAdapter(mPagerAdapter);
        }catch (Exception e){
            e.printStackTrace();
        }
        NetworkChannel.getInstance().deleteObserver(this);
    }
}
