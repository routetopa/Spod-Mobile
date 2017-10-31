package eu.spod.isislab.spodapp.services;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class SpodFirebaseInstanceIdService extends FirebaseInstanceIdService implements Observer {

    private static final String SHARED_PREF_FIREBASE_TOKEN = "eu.spod.isislab.spodapp.services.SpodFirebaseInstanceIdService.firebaseToken";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Firebase", "Refreshed token: " + refreshedToken);
        storeToken(refreshedToken);
        NetworkChannel.getInstance().addRegistrationId(refreshedToken);
    }

    public boolean storeToken(String token){
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(SHARED_PREF_FIREBASE_TOKEN, MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Firebase token", token);
        editor.apply();
        return true;
    }

    @Override
    public void update(Observable o, Object response)
    {
        try {
            JSONObject res = new JSONObject((String)response);
            if(res.getBoolean("status")){
                Log.e("InstanceService", "Firebase token saved");
            }else {
                Log.e("InstanceService", "Firebase token saving error: " + res.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
