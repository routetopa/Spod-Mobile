package eu.spod.isislab.spodapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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

import java.io.UnsupportedEncodingException;
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

    private static final String TAG                 = "NetworkChannel";

    public static final String SERVICE_LOGIN                 = "SERVICE_LOGIN";
    public static final String SERVICE_GET_USER_INFO         = "SERVICE_GET_USER_INFO";

    public static final String NEWSFEED_SERVICE_GET_AUTHORIZATION   = "NEWSFEED_SERVICE_GET_AUTHORIZATION";
    public static final String NEWSFEED_SERVICE_GET_FEED            = "NEWSFEED_SERVICE_GET_FEED";
    public static final String NEWSFEED_SERVICE_GET_POST            = "NEWSFEED_SERVICE_GET_POST";
    public static final String NEWSFEED_SERVICE_LIKE_POST           = "NEWSFEED_SERVICE_LIKE_POST";
    public static final String NEWSFEED_SERVICE_UNLIKE_POST         = "NEWSFEED_SERVICE_UNLIKE_POST";
    public static final String NEWSFEED_SERVICE_GET_POST_COMMENTS   = "NEWSFEED_SERVICE_GET_POST_COMMENTS";
    public static final String NEWSFEED_SERVICE_ADD_NEW_STATUS      = "NEWSFEED_SERVICE_ADD_NEW_STATUS";
    public static final String NEWSFEED_SERVICE_GET_PHOTOS          = "NEWSFEED_SERVICE_GET_PHOTOS";
    public static final String NEWSFEED_SERVICE_ADD_COMMENT         = "NEWSFEED_SERVICE_ADD_COMMENT";
    public static final String NEWSFEED_SERVICE_NEWSFEED_DELETE     = "NEWSFEED_SERVICE_NEWSFEED_DELETE";
    public static final String NEWSFEED_SERVICE_NEWSFEED_FLAG       = "NEWSFEED_SERVICE_NEWSFEED_FLAG";
    public static final String NEWSFEED_SERVICE_GET_LIKES_LIST      = "NEWSFEED_SERVICE_GET_LIKES_LIST";


    private static String SPOD_ENDPOINT                          =  "";
    private static final String POST_LOGIN_HANDLER               = "/openid/ajax.php";//"/base/user/ajax-sign-in/";
    private static final String POST_USER_INFO                   = "/cocreation/ajax/get-user-info/";
    private static final String POST_ADD_NEW_ROW                 = "/ethersheet/mediaroom/addrow/";
    private static final String POST_COCREATION_CREATE_ROOM      = "/cocreation/ajax/create-media-room-from-mobile/";
    private static final String GET_COCREATION_MEDIA_ROOMS_ADDR  = "/cocreation/ajax/get-media-rooms-by-user-id/?userId=";
    private static final String GET_COCREATION_ROOMS_SHEET_DATA  = "/cocreation/ajax/get-sheet-data-by-room-id/?roomId=";

    private static final String NEWSFEED_GET_AUTHORIZATION       = "http://172.16.15.137/oxwall/extfeed/api/get-authorization/";
    private static final String NEWSFEED_GET_POSTS               = "http://172.16.15.137/oxwall/extfeed/api/get-feed/";
    private static final String NEWSFEED_POST_GET_ITEM           = "http://172.16.15.137/oxwall/extfeed/api/get-item/";
    private static final String NEWSFEED_POST_ADD_STATUS         = "http://172.16.15.137/oxwall/extfeed/api/status-update/";
    private static final String NEWSFEED_POST_LIKE               = "http://172.16.15.137/oxwall/extfeed/api/add-like/";
    private static final String NEWSFEED_POST_UNLIKE             = "http://172.16.15.137/oxwall/extfeed/api/remove-like/";
    private static final String NEWSFEED_GET_COMMENTS            = "http://172.16.15.137/oxwall/extfeed/api/comments/";
    private static final String NEWSFEED_POST_ADD_COMMENT        = "http://172.16.15.137/oxwall/extfeed/api/add-comment/";
    private static final String NEWSFEED_GET_LIKES_LIST          = "http://172.16.15.137/oxwall/extfeed/api/likes-list";
    private static final String NEWSFEED_GET_PHOTOS              = "http://172.16.15.137/oxwall/extfeed/api/photos-info/";

    private static final String GET_USER                         = "http://jsonplaceholder.typicode.com/users/";
    private static final String GET_POSTS_COMMENTS               = "http://jsonplaceholder.typicode.com/comments?postId=";
    private static final String POST_SEND                        = "http://jsonplaceholder.typicode.com/posts/";
    private static final String DELETE_POST                      = "http://jsonplaceholder.typicode.com/posts/";

    private static final String GET_DATALET_IMAGES               = "http://nl-spod.routetopa.eu/ow_plugins/ode/datalet_images/";
    private static final String GET_DATALET                      = "http://nl-spod.routetopa.eu/share_datalet/";
    private static final int[] AVAILABLE_DATALETS = new int[]{2,3,4,5,6,7,8,9,10,11,12,13,15,16,19,20,21,23,25,27,28,29,31,32,34,35,37,27,42,43,44,45,47,49,51,53,54,55,60,68,69,70,71,72,73,74,76,79,80,81,82,83,84,85,87,90,93,94,95,96,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114};

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
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, SPOD_ENDPOINT + GET_COCREATION_MEDIA_ROOMS_ADDR + User.getInstance().getId(), jsonArray, new Response.Listener<JSONArray>() {
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
                params.put("titleTextView",       title);
                params.put("description", description);
                params.put("location", location.getLatitude() + "," + location.getLongitude());
                params.put("date",        date);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                byte[] imageBytes = ImageUtils.getInstance().compressBitmap(bitmap, 1, 100);
                params.put("image_file", new DataPart(title + ".jpg",  imageBytes , "image/jpeg"));
                return params;
            }
        };

        mRequestQueue.add(multipartRequest);

    }

    public void getNewsfeedAuthorization(String feedType, String feedId) {
        currentService = NEWSFEED_SERVICE_GET_AUTHORIZATION;
        Log.d(TAG, "getNewsfeedAuthorization: ");

        final Map<String, String> params = new HashMap<>(2);
        params.put("ft", feedType);
        params.put("fi", feedId);

        StringRequest request = new StringRequest(Request.Method.POST, NEWSFEED_GET_AUTHORIZATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        setChanged();
                        notifyObservers(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        unavailableNetworkMessage(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-agent", "SpodApp");
                headers.put("Cookie", "ow_login=3cac599f8c4dbb3ced2ce766e919d8e9");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        mRequestQueue.add(request);
    }

    public void loadNextPosts(String feedType, String feedId, int offset, int count){
        currentService = NEWSFEED_SERVICE_GET_FEED;
        Log.d(TAG, "loadNextPosts: ");

        final Map<String, String> params = new HashMap<>(4);
        params.put("ft", feedType);
        params.put("fi", ""+feedId);
        params.put("offset", ""+offset);
        params.put("count", ""+count);

        StringRequest request = new StringRequest(Request.Method.POST, NEWSFEED_GET_POSTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        setChanged();
                        notifyObservers(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        unavailableNetworkMessage(error);
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-agent", "SpodApp");
                //headers.put("Cookie", "ow_login=19d5ca230ee4dbb933c397db427048f7"); //TODO: remove this line
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        request.setShouldCache(false); //TODO: remove this line
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(request);
    }

    public void getPost(String feedType, String feedId, String entityType, int entityId) {
        currentService = NEWSFEED_SERVICE_GET_POST;
        final HashMap<String, String> par = new HashMap<>(4);
        par.put("etype", entityType);
        par.put("eid", String.valueOf(entityId));
        par.put("ftype", feedType);
        par.put("fid", String.valueOf(feedId));

        Log.d(TAG, "getPost: ");

        StringRequest request = new StringRequest(Request.Method.POST, NEWSFEED_POST_GET_ITEM, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setChanged();
                notifyObservers(response);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                unavailableNetworkMessage(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return par;
            }
        };

        mRequestQueue.add(request);
    }

    public void sendStatus(String feedType, String feedId, String message, final byte[] attachment, final String fileName) {
        currentService = NEWSFEED_SERVICE_ADD_NEW_STATUS;
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);

        String send = null;
        try {
            send = new String(message.getBytes(), "ISO-8859-1");  //Charset conversion
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        switch (feedType) {
            case "site": case "my":
                feedType = "user";
                break;
        }

        final HashMap<String, String> par = new HashMap<>(3);
        par.put("ftype", feedType);
        par.put("fid", feedId);
        par.put("message", send);

        Log.d(TAG, "addStatus: ");

        Request request = new VolleyMultipartRequest(Request.Method.POST, NEWSFEED_POST_ADD_STATUS, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                loading.dismiss();
                String resultResponse = new String(response.data);
                setChanged();
                notifyObservers(resultResponse);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        unavailableNetworkMessage(error);
                    }
                }
        ) {
            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                final Map<String, VolleyMultipartRequest.DataPart> partMap = new HashMap<>(1);
                if(attachment != null) { //TODO: move this outside request and remove tmp
                    partMap.put("attachment", new VolleyMultipartRequest.DataPart(fileName, attachment, "image/jpeg"));
                }
                return partMap;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return par;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.0f));
        mRequestQueue.add(request);
    }

    public void likePost(final String entityType, final int entityId) {
        currentService = NEWSFEED_SERVICE_LIKE_POST;
        final HashMap<String, String> par = new HashMap<>(3);
        par.put("entityType", entityType);
        par.put("entityId", String.valueOf(entityId));

        Log.d(TAG, "nLikeUnlikePost: ");

        StringRequest request = new StringRequest(Request.Method.POST, NEWSFEED_POST_LIKE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setChanged();
                notifyObservers(response);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                unavailableNetworkMessage(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return par;
            }
        };

        mRequestQueue.add(request);
    }

    public void unlikePost(final String entityType, final int entityId) {
        currentService = NEWSFEED_SERVICE_UNLIKE_POST;
        final HashMap<String, String> par = new HashMap<>(3);
        par.put("entityType", entityType);
        par.put("entityId", String.valueOf(entityId));

        Log.d(TAG, "unlikePost: ");

        StringRequest request = new StringRequest(Request.Method.POST, NEWSFEED_POST_UNLIKE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setChanged();
                notifyObservers(response);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                unavailableNetworkMessage(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return par;
            }
        };

        mRequestQueue.add(request);
    }

    public void getPostComments(String entityType, int entityId, int page, int count){
        currentService = NEWSFEED_SERVICE_GET_POST_COMMENTS;

        final Map<String, String> params = new HashMap<>(2);
        params.put("etype", entityType);
        params.put("eid", ""+entityId);

        if(page >= 0 && count > 0) {
            params.put("page", ""+page);
            params.put("count", ""+count);
        }

        StringRequest request = new StringRequest(Request.Method.POST, NEWSFEED_GET_COMMENTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        setChanged();
                        notifyObservers(response);
                    }
                }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                unavailableNetworkMessage(error);
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(request);
    }

    public void addComment(String entityType, int entityId, String pluginKey, int ownerId, String message, final byte[] attachment, final String fileName) {
        currentService = NEWSFEED_SERVICE_ADD_COMMENT;
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);

        String send = null;
        try {
            send = new String(message.getBytes(), "ISO-8859-1");  //Charset conversion
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final HashMap<String, String> par = new HashMap<>(3);
        par.put("etype", entityType);
        par.put("eid", String.valueOf(entityId));
        par.put("pkey", pluginKey);
        par.put("ownerId", String.valueOf(ownerId));
        par.put("message", send);


        Log.d(TAG, "addComment: ");

        Request request = new VolleyMultipartRequest(Request.Method.POST, NEWSFEED_POST_ADD_COMMENT, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                loading.dismiss();
                String resultResponse = new String(response.data);
                setChanged();
                notifyObservers(resultResponse);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        unavailableNetworkMessage(error);
                    }
                }
        ) {
            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                final Map<String, VolleyMultipartRequest.DataPart> partMap = new HashMap<>(1);
                if(attachment != null) { //TODO: move this outside request and remove tmp
                    partMap.put("attachment", new VolleyMultipartRequest.DataPart(fileName + ".jpg", attachment, "image/jpeg"));
                }
                return partMap;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return par;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(request);
    }

    public void getPhotos(int[] photoIds) {
        currentService = NEWSFEED_SERVICE_GET_PHOTOS;

        final HashMap<String, String> par = new HashMap<>(3);
        for (int photoId : photoIds) {
            par.put("ids[" + photoId + "]", "" + photoId);
        }

        Log.d(TAG, "get photos: ");

        StringRequest request = new StringRequest(Request.Method.POST, NEWSFEED_GET_PHOTOS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setChanged();
                notifyObservers(response);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                unavailableNetworkMessage(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return par;
            }
        };

        mRequestQueue.add(request);
    }

    public void deleteContent(String actionUrl, final Map<String, String> actionParams){
        currentService = NEWSFEED_SERVICE_NEWSFEED_DELETE;
//        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);

        StringRequest request = new StringRequest(Request.Method.POST, actionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setChanged();
                notifyObservers(response);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                unavailableNetworkMessage(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return actionParams;
            }
        };

        mRequestQueue.add(request);
    }

    public void flagContent(String actionUrl, final Map<String, String> actionParams){
        currentService = NEWSFEED_SERVICE_NEWSFEED_FLAG;
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);

        StringRequest request = new StringRequest(Request.Method.POST, actionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loading.dismiss();
                setChanged();
                notifyObservers(response);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                unavailableNetworkMessage(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return actionParams;
            }
        };

        mRequestQueue.add(request);
    }

    public void stopFeedRequest() {
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                if(NEWSFEED_GET_POSTS.equals(request.getUrl())) {
                    Log.d(TAG, "stopFeedRequest: request stopped");
                    return true;
                }
                return false;
            }
        });
    }

    private void  unavailableNetworkMessage(VolleyError err){
        Snackbar.make(mainActivity.findViewById(R.id.container), "Network is not available!!!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        err.printStackTrace();

    }

    public void getLikesList(String entityType, String entityId) {
        currentService = NEWSFEED_SERVICE_GET_LIKES_LIST;

        final HashMap<String, String> par = new HashMap<>(3);
        par.put("etype", entityType);
        par.put("eid", entityId);

        Log.d(TAG, "getLikesList: ");

        StringRequest request = new StringRequest(Request.Method.POST, NEWSFEED_GET_LIKES_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setChanged();
                notifyObservers(response);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                unavailableNetworkMessage(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return par;
            }
        };

        mRequestQueue.add(request);
    }

}