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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.User;

public class MembersAdapter extends BaseAdapter {

    private ArrayList<User> allMembers;
    private ArrayList<User> members;
    private ArrayList<User> selectedMember;
    private Context context;
    private static LayoutInflater inflater = null;

    public MembersAdapter(Activity mainActivity, ArrayList<User> members) {
        this.members    = members;
        this.allMembers = new ArrayList<>(members);
        this.selectedMember = new ArrayList<>();
        this.context  = mainActivity;
        inflater      = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void doFilter(String searchKey, int searchType){
        members.clear();
        for(User u : allMembers){
            if((u.getName().toLowerCase().contains(searchKey.toLowerCase()) || searchKey.isEmpty()) &&
               (u.getStatus() == (searchType - 1) || searchType == 0 || (searchType == 4 && selectedMember.contains(u))))
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View rowView  = inflater.inflate(R.layout.member_row, null);
        final Holder holder = new Holder();
        holder.username     = (TextView) rowView.findViewById(R.id.member_username);
        holder.name         = (TextView) rowView.findViewById(R.id.member_name);
        holder.email        = (TextView) rowView.findViewById(R.id.member_email);
        holder.status       = (TextView) rowView.findViewById(R.id.member_status);
        holder.avatar       = (ImageView) rowView.findViewById(R.id.member_avatar);
        holder.selected     = (ImageView) rowView.findViewById(R.id.member_selected);

        holder.username.setText(members.get(position).getUsername());
        holder.name.setText(members.get(position).getName());
        holder.email.setText(members.get(position).getEmail());
        holder.status.setText(context.getResources().getStringArray(R.array.cocreation_room_user_status)[members.get(position).getStatus()]);
        holder.status.setTextColor(Color.parseColor(context.getResources().getStringArray(R.array.cocreation_room_user_status_colors)[members.get(position).getStatus()]));

        Glide.with(context)
                .load(members.get(position).getAvatarImage())
                .apply(new RequestOptions()
                   .centerCrop()
                   .circleCrop()
                        .timeout(10000)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.avatar);

        if(selectedMember.contains(members.get(position))){
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.alpha_scale_animation_press);
            rowView.startAnimation(animation);
            selectedMember.add(members.get(position));
            holder.selected.setVisibility(View.VISIBLE);
        }

        if(members.get(position).getStatus() == User.COCREATION_STATUS_JOINED ||
           members.get(position).getStatus() == User.COCREATION_STATUS_PENDING){
            rowView.setAlpha((float)0.55);
        }else{

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedMember.contains(members.get(position))){
                        Animation animation = AnimationUtils.loadAnimation(context, R.anim.alpha_scale_animation_reset_press);
                        v.startAnimation(animation);
                        selectedMember.remove(members.get(position));
                        holder.selected.setVisibility(View.INVISIBLE);
                    }else{
                        Animation animation = AnimationUtils.loadAnimation(context, R.anim.alpha_scale_animation_press);
                        v.startAnimation(animation);
                        selectedMember.add(members.get(position));
                        holder.selected.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

        return rowView;
    }

    public String getSelectedUsersString(){
        String usersString = "";
        for (User u : selectedMember)
            usersString += u.getEmail() + "#######";
        return usersString;
    }

    private class Holder{
        TextView username;
        TextView name;
        TextView email;
        TextView status;
        ImageView avatar;
        ImageView selected;
    }
}
