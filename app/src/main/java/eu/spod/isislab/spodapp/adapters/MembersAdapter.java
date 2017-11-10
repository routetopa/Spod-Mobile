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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.User;

public class MembersAdapter extends BaseAdapter {

    ArrayList<User> allMembers;
    ArrayList<User> members;
    Context context;
    private static LayoutInflater inflater = null;

    public MembersAdapter(Activity mainActivity, ArrayList<User> members) {
        this.members    = members;
        this.allMembers = new ArrayList<>(members);
        this.context  = mainActivity;
        inflater      = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void doFilter(String searchKey){
        members.clear();
        for(User u : allMembers){
            if((u.getUsername().toLowerCase().contains(searchKey.toLowerCase()) || searchKey.isEmpty()))
                members.add(u);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();
        View rowView  = inflater.inflate(R.layout.member_row, null);
        holder.username = (TextView) rowView.findViewById(R.id.member_username);
        holder.name     = (TextView) rowView.findViewById(R.id.member_name);
        holder.email    = (TextView) rowView.findViewById(R.id.member_email);
        holder.avatar   = (ImageView) rowView.findViewById(R.id.member_avatar);

        holder.username.setText(members.get(position).getUsername());
        holder.name.setText(members.get(position).getName());
        holder.email.setText(members.get(position).getEmail());

        Glide.with(context)
                .load(members.get(position).getAvatarImage())
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(holder.avatar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        holder.avatar.setImageDrawable(circularBitmapDrawable);
                    }
                });

        return rowView;
    }

    private class Holder{
        TextView username;
        TextView name;
        TextView email;
        ImageView avatar;
    }
}
