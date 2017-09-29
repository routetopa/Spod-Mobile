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
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.Polling;
import com.github.nkzawa.engineio.client.transports.PollingXHR;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.User;
import eu.spod.isislab.spodapp.fragments.LoginFragment;
import eu.spod.isislab.spodapp.services.SpodLocationService;

 public class NetworkChannel extends Observable
{
    public static final String SERVICE_LOGIN                         = "SERVICE_LOGIN";
    public static final String SERVICE_GET_USER_INFO                 = "SERVICE_GET_USER_INFO";
    public static final String SERVICE_AGORA_GET_COMMENTS            = "SERVICE_AGORA_GET_COMMENTS";
    public static final String SERVICE_AGORA_ADD_COMMENT             = "SERVICE_AGORA_ADD_COMMENT";
    public static final String SERVICE_COCREATION_GET_SHEET_DATA     = "SERVICE_COCREATION_GET_SHEET_DATA";
    public static final String SERVICE_SYNC_NOTIFICATION             = "SERVICE_SYNC_NOTIFICATION";

    private static String SPOD_ENDPOINT                             = "";
    private static final String POST_LOGIN_HANDLER                  = "/base/user/ajax-sign-in/";//"/openid/ajax.php";;
    private static final String POST_USER_INFO                      = "/cocreation/ajax/get-user-info/";
    private static final String POST_ADD_NEW_ROW                    = "/ethersheet/mediaroom/addrow/";
    private static final String POST_COCREATION_CREATE_ROOM         = "/cocreation/ajax/create-media-room-from-mobile/";
    private static final String GET_COCREATION_MEDIA_ROOMS_ADDR     = "/cocreation/ajax/get-media-rooms-by-user-id/?userId=";
    private static final String GET_COCREATION_ROOMS_SHEET_DATA     = "/cocreation/ajax/get-sheet-data-by-room-id/?roomId=";
    private static final String GET_AGORA_ROOMS                     = "/agora/ajax/get-rooms";
    private static final String POST_AGORA_ADD_ROOM                 = "/agora/ajax/add-agora-room";
    private static final String GET_AGORA_ROOM_COMMENTS             = "/agora/ajax/get-comments-page/?roomId=";
    private static final String POST_AGORA_ROOM_ADD_COMMENTS        = "/agora/ajax/add-comment/";
    private static final String POST_AGORA_ROOM_GET_NESTED_COMMENTS = "/agora/ajax/get-nested-comment-json/";
    private static final String DATALET_STATIC_IMAGE_URL            = "/ow_plugins/ode/datalet_images/datalet_#.png";
    private static final String DATALET_STATIC_URL                  = "/share_datalet/#";
    //Sync notification
    private static final String SYNC_NOTIFICATION_ENDPOINT             = "/realtime_notification";
    private static final String COCREATION_SYNC_NOTIFICATION_ENDPOINT = "/ethersheet/#/pubsub/";

    private static NetworkChannel ourInstance = new NetworkChannel();

    private Activity mainActivity      = null;
    private RequestQueue mRequestQueue = null;
    private String currentService      = null;
    private Socket mSocket             = null;
    //private WebSocketClient mWebSocketClient   = null;
    //Socket mSocket                     = null;

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

    public String getSpodEndpoint(){ return SPOD_ENDPOINT; }
    public void setSpodEndpoint(String spodendpoint){ SPOD_ENDPOINT = "http://" + spodendpoint; }

    // added as an instance method to an Activity
    boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    public String getDataletStaticUrl(String dataletId){
        return (SPOD_ENDPOINT + DATALET_STATIC_URL).replace("#", dataletId);
    }

    public String getDataletImageStaticUrl(String dataletId){
        return (SPOD_ENDPOINT + DATALET_STATIC_IMAGE_URL).replace("#", dataletId);
    }

    public void login(final String username, final String password)
    {
        currentService = SERVICE_LOGIN;
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false);
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

