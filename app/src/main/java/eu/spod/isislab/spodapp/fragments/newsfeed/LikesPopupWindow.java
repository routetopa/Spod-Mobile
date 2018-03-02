package eu.spod.isislab.spodapp.fragments.newsfeed;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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

    private NewsfeedLikesListAdapter mAdapter;
    private LikesWindowInteractionListener mListener;

    private LinearLayout mToolbar;
    private ListView mList;
    private ProgressBar mProgressBar;
    private int mPrimaryColor;

    public LikesPopupWindow(Context ctx, @NonNull View parent, int widthPixels, int heightPixels) {
        super(widthPixels, heightPixels);
        mContext = ctx;
        mParent = parent;

        View view = LayoutInflater.from(mContext).inflate(R.layout.newsfeed_likes_window, null);
        setContentView(view);

        mPrimaryColor = NewsfeedUtils.getColorResource(mContext, R.color.colorPrimary);

        mToolbar = (LinearLayout) view.findViewById(R.id.toolbar2);
        mList = (ListView) view.findViewById(R.id.newsfeed_likes_window_list_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.newsfeed_likes_window_progress_bar);
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
        Drawable barBg = NewsfeedUtils.getDrawableResource(mContext, R.drawable.bar_bg);

        barBg.setColorFilter(mPrimaryColor, PorterDuff.Mode.SRC_ATOP);

        mToolbar.setBackground(barBg);

        mList.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        showAtLocation(mParent, Gravity.CENTER, 0, 0);
        super.update();

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getLikesList(entityType, entityId);
    }


    private void populateLikesWindow(List<NewsfeedLike> likes) {
        mList.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mAdapter = new NewsfeedLikesListAdapter(mContext, 0, likes);

        if(mListener != null) {
            mAdapter.setLikesWindowInteractionListener(new LikesWindowInteractionListener() {
                @Override
                public void onUserClicked(int id, String name, String avatarUrl) {
                    LikesPopupWindow.this.dismiss();
                    if(mListener != null) {
                        mListener.onUserClicked(id, name, avatarUrl);
                    }
                }
            });
        }

        mList.setAdapter(mAdapter);

        super.update();
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

    public void setLikesWindowInteractionListener(LikesPopupWindow.LikesWindowInteractionListener listener) {
        mListener = listener;
    }

    public void setPrimaryColor(int primaryColor) {
        this.mPrimaryColor = primaryColor;
    }

    public interface LikesWindowInteractionListener {
        void onUserClicked(int id, String name, String avatarUrl);
    }
}
