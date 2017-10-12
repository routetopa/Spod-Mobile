package eu.spod.isislab.spodapp.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.AgoraRoomsAdapter;
import eu.spod.isislab.spodapp.entities.AgoraRoom;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

/**
 * Created by Utente on 08/09/2017.
 */
public class AgoraRoomsListFragment extends Fragment implements Observer, View.OnClickListener {

    View asView = null;

    public AgoraRoomsListFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.agora_rooms_list_fragment, container, false);

        FloatingActionButton fab = (FloatingActionButton) asView.findViewById(R.id.agora_add_room);
        fab.setOnClickListener(this);

        return asView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getAgoraRooms();

        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.agora_room_list_message));

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        NetworkChannel.getInstance().addObserver(this);
        super.onResume();
    }

    @Override
    public void update(Observable o, Object arg) {

        ListView listView = (ListView) asView.findViewById(R.id.agora_rooms_list);
        listView.setScrollingCacheEnabled(false);

        JSONArray response = null;
        try {
            response = new JSONArray((String)arg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<AgoraRoom> rooms = new ArrayList<>();

        for (int i=0; i< response.length(); i++)
        {
            try {
                JSONObject j = response.getJSONObject(i);
                //Log.e("", j.toString());
                rooms.add(new AgoraRoom(
                        j.getString("ownerId"),
                        j.getString("subject"),
                        j.getString("body"),
                        j.getString("views"),
                        j.getString("comments"),
                        j.getString("opendata"),
                        j.getString("timestamp"),
                        j.getString("post"),
                        j.getString("id"),
                        j.getString("datalet_graph")));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        listView.setAdapter(new AgoraRoomsAdapter(this.getActivity(), rooms));

    }

    @Override
    public void onClick(View v) {

        this.getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new CreateAgoraRoomFragment())
                .addToBackStack("create_cocoreation_room_fragment")
                .commit();

    }
}
