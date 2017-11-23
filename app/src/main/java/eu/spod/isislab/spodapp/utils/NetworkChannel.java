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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.services.AuthorizationService;
import eu.spod.isislab.spodapp.services.SpodLocationService;

public class NetworkChannel extends Observable
{
    private static NetworkChannel ourInstance = new NetworkChannel();

    private Activity mainActivity      = null;
    private RequestQueue mRequestQueue = null;
    private String currentService      = null;
    private Socket mSocket             = null;
    //private WebSocketClient mWebSocketClient   = null;

    public static NetworkChannel getInstance() {
        return ourInstance;
    }

    private NetworkChannel() {
    }

    public void init(Activity mainActivity){
        this.mainActivity = mainActivity;
        mRequestQueue= Volley.newRequestQueue(this.mainActivity);
        SharedPreferences spodPref = mainActivity.getSharedPreferences(Consts.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);
        Consts.SPOD_ENDPOINT = "http://" +  spodPref.getString(Consts.SPOD_ENDPOINT_PREFERENCES, "");
    }

    public String getCurrentService(){
        return currentService;
    }

    public String getSpodEndpoint(){ return Consts.SPOD_ENDPOINT; }

    public void setSpodEndpoint(String spodendpoint){ Consts.SPOD_ENDPOINT = "http://" + spodendpoint; }

    // added as an instance method to an Activity
    boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    public String getDataletStaticUrl(String dataletId){
        return (Consts.SPOD_ENDPOINT + Consts.DATALET_STATIC_URL).replace("#", dataletId);
    }

    public String getDataletImageStaticUrl(String dataletId){
        return (Consts.SPOD_ENDPOINT + Consts.DATALET_STATIC_IMAGE_URL).replace("#", dataletId);
    }

