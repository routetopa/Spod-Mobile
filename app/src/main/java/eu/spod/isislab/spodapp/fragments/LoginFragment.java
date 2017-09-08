package eu.spod.isislab.spodapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.fragments.CocreationRoomsListFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.User;

/**
 * Created by Utente on 28/06/2017.
 */
public class LoginFragment extends Fragment implements View.OnClickListener, Observer, AdapterView.OnItemSelectedListener {

    public static final String SPOD_MOBILE_PREFERENCES   = " eu.spod.isislab.spodapp.preferences.preferences";
    public static final String USERNAME_PREFERENCES      = " eu.spod.isislab.spodapp.preferences.username";
    public static final String PASSWORD_PREFERENCES      = " eu.spod.isislab.spodapp.preferences.password";
    public static final String SPOD_ENDPOINT_PREFERENCES = " eu.spod.isislab.spodapp.preferences.spod_endpoint";

    View asView = null;
    SharedPreferences spodPref;

    public LoginFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.login_fragment, container, false);

        spodPref = getActivity().getSharedPreferences(SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);

        ((TextView)asView.findViewById(R.id.username)).setText(spodPref.getString(USERNAME_PREFERENCES, ""));
        ((TextView)asView.findViewById(R.id.password)).setText(spodPref.getString(PASSWORD_PREFERENCES, ""));

        Button lBtn = (Button) asView.findViewById(R.id.login_button);
        lBtn.setOnClickListener(this);

        Spinner spinner = (Spinner) asView.findViewById(R.id.spod_endpoints_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.spod_endpoints_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int spinnerPosition = adapter.getPosition(spodPref.getString(SPOD_ENDPOINT_PREFERENCES, ""));
        spinner.setSelection(spinnerPosition);

        spinner.setOnItemSelectedListener(this);

        return asView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.login_button:
                NetworkChannel.getInstance().addObserver(this);
                NetworkChannel.getInstance().login(((TextView)asView.findViewById(R.id.username)).getText().toString(),
                                                   ((TextView)asView.findViewById(R.id.password)).getText().toString());
                break;
        }

    }

    @Override
    public void update(Observable o, Object response) {
        try{
            JSONObject res = new JSONObject((String)response);

            switch(NetworkChannel.getInstance().getCurrentService()){
                case NetworkChannel.SERVICE_LOGIN:
                    Boolean result = res.getBoolean("result");
                    if(result){
                        NetworkChannel.getInstance().getUserInfo(((TextView)asView.findViewById(R.id.username)).getText().toString(), "");

                    }else{
                        Snackbar.make(asView, res.getString("message"), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    break;
                case NetworkChannel.SERVICE_GET_USER_INFO:

                    Boolean status = res.getBoolean("status");
                    if(status) {
                        JSONObject user = new JSONObject(res.getString("user"));
                        User.getInstance().init(user.getString("id"), user.getString("username"), user.getString("image"), user.getString("name"));

                        SharedPreferences.Editor editor = spodPref.edit();
                        editor.putString(USERNAME_PREFERENCES, ((TextView)asView.findViewById(R.id.username)).getText().toString());
                        editor.putString(PASSWORD_PREFERENCES, ((TextView)asView.findViewById(R.id.password)).getText().toString());
                        editor.putString(SPOD_ENDPOINT_PREFERENCES, ((Spinner)asView.findViewById(R.id.spod_endpoints_spinner)).getSelectedItem().toString());
                        editor.apply();

                        ((MainActivity)this.getActivity()).drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        this.getActivity().getSupportFragmentManager().popBackStack();
                        this.getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, new CocreationRoomsListFragment())
                                .addToBackStack("cocoreation_room_list")
                                .commit();
                    }

                    NetworkChannel.getInstance().deleteObserver(this);
                    break;
            }

        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences.Editor editor = spodPref.edit();
        editor.putString(SPOD_ENDPOINT_PREFERENCES, ((Spinner)asView.findViewById(R.id.spod_endpoints_spinner)).getSelectedItem().toString());
        editor.apply();
        NetworkChannel.getInstance().init(getActivity());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
