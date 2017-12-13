package eu.spod.isislab.spodapp.fragments.newsfeed;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.activities.FullscreenActivity;
import eu.spod.isislab.spodapp.adapters.NewsfeedLikesListAdapter;
import eu.spod.isislab.spodapp.adapters.NewsfeedPostsAdapter;
import eu.spod.isislab.spodapp.entities.ContentPost;
import eu.spod.isislab.spodapp.entities.ContextActionMenuItem;
import eu.spod.isislab.spodapp.entities.DataletPost;
import eu.spod.isislab.spodapp.entities.ImageListPost;
import eu.spod.isislab.spodapp.entities.ImagePost;
import eu.spod.isislab.spodapp.entities.JsonImage;
import eu.spod.isislab.spodapp.entities.NewsfeedImageInfo;
import eu.spod.isislab.spodapp.entities.NewsfeedLike;
import eu.spod.isislab.spodapp.entities.Post;
import eu.spod.isislab.spodapp.utils.CompressBitmapTask;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedPostModel;
import eu.spod.isislab.spodapp.utils.NewsfeedPostNetworkInterface;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;
import eu.spod.isislab.spodapp.utils.UserManager;

public class NewsfeedFragment extends Fragment implements NewsfeedPostsAdapter.PostsAdapterInteractionListener, NewsfeedPostModel.NewsfeedPostModelListener {

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

