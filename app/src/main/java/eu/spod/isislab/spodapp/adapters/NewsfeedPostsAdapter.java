package eu.spod.isislab.spodapp.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import eu.spod.isislab.spodapp.entities.ContentPost;
import eu.spod.isislab.spodapp.entities.ContextActionMenuItem;
import eu.spod.isislab.spodapp.entities.DataletPost;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.GlidePalette;
import eu.spod.isislab.spodapp.entities.ImageListPost;
import eu.spod.isislab.spodapp.entities.ImagePost;
import eu.spod.isislab.spodapp.entities.JsonImage;
import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedPostModel;
import eu.spod.isislab.spodapp.utils.NewsfeedPostNetworkInterface;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;
import eu.spod.isislab.spodapp.entities.Post;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.Tooltip;
import eu.spod.isislab.spodapp.fragments.newsfeed.NewsfeedFragment;


import static eu.spod.isislab.spodapp.utils.NewsfeedUtils.htmlToSpannedText;


public class NewsfeedPostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements NewsfeedPostNetworkInterface {
    private boolean mFirstRun = false;

    private NewsfeedPostModel mPosts;
    private NewsfeedPostNetworkInterface mNetworkCommunication;

    private Context mContext;
    private PostsAdapterInteractionListener mListener;

    private boolean mHasFooter;
    private FooterType mFooterType = FooterType.LOADING;

    private static final String TAG = "NewsfeedPostsAdapter";

    private static final int LAYOUT_LOADING = 0;
    private static final int LAYOUT_EMPTY_POST = 1;
    private static final int LAYOUT_TEXT_POST = 2;
    private static final int LAYOUT_IMAGE_POST = 3;
    private static final int LAYOUT_IMAGE_LIST_POST = 4;
    private static final int LAYOUT_CONTENT_POST = 5;
    private static final int LAYOUT_CONTENT_IMAGE_POST = 6;
    private static final int LAYOUT_DATALET_POST = 7;


    public enum FooterType {
        LOADING,
        LOAD_MORE
    }
    public enum AdapterUpdateType {
        LIKES,
        ALL,
        COMMENTS
    }

    private class BasePostViewHolder extends RecyclerView.ViewHolder{
        LinearLayout upperActivityContainer;

        TextView activityString;
        LinearLayout userInfoContainer;
        ImageView userImageView;
        TextView userNameActionTextView;
        TextView timeTextView;
        Button likeButton;
        Button commentsButton;
        ImageButton contextActionMenuButton;

        BasePostViewHolder(View v){
            super(v);

            View headerView = v.findViewById(R.id.newsfeed_post_base_header);
            View footerView = v.findViewById(R.id.newsfeed_post_base_footer);

            upperActivityContainer = (LinearLayout) headerView.findViewById(R.id.newsfeed_post_base_activity_container);
            activityString = (TextView) headerView.findViewById(R.id.newsfeed_post_base_activity_string);
            userInfoContainer = (LinearLayout) headerView.findViewById(R.id.newsfeed_post_base_user_info_container);
            userImageView = (ImageView) headerView.findViewById(R.id.newsfeed_post_base_user_image);
            userNameActionTextView = (TextView) headerView.findViewById(R.id.newsfeed_post_base_user_name_action_text);
            timeTextView = (TextView) headerView.findViewById(R.id.newsfeed_post_base_time_text);
            likeButton = (Button) footerView.findViewById(R.id.newsfeed_post_base_like_button);
            commentsButton = (Button) footerView.findViewById(R.id.newsfeed_post_base_comments_button);
            contextActionMenuButton = null;
        }
    }

    private class TextPostViewHolder extends BasePostViewHolder {
        TextView statusTextView;

