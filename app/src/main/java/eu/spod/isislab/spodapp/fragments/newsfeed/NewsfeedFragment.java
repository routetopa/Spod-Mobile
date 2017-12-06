package eu.spod.isislab.spodapp.fragments.newsfeed;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.CompressBitmapTask;
import eu.spod.isislab.spodapp.ContentPost;
import eu.spod.isislab.spodapp.ContextActionMenuItem;
import eu.spod.isislab.spodapp.DataletPost;
import eu.spod.isislab.spodapp.FullscreenActivity;
import eu.spod.isislab.spodapp.JsonImage;
import eu.spod.isislab.spodapp.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.NewsfeedLike;
import eu.spod.isislab.spodapp.NewsfeedPostModel;
import eu.spod.isislab.spodapp.NewsfeedPostNetworkInterface;
import eu.spod.isislab.spodapp.NewsfeedUtils;
import eu.spod.isislab.spodapp.Post;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.NewsfeedLikesListAdapter;
import eu.spod.isislab.spodapp.adapters.NewsfeedPostsAdapter;
import eu.spod.isislab.spodapp.fragments.LoginFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class NewsfeedFragment extends Fragment implements Observer, NewsfeedPostsAdapter.PostsAdapterInteractionListener, NewsfeedPostModel.NewsfeedPostModelListener{

    private static final String TAG = "NewsfeedFragment";
    public static final String FRAGMENT_NAME = "NewsfeedFragment";

    public static final String NEWSFEED_SHARED_PREF_FIRST_RUN = "eu.spod.isislab.spodapp.preferences.newsfeed.first_run";

    private SharedPreferences mSharedPref;
    private boolean mFirstRun = false;

    private RecyclerView mPostsList;
    private NewsfeedPostNetworkInterface mNetworkInterface;

    private NewsfeedPostsAdapter mPostsAdapter;
    private FloatingActionButton mAddButton;
    private SwipeRefreshLayout mSwipeToRefresh;

    private boolean isLoadingPosts;

    private PopupWindow mLikesWindow;

    private String mFeedType = NewsfeedUtils.FEED_TYPE_MY;
    private String mFeedId = ""+2; //TODO: replace with getUser().getId();

    public NewsfeedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        mPostsList = (RecyclerView) v.findViewById(R.id.newsfeed_post_container);
        mAddButton = (FloatingActionButton) v.findViewById(R.id.newsfeed_add_post_fab);
        mSwipeToRefresh = (SwipeRefreshLayout) v.findViewById(R.id.newsfeed_post_list_swipe_refresh_layout);

        //mPostsAdapter = new NewsfeedPostsAdapter(this.getContext(), this, NewsfeedUtils.FEED_TYPE_MY, ""+1); //TODO: replace 1 with getUser.getID;

        mPostsAdapter = new NewsfeedPostsAdapter(this.getContext(), this, mFeedType, mFeedId); //TODO: replace 1 with getUser.getID;

        mPostsAdapter.setModelListener(this);
        mNetworkInterface = mPostsAdapter;

        final LinearLayoutManager layout = new LinearLayoutManager(this.getContext());
        mPostsList.setLayoutManager(layout);
        mPostsList.setAdapter(mPostsAdapter);


        mPostsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy == 0) {
                    return;
                }

                int lastVisibleItemPosition = layout.findLastCompletelyVisibleItemPosition();
                int lastItemPosition = recyclerView.getAdapter().getItemCount() - 2;

