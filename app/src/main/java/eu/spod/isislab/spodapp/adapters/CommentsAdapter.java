package eu.spod.isislab.spodapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.Comment;
import eu.spod.isislab.spodapp.utils.UserManager;
import eu.spod.isislab.spodapp.fragments.DataletFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CommentsAdapter extends BaseAdapter {

    ArrayList<Comment> comments;
    Context context;
    private static LayoutInflater inflater = null;
    private int maxLevel;

    public CommentsAdapter(Activity mainActivity, ArrayList<Comment> comments, int maxLevel) {
        this.context  = mainActivity;
        inflater      = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.comments = comments;
        this.maxLevel = maxLevel;
    }

    public void add(Comment comment){
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
        if(UserManager.getInstance().getId().equals(comments.get(position).getOwnerId()))
        {
            rowView = inflater.inflate(R.layout.comment_row_right, null);
        }else {
            rowView = inflater.inflate(R.layout.comment_row_left, null);
        }
        holder.ownerImage   = (ImageView) rowView.findViewById(R.id.comment_owner_image);
        holder.ownerName    = (TextView) rowView.findViewById(R.id.comment_owner_name);
        holder.body         = (WebView) rowView.findViewById(R.id.comment_body);
        holder.date         = (TextView) rowView.findViewById(R.id.comment_date);
        holder.replay       = (TextView) rowView.findViewById(R.id.comment_reply);
        holder.dataletImage = (ImageView) rowView.findViewById(R.id.comment_datalet);

        holder.ownerName.setText(comments.get(position).getUsername());
        holder.body.loadDataWithBaseURL("", comments.get(position).getComment(), "text/html", "UTF-8", "");
        holder.body.setBackgroundColor(Color.TRANSPARENT);
        holder.date.setText(comments.get(position).getTimestamp());

        if(Integer.parseInt(comments.get(position).getLevel()) == maxLevel - 1){
            holder.replay.setText("");
        }else{
            holder.replay.setText(context.getResources().getString(R.string.agora_comment_replay_label) + "(" + comments.get(position).getTotal_comment() + ")");
            holder.replay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nestedCommentAction(comments.get(position));
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
                            .replace(R.id.container, dataletFragment, DataletFragment.TAG )
                            .addToBackStack(DataletFragment.TAG)
                            .commit();

                }
            });
        }

        return rowView;
    }

    public void nestedCommentAction(Comment comment){};

    private class Holder{
        ImageView ownerImage;
        ImageView dataletImage;
        TextView  ownerName;
        WebView   body;
        TextView  date;
        TextView  replay;
    }
}


