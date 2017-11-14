package eu.spod.isislab.spodapp.fragments.agora;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.CommentsAdapter;
import eu.spod.isislab.spodapp.entities.Comment;
import eu.spod.isislab.spodapp.fragments.CommentFragment;
import eu.spod.isislab.spodapp.fragments.DataletFragment;
import eu.spod.isislab.spodapp.fragments.NestedCommentFragment;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class AgoraNestedCommentFragment extends NestedCommentFragment {

    public static final String TAG = "AgoraNestedCommentFragment";

    @Override
    public void init() {
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().connectToWebSocket("agora", new String[]{"realtime_message_" +  comment.getRoomId()});
        NetworkChannel.getInstance().getAgoraNestedComments(comment.getRoomId(), comment.getId(), "" + (Integer.parseInt(comment.getLevel()) + 1));
    }

    @Override
    public void onResume() {
        super.onResume();
        comments.clear();
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
            case Consts.SERVICE_AGORA_GET_COMMENTS:

                JSONArray response = null;
                try {
                    response = new JSONArray((String)arg);
                } catch (JSONException e) {
                    e.printStackTrace();
                    break;
                }

                for (int i=0; i < response.length(); i++)
                {
                    try {
                        JSONObject j = response.getJSONObject(i);
                        adapter.add(new Comment(
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
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
                break;
            case Consts.SERVICE_AGORA_ADD_COMMENT:

                try {
                    JSONObject res = new JSONObject((String) arg);

                    String result = res.getString("result");
                    if (result.equals("ok")) {
                        ((EditText)asView.findViewById(R.id.nested_comment_add_new)).setText("");
                        comments.clear();
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(asView.getWindowToken(), 0);

                        NetworkChannel.getInstance().getAgoraNestedComments(comment.getRoomId(), comment.getId(), "" + (Integer.parseInt(comment.getLevel()) + 1));
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
                NetworkChannel.getInstance().getAgoraNestedComments(comment.getRoomId(), comment.getId(), "" + (Integer.parseInt(comment.getLevel()) + 1));
                break;
        }
    }
}
