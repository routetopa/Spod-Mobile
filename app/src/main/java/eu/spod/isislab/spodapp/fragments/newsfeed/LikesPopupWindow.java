package eu.spod.isislab.spodapp.fragments.newsfeed;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.NewsfeedLikesListAdapter;
import eu.spod.isislab.spodapp.entities.NewsfeedLike;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;

public class LikesPopupWindow extends PopupWindow implements Observer{

    private static final String TAG = "LikesPopupWindow";
    private Context mContext;
    private View mParent;

    private Toolbar mToolbar;
    private ListView mList;
    private ProgressBar mProgressBar;

    public LikesPopupWindow(Context ctx, View parent, int widthPixels, int heightPixels) {
        super(widthPixels, heightPixels);
        mContext = ctx;
        mParent = parent;

        View view = LayoutInflater.from(mContext).inflate(R.layout.newsfeed_likes_window, null);
        setContentView(view);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar2);
        mList = (ListView) view.findViewById(R.id.newsfeed_likes_window_list_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.newsfeed_likes_window_progress_bar);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mToolbar.setSubtitle(NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_base_post_likes_string));
        mToolbar.setSubtitleTextColor(NewsfeedUtils.getColorResource(mContext, android.R.color.white));

        setWindowAttributes();
    }


    private void setWindowAttributes() {
        setFocusable(true);
        setBackgroundDrawable(null);
        setTouchable(true);
        setOutsideTouchable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(5.0f);
        }
        setAnimationStyle(android.R.style.Animation_Dialog);
    }

    public void show(String entityType, String entityId) {
        mList.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        showAtLocation(mParent, Gravity.CENTER, 0, 0);
        update();

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getLikesList(entityType, entityId);
    }


    private void populateLikesWindow(List<NewsfeedLike> likes) {
        mList.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        NewsfeedLikesListAdapter adapter = new NewsfeedLikesListAdapter(mContext, 0, likes);
        mList.setAdapter(adapter);

        update();
    }

    @Override
    public void update(Observable o, Object arg) {
        Log.d(TAG, "update: " + o.toString());

        boolean handled = true;
        switch (NetworkChannel.getInstance().getCurrentService()) {
            case Consts.NEWSFEED_SERVICE_GET_LIKES_LIST:
                String errorMessage = NewsfeedJSONHelper.getErrorMessage(((String) arg));

                if(errorMessage != null) {
                    Log.e(TAG, errorMessage);
                    break;
                }

                try {
                    JSONArray likes = new JSONArray(((String) arg));
                    ArrayList<NewsfeedLike> likesList = NewsfeedJSONHelper.createLikesList(likes);
                    populateLikesWindow(likesList);
                } catch (JSONException e) {
                    Log.w(TAG, "Invalid likes list", e);
                    handled = false;
                }
                break;
            default:
                handled = false;
                break;
        }

        if(handled) {
            NetworkChannel.getInstance().deleteObserver(this);
        }
    }
}
