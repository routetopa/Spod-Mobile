package eu.spod.isislab.spodapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.List;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.activities.FullscreenActivity;
import eu.spod.isislab.spodapp.entities.ContentPost;
import eu.spod.isislab.spodapp.entities.DataletPost;
import eu.spod.isislab.spodapp.entities.JsonImage;
import eu.spod.isislab.spodapp.entities.NewsfeedImageInfo;
import eu.spod.isislab.spodapp.entities.Post;
import eu.spod.isislab.spodapp.fragments.newsfeed.LikesPopupWindow;
import eu.spod.isislab.spodapp.fragments.newsfeed.PostCommentsFragment;
import eu.spod.isislab.spodapp.fragments.newsfeed.ProfileViewFragment;

public class NewsfeedPostInteractionHelper {

    private Fragment mTargetFragment;
    private Context mContext;
    private Activity mRootActivity;

    public NewsfeedPostInteractionHelper(Fragment targetFragment) {
        this.mTargetFragment = targetFragment;
        this.mContext = targetFragment.getContext();
        this.mRootActivity = targetFragment.getActivity();
    }


    public void showCommentsFragment(Post post) {
        PostCommentsFragment commentsSheet = PostCommentsFragment.newInstance(post.getEntityType(), post.getEntityId(), post.getPluginKey(), post.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTargetFragment.setExitTransition(TransitionInflater.from(mRootActivity).inflateTransition(android.R.transition.explode));
            commentsSheet.setEnterTransition(TransitionInflater.from(mRootActivity).inflateTransition(android.R.transition.explode));
        }

        mTargetFragment.getFragmentManager()
                .beginTransaction()
                .hide(mTargetFragment)
                .add(R.id.container, commentsSheet)
                .addToBackStack(PostCommentsFragment.FRAGMAENT_NAME)
                .commit();
    }

    public void showLikesWindow(String entityType, String entityId, View rootLayout, Integer primaryColor, LikesPopupWindow.LikesWindowInteractionListener listener) {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();

        float widthOffset = NewsfeedUtils.pxToDp(mContext, 50); //px to dp conversion
        float heightOffset = NewsfeedUtils.pxToDp(mContext, 150);

        LikesPopupWindow likesPopupWindow = new LikesPopupWindow(mContext, rootLayout, dm.widthPixels - (int) widthOffset, dm.heightPixels - (int) heightOffset);
        if(primaryColor != null) {
            likesPopupWindow.setPrimaryColor(primaryColor);
        }

        likesPopupWindow.show(entityType, entityId);

        if(listener != null) {
            likesPopupWindow.setLikesWindowInteractionListener(listener);
        }

    }

    public void openImageVisualizator(Post post, JsonImage selectedImage, String[] imagesList, View postImageView) {
        Post.User user = post.getUserInfo(post.getUserId());
        NewsfeedImageInfo imageInfo = new NewsfeedImageInfo(selectedImage.getId(), selectedImage.getDescription(), post.getTimestamp(), user.getName(), user.getUserId(), null, -1, null);
        Intent intent = new Intent(mContext, FullscreenActivity.class);
        intent.putExtra(FullscreenActivity.URI_ARGUMENT, Uri.parse(selectedImage.getPreviewUrl()));
        intent.putExtra(FullscreenActivity.CURRENT_IMAGE_INFO_ARGUMENT, imageInfo);
        intent.putExtra(FullscreenActivity.IMAGES_ARGUMENT, imagesList); //TODO: implement visualization of multiple images
        intent.putExtra(FullscreenActivity.FRAGMENT_TYPE_ARGUMENT, FullscreenActivity.FRAGMENT_TYPE_IMAGE);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mRootActivity, postImageView, NewsfeedUtils.getStringResource(mRootActivity, R.string.newsfeed_image_transition_name));

        mTargetFragment.startActivity(intent, options.toBundle());
        mRootActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void openDataletVisualizator(DataletPost p) {
        Intent intent = new Intent(mContext, FullscreenActivity.class);
        intent.putExtra(FullscreenActivity.FRAGMENT_TYPE_ARGUMENT, FullscreenActivity.FRAGMENT_TYPE_DATALET);
        intent.putExtra(FullscreenActivity.URI_ARGUMENT, p.getUrl());

        mTargetFragment.startActivity(intent);
        mRootActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void openContentLink(ContentPost post) {
        /*if(post.hasRouting()) {
          //open right fragment/activity according to post.getRouteName()
        } else { */
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getUrl()));
        mTargetFragment.startActivity(browserIntent);
    }

    public void openProfileFragment(Post.User user) {
        ProfileViewFragment fragment = ProfileViewFragment.newInstance(user.getUserId(), user.getName(), user.getAvatarUrl());

        mTargetFragment.getFragmentManager()
                .beginTransaction()
                .hide(mTargetFragment)
                .add(R.id.container, fragment)
                .addToBackStack(ProfileViewFragment.FRAGMENT_NAME)
                .commit();
    }
}
