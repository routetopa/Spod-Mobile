package eu.spod.isislab.spodapp.fragments.newsfeed;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.NewsfeedPostsAdapter;
import eu.spod.isislab.spodapp.adapters.NewsfeedUserProfilePagerAdapter;
import eu.spod.isislab.spodapp.entities.ContentPost;
import eu.spod.isislab.spodapp.entities.ContextActionMenuItem;
import eu.spod.isislab.spodapp.entities.DataletPost;
import eu.spod.isislab.spodapp.entities.ImageListPost;
import eu.spod.isislab.spodapp.entities.ImagePost;
import eu.spod.isislab.spodapp.entities.JsonImage;
import eu.spod.isislab.spodapp.entities.Post;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.GlideBlurTransformation;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedPostInteractionHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedPostModel;
import eu.spod.isislab.spodapp.utils.NewsfeedPostNetworkInterface;
import eu.spod.isislab.spodapp.utils.NewsfeedPostRefreshable;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;

public class ProfileViewFragment extends Fragment implements NewsfeedPostsAdapter.PostsAdapterInteractionListener, NewsfeedPostRefreshable, NewsfeedPostModel.NewsfeedPostModelListener, Observer {
    public static final String FRAGMENT_NAME = "ProfileViewFragment";
    public static final String TAG = "ProfileViewFragment";

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USER_NAME = "user_name";
    private static final String ARG_USER_AVATAR = "user_avatar";


    private int mTopMargin;
    private int mDefaultStatusBarColor;

    private int mUserId;
    private String mUserName;
    private String mUserAvatar;

    private int mUserColor;
    private int mUserColorDark;

    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private TabLayout mTabs;
    private ImageView mProfileImage;
    private Toolbar mToobar;

    private CoordinatorLayout mRootLayout;
    private ViewPager mPager;

    //Feed page elements
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mPostList;
    private NewsfeedPostsAdapter mPostsAdapter;

    //Info page elements
    private TextView mUserNameText;
    private TextView mGenderText;
    private TextView mJoinText;

    private NewsfeedPostNetworkInterface mNetworkInterface;
    private NewsfeedPostInteractionHelper mPostInteractionHelper;

    private boolean mIsLoadingPosts;
    private boolean mIsDefaultAvatar;
    private boolean isPreviousNewsfeed = true;

    public ProfileViewFragment() {}

    public static ProfileViewFragment newInstance(int userId, String userName, String userAvatar) {
        ProfileViewFragment fragment = new ProfileViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putString(ARG_USER_NAME, userName);
        args.putString(ARG_USER_AVATAR, userAvatar);
        fragment.setArguments(args);
        return fragment;
    }

    private void setIsPreviousNewsfeed(boolean flag) {
        isPreviousNewsfeed = flag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getInt(ARG_USER_ID);
            mUserName = getArguments().getString(ARG_USER_NAME);
            mUserAvatar = getArguments().getString(ARG_USER_AVATAR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_view, container, false);

        mIsDefaultAvatar = NewsfeedUtils.isDefaultAvatar(mUserAvatar);
        mUserColor = NewsfeedUtils.getUserColor(getContext(), mUserId);
        mUserColorDark = NewsfeedUtils.makeColorDarker(mUserColor, 0.7f);

        int mColorDarkText = NewsfeedUtils.getColorResource(getContext(), android.R.color.secondary_text_light);
        int colorWhite = NewsfeedUtils.getColorResource(getContext(), android.R.color.white);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDefaultStatusBarColor = getActivity().getWindow().getStatusBarColor();
        }

        mRootLayout = (CoordinatorLayout) v.findViewById(R.id.profile_view_root_layout);
        mAppBarLayout = (AppBarLayout) v.findViewById(R.id.profile_view_appbar_layout);
        mCollapsingToolbar = (CollapsingToolbarLayout) v.findViewById(R.id.profile_view_collapsing_toolbar);
        mTabs = (TabLayout) v.findViewById(R.id.profile_view_tabs);