    private void  unavailableNetworkMessage(VolleyError err){

        Snackbar.make(mainActivity.findViewById(R.id.container), mainActivity.getResources().getString(R.string.unavailable_network_message), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        err.printStackTrace();

    }

    private void makePostRequest(String url, final Map<String, String> params, final boolean splash, final String service)
    {
        final ProgressDialog loading = (splash) ? ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false)
                                                : null;
        StringRequest postRequest = new StringRequest(Request.Method.POST, Consts.SPOD_ENDPOINT + url,
       /* StringRequest postRequest = new StringRequest(Request.Method.POST, ((service != null &&
                (service.equals(Consts.SERVICE_SAVE_NOTIFICATION) ||
                 service.equals(Consts.SERVICE_COCREATION_GET_ROOMS) ||
                 service.equals(Consts.SERVICE_COCREATION_JOIN_ROOM) ||
                 service.equals(Consts.SERVICE_COCREATION_GET_ALL_FRIENDS) ||
                 service.equals(Consts.SERVICE_COCREATION_INVITE_FRIENDS) ||
                 service.equals(Consts.SERVICE_MEDIAROOM_ADD_NEW_ROW) ||
                 service.equals(Consts.SERVICE_COCREATION_GET_SHEET_DATA) ||
                 service.equals(Consts.SERVICE_FIREBASE_REGISTRATION)
                ))
                ? "http://172.16.15.77" : Consts.SPOD_ENDPOINT) + url,*/
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(splash) loading.dismiss();
                        setChanged();
                        if(service != null) currentService = service;
                        notifyObservers(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(splash)loading.dismiss();
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
                params.put("jwt", AuthorizationService.getInstance().getAccessToken());
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                1/*DefaultRetryPolicy.DEFAULT_MAX_RETRIES*/,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(postRequest);
    }

    public void makeMultipartRequest(String url, final Map<String, String> stringParams, final Map<String, DataPart> partParams, final boolean splash, final String service)
    {
        Log.e("MULTIPART", service + " - " + url);
        if(service != null) currentService = service;

        final ProgressDialog loading = (splash) ? ProgressDialog.show(mainActivity,"SPOD Mobile",mainActivity.getResources().getString(R.string.wait_network_message),false,false)
                : null;

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, Consts.SPOD_ENDPOINT + url , new Response.Listener<NetworkResponse>() {
       /* VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,  ((service != null &&
                (service.equals(Consts.SERVICE_SAVE_NOTIFICATION) ||
                        service.equals(Consts.SERVICE_COCREATION_GET_ROOMS) ||
                        service.equals(Consts.SERVICE_COCREATION_JOIN_ROOM) ||
                        service.equals(Consts.SERVICE_COCREATION_GET_ALL_FRIENDS) ||
                        service.equals(Consts.SERVICE_COCREATION_INVITE_FRIENDS) ||
                        service.equals(Consts.SERVICE_MEDIAROOM_ADD_NEW_ROW) ||
                        service.equals(Consts.SERVICE_FIREBASE_REGISTRATION)
                ))
                ? "http://172.16.15.77" : Consts.SPOD_ENDPOINT) + url, new Response.Listener<NetworkResponse>() {*/
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                if(splash)loading.dismiss();
                setChanged();
                if(service != null) currentService = service;
                notifyObservers(resultResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(splash)loading.dismiss();
                unavailableNetworkMessage(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return stringParams;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                return partParams;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                1/*DefaultRetryPolicy.DEFAULT_MAX_RETRIES*/,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(multipartRequest);

    }

    public void getUserInfo(final String email, final String username)
    {
        Map<String, String> params = new HashMap<>();
        if(!email.isEmpty())
            params.put("email", email);
        else
            params.put("username", username);

        makePostRequest(Consts.GET_USER_INFO, params, true, Consts.SERVICE_GET_USER_INFO );
    }

    //COCREATION
    public void getCocreationRooms()
    {
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserManager.getInstance().getId() );
        makePostRequest(Consts.GET_COCREATION_ROOMS, params, true, Consts.SERVICE_COCREATION_GET_ROOMS );
    }

    public void createCocreationRoom(final String name, final String subject, final String description, final String goal, final String invitation_text)
    {
        Map<String, String> params = new HashMap<>();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = sdf.format(c.getTime());

        params.put("ownerId",  UserManager.getInstance().getId());
        params.put("name", name);
        params.put("subject", subject);
        params.put("description", description);
        params.put("goal", goal);
        params.put("data_from", strDate);
        params.put("data_to", strDate);
        params.put("invitation_text", invitation_text);
        params.put("users_value", "");
        params.put("room_type", "media");

        makePostRequest(Consts.COCREATION_CREATE_ROOM, params, true, null);

    }

    public void getSheetData(String roomId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("roomId",  roomId);
        makePostRequest(Consts.GET_COCREATION_ROOMS_SHEET_DATA, params, true, Consts.SERVICE_COCREATION_GET_SHEET_DATA);
    }

    public void addRowToSheet(final String sheetId, final String title, final String description, final String date, final Bitmap bitmap)
    {
        Location location  = SpodLocationService.getCurrentLocation();

        Map<String, String> stringParams = new HashMap<>();
        stringParams.put("sheetId",     sheetId);
        stringParams.put("title",       title);
        stringParams.put("description", description);
        stringParams.put("location",    location.getLatitude() + "," + location.getLongitude());
        stringParams.put("date",        date);
        stringParams.put("user",        UserManager.getInstance().getName());//Realname

        Map<String, DataPart> partParams = new HashMap<>();
        byte[] imageBytes = ImageUtils.getInstance().compressBitmap(bitmap, 1, 100);
        partParams.put("image_file", new DataPart(title + ".jpg",  imageBytes , "image/jpeg"));

        makeMultipartRequest(Consts.MEDIAROOM_ADD_NEW_ROW + sheetId, stringParams, partParams, true, Consts.SERVICE_MEDIAROOM_ADD_NEW_ROW);
    }

    public void getCocreationMetadata(String roomId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("roomId", roomId );
        makePostRequest(Consts.GET_COCREATION_ROOM_METADATA, params, true, Consts.SERVICE_COCREATION_GET_METADATA );
    }

    public void getCocreationDatalets(String roomId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("roomId", roomId );
        makePostRequest(Consts.GET_COCREATION_ROOM_DATALETS, params, true, Consts.SERVICE_COCREATION_GET_DATALETS );
    }

    public void getCocreationRoomComments(String roomId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("entityId", roomId );
        makePostRequest(Consts.GET_COCREATION_ROOM_COMMENTS, params, true, Consts.SERVICE_COCREATION_GET_COMMENTS );
    }

    public void addCocreationRoomComment(String roomId, String comment)
    {
        Map<String, String> params = new HashMap<>();
        params.put("entityId", roomId );
        params.put("comment",  comment );
        makePostRequest(Consts.COCREATION_ROOM_ADD_COMMENT, params, true, Consts.SERVICE_COCREATION_ADD_COMMENT );
    }

    public void cocreationConfirmToJoinToRoom(String memberId, String roomId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("memberId", memberId );
        params.put("roomId",   roomId );
        params.put("mobile",  "true" );
        makePostRequest(Consts.COCREATION_ROOM_JOIN_ROOM, params, true, Consts.SERVICE_COCREATION_JOIN_ROOM);
    }

    public void cocreationGetAllFriends(String roomId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserManager.getInstance().getId() );
        params.put("roomId",  roomId );
        makePostRequest(Consts.COCREATION_ROOM_GET_ALL_FRIENDS, params, true, Consts.SERVICE_COCREATION_GET_ALL_FRIENDS);
    }

    public void cocreationInviteFriends(String roomId, String users)
    {
        Map<String, String> params = new HashMap<>();
        params.put("users", users );
        params.put("roomId",      roomId );
        makePostRequest(Consts.COCREATION_ROOM_INVITE_FRIENDS, params, true, Consts.SERVICE_COCREATION_INVITE_FRIENDS);
    }


    //AGORA
    public void getAgoraRooms()
    {
        Map<String, String> params = new HashMap<>();
        makePostRequest(Consts.GET_AGORA_ROOMS, params, true, Consts.SERVICE_AGORA_GET_ROOMS);
    }

    public void getAgoraRoomPagedComments(final String roomId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("roomId",  roomId);
        makePostRequest(Consts.GET_AGORA_ROOM_COMMENTS, params, true, Consts.SERVICE_AGORA_GET_COMMENTS);
    }

    public void getAgoraRoomPagedComments(final String roomId, final String lastCommentId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("roomId",  roomId);
        params.put("last_id", lastCommentId);
        makePostRequest(Consts.GET_AGORA_ROOM_COMMENTS, params, false, Consts.SERVICE_AGORA_GET_PAGED_COMMENTS);
    }

    public void getAgoraNestedComments(final String entityId, final String parentId, final String level)
    {
        Map<String, String> params = new HashMap<>();
        params.put("entityId",  entityId);
        params.put("parentId",  parentId);
        params.put("level",     level);
        params.put("userId",    UserManager.getInstance().getId());
        makePostRequest(Consts.AGORA_ROOM_GET_NESTED_COMMENTS, params, false, Consts.SERVICE_AGORA_GET_COMMENTS);
    }

    public void addAgoraComment(final String entityId, final String parentId, final String comment, final String level, final String sentiment)
    {
        Map<String, String> params = new HashMap<>();
        params.put("entityId",        entityId);
        params.put("parentId",        parentId);
        params.put("comment",         comment);
        params.put("level",           level);
        params.put("sentiment",       sentiment);
        params.put("userId",          UserManager.getInstance().getId());
        makePostRequest(Consts.AGORA_ROOM_ADD_COMMENTS, params, true, Consts.SERVICE_AGORA_ADD_COMMENT);
    }

    public void addAgoraRoom(final String title, final String description)
    {
        Map<String, String> params = new HashMap<>();
        params.put("subject", title);
        params.put("body",    description);
        params.put("userId",  UserManager.getInstance().getId());
        makePostRequest(Consts.AGORA_ADD_ROOM, params, true, Consts.SERVICE_AGORA_ADD_COMMENT);
    }

    //FIREBASE NOTIFICATION
    public void addRegistrationId(String registrationId){
        Map<String, String> params = new HashMap<>();
        params.put("registrationId", registrationId);
        params.put("userId",  UserManager.getInstance().getId());
        makePostRequest(Consts.FIREBASE_REGISTRATION_ID_ENDPOINT, params, true, Consts.SERVICE_FIREBASE_REGISTRATION);
    }

    //SETTINGS
    public void saveMobileNotification(String status, String plugin, String action, String subAction, String frequency){
        Map<String, String> params = new HashMap<>();
        params.put("userId",    UserManager.getInstance().getId());
        params.put("status",    status);
        params.put("plugin",    plugin);
        params.put("action",    action);
        params.put("subAction", subAction);
        params.put("type",      "mobile");
        params.put("frequency", frequency);
        makePostRequest(Consts.SAVE_MOBILE_NOTIFICATION, params, false, Consts.SERVICE_SAVE_NOTIFICATION);
    }

    //SYNC NOTIFICATION
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

    public void connectToWebSocket(final String plugin, final String[] channels){
        try {
            String endPoint = Consts.SPOD_ENDPOINT + "/";
            IO.Options options = new IO.Options();
            options.port       = 3000;
            options.path = "/realtime_notification";
            options.transports = new String[]{WebSocket.NAME};


            mSocket = IO.socket(endPoint, options);
            mSocket
                    .on(Socket.EVENT_CONNECT, new Emitter.Listener(){
                        @Override
                        public void call(Object... args) {
                            //Log.e("SOKETIO", "CONNECT");
                            JSONObject obj = new JSONObject();
                            try {
                                obj.put("user_id", UserManager.getInstance().getId());
                                obj.put("room_id", UserManager.getInstance().getId());
                                obj.put("plugin", plugin);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            mSocket.emit("online_notification", obj);
                        }
                    })
                    .on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            //Log.e("SOKETIO", "DISCONNECT");
                        }

                    })
                    .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            //Log.e("SOKETIO", "ERROR");
                        }
                    });

