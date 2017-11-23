package eu.spod.isislab.spodapp.fragments.agora;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Observable;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.Comment;
import eu.spod.isislab.spodapp.entities.AgoraRoom;
import eu.spod.isislab.spodapp.fragments.CommentFragment;
import eu.spod.isislab.spodapp.fragments.LoginFragment;
import eu.spod.isislab.spodapp.fragments.settings.SettingsFragment;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class AgoraRoomFragment extends CommentFragment {

    public static final String TAG = "AgoraRoomFragment";

    AgoraRoom room;
    SharedPreferences spodPref;

    public AgoraRoomFragment(){
        this.maxLevel = 2;
    }

    public void setRoom(AgoraRoom room){
        this.room = room;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        spodPref = getActivity().getSharedPreferences(Consts.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);
        initNotificationSwitch(spodPref.getBoolean( NetworkChannel.getInstance().getSpodEndpoint() + Consts.AGORA_ACTION_COMMENT + "_" + room.getId(), false));

        return asView;
    }

    @Override
    public void onNotificationSwitchChange(View v){
        NetworkChannel.getInstance().saveMobileNotification(
                (((Switch)v).isChecked() ? "true" : "false"),
                Consts.AGORA_PLUGIN,
                Consts.AGORA_ACTION_COMMENT + "_" + room.getId(),
                Consts.AGORA_ACTION_COMMENT,
                "" +
                        (spodPref.getInt
                                (NetworkChannel.getInstance().getSpodEndpoint() + getResources().getResourceEntryName(R.id.settings_agora_comment_room_spinner), 0)
                                + 1
                        )
        );
    }

    @Override
    public void addComment(String comment){
        NetworkChannel.getInstance().addAgoraComment(
                room.getId(),
                room.getId(),
                comment,
                "0",
                "0");
    }

    @Override
    public void init() {
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getAgoraRoomPagedComments(room.getId());
        NetworkChannel.getInstance().connectToWebSocket("agora", new String[]{"realtime_message_" +  room.getId()});
        ((MainActivity)getActivity()).setToolbarTitle(room.getSubject());
    }

    @Override
    public void onResume() {
        super.onResume();
        NetworkChannel.getInstance().connectToWebSocket("agora", new String[]{"realtime_message_" +  room.getId()});
        NetworkChannel.getInstance().addObserver(this);
    }

    @Override
    public void onPause() {
        NetworkChannel.getInstance().deleteObserver(this);
        NetworkChannel.getInstance().closeWebSocket();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        NetworkChannel.getInstance().deleteObserver(this);
        NetworkChannel.getInstance().closeWebSocket();
        super.onDestroy();
    }

    @Override
    public void update(Observable o, Object arg) {

        switch(NetworkChannel.getInstance().getCurrentService()) {
            case Consts.SERVICE_SAVE_NOTIFICATION:
                try {
                    JSONObject res = new JSONObject((String)arg);

                    spodPref.edit()
                            .putBoolean(NetworkChannel.getInstance().getSpodEndpoint() + Consts.AGORA_ACTION_COMMENT + "_" + room.getId() ,
                                       ((Switch)asView.findViewById(R.id.notification_switch)).isChecked())
                            .apply();

                    Snackbar.make(asView, res.getString("message"), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Consts.SERVICE_AGORA_GET_COMMENTS:

                try {
                    JSONArray response = new JSONArray((String)arg);

                    for (int i=0; i < response.length(); i++)
                    {
                            JSONObject j = response.getJSONObject(i);
                            comments.add(new Comment(
                                    j.getString("id"),
                                    j.getString("entityId"),
                                    j.getString("ownerId"),
                                    j.getString("comment"),
                                    j.getString("level"),
                                    j.getString("sentiment"),
                                    j.getString("timestamp"),
                                    j.getString("total_comment"),
                                    j.getString("username"),
                                    j.getString("avatar_url"),
                                    j.getString("datalet_id")));
                        }
                    }
                catch (JSONException | ClassCastException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                scrollMyListViewToItemIndex(adapter.getCount() - 1);
                break;

            case Consts.SERVICE_AGORA_GET_PAGED_COMMENTS:
                try {
                    JSONArray response = new JSONArray((String)arg);

                        for (int i = response.length() - 1; i >= 0 ; i--)
                        {
                            JSONObject j = response.getJSONObject(i);
                            comments.add(0, new Comment(
                                    j.getString("id"),
                                    j.getString("entityId"),
                                    j.getString("ownerId"),
                                    j.getString("comment"),
                                    j.getString("level"),
                                    j.getString("sentiment"),
                                    j.getString("timestamp"),
                                    j.getString("total_comment"),
                                    j.getString("username"),
                                    j.getString("avatar_url"),
                                    j.getString("datalet_id")));
                        }

                        adapter.notifyDataSetChanged();
                        //scrollToLastVisibleItem( (response.length() * 2) + 1 );
                        scrollMyListViewToItemIndex( (response.length() * 2) + 1 );
                    showLoader(false);

                }catch (JSONException | ClassCastException e) {
                    e.printStackTrace();
                }
                break;
            case Consts.SERVICE_AGORA_ADD_COMMENT:

                try {
                    JSONObject res = new JSONObject((String) arg);

                    String result = res.getString("result");
                    if (result.equals("ok")) {
                        ((EditText)asView.findViewById(R.id.comment_add_new)).setText("");
                        comments.clear();
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(asView.getWindowToken(), 0);

                        NetworkChannel.getInstance().getAgoraRoomPagedComments(room.getId());
                    } else {
                        Snackbar.make(asView, res.getString("message"), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    break;
                }

                break;
            case Consts.SERVICE_SYNC_NOTIFICATION:
                comments.clear();

                ((EditText)asView.findViewById(R.id.comment_add_new)).setText("");
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(asView.getWindowToken(), 0);

                NetworkChannel.getInstance().getAgoraRoomPagedComments(room.getId());
                break;
        }
    }

    @Override
    public void getNextCommentPage(){
        showLoader(true);
        NetworkChannel.getInstance().getAgoraRoomPagedComments(room.getId(), ((Comment)adapter.getItem(0)).getId());
    }

    @Override
    public void nestedCommentAction(Comment comment){
        AgoraNestedCommentFragment nestedCommentFragment = new AgoraNestedCommentFragment();
        nestedCommentFragment.setComment(comment);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, nestedCommentFragment, AgoraNestedCommentFragment.TAG )
                .addToBackStack(AgoraNestedCommentFragment.TAG)
                .commit();
    }

}
