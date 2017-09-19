package eu.spod.isislab.spodapp.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.AgoraCommentsAdapter;
import eu.spod.isislab.spodapp.entities.AgoraComment;
import eu.spod.isislab.spodapp.utils.EndlessScrollListener;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class AgoraNestedCommentFragment extends Fragment implements Observer {
    View asView       = null;
    ListView listView = null;

    AgoraComment comment;
    ArrayList<AgoraComment> comments = new ArrayList<>();
    AgoraCommentsAdapter adapter;
    AgoraNestedCommentFragment cInstance;

    public AgoraNestedCommentFragment(){
        cInstance = this;
    }

    public void setComment(AgoraComment comment){
        this.comment = comment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.agora_room_nested_comment_fragment, container, false);
        listView = (ListView) asView.findViewById(R.id.room_nested_comment_list);
        adapter = new AgoraCommentsAdapter(getActivity(), comments, Integer.parseInt(comment.getLevel()) + 1);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new EndlessScrollListener(0) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                getNextCommentPage();
                return true;
            }
        });

        ((TextView)asView.findViewById(R.id.agora_nested_comment_owner_name)).setText(comment.getUsername());
        ((TextView)asView.findViewById(R.id.agora_nested_comment_body)).setText(comment.getComment());
        ((TextView)asView.findViewById(R.id.agora_nested_comment_date)).setText(comment.getTimestamp());

        final ImageView ownerImage = (ImageView) asView.findViewById(R.id.agora_nested_comment_owner_image);

        Glide.with(getActivity())
                .load(comment.getAvatar_url())
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(ownerImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ownerImage.setImageDrawable(circularBitmapDrawable);
                    }
                });

        if(!comment.getDatalet_id().equals("null"))
        {
            ImageView dataletImage = (ImageView) asView.findViewById(R.id.agora_comment_datalet);
            Glide.with(getActivity())
                    .load(NetworkChannel.getInstance().getDataletImageStaticUrl(comment.getDatalet_id()))
                    .fitCenter()
                    .into(dataletImage);

            dataletImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    DataletFragment dataletFragment = new DataletFragment();
                    dataletFragment.setComment(comment);
                    (getActivity()).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, dataletFragment, "datalet_fragment" )
                            .addToBackStack("datalet_fragment")
                            .commit();
                }
            });
        }

        ((ImageButton)asView.findViewById(R.id.agora_nested_comment_send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etComment = (EditText)asView.findViewById(R.id.agora_nested_comment_add_new);
                if(!etComment.getText().toString().isEmpty())
                {
                    NetworkChannel.getInstance().addObserver(cInstance);
                    NetworkChannel.getInstance().addAgoraComment(
                            comment.getRoomId(),
                            comment.getId(),
                            etComment.getText().toString(),
                            "" + (Integer.parseInt(comment.getLevel()) + 1),
                            "0");
                }else{
                    Snackbar.make(getActivity().findViewById(R.id.container), getString(R.string.agora_comment_add_empty_comment_warning), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
            }
        });

        return asView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getAgoraNestedComments(comment.getRoomId(), comment.getId(), "" + (Integer.parseInt(comment.getLevel()) + 1));
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        comments.clear();
    }

    @Override
    public void update(Observable o, Object arg) {

        switch(NetworkChannel.getInstance().getCurrentService()) {
            case NetworkChannel.SERVICE_AGORA_GET_COMMENTS:

                JSONArray response = (JSONArray) arg;

                for (int i=response.length() - 1; i > 0; i--)
                {
                    try {
                        JSONObject j = response.getJSONObject(i);
                        adapter.add(new AgoraComment(
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
                NetworkChannel.getInstance().deleteObserver(this);

                break;
            case NetworkChannel.SERVICE_AGORA_ADD_COMMENT:

                try {
                    JSONObject res = new JSONObject((String) arg);

                    String result = res.getString("result");
                    if (result.equals("ok")) {
                        ((EditText)asView.findViewById(R.id.agora_nested_comment_add_new)).setText("");
                        comments.clear();
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(asView.getWindowToken(), 0);

                        NetworkChannel.getInstance().addObserver(this);
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
        }
    }

    private void getNextCommentPage(){
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getAgoraRoomComments(comment.getId(), ((AgoraComment)adapter.getItem(adapter.getCount() - 1)).getId());
    }
}