            Emitter.Listener listener = new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    try {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                    /*try{
                                        JSONObject j = (JSONObject)args[0];
                                        if(!j.getString("user_id").equals(UserManager.getInstance().getId())){
                                            currentService = SERVICE_SYNC_NOTIFICATION;
                                            setChanged();
                                            notifyObservers(args[0]);
                                        }
                                    }catch (JSONException e) {
                                        e.printStackTrace();
                                    }*/
                                currentService = Consts.SERVICE_SYNC_NOTIFICATION;
                                setChanged();
                                notifyObservers(args[0]);
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            for(String channel : channels){
                mSocket.on(channel, listener);
            }

            mSocket.connect();

        }catch(URISyntaxException e){
            e.printStackTrace();
        }

    }

    public void connectAgoraWebSocket(final String roomId)
    {
        try {
            String endPoint = Consts.SPOD_ENDPOINT + "/";
            IO.Options options = new IO.Options();
            options.port       = 3000;
            options.path = "/realtime_notification";
            options.transports = new String[]{WebSocket.NAME};

            mSocket = IO.socket(endPoint, options);
            mSocket
                    .on(Socket.EVENT_CONNECT, new Emitter.Listener(){
                        @Override
                        public void call(Object... args) {
                            //Log.e("SOKETIO", "CONNECT");
                            JSONObject obj = new JSONObject();
                            try {
                                obj.put("user_id", UserManager.getInstance().getId());
                                obj.put("room_id", UserManager.getInstance().getId());
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
                                //Log.e("SOKETIO", "DISCONNECT");
                            }

                    })
                    .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            //Log.e("SOKETIO", "ERROR");
                        }
                    })
                    .on("online_notification_" +  roomId, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            //Log.e("SOKETIO", "Online users fanasia");
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
                                            if(!j.getString("user_id").equals(UserManager.getInstance().getId())){
                                                currentService = Consts.SERVICE_SYNC_NOTIFICATION;
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

    public void connectCocreationWebSocket(final String roomId)
    {

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
                            //Log.e("SOKETIO", "CONNECT");
                        }
                    })
                    .on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            //Log.e("SOKETIO", "DISCONNECT");
                        }

                    })
                    .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            //Log.e("SOKETIO", "ERROR");
                        }
                    })
                    .on("realtime_message_" +  roomId, new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            //Log.e("SOKETIO", "New message fanasy");
                            try {
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try{
                                            JSONObject j = (JSONObject)args[0];
                                            if(!j.getString("user_id").equals(UserManager.getInstance().getId())){
                                                currentService = Consts.SERVICE_SYNC_NOTIFICATION;
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