package eu.spod.isislab.spodapp.adapters;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.spod.isislab.spodapp.entities.ContextActionMenuItem;
import eu.spod.isislab.spodapp.utils.GlidePalette;
import eu.spod.isislab.spodapp.entities.NewsfeedComment;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;
import eu.spod.isislab.spodapp.R;

public class NewsfeedCommentsAdapter extends RecyclerView.Adapter<NewsfeedCommentsAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView userAvatarImageView;

        TextView userNameTextView;

        TextView commentContentTextView;
        TextView timeTextView;
        ImageButton contextMenuButton;
        FrameLayout attachmentContainer;
        public ViewHolder(View view) {
            super(view);
            timeTextView = (TextView) view.findViewById(R.id.newsfeed_comment_item_time);
            userNameTextView = (TextView) view.findViewById(R.id.newsfeed_comment_item_user_name);
            userAvatarImageView = (ImageView) view.findViewById(R.id.newsfeed_comment_item_user_image);
            contextMenuButton = (ImageButton) view.findViewById(R.id.newsfeed_comment_item_context_menu_button);
            commentContentTextView = (TextView) view.findViewById(R.id.newsfeed_comment_item_message_text);
            attachmentContainer = (FrameLayout) view.findViewById(R.id.newsfeed_comment_item_attachment_container);
        }
    }
    private static final int LOADING = 100;


    private static final int COMMENT_NORMAL = 0;
    private static final int COMMENT_PHOTO = 1;
    private static final int COMMENT_LINK = 2;
    private static final int COMMENT_DATALET = 3;
    private ArrayList<NewsfeedComment> mValues;

    private Context mContext;

    private CommentsAdapterInteractionListener mListener;
    private boolean mHasFooter;
    public NewsfeedCommentsAdapter(Context context) {
        this.mContext = context;
        this.mListener = null;
        this.mValues = new ArrayList<>();
    }

    public void setHasFooter(boolean hasFooter) {
        this.mHasFooter = hasFooter;
    }

    public boolean hasFooter() {
        return this.mHasFooter;
    }

    public int getFooterPosition() {
        if(mHasFooter) {
            return getItemCount() - 1;
        }

        return -1;
    }

    public void setInteractionListener(CommentsAdapterInteractionListener mListener) {
        this.mListener = mListener;
    }

    public void appendData(final ArrayList<NewsfeedComment> comments){
        final int oldSize = mValues.size();

        if(comments != null) {
            mValues.addAll(comments);
            //notifyItemRangeInserted can throw an exception in some cases if the recyclerView is being make layout when we call the method.
            //So let's wait for it to finish layout before to call notifyItemRangeInserted
            Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    notifyItemRangeInserted(oldSize, comments.size());
                    if(mHasFooter) {
                        notifyItemChanged(getFooterPosition());
                    }
                }
            };

            handler.post(r);
        }
    }

    public int findItemPosition(int id) {
        int position = -1;
        for (int i = 0; i<mValues.size(); i++) {
            if(mValues.get(i).getId() == id) {
                position = i;
                return position;
            }
        }
        return position;
    }

    public void deleteItemAtPosition(final int itemPosition) {
        if(itemPosition >= 0 ) {
            mValues.remove(itemPosition);
        }

        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                notifyItemRemoved(itemPosition);
            }
        });
    }

    public boolean isLastPosition(int position) {
        int lastPosition = getItemCount() - 1;
        return position == lastPosition;
    }

    @Override
    public int getItemViewType(int position) {
        if(mHasFooter && isLastPosition(position)) {
            return LOADING;
        }

        NewsfeedComment comment = mValues.get(position);
        Map<String, String> attachment = comment.getAttachment();

        if(attachment == null) {
            return COMMENT_NORMAL;
        }


        switch (comment.getAttachmentType()) {
            case "photo":
                return COMMENT_PHOTO;
            case "link":
                return COMMENT_LINK;
            case "datalet":
                return COMMENT_DATALET;
            default:
                return COMMENT_NORMAL;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if(viewType == LOADING) {
            View view = inflater.inflate(R.layout.newsfeed_post_list_footer, parent, false);
            return new ViewHolder(view);
        }

        View view = inflater.inflate(R.layout.newsfeed_comment_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        switch (viewType) {
            case COMMENT_PHOTO:
                ImageView imageView = generateImageView();
                viewHolder.attachmentContainer.addView(imageView);
                break;
            case COMMENT_LINK:
                View v = LayoutInflater.from(mContext).inflate(R.layout.newsfeed_attachment_link_view, viewHolder.attachmentContainer, false);
                viewHolder.attachmentContainer.addView(v);
                break;
            case COMMENT_DATALET:
                ImageView dataletPreview = generateImageView();
                viewHolder.attachmentContainer.addView(dataletPreview);
                break;
        }

        return viewHolder;
    }

    private ImageView generateImageView() {
        ImageView imageView = new ImageView(mContext);
        imageView.setAdjustViewBounds(true);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, NewsfeedUtils.pxToDp(mContext, 200)));
        imageView.setMaxHeight(NewsfeedUtils.pxToDp(mContext, 300));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(0,0,0,0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setElevation(3);
        }

        return imageView;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(mHasFooter && isLastPosition(position)) {
            return;
        }

        final NewsfeedComment c = mValues.get(position);

        Spanned spanned = NewsfeedUtils.htmlToSpannedText(c.getMessage());
        holder.commentContentTextView.setMovementMethod(new LinkMovementMethod());
        NewsfeedUtils.truncateWithViewMore(mContext, spanned, 500, holder.commentContentTextView);
        /*if (spanned.length() == 0) {
            holder.commentContentTextView.setVisibility(View.GONE);
        } else {
            holder.commentContentTextView.setVisibility(View.VISIBLE);
            holder.commentContentTextView.setText(spanned);
        }*/
        //holder.commentContentTextView.setText(spanned);
        NewsfeedUtils.viewVisibleIfNotNull(spanned, holder.commentContentTextView);

        holder.userNameTextView.setText(c.getUserDisplayName());
        holder.timeTextView.setText(NewsfeedUtils.timeToString(mContext, c.getTimeInMillis()));

        if(c.getContextActionMenu().size() > 0) {
            holder.contextMenuButton.setVisibility(View.VISIBLE);
            final PopupMenu menu = inflateContextPopupMenu(holder.contextMenuButton, position);

            holder.contextMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.show();
                }
            });
        } else {
            holder.contextMenuButton.setVisibility(View.GONE);
        }

        if(NewsfeedUtils.isDefaultAvatar(c.getAvatarUrl())) {
            holder.userAvatarImageView.setImageDrawable(NewsfeedUtils.getTextDrawableForUser(mContext,c.getUserId(), c.getUserDisplayName()));
        } else {
            Glide.with(mContext)
                    .load(c.getAvatarUrl())
                    .apply(new RequestOptions()
                            .placeholder(NewsfeedUtils.getTextDrawableForUser(mContext, c.getUserId(), c.getUserDisplayName()))
                            .circleCrop())
                    .into(holder.userAvatarImageView);
        }
        int type = getItemViewType(position);

        View.OnClickListener userClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onOpenUserProfile(c.getUserId(), c.getUserDisplayName(), c.getAvatarUrl());
                }
            }
        };

        holder.userAvatarImageView.setOnClickListener(userClickListener);
        holder.userNameTextView.setOnClickListener(userClickListener);

        if (type != COMMENT_NORMAL) {
            generateAttachmentView(holder, c);
            holder.attachmentContainer.setVisibility(View.VISIBLE);
        }

    }

    private void generateAttachmentView(ViewHolder holder, final NewsfeedComment c) {
        final Map<String, String> attachment = c.getAttachment();

        String type = attachment.get("type");

        switch (type) {
            case "photo":
                final ImageView imageView = (ImageView) holder.attachmentContainer.getChildAt(0);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            mListener.onImageClicked(c, imageView);
                        }
                    }
                });
                Glide.with(mContext)
                        .load(attachment.get("url"))
                        .apply(new RequestOptions()
                                .optionalCircleCrop()
                                .transform(new RoundedCorners(20))
                        )
                        .transition(new DrawableTransitionOptions()
                                .crossFade())
                        .into(imageView);
                break;
            case "link":
                View vi = holder.attachmentContainer.getChildAt(0);
                ImageView image = (ImageView) vi.findViewById(R.id.newsfeed_attachment_link_view_image);
                TextView title = (TextView) vi.findViewById(R.id.newsfeed_attachment_link_view_title);
                TextView description = (TextView) vi.findViewById(R.id.newsfeed_attachment_link_view_description);

                String titleString = attachment.get("title");
                titleString = NewsfeedUtils.truncateString(titleString, 100);
                title.setText(titleString);
                NewsfeedUtils.viewVisibleIfNotNull(titleString, title);


                String descrString = attachment.get("description");
                descrString = NewsfeedUtils.truncateString(descrString, 200);
                description.setText(descrString);
                NewsfeedUtils.viewVisibleIfNotNull(descrString, description);

                NewsfeedUtils.viewVisibleIfNotNull(attachment.get("thumbnail_url"), image);
                if(c.hasLinkImage()) {
                    GlidePalette painter = GlidePalette.painter()
                            .paintBackground(vi, GlidePalette.PaletteType.VIBRANT_LIGHT)
                            .paintText(title, GlidePalette.PaletteType.VIBRANT_LIGHT, GlidePalette.ColorType.TITLE)
                            .paintText(description, GlidePalette.PaletteType.VIBRANT_LIGHT, GlidePalette.ColorType.BODY);

                    Glide.with(mContext)
                            .load(attachment.get("thumbnail_url"))
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_link_darker_gray)
                                    .error(R.drawable.ic_link_darker_gray))
                            .listener(painter)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(image);
                }


                vi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onLinkClicked(attachment.get("href"));
                    }
                });
                break;
            case "datalet":
                ImageView dataletPreview = (ImageView) holder.attachmentContainer.getChildAt(0);
                Glide.with(mContext)
                        .load(attachment.get("previewImage"))
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_pie_chart_24dp))
                        .into(dataletPreview);
                dataletPreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onDataletClicked(c);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(mValues == null) {
            return 0;
        }

        return mHasFooter ? mValues.size() + 1 : mValues.size();
    }

    public void addBottom(NewsfeedComment c) {
        if (c != null ) {
            mValues.add(c);
            notifyItemInserted(mValues.size() - 1);
            //notifyItemRangeChanged(mValues.size(), getItemCount() - 1); //update position of items below the new inserted one
        }
    }

    public void addTop(NewsfeedComment c) {
        if (c != null) {
            mValues.add(0, c);
            notifyItemInserted(0);
        }
    }

    public void clearList() {
        if(mValues.size() > 0) {
            mValues.clear();
            notifyDataSetChanged();
        }
    }
    private PopupMenu inflateContextPopupMenu(View anchor, final int selectedItemPosition) {
        final NewsfeedComment p = mValues.get(selectedItemPosition);
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
                mListener.onContextActionMenuItemClicked(p, actionType);
                return true;
            }
        });
        return menu;
    }

    public interface CommentsAdapterInteractionListener{
        void onImageClicked(NewsfeedComment c, ImageView view);

        void onContextActionMenuItemClicked(NewsfeedComment comment, ContextActionMenuItem.ContextActionType actionType);

        void onDataletClicked(NewsfeedComment c);

        void onLinkClicked(String href);

        void onOpenUserProfile(int userId, String userName, String userAvatar);
    }
}