        TextPostViewHolder(View v) {
            super(v);

            statusTextView = (TextView) v.findViewById(R.id.newsfeed_text_content_status);
            statusTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private class ImagePostViewHolder extends BasePostViewHolder {
        TextView statusTextView;
        ImageView contentImageView;
        ImagePostViewHolder(View v) {
            super(v);
            statusTextView = (TextView) v.findViewById(R.id.newsfeed_image_content_status);
            contentImageView = (ImageView) v.findViewById(R.id.newsfeed_image_content_imageview);

            statusTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private class ImageListPostViewHolder extends BasePostViewHolder {
        LinearLayout imageListContainer;

        HorizontalScrollView imagesScrollView;
        ImageListPostViewHolder(View v) {
            super(v);
            imageListContainer = (LinearLayout) v.findViewById(R.id.newsfeed_image_list_container);
            imagesScrollView = (HorizontalScrollView) v.findViewById(R.id.newsfeed_image_list_scroll_view);
        }
    }

    private class ContentImageViewHolder extends BasePostViewHolder {
        TextView statusTextView;

        ImageView linkPreviewImageView;
        TextView linkTitleTextView;
        TextView linkDescriptionTextView;
        LinearLayout linkInfoContainer;
        ContentImageViewHolder(View v) {
            super(v);

            statusTextView = (TextView) v.findViewById(R.id.newsfeed_imagecontent_content_status);
            linkPreviewImageView = (ImageView) v.findViewById(R.id.newsfeed_imagecontent_content_link_preview);
            linkTitleTextView = (TextView) v.findViewById(R.id.newsfeed_imagecontent_content_link_title);
            linkDescriptionTextView = (TextView) v.findViewById(R.id.newsfeed_imagecontent_content_link_description);
            linkInfoContainer = (LinearLayout) v.findViewById(R.id.newsfeed_imagecontent_content_link_info_container);

            statusTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private class DataletPostViewHolder extends BasePostViewHolder{
        TextView statusTextView;
        ImageView dataletPreviewImageView;


        DataletPostViewHolder(View v) {
            super(v);

            statusTextView = (TextView) v.findViewById(R.id.newsfeed_datalet_content_status);
            dataletPreviewImageView = (ImageView) v.findViewById(R.id.newsfeed_datalet_content_imageview);

            statusTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder{
        ProgressBar loadingBar;
        Button loadMoreButton;

        LoadingViewHolder(View itemView) {
            super(itemView);
            loadingBar = (ProgressBar) itemView.findViewById(R.id.newsfeed_post_list_footer_progress);
            loadMoreButton = (Button) itemView.findViewById(R.id.newsfeed_post_list_footer_button);
        }
    }

    public NewsfeedPostsAdapter(Context context, PostsAdapterInteractionListener listener, String feedType, String feedId) {
        mContext = context;
        mListener = listener;
        mPosts = new NewsfeedPostModel(this, feedType, feedId);
        mNetworkCommunication = mPosts;
        mHasFooter = false;

        mFirstRun = mContext.getSharedPreferences(Consts.SPOD_MOBILE_PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(NewsfeedFragment.NEWSFEED_SHARED_PREF_FIRST_RUN, false);

        Log.d(TAG, "NewsfeedPostsAdapter: firstRun="+mFirstRun);
    }

    public void isFirstRun(boolean b) {
        mFirstRun = b;
    }

    public void setData(ArrayList<Post> posts){
        mPosts.setData(posts);
    }

    public Post getItemAtPosition(int position){
        return mPosts.getItemAtPosition(position);
    }


    public int getFooterPosition() {
        if(mHasFooter) {
            return getItemCount() - 1;
        }

        return -1;
    }

    public void setHasFooter(boolean b){
        this.mHasFooter = b;
        //notifyDataSetChanged();
    }

    public void setFooterType(FooterType type) {
        mFooterType = type;

        if(mHasFooter) {
            Handler h = new Handler();
            h.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(getFooterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(mPosts == null)
            return 0;

        return mHasFooter ? mPosts.size() + 1 : mPosts.size();
    }

    private boolean isLastPosition(int position){
        /*int lastPostPosition = mPosts.size() - 1;
        if(mHasFooter) {
            lastPostPosition = lastPostPosition + 1; //if list has footer then it have another position after last post's position
        }*/
        int lastPostPosition = getItemCount() - 1;
        return position == lastPostPosition;
    }

    @Override
    public int getItemViewType(int position) {
        if(mHasFooter && isLastPosition(position)) {
            return LAYOUT_LOADING;
        }

        Post p = mPosts.get(position);
        switch (p.getFormat()) {
            case NewsfeedJSONHelper.Formats.FORMAT_TEXT:
                return LAYOUT_TEXT_POST;
            case NewsfeedJSONHelper.Formats.FORMAT_CONTENT:
                return LAYOUT_CONTENT_POST;
            case NewsfeedJSONHelper.Formats.FORMAT_CONTENT_IMAGE:
                return LAYOUT_CONTENT_IMAGE_POST;
            case NewsfeedJSONHelper.Formats.FORMAT_IMAGE:
                return LAYOUT_IMAGE_POST;
            case NewsfeedJSONHelper.Formats.FORMAT_IMAGE_LIST:
                return LAYOUT_IMAGE_LIST_POST;
            case NewsfeedJSONHelper.Formats.FORMAT_DATALET:
                return LAYOUT_DATALET_POST;
            default:
                return LAYOUT_EMPTY_POST;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case LAYOUT_LOADING:
                v = inflater.inflate(R.layout.newsfeed_post_list_footer, parent, false);
                return new LoadingViewHolder(v);
            case LAYOUT_TEXT_POST:
                v = inflater.inflate(R.layout.newsfeed_post_item_text, parent, false);
                return new TextPostViewHolder(v);
            case LAYOUT_IMAGE_POST:
                v = inflater.inflate(R.layout.newsfeed_post_item_image, parent, false);
                return new ImagePostViewHolder(v);
            case LAYOUT_IMAGE_LIST_POST:
                v = inflater.inflate(R.layout.newsfeed_post_item_image_list, parent, false);
                return new ImageListPostViewHolder(v);
            case LAYOUT_CONTENT_POST:
            case LAYOUT_CONTENT_IMAGE_POST:
                v = inflater.inflate(R.layout.newsfeed_post_item_contentimage, parent, false);
                return new ContentImageViewHolder(v);
            case LAYOUT_DATALET_POST:
                v = inflater.inflate(R.layout.newsfeed_post_item_datalet, parent, false);
                return new DataletPostViewHolder(v);
            default:
                v = inflater.inflate(R.layout.newsfeed_post_item_empty, parent, false);
                return new BasePostViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            updateViewHolder(h, position, payloads);
        } else {
            onBindViewHolder(h, position);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        int viewType = getItemViewType(position);
        if(viewType == LAYOUT_LOADING) {
            bindLoadingViewHolder((LoadingViewHolder) h);
            return;
        }

        bindBaseViewHolder(h, position);

        switch (viewType) {
            case LAYOUT_TEXT_POST:
                bindTextViewHolder((TextPostViewHolder) h, position);
                break;
            case LAYOUT_IMAGE_POST:
                bindImageViewHolder((ImagePostViewHolder) h, position);
                break;
            case LAYOUT_IMAGE_LIST_POST:
                bindImageListViewHolder((ImageListPostViewHolder) h, position);
                break;
            case LAYOUT_CONTENT_POST:
            case LAYOUT_CONTENT_IMAGE_POST:
                bindImageContentViewHolder((ContentImageViewHolder) h, position);
                break;
            case LAYOUT_DATALET_POST:
                bindDataletViewHolder((DataletPostViewHolder) h, position);
                break;
        }
    }

    private void updateViewHolder(RecyclerView.ViewHolder h, int position, List<Object> payloads) {
        Post p = mPosts.get(position);
        for (Object payload : payloads) {
            AdapterUpdateType type = (AdapterUpdateType) payload;
            switch (type) {
                case LIKES:
                    String likesButtonText = p.getLikesCount() + " " + mContext.getText(R.string.newsfeed_base_post_likes_string);
                    ((BasePostViewHolder) h).likeButton.setText(likesButtonText);
                    break;
                case COMMENTS:
                    String commentsString = p.getCommentCount() + " " + NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_base_post_comments_string);
                    ((BasePostViewHolder) h).commentsButton.setText(commentsString);
                    break;
                default:
                    onBindViewHolder(h, position);
                    break;
            }
        }
    }

    private void bindLoadingViewHolder(LoadingViewHolder h) {
        h.loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nLoadFeedPage(false);
            }
        });

        if(mFooterType == FooterType.LOADING) {
            h.loadMoreButton.setVisibility(View.GONE);
            h.loadingBar.setVisibility(View.VISIBLE);
        } else {
            h.loadMoreButton.setVisibility(View.VISIBLE);
            h.loadingBar.setVisibility(View.GONE);
        }
    }

    private void bindBaseViewHolder(final RecyclerView.ViewHolder h, final int position) {
        final BasePostViewHolder holder = (BasePostViewHolder) h;

        final Post p = mPosts.get(position);

        if(holder.contextActionMenuButton == null) {
            holder.contextActionMenuButton = new ImageButton(mContext, null, R.style.ButtonBorderless);
            holder.contextActionMenuButton.setImageResource(R.drawable.ic_keyboard_arrow_down_darker_gray_24dp);
            holder.contextActionMenuButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            holder.contextActionMenuButton.setMinimumHeight(0);
            holder.contextActionMenuButton.setMinimumWidth(0);
            holder.contextActionMenuButton.setPadding(5, 5, 5, 5);
        }

        final PopupMenu menu = inflateContextPopupMenu(holder.contextActionMenuButton, position);
        holder.contextActionMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.show();
            }
        });

        if(p.getContextActionMenu().size() > 0) {
            holder.contextActionMenuButton.setVisibility(View.VISIBLE);
        } else {
            holder.contextActionMenuButton.setVisibility(View.GONE);
        }

        ViewParent contextMenuButtonParent = holder.contextActionMenuButton.getParent(); //in case of reuse of an holder contextActionMenuButton has always a parent (in older holder) so we have to remove them
        if(contextMenuButtonParent != null) {
            ((ViewGroup) contextMenuButtonParent).removeView(holder.contextActionMenuButton);
        }

        if(p.hasActivityRespond()){
            holder.upperActivityContainer.setVisibility(View.VISIBLE);
            String activityRespondText = p.getUserInfo(p.getActivityUserId()).getName() + " " + htmlToSpannedText(p.getActivityString());
            holder.activityString.setText(activityRespondText);
            holder.upperActivityContainer.addView(holder.contextActionMenuButton);
        }else{
            holder.upperActivityContainer.setVisibility(View.GONE);
            holder.userInfoContainer.addView(holder.contextActionMenuButton);
        }

        Post.User owner = p.getUserInfo(p.getUserId());
        String ownerName = owner.getName();
        String actionString = p.getString() != null
                ? " "+ htmlToSpannedText(p.getString())
                : "";

        if(p.hasContext()) {
            actionString = " \u00BB " + p.getContextLabel();
        }

        String text = ownerName + actionString;
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new RelativeSizeSpan(1.1f), 0, ownerName.length(), 0);
        spannableString.setSpan(new ForegroundColorSpan(NewsfeedUtils.getColorResource(mContext, android.R.color.black)), 0, ownerName.length(), 0);
        holder.userNameActionTextView.setText(spannableString);

        long timestamp = p.getTimestampInMillis();
        String time = NewsfeedUtils.timeToString(mContext, timestamp);
        holder.timeTextView.setText(time);

        if(p.hasCommentsFeature()) {
            String commentsButtonText = p.getCommentCount() + " " + mContext.getText(R.string.newsfeed_base_post_comments_string);
            holder.commentsButton.setText(commentsButtonText);

            holder.commentsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onCommentsButtonClicked(p, position);
                    }
                }
            });
        } else {
            holder.commentsButton.setVisibility(View.GONE);
        }

