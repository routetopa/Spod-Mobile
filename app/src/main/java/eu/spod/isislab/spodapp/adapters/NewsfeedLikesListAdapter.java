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

public class NewsfeedLikesListAdapter extends ArrayAdapter<NewsfeedLike> {

    private int mResource;

    public NewsfeedLikesListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<NewsfeedLike> objects) {
        super(context, resource, objects);
        this.mResource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        NewsfeedLike like = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.newsfeed_likes_list_item, parent, false);
        }

        ImageView avatarImage = (ImageView) convertView.findViewById(R.id.newsfeed_likes_list_item_icon);
        TextView userDisplay = (TextView) convertView.findViewById(R.id.newsfeed_likes_list_item_text);

        Glide.with(getContext())
                .load(like.getAvatarUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.user_placeholder)
                        .circleCrop())
                .into(avatarImage);

        userDisplay.setText(like.getDisplayUserName());

        return convertView;
    }
}
