package eu.spod.isislab.spodapp.fragments.agora;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
    AgoraRoomsAdapter adapter;

    public AgoraRoomsListFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.agora_rooms_list_fragment, container, false);

        FloatingActionButton fab = (FloatingActionButton) asView.findViewById(R.id.agora_add_room);
        fab.setOnClickListener(this);

        setHasOptionsMenu(true);

        ((TextView)asView.findViewById(R.id.agora_search_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                adapter.doFilter( s.toString() );
            }
        });

        return asView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, "Search").setIcon(android.R.drawable.ic_menu_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //getActivity().invalidateOptionsMenu();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("Search")){
            LinearLayout searchBar = (LinearLayout)asView.findViewById(R.id.agora_search_bar);
            if(searchBar.getHeight() == 0) {
                searchBar.setLayoutParams(new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                searchBar.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));
            }else{
                searchBar.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right));
                searchBar.setLayoutParams(new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0 ));
            }
        }
        return super.onOptionsItemSelected(item);
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

        if(NetworkChannel.getInstance().getCurrentService().equals(NetworkChannel.SERVICE_AGORA_GET_ROOMS))
        {
            ListView listView = (ListView) asView.findViewById(R.id.agora_rooms_list);
            listView.setScrollingCacheEnabled(false);

            JSONArray response = null;
            try {
                response = new JSONArray((String)arg);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
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

            adapter = new AgoraRoomsAdapter(this.getActivity(), rooms);
            listView.setAdapter(adapter);
        }

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