        mProfileImage = (ImageView) v.findViewById(R.id.profile_view_profile_image);
        mToobar = (Toolbar) v.findViewById(R.id.profile_view_toolbar);

        mPager = (ViewPager) v.findViewById(R.id.profile_view_pager);

        mToobar.setTitle(mUserName);
        mToobar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToobar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                if(fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStackImmediate();
                } else {
                    getActivity().onBackPressed();
                }
            }
        });
        mToobar.setContentInsetStartWithNavigation(0);

        mCollapsingToolbar.setContentScrimColor(mUserColor);
        mTabs.setSelectedTabIndicatorColor(mUserColorDark);

        if(mIsDefaultAvatar){
            mProfileImage.setImageDrawable(new ColorDrawable(mUserColor));
            mAppBarLayout.setExpanded(false);
            mTabs.setTabTextColors(mColorDarkText, mUserColorDark);
        } else {
            mTabs.setTabTextColors(colorWhite, mUserColorDark);
            Glide.with(this)
                    .load(mUserAvatar)
                    .apply(new RequestOptions()
                            .transform(new GlideBlurTransformation(getContext(), 20))
                    )
                    .into(mProfileImage);

        }


        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
                if(behavior!=null && positionOffset > 0) {
                    if(behavior.getTopAndBottomOffset() == 0) {
                        return; //if already expanded we do nothing
                    }
                    float positionOffsetInverse = 1 - positionOffset;
                    int offset = Math.round(positionOffsetInverse * mAppBarLayout.getTotalScrollRange()); //offset goes from mAppBarLayout.getTotalScrollRange() to 0
                    behavior.setTopAndBottomOffset(-offset);
                    behavior.onNestedPreScroll(mRootLayout, mAppBarLayout, null, 0, offset, new int[2]);
                    if(offset == 0){
                        mAppBarLayout.setExpanded(true, false);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        NewsfeedUserProfilePagerAdapter pagerAdapter = new NewsfeedUserProfilePagerAdapter(getContext());
        mPager.setAdapter(pagerAdapter);

        mTabs.setupWithViewPager(mPager);

        mPager.setCurrentItem(0);

        mPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initPagesElements();
                initList();
                initInfo();
                mPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(mUserColorDark);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        if(getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        super.onAttach(context);
        RelativeLayout.LayoutParams parameter = (RelativeLayout.LayoutParams) (getActivity()).findViewById(R.id.container).getLayoutParams();
        mTopMargin = parameter.topMargin;
        parameter.setMargins(parameter.leftMargin, 0, parameter.rightMargin, parameter.bottomMargin);
        (getActivity()).findViewById(R.id.container).setLayoutParams(parameter);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        if(getActivity() instanceof MainActivity) {
            int index = getFragmentManager().getBackStackEntryCount() - 2;
            int index2 = getFragmentManager().getBackStackEntryCount() - 3;

            String previousFragment = getFragmentManager().getBackStackEntryAt(index).getName();
            String previousFragment2 = null;
            if(getFragmentManager().getBackStackEntryCount() > 2) {
                previousFragment2 = getFragmentManager().getBackStackEntryAt(index2).getName(); //in user profile is opened from comments
            }

            boolean isNewsfeed = NewsfeedFragment.FRAGMENT_NAME.equals(previousFragment) ||
                    (PostCommentsFragment.FRAGMAENT_NAME.equals(previousFragment) && NewsfeedFragment.FRAGMENT_NAME.equals(previousFragment2));

            setIsPreviousNewsfeed(isNewsfeed);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(getActivity() instanceof MainActivity) {
            if(isPreviousNewsfeed) {
                ((MainActivity) getActivity()).drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                RelativeLayout.LayoutParams parameter = (RelativeLayout.LayoutParams) (getActivity()).findViewById(R.id.container).getLayoutParams();
                parameter.setMargins(parameter.leftMargin, mTopMargin, parameter.rightMargin, parameter.bottomMargin); // left, top, right, bottom
                (getActivity()).findViewById(R.id.container).setLayoutParams(parameter);
                ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                Log.d(TAG, "onHiddenChanged: action bar showed!");
            }
        }

        mNetworkInterface.nStopPendingRequest();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(mDefaultStatusBarColor);
        }

        NetworkChannel.getInstance().deleteObserver(this);
    }

    private void initPagesElements(){
        //FEED PAGE
        View feedPage = mPager.findViewWithTag("FeedPage");
        mRefreshLayout = (SwipeRefreshLayout) feedPage.findViewById(R.id.profile_view_swipe_refresh);
        mPostList = (RecyclerView) mRefreshLayout.findViewById(R.id.profile_view_post_container);

        int color4 = NewsfeedUtils.makeColorDarker(mUserColor, 0.7f);
        int color3 = NewsfeedUtils.makeColorDarker(mUserColor, 0.8f);
        int color2 = NewsfeedUtils.makeColorDarker(mUserColor, 0.9f);

        mRefreshLayout.setColorSchemeColors(mUserColor, color2, color3, color4, color3, color2);

        //INFO PAGE
        View infoPage = mPager.findViewWithTag("InfoPage");
        ((TextView) infoPage.findViewById(R.id.profile_view_info_base_text)).setTextColor(mUserColor);
        ((TextView) infoPage.findViewById(R.id.profile_view_info_username_label)).setTextColor(mUserColor);
        ((TextView) infoPage.findViewById(R.id.profile_view_info_join_label)).setTextColor(mUserColor);
        ((TextView) infoPage.findViewById(R.id.profile_view_info_gender_label)).setTextColor(mUserColor);

        mUserNameText = (TextView) infoPage.findViewById(R.id.profile_view_info_username_value);
        mJoinText = (TextView) infoPage.findViewById(R.id.profile_view_info_join_value);
        mGenderText = (TextView) infoPage.findViewById(R.id.profile_view_info_gender_value);
    }

    private void initList(){
        mPostList.setLayoutManager(new LinearLayoutManager(getContext()));
        mPostList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layout = (LinearLayoutManager) recyclerView.getLayoutManager();

                int lastVisibleItemPosition = layout.findLastCompletelyVisibleItemPosition();
                int lastItemPosition = recyclerView.getAdapter().getItemCount() - 3;

                if(lastVisibleItemPosition >= lastItemPosition && lastItemPosition > 0) {
                    if (!mIsLoadingPosts) {
                        mNetworkInterface.nLoadFeedPage(false);
                    }
                }
            }
        });

        mPostInteractionHelper = new NewsfeedPostInteractionHelper(this);

        mPostsAdapter = new NewsfeedPostsAdapter(getContext(), this, "user", ""+mUserId);
        mPostsAdapter.setModelListener(this);
        mPostsAdapter.setPrimaryColor(mUserColor);

        mNetworkInterface = mPostsAdapter;
        mPostList.setAdapter(mPostsAdapter);

        mNetworkInterface.nRequestAuthorization();
    }

    private void initInfo() {
        NetworkChannel.getInstance().getUserInfo(mUserId);
        NetworkChannel.getInstance().addObserver(this);
    }
    @Override
    public void refreshPost(String entityType, int entityId, NewsfeedPostsAdapter.AdapterUpdateType updateType) {
        mNetworkInterface.nRefreshPost(entityType, entityId, updateType);
    }

    //LISTENERS FOR RECYCLERVIEW INTERACTION
    @Override
    public void onCommentsButtonClicked(Post post, int position) {
        mNetworkInterface.nStopPendingRequest();

        mPostInteractionHelper.showCommentsFragment(post);
    }


    @Override
    public void onLikeButtonLongClicked(Post p) {
        mNetworkInterface.nStopPendingRequest();

        mPostInteractionHelper.showLikesWindow(p.getEntityType(), ""+p.getEntityId(), mRootLayout, mUserColorDark, new LikesPopupWindow.LikesWindowInteractionListener() {
            @Override
            public void onUserClicked(int id, String name, String avatarUrl) {
                if(id == mUserId) {
                    return;
                }

                ProfileViewFragment fragment = ProfileViewFragment.newInstance(id, name, avatarUrl);

                getFragmentManager()
                        .beginTransaction()
                        .hide(ProfileViewFragment.this)
                        .add(R.id.container, fragment)
                        .addToBackStack(ProfileViewFragment.FRAGMENT_NAME)
                        .commit();
            }
        });
    }

    @Override
    public void onPostImageClicked(ImagePost post, ImageView postImageView) {
        mNetworkInterface.nStopPendingRequest();


        JsonImage image = post.getImage();
        mPostInteractionHelper.openImageVisualizator(post, image, new String[]{image.getId()}, postImageView);
    }

    @Override
    public void onPostImageClicked(ImageListPost post, String selectedImageId, ImageView postImageView) {
        mNetworkInterface.nStopPendingRequest();

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
        mNetworkInterface.nStopPendingRequest();

        mPostInteractionHelper.openDataletVisualizator(p);
    }

    @Override
    public void onContextActionMenuItemClicked(final int position, ContextActionMenuItem.ContextActionType actionType) {
        switch (actionType) {
            case DELETE_POST:
                new AlertDialog.Builder(getContext())
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
        if(user.getUserId() != mUserId) {
            ProfileViewFragment fragment = ProfileViewFragment.newInstance(user.getUserId(), user.getName(), user.getAvatarUrl());
            fragment.setIsPreviousNewsfeed(false);

            getFragmentManager()
                    .beginTransaction()
                    .hide(this)
                    .add(R.id.container, fragment)
                    .addToBackStack(ProfileViewFragment.FRAGMENT_NAME)
                    .commit();
        }
    }

    //NEWSFEED MODEL LISTENER
    @Override
    public void onAuthorizationResult(boolean canView, boolean canWrite) {
        if(canView) {
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNetworkInterface.nLoadFeedPage(true);
                }
            }, 500);
        }
    }

    @Override
    public void onContentLoadingStarted(boolean resetList) {
        mIsLoadingPosts = true;

        if(resetList) {
            mRefreshLayout.setEnabled(true);
            mRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onContentLoaded(boolean scrollOnTop) {
        mIsLoadingPosts = false;

        if(mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setEnabled(false);
            mRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onContentLoadStopped() {
        mIsLoadingPosts = false;
    }

    @Override
    public void onError(String tag, String message) {
        mIsLoadingPosts = false;
    }

    @Override
    public void onError(int resource) {
        mIsLoadingPosts = false;
    }

    @Override
    public void onFeedLoadingTimeout() {
        mIsLoadingPosts = false;

        if(mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setEnabled(false);
            mRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        String currentService = NetworkChannel.getInstance().getCurrentService();

        if(currentService.equals(Consts.NEWSFEED_SERVICE_GET_USER_INFO)) {
            String res = (String) arg;

            try {
                JSONObject json = new JSONObject(res);
                JSONObject data = json.getJSONObject("data");
                Map<String, String> dataMap = NewsfeedJSONHelper.convertToMap(data);

                mUserNameText.setText(dataMap.get(NewsfeedJSONHelper.USERNAME));
                mJoinText.setText(dataMap.get(NewsfeedJSONHelper.JOIN_STAMP));
                mGenderText.setText(dataMap.get(NewsfeedJSONHelper.SEX));
                NetworkChannel.getInstance().deleteObserver(this);
            } catch (JSONException e) {
                e.printStackTrace();
                NetworkChannel.getInstance().deleteObserver(this);
            }
        }

    }
}
