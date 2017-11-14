package eu.spod.isislab.spodapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.UserManager;
import eu.spod.isislab.spodapp.fragments.cocreation.data.CocreationDataRoomFragment;
import eu.spod.isislab.spodapp.fragments.cocreation.knowledge.CocreationKnowledgeRoomFragment;
import eu.spod.isislab.spodapp.fragments.cocreation.media.CocreationMediaRoomGridFragment;
import eu.spod.isislab.spodapp.entities.CocreationRoom;
import eu.spod.isislab.spodapp.fragments.cocreation.CocreationRoomFragment;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationRoomsAdapter extends BaseAdapter implements Observer{

    public static final String[] ROOM_TYPES = {"all", "media", "data", "knowledge"};

    ArrayList<CocreationRoom> rooms;
    ArrayList<CocreationRoom> allRooms;
    Context context;
    CocreationRoomsAdapter mInstance;
    CocreationRoom selectedRoom;

    private static LayoutInflater inflater = null;

    public CocreationRoomsAdapter(Activity mainActivity, ArrayList<CocreationRoom> rooms){
        this.rooms    = rooms;
        this.allRooms = new ArrayList<>(rooms);
        this.context  = mainActivity;
        inflater      = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInstance     = this;
    }

    public void doFilter(String searchKey, String searchType){
        rooms.clear();
        for(CocreationRoom r : allRooms){
            if((r.getName().toLowerCase().contains(searchKey.toLowerCase()) || searchKey.isEmpty()) &&
               (r.getType().equals(searchType) || searchType.equals("all")))
               rooms.add(r);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Holder holder = new Holder();
        final View rowView = inflater.inflate(R.layout.cocreation_room_row, null);

        holder.name = (TextView) rowView.findViewById(R.id.cocreation_room_row_name);
        holder.description = (TextView) rowView.findViewById(R.id.cocreation_room_row_description);
        holder.ownerName = (TextView) rowView.findViewById(R.id.cocreation_room_owner_name);
        holder.ownerImage = (ImageView) rowView.findViewById(R.id.cocreation_room_owner_image);
        holder.date = (TextView) rowView.findViewById(R.id.cocreation_room_row_date);

        holder.name.setText(rooms.get(position).getName());
        holder.description.setText(rooms.get(position).getDescription());
        holder.ownerName.setText(rooms.get(position).getOwnerName());
        holder.date.setText(rooms.get(position).getTimestamp());

        Glide.with(context)
                .load(rooms.get(position).getOwnerImage())
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

        if(rooms.get(position).isHasJoined()) {
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   openRoom(rooms.get(position));
                }
            });
        }else{
            LinearLayout mainContainer = (LinearLayout) rowView.findViewById(R.id.cocreation_room_main_container);
            mainContainer.setAlpha((float)0.55);
            Button joinButton = (Button) rowView.findViewById(R.id.cocreation_row_join_button);
            joinButton.setVisibility(View.VISIBLE);
            joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedRoom = rooms.get(position);
                    NetworkChannel.getInstance().addObserver(mInstance);
                    NetworkChannel.getInstance().cocreationConfirmToJoinToRoom(UserManager.getInstance().getId(), rooms.get(position).getId());
                }
            });
        }

        switch (rooms.get(position).getType()) {
            case "data":
                ((LinearLayout) rowView.findViewById(R.id.cocoreation_rooms_type)).setBackgroundColor(Color.parseColor("#4CAF50"));
                ((ImageView) rowView.findViewById(R.id.cocreation_room_type_icon)).setImageResource(R.drawable.ic_assessment_white_24dp);
                break;
            case "knowledge":
                ((LinearLayout) rowView.findViewById(R.id.cocoreation_rooms_type)).setBackgroundColor(Color.parseColor("#2196F3"));
                ((ImageView) rowView.findViewById(R.id.cocreation_room_type_icon)).setImageResource(R.drawable.ic_description_white_24dp);
                break;
            case "media":
                ((LinearLayout) rowView.findViewById(R.id.cocoreation_rooms_type)).setBackgroundColor(Color.parseColor("#ff9800"));
                ((ImageView) rowView.findViewById(R.id.cocreation_room_type_icon)).setImageResource(R.drawable.ic_collections_white_24dp);
        }

        return rowView;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        try {
            JSONObject res = new JSONObject((String)arg);
            Snackbar.make(((MainActivity)context).findViewById(R.id.container), res.getString("message"), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            NetworkChannel.getInstance().deleteObserver(mInstance);
            openRoom(selectedRoom);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void openRoom(CocreationRoom room)
    {
        CocreationRoomFragment roomFragment = null;
        String tag = "";
        switch (room.getType()) {
            case "media":
                roomFragment = new CocreationMediaRoomGridFragment();
                tag = CocreationMediaRoomGridFragment.TAG;
                break;
            case "data":
                roomFragment = new CocreationDataRoomFragment();
                tag = CocreationDataRoomFragment.TAG;
                break;
            case "knowledge":
                roomFragment = new CocreationKnowledgeRoomFragment();
                tag = CocreationKnowledgeRoomFragment.TAG;
                break;
        }
        roomFragment.setRoom(room);
        ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, roomFragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    private class Holder{
        TextView  name;
        TextView  description;
        TextView  ownerName;
        ImageView ownerImage;
        TextView  date;
    }


}
