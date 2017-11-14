package eu.spod.isislab.spodapp.fragments.cocreation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.CocreationRoom;
import eu.spod.isislab.spodapp.utils.UserManager;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationRoomFragment extends Fragment implements Observer {

    public static final String TAG = "CocreationRoomFragment";

    public CocreationRoom room;
    public JSONArray response;

    public CocreationRoomFragment(){ }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(room.getOwnerId().equals(UserManager.getInstance().getId()))
            menu.add(0, 1, 0, "Members").setIcon(R.drawable.ic_person_white_48dp)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("Members"))
        {
            MembersFragment membersFragment = new MembersFragment();
            membersFragment.setRoom(room);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, membersFragment, MembersFragment.TAG)
                    .addToBackStack(MembersFragment.TAG)
                    .commit();

        }
        return super.onOptionsItemSelected(item);
    }

    public void setRoom(CocreationRoom room){
        this.room = room;
    }

    public void refreshData(){
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getSheetData(this.room.getId());
    }

    @Override
    public void onPause() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onPause();
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            response = new JSONArray((String) arg);
        }catch (Exception e){
            e.printStackTrace();
        }
        NetworkChannel.getInstance().deleteObserver(this);
    }
}