//                Log.d(TAG, "onScrolled: lastVisibleItemPosition="+lastVisibleItemPosition+"  lastItemPosition=" +lastItemPosition);
                if(lastVisibleItemPosition >= lastItemPosition && lastItemPosition > 0) {
                    if (!isLoadingPosts) {
                        loadNextPage(false);
                    }
                }
            }


        });


        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment addPostFragment = new AddPostFragment();
                addPostFragment.setTargetFragment(NewsfeedFragment.this, 0);
                addPostFragment.show(getActivity().getSupportFragmentManager(), "AddPostDialogFragment");
            }
        });


        mSwipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh: ");
                loadNextPage(true);
            }
        });

        mSwipeToRefresh.setColorSchemeResources(R.color.indigo, R.color.soft_green, R.color.colorPrimary, R.color.colorAccent);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
        NetworkChannel.getInstance().getNewsfeedAuthorization(mFeedType, mFeedId); //TODO: remove this LINE
        NetworkChannel.getInstance().addObserver(this);
        //nLoadFeedPage(true); TODO: uncomment this!!!!!!!
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == AddPostFragment.ADD_POST_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            final String message = data.getStringExtra(AddPostFragment.EXTRA_DATA_POST_MESSAGE);
            Uri attachmentUri = data.getParcelableExtra(AddPostFragment.EXTRA_DATA_POST_ATTACHMENT_URI);

            if(TextUtils.isEmpty(message) && attachmentUri == null) {
                Toast.makeText(getContext(), NewsfeedUtils.getStringResource(getContext(), R.string.newsfeed_empty_field_or_attachment), Toast.LENGTH_LONG).show();
                return;
            }
            if(attachmentUri != null) {
                try {
                    Bitmap attachment;
                    String fileName;
                    String path = NewsfeedUtils.uriToPath(getContext(), attachmentUri);
                    attachment = NewsfeedUtils.loadBitmap(path);

                    if(attachment == null) {
                        throw new IOException();
                    }

                    int index = path.lastIndexOf(File.separatorChar);
                    fileName = path.substring(index + 1);

                    CompressBitmapTask compressTask = new CompressBitmapTask();
                    final String finalFileName = fileName;
                    compressTask.setResultHandler(new CompressBitmapTask.ResultHandler() {
                        @Override
                        public void onCompress(byte[] result) {
                            mNetworkInterface.nSendPost(message, result, finalFileName);
                        }
                    });

                    compressTask.execute(attachment);
                }catch (IOException e) {
                    Toast.makeText(getContext(), "Failed attachment loading", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                mNetworkInterface.nSendPost(message, null, null);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
        getActivity().setTitle(getString(R.string.newsfeed_title));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mSharedPref = getContext().getSharedPreferences(LoginFragment.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);
        if(!mSharedPref.contains(NEWSFEED_SHARED_PREF_FIRST_RUN)) {
            mSharedPref.edit().putBoolean(NEWSFEED_SHARED_PREF_FIRST_RUN, true).apply();
            mFirstRun = true;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        if(mFirstRun) {
            mSharedPref.edit().putBoolean(NEWSFEED_SHARED_PREF_FIRST_RUN, false).apply();
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        NetworkChannel.getInstance().deleteObserver(this);

    }


    private void loadNextPage(boolean reset) {
        mPostsAdapter.nLoadFeedPage(reset);
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d(TAG, "update: " + o.toString());

        boolean handled = true;
        switch (NetworkChannel.getInstance().getCurrentService()) {
            case NetworkChannel.NEWSFEED_SERVICE_GET_AUTHORIZATION:
                try {
                    JSONObject res = new JSONObject((String) o);
                     /** Code for check user's authorization to view/write on current feed. In this case is useless because user always can view (and write on) its personal feed */
                    /*String errorMessage = NewsfeedJSONHelper.getErrorMessage((String) o);
                    if(errorMessage != null) {
                        onError(NetworkChannel.getInstance().getCurrentService(), errorMessage);
                        break;
                    }

                    boolean canView = NewsfeedJSONHelper.isAuthorized(res, NewsfeedJSONHelper.VIEW);
                    boolean canWrite = NewsfeedJSONHelper.isAuthorized(res, NewsfeedJSONHelper.WRITE);

                    if(!canView) {
                        String reason = NewsfeedJSONHelper.getErrorMessage(res.getJSONObject(NewsfeedJSONHelper.VIEW).toString());
                        mAddButton.setVisibility(View.GONE);
                    } else {
                        loadNextPage(false);
                    }

                    if(!canWrite) {
                        String reason = NewsfeedJSONHelper.getErrorMessage(res.getJSONObject(NewsfeedJSONHelper.WRITE).toString());
                        mAddButton.setVisibility(View.GONE);
                    }*/

                    String login = res.getString("login"); //TODO: remove this
                    Toast.makeText(getContext(), login, Toast.LENGTH_LONG).show();
                    loadNextPage(false);
                } catch (JSONException e) {
                    handled = false;
                    e.printStackTrace();
                }
                break;
            case NetworkChannel.NEWSFEED_SERVICE_GET_LIKES_LIST:
                String errorMessage = NewsfeedJSONHelper.getErrorMessage(((String) o));

                if(errorMessage != null) {
                    onError(NetworkChannel.NEWSFEED_SERVICE_GET_LIKES_LIST, errorMessage);
                    break;
                }

                try {
                    JSONArray likes = new JSONArray(((String) o));
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
          /*  isLoadingPosts = false;

            if(mSwipeToRefresh.isRefreshing()) {
                mSwipeToRefresh.setRefreshing(false);
                mAddButton.show();
            }*/

            NetworkChannel.getInstance().deleteObserver(this);
        }
    }

    public void refreshPost(String entityType, int entityId, NewsfeedPostsAdapter.AdapterUpdateType updateType) {
        mPostsAdapter.nRefreshPost(entityType, entityId, updateType);
    }

    //LISTENERS FOR RECYCLERVIEW INTERACTION
    @Override
    public void onCommentsButtonClicked(Post post, int position) {
        if(isLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        PostCommentsFragment commentsSheet = PostCommentsFragment.newInstance(post.getEntityType(), post.getEntityId(), post.getPluginKey(), post.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.explode));
            commentsSheet.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.explode));
            //commentsSheet.setReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));
        }
        getFragmentManager()
                .beginTransaction()
                .hide(this)
                .add(R.id.container, commentsSheet)
                .addToBackStack(PostCommentsFragment.FRAGMAENT_NAME)
                .commit();
    }

    @Override
    public void onLikeButtonLongClicked(Post p) {
        if(isLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }
        showLikesWindow();
        NetworkChannel.getInstance().getLikesList(p.getEntityType(), ""+p.getEntityId());
        NetworkChannel.getInstance().addObserver(this);
    }

    @Override
    public void onPostImageClicked(JsonImage image, ImageView postImageView) {
        if(isLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        Intent intent = new Intent(getContext(), FullscreenActivity.class);
        intent.putExtra(FullscreenActivity.URI_ARGUMENT, Uri.parse(image.getPreviewUrl()));
        intent.putExtra(FullscreenActivity.CURRENT_IMAGE_ID_ARGUMENT, image.getId());
        intent.putExtra(FullscreenActivity.IMAGES_ARGUMENT, new int[] {image.getId()});
        intent.putExtra(FullscreenActivity.FRAGMENT_TYPE_ARGUMENT, FullscreenActivity.FRAGMENT_TYPE_IMAGE);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), postImageView, NewsfeedUtils.getStringResource(getActivity(), R.string.image_transition_name));

        startActivity(intent, options.toBundle());
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

/*        ImageVisualizationFragment imageVisualizationFragment = ImageVisualizationFragment.newInstance(image.getId(), Uri.parse(image.getPreviewUrl()), new int[]{image.getId()});

        getFragmentManager().beginTransaction()
                .addToBackStack(ImageVisualizationFragment.FRAGMENT_NAME)
                .addSharedElement(postImageView, ViewCompat.getTransitionName(postImageView))
                .(R.id.container, imageVisualizationFragment)
                .commit();*/
    }

    @Override
    public void onDataletImageClicked(DataletPost p) {
        if(isLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        Intent intent = new Intent(getContext(), FullscreenActivity.class);
        Log.d(TAG, "onClick: sending url:"+p.getUrl());
        intent.putExtra(FullscreenActivity.FRAGMENT_TYPE_ARGUMENT, FullscreenActivity.FRAGMENT_TYPE_DATALET);
        intent.putExtra(FullscreenActivity.URI_ARGUMENT, p.getUrl());

        startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onContextActionMenuItemClicked(final int position, ContextActionMenuItem.ContextActionType actionType) {
        final Post p = mPostsAdapter.getItemAtPosition(position);
        final ContextActionMenuItem item = p.getContextActionMenuItem(actionType);

        switch (item.getActionType()) {
            case DELETE:
                new AlertDialog.Builder(this.getContext())
                        .setTitle(R.string.newsfeed_delete_confirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mNetworkInterface.nDeletePost(position);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                break;
            case FLAG:
                final List<String> options = item.getOptions();
                final Map<String, String> optionsLabelKey = new HashMap<>(options.size());
                for (String key : options) {
                    int stringId = getResources().getIdentifier("newsfeed_flag_"+key, "string", getActivity().getPackageName());
                    String label = NewsfeedUtils.getStringResource(getContext(), stringId);
                    optionsLabelKey.put(label, key);
                }

                final String[] optionsLabel = optionsLabelKey.keySet().toArray(new String[3]);

                final ArrayAdapter<String> chooseList = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_list_item_1,
                        optionsLabel
                );
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.newsfeed_select_reason)
                        .setSingleChoiceItems(chooseList, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String reasonLabel = optionsLabel[i];
                                String reason = optionsLabelKey.get(reasonLabel);

                                dialogInterface.dismiss();

                                mNetworkInterface.nFlagContent(position, reason);
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    public void onContentLinkClicked(ContentPost post) {
        /*if(post.hasRouting()) {
          //open right fragment/activity according to post.getRouteName()
        } else { */
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getUrl()));
        startActivity(browserIntent);
    }


    private void showLikesWindow() {
        if(mLikesWindow == null) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.newsfeed_likes_window, null);

            Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar2);

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLikesWindow.dismiss();
                }
            });
            toolbar.setSubtitle(NewsfeedUtils.getStringResource(getContext(), R.string.newsfeed_base_post_likes_string));

            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            float widthOffset = 50 * dm.density; //px to dp conversion
            float heightOffset = 150 * dm.density;

            mLikesWindow = new PopupWindow(view, dm.widthPixels - (int) widthOffset, dm.heightPixels - (int) heightOffset);

            mLikesWindow.setFocusable(true);
            mLikesWindow.setBackgroundDrawable(null);
            mLikesWindow.setTouchable(true);
            mLikesWindow.setOutsideTouchable(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mLikesWindow.setElevation(5.0f);
            }
            mLikesWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        }

        View view = mLikesWindow.getContentView();
        ListView list = (ListView) view.findViewById(R.id.newsfeed_likes_window_list_view);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.newsfeed_likes_window_progress_bar);

        list.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        mLikesWindow.showAtLocation(mSwipeToRefresh, Gravity.CENTER, 0, 0);
        mLikesWindow.update();
    }

    private void populateLikesWindow(List<NewsfeedLike> likes) {
        if(mLikesWindow != null){
            View view = mLikesWindow.getContentView();
            ListView list = (ListView) view.findViewById(R.id.newsfeed_likes_window_list_view);
            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.newsfeed_likes_window_progress_bar);

            list.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            NewsfeedLikesListAdapter adapter = new NewsfeedLikesListAdapter(getContext(), 0, likes);
            list.setAdapter(adapter);

            mLikesWindow.update();
        }
    }

    //MESSAGES FROM MODEL
    @Override
    public void onContentLoadingStarted(boolean resetList) {
        if (resetList && !mSwipeToRefresh.isRefreshing()) {
            mSwipeToRefresh.setRefreshing(true);
            mAddButton.hide();
        }

        isLoadingPosts = true;
    }

    @Override
    public void onContentLoaded(boolean scrollOnTop) {
        isLoadingPosts = false;

        if (mSwipeToRefresh.isRefreshing()) {
            mSwipeToRefresh.setRefreshing(false);
            mAddButton.show();
        }

        if(scrollOnTop) {
            mPostsList.scrollToPosition(0);
        }
    }

    @Override
    public void onContentLoadStopped() {
        if(isLoadingPosts) {
            LinearLayoutManager layout = (LinearLayoutManager) mPostsList.getLayoutManager();
            int lastVisibleItemPosition = layout.findLastCompletelyVisibleItemPosition();
            int lastItemPosition = mPostsList.getAdapter().getItemCount() - 2;

            if(lastVisibleItemPosition >= lastItemPosition && lastItemPosition > 0) {
                mPostsAdapter.setFooterType(NewsfeedPostsAdapter.FooterType.LOAD_MORE);
            }

        }
        isLoadingPosts = false;
    }

    @Override
    public void onError(String service, String message) {
        Toast.makeText(getContext(), service + ": " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFeedLoadingTimeout() {
        Snackbar.make(getView(), R.string.newsfeed_slow_connection, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.newsfeed_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadNextPage(false);
                    }
                }).show();
    }

    /*@Override
    public void onLikeButtonClicked(Post p, int position) {
        NetworkChannel.getInstance().addObserver(this);
        lastPostUpdated = p;
        lastPostUpdatedPosition = position;

        if(!p.isLiked()) {
            p.setLiked(true);
            NetworkChannel.getInstance().nLikeUnlikePost(p.getEntityType(), p.getEntityId());
        } else {
            p.setLiked(false);
            NetworkChannel.getInstance().unlikePost(p.getEntityType(), p.getEntityId());
        }
    }*/
}
