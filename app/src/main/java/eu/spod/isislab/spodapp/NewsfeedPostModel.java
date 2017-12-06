package eu.spod.isislab.spodapp;


import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.adapters.NewsfeedPostsAdapter;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class NewsfeedPostModel implements Observer, NewsfeedPostNetworkInterface {

    private static final String TAG = "NewsfeedPostModel";

    private NewsfeedPostsAdapter mAdapter;
    private ArrayList<Post> mPosts;
    private NewsfeedPostModelListener mModelListener;

    private Post lastPostUpdated = null;
    private int lastPostUpdatedPosition = -1;
    private NewsfeedPostsAdapter.AdapterUpdateType lastPostUpdatedType;

    private String mCurrentFeedType;
    private String mCurrentFeedId;

    private int mCurrentPage = -1;
    private int mPostPageCount = NewsfeedUtils.DEFAULT_ITEM_PER_PAGE_COUNT;

    private boolean mIsLoading = false;
    private int mRequestTryCount = 0;
    private CountDownTimer mRequestTimer = new CountDownTimer(30 * 1000, 1000) {

        @Override
        public void onTick(long l) {
            Log.d(TAG, "onTick: " + l);
        }

        @Override
        public void onFinish() {
            NetworkChannel.getInstance().stopFeedRequest();
            NetworkChannel.getInstance().deleteObserver(NewsfeedPostModel.this);

            mCurrentPage = mCurrentPage - 1; //current page not loaded, so we return to previous page
            if (/*mRequestTryCount < 3 ||*/ mPostPageCount > 3) {
                mRequestTryCount++;
                mPostPageCount = Math.round(mPostPageCount / 2f);
                //mCurrentPage = mCurrentPage - 1; //We have to retry to load last page
                Log.d(TAG, "Loading timeout. Trying with "  + mPostPageCount + " posts");
                nLoadFeedPage(false);
            } else {
                //mCurrentPage = mCurrentPage - 1; //current page not loaded, so we return to previous page
                if (mModelListener != null) {
                    mModelListener.onFeedLoadingTimeout();
                }
            }
        }
    };

    public NewsfeedPostModel(NewsfeedPostsAdapter mAdapter, String feedType, String feedId) {
        this.mAdapter = mAdapter;
        this.mPosts = new ArrayList<>();
        this.mCurrentFeedType = feedType;
        this.mCurrentFeedId = feedId;
    }

    public void setModelListener(NewsfeedPostModelListener modelListener) {
        this.mModelListener = modelListener;
    }

    public void setData(ArrayList<Post> posts) {
        this.mPosts = posts;
        mAdapter.notifyDataSetChanged();
    }

    public void addOnTop(Post post) {
        mPosts.add(0, post);
        mAdapter.notifyItemInserted(0);
        mAdapter.notifyItemRangeChanged(0, mPosts.size()); //update position of items below the new inserted one
    }

    public void appendData(final ArrayList<Post> posts) {
        final int lastPosition = mAdapter.getItemCount();
        mPosts.addAll(posts);

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                mAdapter.notifyItemRangeInserted(lastPosition, mPosts.size());
            }
        };

        handler.post(r);

        mAdapter.setHasFooter(true);
    }

    public Post get(int position) {
        return mPosts.get(position);
    }

    public int size() {
        return mPosts.size();
    }

    public void replaceAtPosition(Post p, int position, NewsfeedPostsAdapter.AdapterUpdateType updateType) {
        mPosts.set(position, p);
        mAdapter.notifyItemChanged(position, updateType);
    }

    public void remove(int position) {
        mPosts.remove(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, mPosts.size() - position);
    }

    public Post getItemAtPosition(int position) {
        if (mPosts != null)
            return mPosts.get(position);

        return null;
    }

    public int findItemPosition(String entityType, int entityId) {
        int position = -1;

        for (int i = 0; i < mPosts.size(); i++) {
            Post p = mPosts.get(i);
            if (p.getEntityType().equals(entityType) && p.getEntityId() == entityId) {
                position = i;
                break;
            }
        }

        return position;
    }

    public void clearList() {
        int listSize = mPosts.size();
        if (listSize > 0) {
            mPosts.clear();
            //notifyItemRangeRemoved(0, listSize);
            mAdapter.setHasFooter(false);
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void update(Observable observable, Object o) {
        Log.d(TAG, "update: " + o);

        String response = ((String) o);
        boolean handled = true;
        boolean scrollOnTop = false;

        String currentService = NetworkChannel.getInstance().getCurrentService();

        switch (currentService) {
            case NetworkChannel.NEWSFEED_SERVICE_GET_FEED:
                JSONArray jsonPostsArray;
                try {
                    jsonPostsArray = new JSONArray(response);
                } catch (JSONException e) {
                    String errorMessage = NewsfeedJSONHelper.getErrorMessage(response);

                    if(errorMessage != null) {
                        mModelListener.onError(currentService, errorMessage);
                    } else {
                        handled = false;
                        Log.e(TAG, "Failed post loading", e);
                    }
                    break;
                }
                /*if (jsonPostsArray.length() == 0) {
                    handled = false;
                }*/

                mRequestTimer.cancel();
                mIsLoading = false;

                Log.d(TAG, "Array=" + jsonPostsArray);
                final ArrayList<Post> posts = new ArrayList<>(jsonPostsArray.length());
                for (int i = 0; i < jsonPostsArray.length(); i++) {
                    try {
                        JSONObject jsonPost = jsonPostsArray.getJSONObject(i);
                        Post p = NewsfeedJSONHelper.createPost(jsonPost);
                        posts.add(p);
                    } catch (JSONException e) {
                        //e.printStackTrace();
                        Log.e(TAG, "Error creating post at position " + i, e);
                        handled = false;
                        break;
                    }
                }

                if (posts.size() > 0) {
                    appendData(posts);
                } else {
                    mAdapter.setHasFooter(false);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case NetworkChannel.NEWSFEED_SERVICE_GET_POST:
                try {
                    String errorMessage = NewsfeedJSONHelper.getErrorMessage(response);
                    if(errorMessage != null) {
                        mModelListener.onError(currentService, errorMessage);
                        break;
                    }

                    JSONObject jsonPost = new JSONObject(response);
                    Log.d(TAG, "JSONObj=" + jsonPost);
                    Post p = NewsfeedJSONHelper.createPost(jsonPost);
                    int itemPosition = findItemPosition(p.getEntityType(), p.getEntityId());
                    if (itemPosition >= 0) { //if model contains yet this post we have to update it
                        lastPostUpdatedType = lastPostUpdatedType != null ? lastPostUpdatedType : NewsfeedPostsAdapter.AdapterUpdateType.ALL;
                        updatePostAtPosition(itemPosition, p, lastPostUpdatedType);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error on creating new single post", e);
                    handled = false;
                }
                break;
            case NetworkChannel.NEWSFEED_SERVICE_ADD_NEW_STATUS:
                String errorMessage = NewsfeedJSONHelper.getErrorMessage(response);
                if(errorMessage != null) {
                    mModelListener.onError(currentService, errorMessage);
                    break;
                }

                try {
                    JSONObject jsonPost = new JSONObject(response);
                    Log.d(TAG, "JSONObj=" + jsonPost);
                    Post p = NewsfeedJSONHelper.createPost(jsonPost);
                    addOnTop(p);
                    scrollOnTop = true;
                } catch (JSONException e) {
                    Log.e(TAG, "Error on creating new single post", e);
                    handled = false;
                }
                break;
            case NetworkChannel.NEWSFEED_SERVICE_LIKE_POST:
            case NetworkChannel.NEWSFEED_SERVICE_UNLIKE_POST:
                try {
                    JSONObject jsonLikeResponse = new JSONObject(response);
                    boolean result = jsonLikeResponse.getBoolean(NewsfeedJSONHelper.RESULT);
                    if (result) {
                        if (lastPostUpdated != null) {
                            int likeCount = jsonLikeResponse.getInt(NewsfeedJSONHelper.LIKE_COUNT);
                            lastPostUpdated.setLikesCount(likeCount);
                            updatePostAtPosition(lastPostUpdatedPosition, lastPostUpdated, NewsfeedPostsAdapter.AdapterUpdateType.LIKES);
                            lastPostUpdated = null;
                            lastPostUpdatedPosition = -1;
                        }
                    } else {
                        String message = jsonLikeResponse.getString(NewsfeedJSONHelper.MESSAGE);
                        Log.w(TAG, "LikePost: " + message);
                        if (lastPostUpdated != null) {
                            lastPostUpdated.setLiked(!lastPostUpdated.isLiked()); //in case of error we have to reverse the field setted in 'onLikeButtonClicked' to undo the change
                            updatePostAtPosition(lastPostUpdatedPosition, lastPostUpdated, NewsfeedPostsAdapter.AdapterUpdateType.LIKES);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Invalid like response", e);
                }
                break;
            default:
                handled = false;
                break;
        }

        if (handled) {
            mModelListener.onContentLoaded(scrollOnTop);
            NetworkChannel.getInstance().deleteObserver(this);
        }
    }


    private void updatePostAtPosition(final int position, final Post post, final NewsfeedPostsAdapter.AdapterUpdateType updateType) {

        replaceAtPosition(post, position, updateType);
        //mPostsList.getAdapter().notifyItemChanged(position, updateType);

        lastPostUpdated = null;
        lastPostUpdatedPosition = -1;
        lastPostUpdatedType = null;
    }

    //METHODS FOR NETWORK COMMUNICATION
    @Override
    public void nLoadFeedPage(boolean resetList) {
        mRequestTryCount++;
        mModelListener.onContentLoadingStarted(resetList);

        if (resetList) {
            clearList();

            mCurrentPage = -1;
            mRequestTryCount = -1;
        }

        mCurrentPage++;
        int offset = mCurrentPage * mPostPageCount;

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().loadNextPosts(mCurrentFeedType, mCurrentFeedId, offset, mPostPageCount);

        mRequestTimer.start();
        mIsLoading = true;
    }

    @Override
    public void nRefreshPost(String entityType, int entityId, NewsfeedPostsAdapter.AdapterUpdateType updateType) {
        if(mIsLoading) {
            nStopPendingRequest();
        }

        lastPostUpdatedType = updateType;
        NetworkChannel.getInstance().getPost(mCurrentFeedType, mCurrentFeedId, entityType, entityId);
        NetworkChannel.getInstance().addObserver(this);
    }


    @Override
    public void nSendPost(String message, byte[] attachment, String filename) {
        Log.d(TAG, "nSendPost: " + mCurrentFeedType);
        NetworkChannel.getInstance().sendStatus(mCurrentFeedType, mCurrentFeedId, message, attachment, filename);
        NetworkChannel.getInstance().addObserver(this);
        Log.d(TAG, "nSendPost: " + mCurrentFeedType);
    }

    @Override
    public void nLikeUnlikePost(int position){
        if(mIsLoading) {
            nStopPendingRequest();
        }

        Post p = get(position);

        NetworkChannel.getInstance().addObserver(this);
        lastPostUpdated = p;
        lastPostUpdatedPosition = position;

        if(!p.isLiked()) {
            p.setLiked(true);
            NetworkChannel.getInstance().likePost(p.getEntityType(), p.getEntityId());
        } else {
            p.setLiked(false);
            NetworkChannel.getInstance().unlikePost(p.getEntityType(), p.getEntityId());
        }
    }

    @Override
    public void nDeletePost(int position) {
        if(mIsLoading) {
            nStopPendingRequest();
        }

        Post p = get(position);
        ContextActionMenuItem action = p.getContextActionMenuItem(ContextActionMenuItem.ContextActionType.DELETE);

        NetworkChannel.getInstance().deleteContent(action.getActionUrl(), action.getParams());
        NetworkChannel.getInstance().addObserver(this);

        remove(position);
    }

    @Override
    public void nFlagContent(int position, String reason) {
        if(mIsLoading) {
            nStopPendingRequest();
        }

        Post p = get(position);
        ContextActionMenuItem action = p.getContextActionMenuItem(ContextActionMenuItem.ContextActionType.FLAG);
        Map<String, String> params = action.getParams();
        params.put(action.getParamOptionsTarget(), reason);

        NetworkChannel.getInstance().flagContent(action.getActionUrl(), params);
        NetworkChannel.getInstance().addObserver(this);
    }

    @Override
    public void nStopPendingRequest() {
        if(mIsLoading) {
            mCurrentPage = mCurrentPage - 1;
            NetworkChannel.getInstance().stopFeedRequest();
            mRequestTimer.cancel();
            mModelListener.onContentLoadStopped();
            mIsLoading = false;
        }
    }


    public interface NewsfeedPostModelListener {
        void onContentLoadingStarted(boolean resetList);
        void onContentLoaded(boolean scrollOnTop);
        void onContentLoadStopped();
        void onError(String service, String message);
        void onFeedLoadingTimeout();
    }

}