        //TODO: remove t
        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);

        View v = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        mPostsList = (RecyclerView) v.findViewById(R.id.newsfeed_post_container);
        mAddButton = (FloatingActionButton) v.findViewById(R.id.newsfeed_add_post_fab);
        mSwipeToRefresh = (SwipeRefreshLayout) v.findViewById(R.id.newsfeed_post_list_swipe_refresh_layout);

        mPostsAdapter = new NewsfeedPostsAdapter(this.getContext(), this, NewsfeedUtils.FEED_TYPE_SITE, UserManager.getInstance().getId()); //TODO: replace 1 with getUser.getID;

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

        mSwipeToRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.indigo);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: ");
        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.newsfeed_title));
        if(mFirstRun) {
            mSharedPref.edit().putBoolean(NEWSFEED_SHARED_PREF_FIRST_RUN, false).apply();
        }

        mPostsAdapter.nRequestAuthorization();

        super.onActivityCreated(savedInstanceState);
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
                final ProgressDialog loading = ProgressDialog.show(getContext(), "SPOD Mobile", getContext().getString(R.string.wait_network_message), false, false);
                try {
                    Bitmap attachment;
                    String fileName;
                    String path = NewsfeedUtils.uriToPath(getContext(), attachmentUri);
                    attachment = NewsfeedUtils.loadBitmap(getContext(), path);

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
                            loading.dismiss();
                            mNetworkInterface.nSendPost(message, result, finalFileName);
                        }
                    });

                    compressTask.execute(attachment);
                    attachment = null;
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
        getActivity().setTitle(R.string.newsfeed_title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        mSharedPref = getContext().getSharedPreferences(Consts.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);
        if(!mSharedPref.contains(NEWSFEED_SHARED_PREF_FIRST_RUN)) {
            mSharedPref.edit().putBoolean(NEWSFEED_SHARED_PREF_FIRST_RUN, true).apply();
            mFirstRun = true;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
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
        //NetworkChannel.getInstance().deleteObserver(this);

    }


    private void loadNextPage(boolean reset) {
        mPostsAdapter.nLoadFeedPage(reset);
    }

    public void refreshPost(String entityType, int entityId, NewsfeedPostsAdapter.AdapterUpdateType updateType) {
        mPostsAdapter.nRefreshPost(entityType, entityId, updateType);
    }

    private void showLikesWindow(String entityType, String entityId) {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        float widthOffset = NewsfeedUtils.pxToDp(getContext(), 50); //px to dp conversion
        float heightOffset = NewsfeedUtils.pxToDp(getContext(), 150);

        LikesPopupWindow likesPopupWindow = new LikesPopupWindow(getContext(), mSwipeToRefresh, dm.widthPixels - (int) widthOffset, dm.heightPixels - (int) heightOffset);
        likesPopupWindow.show(entityType, entityId);
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
        showLikesWindow(p.getEntityType(), ""+p.getEntityId());
    }

    @Override
    public void onPostImageClicked(ImagePost post, ImageView postImageView) {
        if(isLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        JsonImage image = post.getImage();
        Post.User user = post.getUserInfo(post.getUserId());
        NewsfeedImageInfo imageInfo = new NewsfeedImageInfo(image.getId(), image.getDescription(), post.getTimestamp(), user.getName(), user.getUserId(), null, -1, null);
        Intent intent = new Intent(getContext(), FullscreenActivity.class);
        intent.putExtra(FullscreenActivity.URI_ARGUMENT, Uri.parse(image.getPreviewUrl()));
        intent.putExtra(FullscreenActivity.CURRENT_IMAGE_INFO_ARGUMENT, imageInfo);
        intent.putExtra(FullscreenActivity.IMAGES_ARGUMENT, new String[] {image.getId()}); //TODO: implement visualization of multiple images
        intent.putExtra(FullscreenActivity.FRAGMENT_TYPE_ARGUMENT, FullscreenActivity.FRAGMENT_TYPE_IMAGE);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), postImageView, NewsfeedUtils.getStringResource(getActivity(), R.string.newsfeed_image_transition_name));

        startActivity(intent, options.toBundle());
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onPostImageClicked(ImageListPost post, String selectedImageId, ImageView postImageView) {
        if(isLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        JsonImage image = post.getImageById(selectedImageId);
        Post.User user = post.getUserInfo(post.getUserId());
        NewsfeedImageInfo imageInfo = new NewsfeedImageInfo(image.getId(), image.getDescription(), post.getTimestamp(), user.getName(), user.getUserId(), null, -1, null);
        Intent intent = new Intent(getContext(), FullscreenActivity.class);
        intent.putExtra(FullscreenActivity.URI_ARGUMENT, Uri.parse(image.getPreviewUrl()));
        intent.putExtra(FullscreenActivity.CURRENT_IMAGE_INFO_ARGUMENT, imageInfo);
        intent.putExtra(FullscreenActivity.IMAGES_ARGUMENT, post.getIdList()); //TODO: implement visualization of multiple images
        intent.putExtra(FullscreenActivity.FRAGMENT_TYPE_ARGUMENT, FullscreenActivity.FRAGMENT_TYPE_IMAGE);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), postImageView, NewsfeedUtils.getStringResource(getActivity(), R.string.newsfeed_image_transition_name));

        startActivity(intent, options.toBundle());
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
            case DELETE_POST:
                new AlertDialog.Builder(this.getContext())
                        .setMessage(R.string.newsfeed_delete_confirm)
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
            case FLAG_CONTENT:
                final String[] reasonKeys = getResources().getStringArray(R.array.newsfeed_flag_reason);
                final String[] reasonsLabel = new String[reasonKeys.length];

                for(int i = 0; i<reasonKeys.length; i++) {
                    reasonsLabel[i] = NewsfeedUtils.getStringByResourceName(getContext(), getActivity().getPackageName(), "newsfeed_", reasonKeys[i]);
                }

                final ArrayAdapter<String> chooseList = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_list_item_1,
                        reasonsLabel
                );
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.newsfeed_select_reason)
                        .setSingleChoiceItems(chooseList, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String reason = reasonKeys[i];
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
    public void onAuthorizationResult(boolean canView, boolean canWrite) {
        if(canView) {
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadNextPage(true);
                }
            }, 500);
        } else {

        }

        if(!canWrite) {
            mAddButton.setVisibility(View.GONE);
        }
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
        //Toast.makeText(getContext(), service + ": " + message, Toast.LENGTH_LONG).show();
        Log.e(TAG, service + ": " + message);
    }

    @Override
    public void onError(int resource) {
        Toast.makeText(getContext(), resource, Toast.LENGTH_LONG).show();
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

}
