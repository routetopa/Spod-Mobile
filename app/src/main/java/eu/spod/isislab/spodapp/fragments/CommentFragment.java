package eu.spod.isislab.spodapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.CommentsAdapter;
import eu.spod.isislab.spodapp.entities.Comment;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.EndlessScrollListener;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CommentFragment extends Fragment implements Observer {

    public static final String TAG = "CommentFragment";

    CommentFragment cInstance = null;

    public View asView         = null;
    public ListView listView   = null;
    public LinearLayout loader = null;

    public ArrayList<Comment> comments = new ArrayList<>();
    public EndlessScrollListener scrollListener;
    public CommentsAdapter adapter;

    public int maxLevel = 1;

    public CommentFragment(){
        cInstance = this;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.comment_fragment, container, false);

        loader = (LinearLayout) asView.findViewById(R.id.room_comment_loader);

        Glide.with(getActivity())
                .asGif()
                .load(R.drawable.jelly_fluid_loader)
                .apply(new RequestOptions()
                        .centerCrop()
                        .timeout(10000)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into((ImageView)asView.findViewById(R.id.loader_image));

        listView = (ListView) asView.findViewById(R.id.room_comment_list);
        adapter = new CommentsAdapter(getActivity(), comments, maxLevel){
            @Override
            public void nestedCommentAction(Comment comment){
                cInstance.nestedCommentAction(comment);
            }
        };
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

        ((ImageButton)asView.findViewById(R.id.comment_send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etComment = (EditText)asView.findViewById(R.id.comment_add_new);
                if(!etComment.getText().toString().isEmpty())
                {
                    addComment(etComment.getText().toString());
                }else{
                    Snackbar.make(getActivity().findViewById(R.id.container), getString(R.string.comment_add_empty_comment_warning), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
            }
        });

        return asView;
    }

    @Override
    public void onAttach(Context context) {
        init();
        super.onAttach(context);
    }

    public void scrollMyListViewToItemIndex(final int size) {
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(size);
            }
        });
    }

    public void scrollToLastVisibleItem(final int index){
        listView.post(new Runnable() {
            @Override
            public void run() {
                View v = listView.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
                listView.setSelectionFromTop(index, top);
            }
        });
    }

    public void showLoader(boolean active){
        ViewGroup.LayoutParams params = loader.getLayoutParams();
        params.height = (active) ? 100 : 0;
        loader.setLayoutParams(params);
    }

    public void init(){ }

    public void initAdapter(){}

    public void initNotificationSwitch(boolean checked){
        Switch notificationSwitch = (Switch) (asView.findViewById(R.id.notification_switch));
        notificationSwitch.setChecked(checked);
        ((Switch) (asView.findViewById(R.id.notification_switch))).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onNotificationSwitchChange(buttonView);
            }
        });
    }

    public void addComment(String comment){}

    public void getNextCommentPage(){};

    public void nestedCommentAction(Comment comment){}

    public void onNotificationSwitchChange(View v){}

    @Override
    public void update(Observable o, Object arg) {

    }
}
