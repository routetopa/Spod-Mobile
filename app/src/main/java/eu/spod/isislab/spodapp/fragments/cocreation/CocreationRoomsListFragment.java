package eu.spod.isislab.spodapp.fragments.cocreation;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
    CocreationRoomsAdapter adapter;
    String currentRoomType  = "all";
    String currentSearchKey = "";

    public CocreationRoomsListFragment(){
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.cocreation_rooms_list_fragment, container, false);

        FloatingActionButton fab = (FloatingActionButton) asView.findViewById(R.id.cocoreation_add_room);
        fab.setOnClickListener(this);

        setHasOptionsMenu(true);

        ((TextView)asView.findViewById(R.id.cocreation_search_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                currentSearchKey = s.toString();
                adapter.doFilter(currentSearchKey, currentRoomType);
            }
        });

        Spinner spinner = (Spinner)asView.findViewById(R.id.cocreation_search_spinner);
        spinner.setSelection(0,false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentRoomType = CocreationRoomsAdapter.ROOM_TYPES[position];
                adapter.doFilter(currentSearchKey, currentRoomType );
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return asView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getCocreationRooms();

        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.cocreation_room_list_message));

        super.onActivityCreated(savedInstanceState);
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
            LinearLayout searchBar = (LinearLayout)asView.findViewById(R.id.cocreation_search_bar);
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
    public void onDestroy() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onDestroy();
    }


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

                ArrayList<String> docs = new ArrayList<>();
                JSONArray jsonArray = j.getJSONArray("docs");
                for(int z = 0, count = jsonArray.length(); z< count; z++)
                        docs.add(jsonArray.getJSONObject(z).getString("url"));

                //if( j.getString("type").equals("media"))
                //{
                    rooms.add(new CocreationRoom(
                            Html.fromHtml(j.getString("name")).toString(),
                            Html.fromHtml(j.getString("description")).toString(),
                            j.getString("id"),
                            j.getString("sheetId"),
                            j.getString("ownerName"),
                            j.getString("ownerImage"),
                            j.getString("timestamp"),
                            j.getString("type"),
                            docs));
                //}
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter = new CocreationRoomsAdapter(this.getActivity(), rooms);
        listView.setAdapter(adapter);
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