    public void getUserInfo(final String email, final String username){
        currentService = SERVICE_GET_USER_INFO;
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false);
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
                if(!email.isEmpty())
                   params.put("email", email);
                else
                   params.put("username", username);
                return params;
            }
        };
        mRequestQueue.add(postRequest);
    }

    //COCREATION
    public void getCocreationMediaRooms(){
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false);
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
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false);
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
        currentService = SERVICE_COCREATION_GET_SHEET_DATA;
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false);
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
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile", mainActivity.getResources().getString(R.string.wait_network_message),false,false);

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

                Location location  = SpodLocationService.getCurrentLocation();

                Map<String, String> params = new HashMap<>();
                params.put("sheetId",     sheetId);
                params.put("title",       title);
                params.put("description", description);
                params.put("location",    location.getLatitude() + "," + location.getLongitude());
                params.put("date",        date);
                params.put("user",        User.getInstance().getUsername());
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

    //AGORA
    public void getAgoraRooms(){
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile", mainActivity.getResources().getString(R.string.wait_network_message), false, false);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST , SPOD_ENDPOINT + GET_AGORA_ROOMS, null, new Response.Listener<JSONArray>() {
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
    }

    private void  unavailableNetworkMessage(VolleyError err){
        if(!isNetworkConnectionAvailable()){
            Snackbar.make(mainActivity.findViewById(R.id.container), mainActivity.getResources().getString(R.string.unavailable_network_message), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        err.printStackTrace();

    }

    public void getAgoraRoomComments(final String roomId){
        currentService = SERVICE_AGORA_GET_COMMENTS;

        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST , SPOD_ENDPOINT + GET_AGORA_ROOM_COMMENTS + roomId, null, new Response.Listener<JSONArray>() {
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
    }

    public void getAgoraRoomComments(final String roomId, final String lastCommentId){
        //final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile","Please wait...",false,false);
        currentService = SERVICE_AGORA_GET_COMMENTS;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST , SPOD_ENDPOINT + GET_AGORA_ROOM_COMMENTS + roomId + "&last_id=" + lastCommentId, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //loading.dismiss();
                setChanged();
                notifyObservers(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //loading.dismiss();
                unavailableNetworkMessage(error);
            }
        });

        mRequestQueue.add(jsonArrayRequest);

    }

    public void getAgoraNestedComments(final String entityId, final String parentId, final String level)
    {
        currentService = SERVICE_AGORA_GET_COMMENTS;

        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false);
        StringRequest postRequest = new StringRequest(Request.Method.POST, SPOD_ENDPOINT + POST_AGORA_ROOM_GET_NESTED_COMMENTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        setChanged();
                        try {
                            notifyObservers(new JSONArray(response));
                        }catch (JSONException e){e.printStackTrace();}
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

                params.put("entityId",  entityId);
                params.put("parentId",  parentId);
                params.put("level",     level);
                params.put("userId",    User.getInstance().getId());

                return params;
            }
        };
        mRequestQueue.add(postRequest);
    }

    public void addAgoraComment(final String entityId, final String parentId, final String comment, final String level, final String sentiment)
    {
        currentService = SERVICE_AGORA_ADD_COMMENT;

        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false);
        StringRequest postRequest = new StringRequest(Request.Method.POST, SPOD_ENDPOINT + POST_AGORA_ROOM_ADD_COMMENTS,
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

                params.put("entityId",        entityId);
                params.put("parentId",        parentId);
                params.put("comment",         comment);
                params.put("level",           level);
                params.put("sentiment",       sentiment);
                params.put("userId",          User.getInstance().getId());

                return params;
            }
        };
        mRequestQueue.add(postRequest);
    }

    public void addAgoraRoom(final String title, final String description)
    {
        final ProgressDialog loading = ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false);
        StringRequest postRequest = new StringRequest(Request.Method.POST, SPOD_ENDPOINT + POST_AGORA_ADD_ROOM,
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

                params.put("subject", title);
                params.put("body",    description);
                params.put("userId",  User.getInstance().getId());

                return params;
            }
        };
        mRequestQueue.add(postRequest);
    }

    //Sync notification
   /* public void connectWebSocket(){
        URI uri;
        try {
            uri = new URI(SPOD_ENDPOINT.replace("http", "ws") + SYNC_NOTIFICATION_ENDPOINT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                //mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String message) {
                currentService = SERVICE_SYNC_NOTIFICATION;
                notifyObservers(message);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();
    }*/

    public void closeWebSocket(){
        if(mSocket != null){
            mSocket.disconnect();
            mSocket.off();
            mSocket.close();
        }
    }

    public void connectAgoraWebSocket(final String roomId){

        try {
            String endPoint = SPOD_ENDPOINT + "/";
            IO.Options options = new IO.Options();
            options.port       = 3000;
            options.path = "/realtime_notification";
            options.transports = new String[]{WebSocket.NAME};

            mSocket = IO.socket(endPoint, options);
            mSocket
                    .on(Socket.EVENT_CONNECT, new Emitter.Listener(){
                        @Override
                        public void call(Object... args) {
                            Log.e("SOKETIO", "CONNECT");
                            JSONObject obj = new JSONObject();
                            try {
                                obj.put("user_id", User.getInstance().getId());
                                obj.put("room_id", User.getInstance().getId());
                                obj.put("plugin", "agora");
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            mSocket.emit("online_notification", obj);
                        }
                    })
                    .on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                Log.e("SOKETIO", "DISCONNECT");
                            }

                    })
                    .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.e("SOKETIO", "ERROR");
                        }
                    })
                    .on("online_notification_" +  roomId, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.e("SOKETIO", "Online users fanasia");
                        }
                    })
                    .on("realtime_message_" +  roomId, new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            Log.e("SOKETIO", "New message fanasy");
                            try {
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try{
                                            JSONObject j = (JSONObject)args[0];
                                            if(!j.getString("user_id").equals(User.getInstance().getId())){
                                                currentService = SERVICE_SYNC_NOTIFICATION;
                                                setChanged();
                                                notifyObservers(args[0]);
                                            }
                                        }catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });

            mSocket.connect();

        }catch(URISyntaxException e){
            e.printStackTrace();
        }
    }

    public void connectCocreationWebSocket(final String roomId){

        try {
            String endPoint = "http://172.16.15.77/ethersheet/s/dataset_room_111_wEZGe/";//SPOD_ENDPOINT  + COCREATION_SYNC_NOTIFICATION_ENDPOINT.replace("#", roomId);
            IO.Options options = new IO.Options();
            options.port       = 80;
            options.path = "/pubsub";
            //options.transports = new String[]{Polling.NAME};

            mSocket = IO.socket(endPoint, options);
            mSocket
                    .on(Socket.EVENT_CONNECT, new Emitter.Listener(){
                        @Override
                        public void call(Object... args) {
                            Log.e("SOKETIO", "CONNECT");
                        }
                    })
                    .on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.e("SOKETIO", "DISCONNECT");
                        }

                    })
                    .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.e("SOKETIO", "ERROR");
                        }
                    })
                    .on("realtime_message_" +  roomId, new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            Log.e("SOKETIO", "New message fanasy");
                            try {
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try{
                                            JSONObject j = (JSONObject)args[0];
                                            if(!j.getString("user_id").equals(User.getInstance().getId())){
                                                currentService = SERVICE_SYNC_NOTIFICATION;
                                                setChanged();
                                                notifyObservers(args[0]);
                                            }
                                        }catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });

            mSocket.connect();

        }catch(URISyntaxException e){
            e.printStackTrace();
        }
    }

}