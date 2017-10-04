package eu.spod.isislab.spodapp.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.adapters.CocreationRoomsAdapter;
import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.entities.CocreationRoom;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.R;

public class CocreationRoomsListFragment extends Fragment implements Observer, View.OnClickListener{

    View asView = null;

    public CocreationRoomsListFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.cocreation_rooms_list_fragment, container, false);

        FloatingActionButton fab = (FloatingActionButton) asView.findViewById(R.id.cocoreation_add_room);
        fab.setOnClickListener(this);

        return asView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getCocreationMediaRooms();

        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.cocreation_room_list_message));

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onDestroy();
    }

    /*@Override
    public void onResume() {
        NetworkChannel.getInstance().getCocreationMediaRooms();
        NetworkChannel.getInstance().addObserver(this);
        super.onResume();
    }*/

    @Override
    public void onPause() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onPause();
    }

    @Override
    public void update(Observable o, Object arg) {
        ListView listView = (ListView) asView.findViewById(R.id.cocoreation_rooms_list);

        JSONArray response = null;
        try {
            response = new JSONArray((String)arg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<CocreationRoom> rooms = new ArrayList<>();

        for (int i=0; i< response.length(); i++)
        {
            try {
                JSONObject j = response.getJSONObject(i);
                rooms.add(new CocreationRoom(
                        j.getString("name"),
                        j.getString("description"),
                        j.getString("id"),
                        j.getString("sheetId"),
                        j.getString("ownerName"),
                        j.getString("ownerImage"),
                        j.getString("timestamp")));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        listView.setAdapter(new CocreationRoomsAdapter(this.getActivity(), rooms));
    }

    @Override
    public void onClick(View view) {

        this.getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new CreateCocreationRoomFragment())
                .addToBackStack("create_cocoreation_room_fragment")
                .commit();
    }
}
