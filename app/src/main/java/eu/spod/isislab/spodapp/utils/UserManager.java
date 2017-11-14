package eu.spod.isislab.spodapp.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.entities.User;
import eu.spod.isislab.spodapp.fragments.LoginFragment;
import eu.spod.isislab.spodapp.services.SpodFirebaseInstanceIdService;

public class UserManager implements Observer {

    private User user;
    private Context mContext;

    private static UserManager ourInstance = new UserManager();

    private UserManager() {}

    public void init(Context ctx, String id, String username, String avatarImage, String name){
        this.user = new User(id, username, avatarImage, name);
        this.mContext = ctx;
        String refreshedToken =  this.mContext.getSharedPreferences(Consts.SPOD_MOBILE_PREFERENCES, MainActivity.MODE_PRIVATE)
                .getString(Consts.SHARED_PREF_FIREBASE_TOKEN,"");
        if(!refreshedToken.isEmpty()) {
            NetworkChannel.getInstance().addObserver(this);
            NetworkChannel.getInstance().addRegistrationId(refreshedToken);
        }
    }

    public static UserManager getInstance() {
        return ourInstance;
    }

    public String getId() {
        return user.getId();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getAvatarImage() {
        return user.getAvatarImage();
    }

    public String getName() {
        return user.getName();
    }

    public void setName(String name) { user.setName(name); }

    @Override
    public void update(Observable o, Object response)
    {
        try {
            JSONObject res = new JSONObject((String)response);
            if(res.getBoolean("status")){
                Log.e("UserManager", "Firebase token saved");
            }else {
                Log.e("UserManager", "Firebase token saving error: " + res.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        NetworkChannel.getInstance().deleteObserver(this);
    }
}
