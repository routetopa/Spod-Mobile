package eu.spod.isislab.spodapp.fragments.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.fragments.LoginFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class SettingsFragment extends Fragment implements Observer, View.OnClickListener
{
    public static String COCREATION_PLUGIN                    = "cocreation";
    public static String COCREATION_ACTION_NEW_ROOM           = COCREATION_PLUGIN + "_new_room";
    public static String COCREATION_ACTION_COMMENT            = COCREATION_PLUGIN + "_add_comment";
    public static String COCREATION_ACTION_INVITE             = COCREATION_PLUGIN + "_invite";
    public static String COCREATION_ACTION_DATASET_PUBLISHED  = COCREATION_PLUGIN + "_dataset_published";
    public static String AGORA_PLUGIN                         = "agora";
    public static String AGORA_ACTION_NEW_ROOM                = AGORA_PLUGIN + "_new_room";
    public static String AGORA_ACTION_COMMENT                 = AGORA_PLUGIN + "_add_comment";
    public static String AGORA_ACTION_MENTION                 = AGORA_PLUGIN + "_mention";
    public static String AGORA_ACTION_REPLAY                  = AGORA_PLUGIN + "_replay";


    View asView;
    Switch currentPreferenceSwitch;
    Spinner currentFrequencySpinner;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.settings_fragment, container, false);
        initUIElements((LinearLayout) asView.findViewById(R.id.settings_main_container));
        return asView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        NetworkChannel.getInstance().addObserver(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    public void update(Observable o, Object arg) {
           try {
               JSONObject res = new JSONObject((String)arg);
               Snackbar.make(getActivity().findViewById(R.id.container), res.getString("message"), Snackbar.LENGTH_LONG)
                       .setAction("Action", null).show();

               SharedPreferences.Editor editor = getActivity().getSharedPreferences(LoginFragment.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE).edit();
               editor.putBoolean(NetworkChannel.getInstance().getSpodEndpoint() + getResources().getResourceEntryName(currentPreferenceSwitch.getId()), currentPreferenceSwitch.isChecked());
               editor.putInt(NetworkChannel.getInstance().getSpodEndpoint() + getResources().getResourceEntryName(currentFrequencySpinner.getId()), currentFrequencySpinner.getSelectedItemPosition());
               editor.apply();

           } catch (JSONException e) {
               e.printStackTrace();
           }
    }

    @Override
    public void onClick(View v) {
        String plugin = null, action = null, subAction = "", status, frequency = "1";

        if(getResources().getResourceEntryName(v.getId()).contains(COCREATION_PLUGIN)) plugin = COCREATION_PLUGIN;
        if(getResources().getResourceEntryName(v.getId()).contains(AGORA_PLUGIN))      plugin = AGORA_PLUGIN;

        currentPreferenceSwitch = (Switch)v;
        status = (currentPreferenceSwitch.isChecked()) ? "true" : "false";

        switch(v.getId()){
            case R.id.settings_cocreation_new_room_toggle:
                action = COCREATION_ACTION_NEW_ROOM;
                currentFrequencySpinner =  (Spinner)asView.findViewById(R.id.settings_cocreation_new_room_spinner);
               break;
            case R.id.settings_cocreation_comment_room_toggle:
                action = COCREATION_ACTION_COMMENT;
                currentFrequencySpinner =  (Spinner)asView.findViewById(R.id.settings_cocreation_comment_room_spinner);
                break;
            case R.id.settings_cocreation_invite_room_toggle:
                action = COCREATION_ACTION_INVITE;
                currentFrequencySpinner =  (Spinner)asView.findViewById(R.id.settings_cocreation_invite_room_spinner);
                break;
            case R.id.settings_cocreation_dataset_publication_toggle:
                action = COCREATION_ACTION_DATASET_PUBLISHED;
                currentFrequencySpinner =  (Spinner)asView.findViewById(R.id.settings_cocreation_dataset_publication_spinner);
                break;
            case R.id.settings_agora_new_room_toggle:
                action = AGORA_ACTION_NEW_ROOM;
                currentFrequencySpinner =  (Spinner)asView.findViewById(R.id.settings_agora_new_room_spinner);
                break;
            case R.id.settings_agora_comment_room_toggle:
                action = AGORA_ACTION_COMMENT;
                currentFrequencySpinner =  (Spinner)asView.findViewById(R.id.settings_agora_comment_room_spinner);
                break;
            case R.id.settings_agora_mention_toggle:
                action = AGORA_ACTION_MENTION;
                currentFrequencySpinner =  (Spinner)asView.findViewById(R.id.settings_agora_mention_spinner);
                break;
            case R.id.settings_agora_replay_toggle:
                action = AGORA_ACTION_REPLAY;
                currentFrequencySpinner =  (Spinner)asView.findViewById(R.id.settings_agora_replay_spinner);
                break;
        }
        NetworkChannel.getInstance().saveMobileNotification(status, plugin, action, subAction, "" + (currentFrequencySpinner.getSelectedItemPosition() + 1));
    }

    private void initUIElements(LinearLayout container)
    {
        SharedPreferences spodPref =  getActivity().getSharedPreferences(LoginFragment.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);
        int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = container.getChildAt(i);
            if (v instanceof LinearLayout)
                initUIElements((LinearLayout)v);
            if (v instanceof Switch) {
                v.setOnClickListener(this);
                ((Switch)v).setChecked(spodPref.getBoolean(NetworkChannel.getInstance().getSpodEndpoint() + getResources().getResourceEntryName(v.getId()), false));
            }
            if(v instanceof Spinner){
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        getActivity(), R.array.settings_menu_spinner, R.layout.settings_spinner_layout);
                adapter.setDropDownViewResource(R.layout.settings_spinner_layout);
                ((Spinner)v).setAdapter(adapter);
                ((Spinner)v).setSelection(spodPref.getInt(NetworkChannel.getInstance().getSpodEndpoint() + getResources().getResourceEntryName(v.getId()), 0));
            }
        }
    }

}