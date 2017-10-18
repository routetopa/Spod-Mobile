package eu.spod.isislab.spodapp.fragments.cocreation;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.CocreationRoom;
import eu.spod.isislab.spodapp.entities.Comment;
import eu.spod.isislab.spodapp.fragments.CommentFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationCommentsFragment extends CommentFragment {

    CocreationRoom room;

    public void setRoom(CocreationRoom room){
        this.room = room;
    }

    @Override
    public void addComment(String comment){
        NetworkChannel.getInstance().addCocreationRoomComment(room.getId(), comment);
    }

    @Override
    public void init() {
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getCocreationRoomComments(room.getId());
        NetworkChannel.getInstance().connectToWebSocket("cocreation", new String[]{"realtime_cocreation_message_" + room.getId()});
        ((MainActivity)getActivity()).setToolbarTitle(room.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
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
            case NetworkChannel.SERVICE_COCREATION_GET_COMMENTS:

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
                                "0",
                                "0",
                                j.getString("timestamp"),
                                "0",
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

            case NetworkChannel.SERVICE_COCREATION_ADD_COMMENT:

                try {
                    JSONObject res = new JSONObject((String) arg);

                    String result = res.getString("result");
                    if (result.equals("ok")) {
                        ((EditText)asView.findViewById(R.id.comment_add_new)).setText("");
                        comments.clear();
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(asView.getWindowToken(), 0);

                        NetworkChannel.getInstance().getCocreationRoomComments(room.getId());
                    } else {
                        Snackbar.make(asView, res.getString("message"), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    break;
                }

                break;
            case NetworkChannel.SERVICE_SYNC_NOTIFICATION:
                comments.clear();
                NetworkChannel.getInstance().getCocreationRoomComments(room.getId());
                break;
        }
    }

}
