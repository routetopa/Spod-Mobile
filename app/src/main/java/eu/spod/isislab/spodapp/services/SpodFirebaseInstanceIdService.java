package eu.spod.isislab.spodapp.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.fragments.LoginFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class SpodFirebaseInstanceIdService extends FirebaseInstanceIdService {

    public static final String SHARED_PREF_FIREBASE_TOKEN = "eu.spod.isislab.spodapp.services.SpodFirebaseInstanceIdService.firebaseToken";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Firebase", "Refreshed token: " + refreshedToken);
        storeToken(refreshedToken);
    }

    public boolean storeToken(String token){
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(LoginFragment.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences
                .edit()
                .putString(SHARED_PREF_FIREBASE_TOKEN, token)
                .apply();
        return true;
    }

}
