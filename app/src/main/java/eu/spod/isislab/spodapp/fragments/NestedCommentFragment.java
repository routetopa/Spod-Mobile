package eu.spod.isislab.spodapp.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.CommentsAdapter;
import eu.spod.isislab.spodapp.entities.Comment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class NestedCommentFragment extends CommentFragment {

    public static final String TAG = "NestedCommentFragment";

    public Comment comment;
    NestedCommentFragment cInstance;

    public NestedCommentFragment(){
        cInstance = this;
        maxLevel  = 2;
    }

    public void setComment(Comment comment){
        this.comment = comment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        asView = inflater.inflate(R.layout.room_nested_comment_fragment, container, false);
        listView = (ListView) asView.findViewById(R.id.nested_comment_list);
        adapter = new CommentsAdapter(getActivity(), comments, maxLevel/*Integer.parseInt(comment.getLevel()) + 1*/);
        listView.setAdapter(adapter);

        ((TextView)asView.findViewById(R.id.nested_comment_owner_name)).setText(comment.getUsername());
        ((WebView)asView.findViewById(R.id.nested_comment_body)).loadDataWithBaseURL("", comment.getComment(), "text/html", "UTF-8", "");
        ((TextView)asView.findViewById(R.id.nested_comment_date)).setText(comment.getTimestamp());

        final ImageView ownerImage = (ImageView) asView.findViewById(R.id.nested_comment_owner_image);

        Glide.with(getActivity())
                .load(comment.getAvatar_url())
                .apply(new RequestOptions()
                        .centerCrop()
                        .circleCrop()
                        .timeout(10000)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(ownerImage);

        if(!comment.getDatalet_id().equals("null"))
        {
            ImageView dataletImage = (ImageView) asView.findViewById(R.id.comment_datalet);

            Glide.with(getActivity())
                    .load(NetworkChannel.getInstance().getDataletImageStaticUrl(comment.getDatalet_id()))
                    .apply(new RequestOptions()
                            .fitCenter()
                            .timeout(10000)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(dataletImage);

            dataletImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    DataletFragment dataletFragment = new DataletFragment();
                    dataletFragment.setComment(comment);
                    (getActivity()).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, dataletFragment, DataletFragment.TAG )
                            .addToBackStack(DataletFragment.TAG)
                            .commit();
                }
            });
        }

        ((ImageButton)asView.findViewById(R.id.nested_comment_send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etComment = (EditText)asView.findViewById(R.id.nested_comment_add_new);
                if(!etComment.getText().toString().isEmpty())
                {
                    NetworkChannel.getInstance().addObserver(cInstance);
                    NetworkChannel.getInstance().addAgoraComment(
                            comment.getRoomId(),
                            comment.getId(),
                            etComment.getText().toString(),
                            "" + (Integer.parseInt(comment.getLevel()) + 1),
                            "0");
                    comment.setTotal_comment("" + (Integer.parseInt(comment.getTotal_comment()) + 1));
                }else{
                    Snackbar.make(getActivity().findViewById(R.id.container), getString(R.string.comment_add_empty_comment_warning), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
            }
        });

        return asView;
    }


}
