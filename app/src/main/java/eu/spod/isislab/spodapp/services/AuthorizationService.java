package eu.spod.isislab.spodapp.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.User;
import eu.spod.isislab.spodapp.fragments.CocreationRoomsListFragment;
import eu.spod.isislab.spodapp.fragments.LoginFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AuthorizationService implements Observer {

    private static final String SPOD_AUTH_PREFERENCES_NAME = "eu.spod.isislab.spodapp.preferences.AuthStatePreference";
    private static final String AUTH_STATE                 = "eu.spod.isislab.spodapp.preferences.AUTH_STATE";
    private static final String USED_INTENT                = "eu.spod.isislab.spodapp.preferences.USED_INTENT";

    private Context context;
    private SharedPreferences spodPref;
    private AuthState mAuthState;

    private static final AuthorizationService ourInstance = new AuthorizationService();

    public static AuthorizationService getInstance() {
        return ourInstance;
    }

    private AuthorizationService() { }

    public void init(Context context) {
        this.context = context;
    }

    public String getAccessToken() {
        return mAuthState.getAccessToken();
    }

    public void authorizationRequest(){
        // code from the section 'Making API Calls' goes here
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse(NetworkChannel.getInstance().getSpodEndpoint() + "/oauth2/oauth/authorize") /* auth endpoint */,
                Uri.parse(NetworkChannel.getInstance().getSpodEndpoint() + "/oauth2/oauth/token") /* token endpoint */
        );

        String clientId = "spod-mobile";
        Uri redirectUri = Uri.parse("com.google.codelabs.appauth:/oauth2callback");
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                clientId,
                AuthorizationRequest.RESPONSE_TYPE_CODE,
                redirectUri
        );
        builder.setScopes("authenticate");
        AuthorizationRequest request = builder.build();

        net.openid.appauth.AuthorizationService mAuthorizationService = new net.openid.appauth.AuthorizationService(context);

        String action = "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE";
        Intent postAuthorizationIntent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, request.hashCode(), postAuthorizationIntent, 0);
        mAuthorizationService.performAuthorizationRequest(request, pendingIntent);
    }

    public void handleAuthorizationResponse(@NonNull Intent intent) {

        // code from the step 'Handle the Authorization Response' goes here.
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);

        if (response != null) {
            Log.i("MAIN", String.format("Handled Authorization Response %s ", authState.toJsonString()));
            net.openid.appauth.AuthorizationService service = new net.openid.appauth.AuthorizationService(context);
            service.performTokenRequest(response.createTokenExchangeRequest(), new net.openid.appauth.AuthorizationService.TokenResponseCallback() {
                @Override
                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
                    if (exception != null) {
                        Log.w("MAIN", "Token Exchange failed", exception);
                    } else {
                        if (tokenResponse != null) {
                            authState.update(tokenResponse, exception);
                            persistAuthState(authState);
                            Log.i("MAIN", String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
                        }
                    }
                }
            });
        }
    }

    public void signOut(){
        mAuthState = null;
        clearAuthState();
        enablePostAuthorizationFlows();
    }

    public void enablePostAuthorizationFlows() {
        mAuthState = restoreAuthState();
        if (mAuthState != null && mAuthState.isAuthorized()) {

            mAuthState.performActionWithFreshTokens( new net.openid.appauth.AuthorizationService(context), new AuthState.AuthStateAction() {
                @Override
                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
                    new AsyncTask<String, Void, JSONObject>() {
                        @Override
                        protected JSONObject doInBackground(String... tokens) {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(NetworkChannel.getInstance().getSpodEndpoint() + "/oauth2/oauth/v1/userinfo")
                                    .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                                    .build();
                            try {
                                Response response = client.newCall(request).execute();
                                String jsonBody = response.body().string();
                                Log.i("AUTH", String.format("User Info Response %s", jsonBody));
                                return new JSONObject(jsonBody);
                            } catch (Exception exception) {
                                Log.w("AUTH", exception);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(JSONObject userInfo) {
                            if (userInfo != null) {
                                NetworkChannel.getInstance().addObserver(AuthorizationService.getInstance());
                                NetworkChannel.getInstance().getUserInfo(userInfo.optString("email", null), "");
                            }else{
                                Log.e("Auth", "user email null");
                            }
                        }
                    }.execute(mAuthState.getAccessToken());
                }
            });
        } else {
            ((MainActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.container, new LoginFragment()).addToBackStack("login").commit();
        }
    }

    private void persistAuthState(@NonNull AuthState authState) {
        context.getSharedPreferences(SPOD_AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString(AUTH_STATE, authState.toJsonString())
                .apply();
        enablePostAuthorizationFlows();
    }

    private AuthState restoreAuthState() {
        String jsonString = context.getSharedPreferences(SPOD_AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(AUTH_STATE, null);
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                return AuthState.fromJson(jsonString);
            } catch (JSONException jsonException) {
                // should never happen
            }
        }
        return null;
    }

    private void clearAuthState() {
        context.getSharedPreferences(SPOD_AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(AUTH_STATE)
                .apply();
    }

    @Override
    public void update(Observable o, Object response) {
        try{
            JSONObject res = new JSONObject((String)response);

            switch(NetworkChannel.getInstance().getCurrentService()){
                case NetworkChannel.SERVICE_GET_USER_INFO:

                    Boolean status = res.getBoolean("status");
                    if(status)
                    {
                        NetworkChannel.getInstance().deleteObserver(this);

                        JSONObject user = new JSONObject(res.getString("user"));
                        User.getInstance().init(user.getString("id"), user.getString("username"), user.getString("image"), user.getString("name"));

                        ((MainActivity)context).drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        ((MainActivity)context).getSupportFragmentManager().popBackStack();
                        ((MainActivity)context).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, new CocreationRoomsListFragment())
                                .addToBackStack("cocoreation_room_list")
                                .commit();
                    }
                    break;
            }

        }catch(JSONException e){
            e.printStackTrace();
        }

    }
}
