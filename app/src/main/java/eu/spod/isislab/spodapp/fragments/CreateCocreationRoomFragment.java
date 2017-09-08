package eu.spod.isislab.spodapp.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.services.SpodLocationServices;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.ZoomOutPageTransformer;

/**
 * Created by Utente on 18/07/2017.
 */
public class CreateCocreationRoomFragment extends Fragment implements Observer, OnNavigationItemSelectedListener {
    private View asView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.create_cocreation_room_fragment, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) asView.findViewById(R.id.new_room_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        return asView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        NetworkChannel.getInstance().addObserver(this);
        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.cocreation_room_head_message));
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottom_bar_create_new_room:
                String name            = ((TextView)asView.findViewById(R.id.new_room_name)).getText().toString();
                String subject         = ((TextView)asView.findViewById(R.id.new_room_subject)).getText().toString();
                String description     = ((TextView)asView.findViewById(R.id.new_room_description)).getText().toString();
                String goal            = ((TextView)asView.findViewById(R.id.new_room_goal)).getText().toString();
                String invitation_text = ((TextView)asView.findViewById(R.id.new_room_invitation_text)).getText().toString();

                if(name.isEmpty() || subject.isEmpty() || description.isEmpty() || goal.isEmpty() || invitation_text.isEmpty()){
                    Snackbar.make(getActivity().findViewById(R.id.container), getString(R.string.cocreation_fill_form_correctly), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{
                    NetworkChannel.getInstance().createCocreationRoom(name, subject, description, goal, invitation_text);
                }
                break;
        }
        return true;
    }

    @Override
    public void update(Observable o, Object response) {
        NetworkChannel.getInstance().deleteObserver(this);
        try{
            JSONObject res = new JSONObject((String)response);
            if(res.getBoolean("status")){
                Snackbar.make(asView, getString(R.string.cocreation_room_created), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                this.getActivity().getSupportFragmentManager().popBackStack();

            }else{
                Snackbar.make(asView, res.getString("message"), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}
