package eu.spod.isislab.spodapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import eu.spod.isislab.spodapp.services.AuthorizationService;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class LoginFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final String SPOD_MOBILE_PREFERENCES    = "eu.spod.isislab.spodapp.preferences.preferences";
    public static final String SPOD_ENDPOINT_PREFERENCES  = "eu.spod.isislab.spodapp.preferences.spod_endpoint";
    public static final String USED_INTENT                = "eu.spod.isislab.spodapp.preferences.USED_INTENT";


    View asView = null;
    SharedPreferences spodPref;

    public LoginFragment(){ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.login_fragment, container, false);

        spodPref = getActivity().getSharedPreferences(SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);

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
        checkIntent(getActivity().getIntent());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.login_button:
                SharedPreferences.Editor editor = spodPref.edit();
                editor.putString(SPOD_ENDPOINT_PREFERENCES, ((Spinner)asView.findViewById(R.id.spod_endpoints_spinner)).getSelectedItem().toString());
                editor.apply();

                NetworkChannel.getInstance().setSpodEndpoint(((Spinner)asView.findViewById(R.id.spod_endpoints_spinner)).getSelectedItem().toString());
                AuthorizationService.getInstance().authorizationRequest();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences.Editor editor = spodPref.edit();
        editor.putString(SPOD_ENDPOINT_PREFERENCES, ((Spinner)asView.findViewById(R.id.spod_endpoints_spinner)).getSelectedItem().toString());
        editor.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void checkIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE":
                    if (!intent.hasExtra(USED_INTENT)) {
                        AuthorizationService.getInstance().handleAuthorizationResponse(intent);
                        intent.putExtra(USED_INTENT, true);
                    }
                    break;
                default:
                    // do nothing
            }
        }
    }

}
