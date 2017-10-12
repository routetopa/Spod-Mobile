package eu.spod.isislab.spodapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.AgoraCommentsAdapter;
import eu.spod.isislab.spodapp.entities.AgoraComment;
import eu.spod.isislab.spodapp.entities.AgoraRoom;
import eu.spod.isislab.spodapp.utils.EndlessScrollListener;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class AgoraRoomFragment extends Fragment implements Observer {
    View asView       = null;
    ListView listView = null;

    AgoraRoom room;
    ArrayList<AgoraComment> comments = new ArrayList<>();
    AgoraCommentsAdapter adapter;
    EndlessScrollListener scrollListener;
    boolean resume = false;

    public AgoraRoomFragment(){}

    public void setRoom(AgoraRoom room){
        this.room = room;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.agora_room_fragment, container, false);
        listView = (ListView) asView.findViewById(R.id.room_comment_list);
        adapter = new AgoraCommentsAdapter(getActivity(), comments, 0);
        listView.setAdapter(adapter);

        scrollListener = new EndlessScrollListener(0) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                getNextCommentPage();
            }
        };
        scrollListener.setScrollDirection(EndlessScrollListener.SCROLL_DIRECTION_UP);

        listView.setOnScrollListener(scrollListener);
        listView.setOnTouchListener(scrollListener);

        ((ImageButton)asView.findViewById(R.id.agora_comment_send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etComment = (EditText)asView.findViewById(R.id.agora_comment_add_new);
                if(!etComment.getText().toString().isEmpty())
                {
                    NetworkChannel.getInstance().addAgoraComment(
                            room.getId(),
                            room.getId(),
                            etComment.getText().toString(),
                            "0",
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
    public void onAttach(Context context) {
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getAgoraRoomPagedComments(room.getId());
        NetworkChannel.getInstance().connectAgoraWebSocket(room.getId());
        ((MainActivity)getActivity()).setToolbarTitle(room.getSubject());

        super.onAttach(context);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void update(Observable o, Object arg) {

        switch(NetworkChannel.getInstance().getCurrentService()) {
            case NetworkChannel.SERVICE_AGORA_GET_COMMENTS:

                try {
                    JSONArray response = new JSONArray((String)arg);

                    for (int i=0; i < response.length(); i++)
                    {
                            JSONObject j = response.getJSONObject(i);
                            comments.add(new AgoraComment(
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

            case NetworkChannel.SERVICE_AGORA_GET_PAGED_COMMENTS:
                try {
                    JSONArray response = new JSONArray((String)arg);

                        for (int i = response.length() - 1; i >= 0 ; i--)
                        {
                            JSONObject j = response.getJSONObject(i);
                            comments.add(0, new AgoraComment(
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

                }catch (JSONException | ClassCastException e) {
                    e.printStackTrace();
                }
                break;
            case NetworkChannel.SERVICE_AGORA_ADD_COMMENT:

                try {
                    JSONObject res = new JSONObject((String) arg);

                    String result = res.getString("result");
                    if (result.equals("ok")) {
                        ((EditText)asView.findViewById(R.id.agora_comment_add_new)).setText("");
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
            case NetworkChannel.SERVICE_SYNC_NOTIFICATION:
                comments.clear();
                NetworkChannel.getInstance().getAgoraRoomPagedComments(room.getId());
                break;
        }
    }

    private void getNextCommentPage(){
        NetworkChannel.getInstance().getAgoraRoomPagedComments(room.getId(), ((AgoraComment)adapter.getItem(0)).getId());
    }

    private void scrollMyListViewToItemIndex(final int size) {
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(size);
            }
        });
    }

    private void scrollToLastVisibleItem(final int index){
        listView.post(new Runnable() {
            @Override
            public void run() {
                View v = listView.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
                listView.setSelectionFromTop(index, top);
            }
        });
    }


}
