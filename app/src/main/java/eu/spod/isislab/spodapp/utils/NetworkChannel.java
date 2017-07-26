 package eu.spod.isislab.spodapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.BitmapCompat;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.fragments.LoginFragment;
import eu.spod.isislab.spodapp.services.SpodLocationServices;

public class NetworkChannel extends Observable
{
    public static final String SERVICE_LOGIN                 = "SERVICE_LOGIN";
    public static final String SERVICE_GET_USER_INFO         = "SERVICE_GET_USER_INFO";

    private static String SPOD_ENDPOINT                          =  "";
    private static final String POST_LOGIN_HANDLER               = "/openid/ajax.php";//"/base/user/ajax-sign-in/";
    private static final String POST_USER_INFO                   = "/cocreation/ajax/get-user-info/";
    private static final String POST_ADD_NEW_ROW                 = "/ethersheet/mediaroom/addrow/";
    private static final String POST_COCREATION_CREATE_ROOM      = "/cocreation/ajax/create-media-room-from-mobile/";
    private static final String GET_COCREATION_MEDIA_ROOMS_ADDR  = "/cocreation/ajax/get-media-rooms-by-user-id/?userId=1";
    private static final String GET_COCREATION_ROOMS_SHEET_DATA  = "/cocreation/ajax/get-sheet-data-by-room-id/?roomId=";

    private int IMAGE_SIZE_LIMIT = 1048576;

    private static NetworkChannel ourInstance = new NetworkChannel();

    private Activity mainActivity      = null;
    private RequestQueue mRequestQueue = null;
    private String currentService      = null;

    public static NetworkChannel getInstance() {
        return ourInstance;
    }

    private NetworkChannel() {
    }

    public void init(Activity mainActivity){
        this.mainActivity = mainActivity;
        mRequestQueue= Volley.newRequestQueue(this.mainActivity);
        SharedPreferences spodPref = mainActivity.getSharedPreferences(LoginFragment.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);
        SPOD_ENDPOINT = "http://" +  spodPref.getString(LoginFragment.SPOD_ENDPOINT_PREFERENCES, "");
    }

    public String getCurrentService(){
        return currentService;
    }

    // added as an instance method to an Activity
    boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    public void login(final String username, final String password)
    {
        currentService = SERVICE_LOGIN;
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);
        StringRequest postRequest = new StringRequest(Request.Method.POST, SPOD_ENDPOINT + POST_LOGIN_HANDLER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        setChanged();
                        notifyObservers(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        unavailableNetworkMessage(error);
                        Snackbar.make(mainActivity.findViewById(R.id.container), "There are some problem with server connection!!!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }
                }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("identity", username);
                    params.put("password", password);
                    return params;
                }
        };
        mRequestQueue.add(postRequest);
    }

    public void getUserInfo(final String email){
        currentService = SERVICE_GET_USER_INFO;
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);
        StringRequest postRequest = new StringRequest(Request.Method.POST, SPOD_ENDPOINT + POST_USER_INFO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        setChanged();
                        notifyObservers(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        unavailableNetworkMessage(error);
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };
        mRequestQueue.add(postRequest);
    }

    public void getCocreationMediaRooms(){
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);
        try {
            JSONObject params   = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            params.put("userId", User.getInstance().getId() );
            jsonArray.put(params);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, SPOD_ENDPOINT + GET_COCREATION_MEDIA_ROOMS_ADDR, jsonArray, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    loading.dismiss();
                    setChanged();
                    notifyObservers(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loading.dismiss();
                    unavailableNetworkMessage(error);
                }
            });

            mRequestQueue.add(jsonArrayRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createCocreationRoom(final String name, final String subject, final String description, final String goal, final String invitation_text)
    {
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);
        StringRequest postRequest = new StringRequest(Request.Method.POST, SPOD_ENDPOINT + POST_COCREATION_CREATE_ROOM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        setChanged();
                        notifyObservers(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        unavailableNetworkMessage(error);

                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String strDate = sdf.format(c.getTime());

                params.put("ownerId",  User.getInstance().getId());
                params.put("name", name);
                params.put("subject", subject);
                params.put("description", description);
                params.put("goal", goal);
                params.put("data_from", strDate);
                params.put("data_to", strDate);
                params.put("invitation_text", invitation_text);
                params.put("users_value", "");
                params.put("room_type", "media");

                return params;
            }
        };
        mRequestQueue.add(postRequest);
    }

    public void getSheetData(String roomId){
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);
        try {
            JSONObject params   = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            params.put("roomId", roomId );
            jsonArray.put(params);
            JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.POST, SPOD_ENDPOINT + GET_COCREATION_ROOMS_SHEET_DATA + roomId, jsonArray, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    loading.dismiss();
                    setChanged();
                    notifyObservers(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loading.dismiss();
                    unavailableNetworkMessage(error);
                }
            });

            mRequestQueue.add(jsonRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public byte[] getByteImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public ByteArrayOutputStream compressPass(Bitmap bitmap, int sampleSize,int quality){
        ByteArrayOutputStream baos    = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos;
    }

    public byte[] compressBitmap(Bitmap bitmap, int sampleSize, int quality) {
        ByteArrayOutputStream baos = null;
        try {
            baos = compressPass(bitmap, sampleSize, quality);
            long lengthInByte = baos.toByteArray().length;
            while (lengthInByte > IMAGE_SIZE_LIMIT) {
                sampleSize *= 2;
                quality /= 4;
                baos = compressPass(bitmap, sampleSize, quality);
                lengthInByte = baos.toByteArray().length;
            }
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public void addRowToSheet(final String sheetId, final String title, final String description, final String date, final Bitmap bitmap)
    {
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, SPOD_ENDPOINT + POST_ADD_NEW_ROW + sheetId, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                loading.dismiss();
                setChanged();
                notifyObservers(resultResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
                unavailableNetworkMessage(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Location location  = SpodLocationServices.getCurrentLocation();

                Map<String, String> params = new HashMap<>();
                params.put("sheetId",     sheetId);
                params.put("title",       title);
                params.put("description", description);
                params.put("location", location.getLatitude() + "," + location.getLongitude());
                params.put("date",        date);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                byte[] imageBytes = compressBitmap(bitmap, 1, 100);
                params.put("image_file", new DataPart(title + ".jpg",  imageBytes , "image/jpeg"));
                return params;
            }
        };

        mRequestQueue.add(multipartRequest);

    }

    private void  unavailableNetworkMessage(VolleyError err){
        if(!isNetworkConnectionAvailable()){
            Snackbar.make(mainActivity.findViewById(R.id.container), "Network is not available!!!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        err.printStackTrace();

    }

}