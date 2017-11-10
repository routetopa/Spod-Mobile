package eu.spod.isislab.spodapp.fragments.cocreation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.MembersAdapter;
import eu.spod.isislab.spodapp.entities.CocreationRoom;
import eu.spod.isislab.spodapp.entities.User;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class MembersFragment extends Fragment implements Observer,  BottomNavigationView.OnNavigationItemSelectedListener{

    ViewGroup asView;
    CocreationRoom room;
    MembersAdapter adapter;

    public void setRoom(CocreationRoom room) {
        this.room = room;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        asView = (ViewGroup) inflater.inflate(
                R.layout.members_fragment, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
        asView.findViewById(R.id.members_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        setHasOptionsMenu(true);

        ((TextView)asView.findViewById(R.id.members_search_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                adapter.doFilter(s.toString());
            }
        });

        return asView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, "Search").setIcon(android.R.drawable.ic_menu_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("Search")){
            LinearLayout searchBar = (LinearLayout)asView.findViewById(R.id.members_search_bar);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().cocreationGetAllFriends(room.getId());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        ListView listView = (ListView) asView.findViewById(R.id.members_list);
        listView.setScrollingCacheEnabled(false);

        ArrayList<User> members = new ArrayList<>();
        try {
            JSONObject res = new JSONObject((String)arg);
            if(res.getBoolean("status")){
                JSONArray friends = res.getJSONArray("friends");
                for (int i=0; i< friends.length(); i++)
                {
                    try {
                        JSONObject j = friends.getJSONObject(i);
                        User u = new User(
                                j.getString("id"),
                                j.getString("username"),
                                j.getString("avatar"),
                                j.getString("name"));
                        u.setEmail(j.getString("email"));
                        members.add(u);

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new MembersAdapter(this.getActivity(), members);
        listView.setAdapter(adapter);
        NetworkChannel.getInstance().deleteObserver(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
