package eu.spod.isislab.spodapp.fragments.newsfeed;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.NewsfeedPostsAdapter;
import eu.spod.isislab.spodapp.entities.ContentPost;
import eu.spod.isislab.spodapp.entities.ContextActionMenuItem;
import eu.spod.isislab.spodapp.entities.DataletPost;
import eu.spod.isislab.spodapp.entities.ImageListPost;
import eu.spod.isislab.spodapp.entities.ImagePost;
import eu.spod.isislab.spodapp.entities.JsonImage;
import eu.spod.isislab.spodapp.entities.Post;
import eu.spod.isislab.spodapp.utils.CompressBitmapTask;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedPostInteractionHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedPostModel;
import eu.spod.isislab.spodapp.utils.NewsfeedPostNetworkInterface;
import eu.spod.isislab.spodapp.utils.NewsfeedPostRefreshable;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;
import eu.spod.isislab.spodapp.utils.Tooltip;
import eu.spod.isislab.spodapp.utils.UserManager;

public class NewsfeedFragment extends Fragment implements NewsfeedPostsAdapter.PostsAdapterInteractionListener, NewsfeedPostModel.NewsfeedPostModelListener, NewsfeedPostRefreshable {

    private static final String TAG = "NewsfeedFragment";
    public static final String FRAGMENT_NAME = "NewsfeedFragment";

    public static final String NEWSFEED_SHARED_PREF_FIRST_RUN = "eu.spod.isislab.spodapp.preferences.newsfeed.first_run2";
    public static final String NEWSFEED_SHARED_PREF_DEFAULT_FEED = "eu.spod.isislab.spodapp.preferences.newsfeed.default_feed";

    private SharedPreferences mSharedPref;

    private RecyclerView mPostsList;
    private NewsfeedPostNetworkInterface mNetworkInterface;

    private NewsfeedPostsAdapter mPostsAdapter;
    private FloatingActionMenu mAddButton;
    private SwipeRefreshLayout mSwipeToRefresh;
    private Switch mFriendsActivitiesSwitch;

    private boolean mIsLoadingPosts;

    private NewsfeedPostInteractionHelper mPostInteractionHelper;
    public NewsfeedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        mSwipeToRefresh = (SwipeRefreshLayout) v.findViewById(R.id.newsfeed_post_list_swipe_refresh_layout);
        mPostsList = (RecyclerView) v.findViewById(R.id.newsfeed_post_container);
        mFriendsActivitiesSwitch = (Switch) v.findViewById(R.id.newsfeed_show_friends_activities_switch);
        mAddButton = (FloatingActionMenu) v.findViewById(R.id.newsfeed_add_post_fab);
        mSharedPref = getContext().getSharedPreferences(Consts.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE);

        final LinearLayoutManager layout = new LinearLayoutManager(this.getContext());
        mPostsList.setLayoutManager(layout);
        mPostsList.setAdapter(mPostsAdapter);

        mPostsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy == 0) {
                    return;
                }

                if(dy > 20) {
                    if (mFriendsActivitiesSwitch.getVisibility() == View.VISIBLE) {
                        mFriendsActivitiesSwitch.setVisibility(View.GONE);
                        mPostsList.setPadding(0, 0,0,0);
                    }
                    if (!mAddButton.isMenuButtonHidden()) {
                        mAddButton.hideMenuButton(true);
                    }
                } else if(dy < -20) {
                    if (mFriendsActivitiesSwitch.getVisibility() == View.GONE) {
                        mFriendsActivitiesSwitch.setVisibility(View.VISIBLE);
                        mPostsList.setPadding(0, mFriendsActivitiesSwitch.getMeasuredHeight(),0,0);
                    }
                    if (mAddButton.isMenuButtonHidden()) {
                        mAddButton.showMenuButton(true);
                    }
                }

                int lastVisibleItemPosition = layout.findLastCompletelyVisibleItemPosition();
                int lastItemPosition = recyclerView.getAdapter().getItemCount() - 3;

