package eu.spod.isislab.spodapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.AgoraComment;
import eu.spod.isislab.spodapp.entities.User;
import eu.spod.isislab.spodapp.fragments.AgoraNestedCommentFragment;
import eu.spod.isislab.spodapp.fragments.AgoraRoomFragment;
import eu.spod.isislab.spodapp.fragments.DataletFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

/**
 * Created by Utente on 11/09/2017.
 */
public class AgoraCommentsAdapter extends BaseAdapter {

    ArrayList<AgoraComment> comments;
    Context context;
    private static LayoutInflater inflater = null;
    private int level;

    public AgoraCommentsAdapter(Activity mainActivity, ArrayList<AgoraComment> comments, int level) {
        this.context  = mainActivity;
        inflater      = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.comments = comments;
        this.level = level;
    }

    public void add(AgoraComment comment){
       comments.add(comment);
    }

    @Override
    public int getCount() {
        return this.comments.size();
    }

    @Override
    public Object getItem(int position)
    {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();
        View rowView;
        if(User.getInstance().getId().equals(comments.get(position).getOwnerId()))
        {
            rowView = inflater.inflate(R.layout.agora_comment_row_right, null);
        }else {
            rowView = inflater.inflate(R.layout.agora_comment_row_left, null);
        }
        holder.ownerImage   = (ImageView) rowView.findViewById(R.id.agora_comment_owner_image);
        holder.ownerName    = (TextView) rowView.findViewById(R.id.agora_comment_owner_name);
        holder.body         = (TextView) rowView.findViewById(R.id.agora_comment_body);
        holder.date         = (TextView) rowView.findViewById(R.id.agora_comment_date);
        holder.replay       = (TextView) rowView.findViewById(R.id.agora_comment_reply);
        holder.dataletImage = (ImageView) rowView.findViewById(R.id.agora_comment_datalet);

        holder.ownerName.setText(comments.get(position).getUsername());
        holder.body.setText(comments.get(position).getComment());
        holder.date.setText(comments.get(position).getTimestamp());

        if(level > 0){
            holder.replay.setText("");
        }else{
            holder.replay.setText(context.getResources().getString(R.string.agora_comment_replay_label) + "(" + comments.get(position).getTotal_comment() + ")");
            holder.replay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AgoraNestedCommentFragment nestedCommentFragment = new AgoraNestedCommentFragment();
                    nestedCommentFragment.setComment(comments.get(position));
                    ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, nestedCommentFragment, "nested_comment_fragment" )
                            .addToBackStack("nested_comment_fragment")
                            .commit();

                }
            });
        }

        Glide.with(context)
                .load(comments.get(position).getAvatar_url())
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(holder.ownerImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        holder.ownerImage.setImageDrawable(circularBitmapDrawable);
                    }
                });

        if(!comments.get(position).getDatalet_id().equals("null"))
        {

            Glide.with(context)
                    .load(NetworkChannel.getInstance().getDataletImageStaticUrl(comments.get(position).getDatalet_id()))
                    .fitCenter()
                    .into(holder.dataletImage);

            holder.dataletImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    DataletFragment dataletFragment = new DataletFragment();
                    dataletFragment.setComment(comments.get(position));
                    ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, dataletFragment, "datalet_fragment" )
                            .addToBackStack("datalet_fragment")
                            .commit();

                }
            });
        }

        return rowView;
    }

    private class Holder{
        ImageView ownerImage;
        ImageView dataletImage;
        TextView  ownerName;
        TextView  body;
        TextView  date;
        TextView  replay;
    }
}


