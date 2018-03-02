package eu.spod.isislab.spodapp.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import eu.spod.isislab.spodapp.entities.NewsfeedLike;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.fragments.newsfeed.LikesPopupWindow;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;
import eu.spod.isislab.spodapp.utils.UserManager;

public class NewsfeedLikesListAdapter extends ArrayAdapter<NewsfeedLike> {

    private int mResource;
    private LikesPopupWindow.LikesWindowInteractionListener mListener;

    public NewsfeedLikesListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<NewsfeedLike> objects) {
        super(context, resource, objects);
        this.mResource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final NewsfeedLike like = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.newsfeed_likes_list_item, parent, false);
        }

        ImageView avatarImage = (ImageView) convertView.findViewById(R.id.newsfeed_likes_list_item_icon);
        TextView userDisplay = (TextView) convertView.findViewById(R.id.newsfeed_likes_list_item_text);

        if(NewsfeedUtils.isDefaultAvatar(like.getAvatarUrl())) {
            avatarImage.setImageDrawable(NewsfeedUtils.getTextDrawableForUser(getContext(), like.getUserId(), like.getDisplayUserName()));
        } else {
            Glide.with(getContext())
                    .load(like.getAvatarUrl())
                    .apply(new RequestOptions()
                            .placeholder(NewsfeedUtils.getTextDrawableForUser(getContext(), like.getUserId(), like.getDisplayUserName()))
                            .circleCrop())
                    .into(avatarImage);
        }
        userDisplay.setText(like.getDisplayUserName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onUserClicked(like.getUserId(), like.getDisplayUserName(), like.getAvatarUrl());
                }
            }
        });

        return convertView;
    }


    public void setLikesWindowInteractionListener(LikesPopupWindow.LikesWindowInteractionListener mListener) {
        this.mListener = mListener;
    }
}