//                Log.d(TAG, "onScrolled: lastVisibleItemPosition="+lastVisibleItemPosition+"  lastItemPosition=" +lastItemPosition);
                if(lastVisibleItemPosition >= lastItemPosition && lastItemPosition > 0) {
                    if (!mIsLoadingPosts) {
                        loadNextPage(false);
                    }
                }
            }


        });

        mFriendsActivitiesSwitch.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPostsList.setPadding(0, mFriendsActivitiesSwitch.getMeasuredHeight(),0,0);
                mFriendsActivitiesSwitch.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        mFriendsActivitiesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    changePostAdapter(NewsfeedUtils.FEED_TYPE_MY);
                } else {
                    changePostAdapter(NewsfeedUtils.FEED_TYPE_SITE);
                }
            }
        });

        mAddButton.findViewById(R.id.newsfeed_add_link_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

                mAddButton.close(true);
                View content = LayoutInflater.from(getContext()).inflate(R.layout.newsfeed_paste_link_dialog, null);
                final EditText editText = (EditText) content.findViewById(R.id.newsfeed_paste_link_edit_text);
                ImageButton pasteButton = (ImageButton) content.findViewById(R.id.newsfeed_paste_link_paste_button);
                pasteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setText(clipboard.getPrimaryClip().getItemAt(0).getText());
                    }
                });

                if(clipboard == null || !clipboard.hasPrimaryClip() || !clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    pasteButton.setEnabled(false);
                }

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == DialogInterface.BUTTON_POSITIVE) {
                            String link = editText.getText().toString();
                            DialogFragment addPostFragment = AddPostFragment.getLinkInstance(link);
                            addPostFragment.setTargetFragment(NewsfeedFragment.this, 0);
                            addPostFragment.show(getActivity().getSupportFragmentManager(), "AddPostDialogFragment");
                        }
                        dialog.dismiss();
                    }
                };

                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle(R.string.newsfeed_insert_link)
                        .setView(content)
                        .setPositiveButton(R.string.ok, listener)
                        .setNegativeButton(R.string.cancel, listener)
                        .show();
            }
        });

        mAddButton.findViewById(R.id.newsfeed_add_text_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddButton.close(true);
                DialogFragment addPostFragment = AddPostFragment.getTextInstance();
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

        mSwipeToRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mPostInteractionHelper = new NewsfeedPostInteractionHelper(this);
        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.newsfeed_title));
        String feedType = NewsfeedUtils.FEED_TYPE_SITE;
        if(mSharedPref.contains(NEWSFEED_SHARED_PREF_DEFAULT_FEED)) {
            feedType = mSharedPref.getString(NEWSFEED_SHARED_PREF_DEFAULT_FEED, NewsfeedUtils.FEED_TYPE_SITE);
        }

        if(feedType.equals(NewsfeedUtils.FEED_TYPE_MY)) {
            mFriendsActivitiesSwitch.setChecked(true);
        }

        changePostAdapter(feedType);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: ");
        mNetworkInterface.nStopPendingRequest();
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == AddPostFragment.ADD_POST_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
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
        } else if(requestCode == AddPostFragment.ADD_POST_LINK_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            @SuppressWarnings("unchecked")
            HashMap<String, String> attachment = (HashMap<String, String>) data.getSerializableExtra(AddPostFragment.EXTRA_DATA_POST_LINK_CONTENT);
            final String message = data.getStringExtra(AddPostFragment.EXTRA_DATA_POST_MESSAGE);
            attachment.remove(NewsfeedJSONHelper.ALL_IMAGES);
            JSONObject obj = new JSONObject(attachment);
            mNetworkInterface.nSendPost(message, obj.toString());
        }
    }

    private void changePostAdapter(String feedType) {
        if(!feedType.equals(NewsfeedUtils.FEED_TYPE_MY) && !feedType.equals(NewsfeedUtils.FEED_TYPE_SITE)) {
            return;
        }

        mSharedPref.edit().putString(NEWSFEED_SHARED_PREF_DEFAULT_FEED, feedType).apply();
        mPostsAdapter = new NewsfeedPostsAdapter(getContext(), this, feedType, UserManager.getInstance().getId());
        mNetworkInterface = mPostsAdapter;
        mPostsList.setAdapter(mPostsAdapter);
        mPostsAdapter.setModelListener(this);
        mNetworkInterface.nRequestAuthorization();
    }


    private void loadNextPage(boolean reset) {
        mPostsAdapter.nLoadFeedPage(reset);
    }

    private void showLikeButtonTip() {
        mPostsList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View firstView = mPostsList.getLayoutManager().findViewByPosition(0);
                if(firstView != null) {
                    View likeButton = firstView.findViewById(R.id.newsfeed_post_base_like_button);
                    Tooltip.create(getContext())
                            .rootView(getView())
                            .tip(R.string.newsfeed_like_button_tip)
                            .on(likeButton)
                            .onDismiss(new PopupWindow.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    mSharedPref.edit().putBoolean(NEWSFEED_SHARED_PREF_FIRST_RUN, false).apply();
                                }
                            })
                            .show();
                }
                mPostsList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }


    @Override
    public void refreshPost(String entityType, int entityId, NewsfeedPostsAdapter.AdapterUpdateType updateType) {
        mNetworkInterface.nRefreshPost(entityType, entityId, updateType);
    }

    //LISTENERS FOR RECYCLERVIEW INTERACTION
    @Override
    public void onCommentsButtonClicked(Post post, int position) {
        if(mIsLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        mPostInteractionHelper.showCommentsFragment(post);
    }

    @Override
    public void onLikeButtonLongClicked(Post p) {
        if(mIsLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        mPostInteractionHelper.showLikesWindow(p.getEntityType(), "" + p.getEntityId(), mSwipeToRefresh, null, new LikesPopupWindow.LikesWindowInteractionListener() {
            @Override
            public void onUserClicked(int id, String name, String avatarUrl) {
                ProfileViewFragment fragment = ProfileViewFragment.newInstance(id, name, avatarUrl);

                getFragmentManager()
                        .beginTransaction()
                        .hide(NewsfeedFragment.this)
                        .add(R.id.container, fragment)
                        .addToBackStack(ProfileViewFragment.FRAGMENT_NAME)
                        .commit();
            }
        });
    }

    @Override
    public void onPostImageClicked(ImagePost post, ImageView postImageView) {
        if(mIsLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        JsonImage image = post.getImage();
        mPostInteractionHelper.openImageVisualizator(post, image, new String[]{image.getId()}, postImageView);
    }

    @Override
    public void onPostImageClicked(ImageListPost post, String selectedImageId, ImageView postImageView) {
        if(mIsLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        JsonImage image = post.getImageById(selectedImageId);
        String[] imageIds = new String[post.getImagesCount()];
        List<JsonImage> images = post.getImages();

        for (int i = 0; i < images.size(); i++) {
            imageIds[i] = String.valueOf(images.get(i));
        }

        mPostInteractionHelper.openImageVisualizator(post, image, imageIds, postImageView);
    }

    @Override
    public void onDataletImageClicked(DataletPost p) {
        if(mIsLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        mPostInteractionHelper.openDataletVisualizator(p);
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
       mPostInteractionHelper.openContentLink(post);
    }

    @Override
    public void onOpenUserProfile(Post.User user) {
        if(mIsLoadingPosts) {
            mNetworkInterface.nStopPendingRequest();
        }

        mPostInteractionHelper.openProfileFragment(user);
    }

    //MESSAGES FROM MODEL
    @Override
    public void onContentLoadingStarted(boolean resetList) {
        if (resetList && !mSwipeToRefresh.isRefreshing()) {
            mSwipeToRefresh.setRefreshing(true);
            mAddButton.close(true);
            mAddButton.hideMenuButton(true);
        }

        mIsLoadingPosts = true;
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
        mIsLoadingPosts = false;

        if (mSwipeToRefresh.isRefreshing()) {
            mSwipeToRefresh.setRefreshing(false);
            mAddButton.showMenuButton(true);
        }

        if(scrollOnTop) {
            mPostsList.scrollToPosition(0);
        }

        if(mSharedPref.getBoolean(NEWSFEED_SHARED_PREF_FIRST_RUN, true)) {
            showLikeButtonTip();
        }

    }

    @Override
    public void onContentLoadStopped() {
        if(mIsLoadingPosts) {
            LinearLayoutManager layout = (LinearLayoutManager) mPostsList.getLayoutManager();
            int lastVisibleItemPosition = layout.findLastCompletelyVisibleItemPosition();
            int lastItemPosition = mPostsList.getAdapter().getItemCount() - 2;

            if(lastVisibleItemPosition >= lastItemPosition && lastItemPosition > 0) {
                mPostsAdapter.setFooterType(NewsfeedPostsAdapter.FooterType.LOAD_MORE);
            }

        }
        mIsLoadingPosts = false;
    }

    @Override
    public void onError(String tag, String message) {
        Log.e(TAG, tag + ": " + message);
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