        if(p.hasLikeFeature()) {
            String likesButtonText = p.getLikesCount() + " " + mContext.getText(R.string.newsfeed_base_post_likes_string);
            holder.likeButton.setText(likesButtonText);

            Drawable coloredLikeDrawable = NewsfeedUtils.getDrawableResource(mContext, R.drawable.ic_post_heart_colored_24dp);
            Drawable likeDrawable =  NewsfeedUtils.getDrawableResource(mContext, R.drawable.ic_post_heart_outline_24dp);
            final int likedColor = NewsfeedUtils.getColorResource(mContext, R.color.colorAccent);
            final int notLikedColor = NewsfeedUtils.getColorResource(mContext, android.R.color.darker_gray);

            holder.likeButton.setTextColor(notLikedColor);
            holder.likeButton.setCompoundDrawablesWithIntrinsicBounds(new TransitionDrawable(new Drawable[]{likeDrawable, coloredLikeDrawable}), null, null, null);

            if(p.isLiked()) {
                holder.likeButton.setTextColor(likedColor);
                ((TransitionDrawable) holder.likeButton.getCompoundDrawables()[0]).startTransition(300);
            }

            holder.likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TransitionDrawable transitionDrawable = (TransitionDrawable) holder.likeButton.getCompoundDrawables()[0];
                    transitionDrawable.setCrossFadeEnabled(true);
                    if (!p.isLiked()) {
                        holder.likeButton.setTextColor(likedColor);
                        transitionDrawable.startTransition(300);
                    } else {
                        holder.likeButton.setTextColor(notLikedColor);
                        transitionDrawable.reverseTransition(300);
                    }
                    //mListener.onLikeButtonClicked(p, position);
                    mPosts.nLikeUnlikePost(position);
                }
            });

            if(mFirstRun && position == 0) {
                //showTooltip(holder.likeButton);
            }

            holder.likeButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mListener.onLikeButtonLongClicked(p);
                    return true;
                }
            });
        } else {
            holder.likeButton.setVisibility(View.GONE);
        }

        if(NewsfeedUtils.isDefaultAvatar(owner.getAvatarUrl())) {
            holder.userImageView.setImageDrawable(NewsfeedUtils.getTextDrawableForUser(mContext, owner.getUserId(), owner.getName()));
        } else {
            Glide.with(mContext)
                    .load(owner.getAvatarUrl())
                    .apply(new RequestOptions()
                            .placeholder(NewsfeedUtils.getTextDrawableForUser(mContext, owner.getUserId(), owner.getName()))
                            .circleCrop())
                    .into(holder.userImageView);
        }
    }

    /*private void showTooltip(final View anchor) {

        Tooltip.create(mContext)
                .on(anchor)
                .tip(R.string.newsfeed_like_button_tip)
                .onDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mFirstRun = false;
                    }
                })
                .show();
    }*/

    /*private void truncateWithViewMore(String text, final TextView target) {
        if(text == null || text.length() <= 1000) {
            target.setText(text);
            return;
        }

        String truncatedStr = NewsfeedUtils.truncateString(text, 1000);
        String viewMore = NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_view_more);
        String viewLess = NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_view_less);

        final SpannableString spannableMore = new SpannableString(truncatedStr + " " + viewMore);
        final SpannableString spannableLess = new SpannableString(text + " " + viewLess);

        spannableLess.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                target.setText(spannableMore);
            }
        }, text.length() + 1, spannableLess.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableMore.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                target.setText(spannableLess);
            }
        }, truncatedStr.length() + 1, spannableMore.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        target.setText(spannableMore);
    }

    private void truncateWithViewMore(Spanned text, final TextView target) {
        if(text == null || text.length() <= 1000) {
            target.setText(text);
            return;
        }

        String truncatedStr = NewsfeedUtils.truncateString(text.toString(), 1000);
        String viewMore = NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_view_more);
        String viewLess = NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_view_less);

        final SpannableString spannableMore = new SpannableString(truncatedStr + " " + viewMore);
        final SpannableString spannableLess = new SpannableString(text + " " + viewLess);

        spannableLess.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                target.setText(spannableMore);
            }
        }, text.length() + 1, spannableLess.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableMore.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ");
                target.setText(spannableLess);
            }
        }, truncatedStr.length() + 1, spannableMore.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        target.setText(spannableMore);
    }*/

    private void bindTextViewHolder(TextPostViewHolder h, int position) {
        Post post = mPosts.get(position);
        //h.statusTextView.setText(htmlToSpannedText(post.getStatus()));

        NewsfeedUtils.truncateWithViewMore(mContext, htmlToSpannedText(post.getStatus()), 1000,  h.statusTextView);
    }

    private void bindImageViewHolder(final ImagePostViewHolder h, int position) {
        final ImagePost post = (ImagePost) mPosts.get(position); //At this point we should be sure we can do this cast safely

        h.statusTextView.setLinksClickable(true);
        h.statusTextView.setMovementMethod(LinkMovementMethod.getInstance());
        //h.statusTextView.setText(post.getStatus());
        NewsfeedUtils.truncateWithViewMore(mContext, post.getStatus(), 1000, h.statusTextView);

        String imageSrc = post.getImagePreviewUrl();
        //h.contentImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);


        //int w =  NewsfeedUtils.pxToDp(mContext, post.getImagePreviewWidth());
        //int he = NewsfeedUtils.pxToDp(mContext, post.getImagePreviewHeight());
        int[] dimensions = post.getImage().getPreviewDimensions();

        int[] relativeDimensions = NewsfeedUtils.getRelativeDimensions(mContext, dimensions);

        int w = relativeDimensions[0];
        int he = relativeDimensions[1];

        GradientDrawable placeholder = new GradientDrawable();
        placeholder.setSize(w,he);
        placeholder.setColors(new int[]{android.R.color.white, android.R.color.white});

        Glide.with(mContext)
                .load(imageSrc)
                .apply(new RequestOptions()
                        .fitCenter()
                        .placeholder(placeholder)
                )
                .transition(new DrawableTransitionOptions()
                        .crossFade())
                .into(h.contentImageView);

        h.contentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onPostImageClicked(post, h.contentImageView);
            }
        });
    }


    private void bindImageListViewHolder(ImageListPostViewHolder h, int position) {
        final ImageListPost post = (ImageListPost) mPosts.get(position);

        h.imagesScrollView.setHorizontalScrollBarEnabled(false);

        List<JsonImage> images = post.getImages();

        int size = getMaxHeightSize(images);
        h.imageListContainer.removeAllViews();
        h.imagesScrollView.scrollTo(0,0);
        for (final JsonImage image : images) {
            final ImageView content = new ImageView(mContext);
            content.setLayoutParams((new LinearLayout.LayoutParams(size, size)));
            //content.setScaleType(ImageView.ScaleType.CENTER_CROP);
            content.setPadding(0,0,5,0);
            ViewCompat.setTransitionName(content, NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_image_transition_name));
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onPostImageClicked(post, image.getId(), content);
                }
            });

            Glide.with(mContext)
                    .load(image.getPreviewUrl())
                    .apply(new RequestOptions().centerCrop())
                    .transition(new DrawableTransitionOptions()
                            .crossFade())
                    .into(content);

            h.imageListContainer.addView(content);
        }
    }

    private int getMaxHeightSize(List<JsonImage> images) {
        int size = 200; //the size is at least of 200px
        for (JsonImage image : images) {
            if(image.getPreviewHeight() > size) {
                size = image.getPreviewHeight();
            }
        }
        return NewsfeedUtils.pxToDp(mContext, size);
    }

    private void bindImageContentViewHolder(final ContentImageViewHolder h, final int position) {
        final ContentPost post = (ContentPost) mPosts.get(position);

        Spanned status = htmlToSpannedText(post.getStatus());
        if (TextUtils.isEmpty(status.toString())) {
            h.statusTextView.setVisibility(View.GONE);
        } else {
            h.statusTextView.setVisibility(View.VISIBLE); //Repeated visibility for RecyclerView's reuse
            //h.statusTextView.setText(status);
            NewsfeedUtils.truncateWithViewMore(mContext, status, 1000, h.statusTextView);
        }

        if(post.hasImage()) {
            String url = post.getPreferredPreviewImageUrl();
            final GlidePalette painter = GlidePalette.painter()
                    .paintBackground(h.linkInfoContainer, GlidePalette.PaletteType.VIBRANT)
                    .paintText(h.linkTitleTextView, GlidePalette.PaletteType.VIBRANT, GlidePalette.ColorType.TITLE)
                    .paintText(h.linkDescriptionTextView, GlidePalette.PaletteType.VIBRANT, GlidePalette.ColorType.BODY);

            Glide.with(mContext)
                    .load(url)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_link_darker_gray))
                    .listener(painter)
                    .error(Glide.with(mContext)
                            .load(R.drawable.ic_link_darker_gray)
                            .apply(new RequestOptions()
                                    .fitCenter())
                            .listener(painter))
                    .transition(new DrawableTransitionOptions()
                            .crossFade())
                    .into(h.linkPreviewImageView);
        } else {
            Drawable drawable = NewsfeedUtils.getDrawableResource(mContext, getContextPlaceholderResource(post), null);
            drawable.setAlpha(100);
            h.linkPreviewImageView.setImageDrawable(drawable);
            h.linkTitleTextView.setTextColor(NewsfeedUtils.getColorResource(mContext, android.R.color.white));
            h.linkDescriptionTextView.setTextColor(NewsfeedUtils.getColorResource(mContext, android.R.color.white));
            ColorDrawable black = new ColorDrawable(NewsfeedUtils.getColorResource(mContext, android.R.color.black));
            black.setAlpha(100);
            h.linkInfoContainer.setBackground(black);
        }

        String linkTitle = NewsfeedUtils.truncateString(post.getLinkTitle(), 100);
        String linkDescription =  NewsfeedUtils.truncateString(post.getLinkDescription(), 200);

        h.linkTitleTextView.setText(htmlToSpannedText(linkTitle));

        if(linkDescription != null) {
            h.linkDescriptionTextView.setVisibility(View.VISIBLE);
            h.linkDescriptionTextView.setText(htmlToSpannedText(linkDescription));
        } else {
            h.linkDescriptionTextView.setVisibility(View.GONE);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onContentLinkClicked(post);
            }
        };

        h.linkPreviewImageView.setOnClickListener(listener);
        h.linkTitleTextView.setOnClickListener(listener);
        h.linkDescriptionTextView.setOnClickListener(listener);
    }

    private int getContextPlaceholderResource(ContentPost p) {
        if(!p.hasRouting()) {
            return R.drawable.ic_link_darker_gray;
        }

        switch (p.getRouteName()) {
            case "event.view":
                return R.drawable.ic_event_darker_gray_24dp;
            case "groups-view":
                return R.drawable.ic_people_outline_darker_gray_24dp;
            default:
                return R.drawable.ic_link_darker_gray;
        }
    }

    private void bindDataletViewHolder(DataletPostViewHolder h, final int position){
        final DataletPost p = (DataletPost) mPosts.get(position);

        //h.statusTextView.setText(NewsfeedUtils.htmlToSpannedText(p.getStatus()));
        NewsfeedUtils.truncateWithViewMore(mContext, p.getStatus(),1000, h.statusTextView);

        h.dataletPreviewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDataletImageClicked(p);
            }
        });

        GradientDrawable placeholder = new GradientDrawable();
        placeholder.setSize(400, 300);

        Glide.with(mContext)
                .load(p.getPreviewImage())
                .apply(new RequestOptions()
                        .placeholder(placeholder))
                .transition(new DrawableTransitionOptions()
                        .crossFade())
                .into(h.dataletPreviewImageView);
    }

    private PopupMenu inflateContextPopupMenu(View anchor, final int selectedItemPosition) {
        final Post p = mPosts.get(selectedItemPosition);
        final PopupMenu menu = new PopupMenu(mContext, anchor);
        List<ContextActionMenuItem> contextActionMenu = p.getContextActionMenu();

        for (ContextActionMenuItem item : contextActionMenu) {
            int id = NewsfeedUtils.ContextActionTypeToId(item.getActionType());
            menu.getMenu().add(Menu.NONE, id, Menu.NONE, item.getLabel());
        }

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ContextActionMenuItem.ContextActionType actionType = NewsfeedUtils.idToContextActionType(item.getItemId());
                mListener.onContextActionMenuItemClicked(selectedItemPosition, actionType);
                return true;
            }
        });
        return menu;
    }

    //MODEL LISTENER BRIDGE
    public void setModelListener(NewsfeedPostModel.NewsfeedPostModelListener listener) {
        mPosts.setModelListener(listener);
    }

    //MODEL NETWORK COMMUNICATIONS BRIDGE
    @Override
    public void nLoadFeedPage(boolean resetList) {
        setFooterType(FooterType.LOADING);
        mNetworkCommunication.nLoadFeedPage(resetList);
    }

    @Override
    public void nRequestAuthorization() {
        mNetworkCommunication.nRequestAuthorization();
    }

    @Override
    public void nRefreshPost(String entityType, int entityId, AdapterUpdateType updateType) {
        mNetworkCommunication.nRefreshPost(entityType, entityId, updateType);
    }

    @Override
    public void nSendPost(String message, byte[] attachment, String filename) {
        mNetworkCommunication.nSendPost(message, attachment, filename);
    }

    @Override
    public void nSendPost(String message, String attachment) {
        mNetworkCommunication.nSendPost(message, attachment);
    }

    @Override
    public void nLikeUnlikePost(int position) {
        mNetworkCommunication.nLikeUnlikePost(position);
    }

    @Override
    public void nDeletePost(int position) {
        mNetworkCommunication.nDeletePost(position);
    }

    @Override
    public void nFlagContent(int position, String reason) {
        mNetworkCommunication.nFlagContent(position, reason);
    }

    @Override
    public void nStopPendingRequest() {
        mNetworkCommunication.nStopPendingRequest();
    }

    public interface PostsAdapterInteractionListener{
        void onCommentsButtonClicked(Post post, int position);
        void onLikeButtonLongClicked(Post post);

        void onPostImageClicked(ImagePost post, ImageView postImageView);
        void onPostImageClicked(ImageListPost post, String selectedImageId, ImageView postImageView);
        void onDataletImageClicked(DataletPost post);

        void onContextActionMenuItemClicked(int position, ContextActionMenuItem.ContextActionType actionType);

        void onContentLinkClicked(ContentPost post);
    }
}
