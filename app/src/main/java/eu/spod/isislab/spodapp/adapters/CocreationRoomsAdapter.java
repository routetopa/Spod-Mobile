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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.fragments.cocreation.data.CocreationDataRoomFragment;
import eu.spod.isislab.spodapp.fragments.cocreation.knowledge.CocreationKnowledgeRoomFragment;
import eu.spod.isislab.spodapp.fragments.cocreation.media.CocreationMediaRoomGridFragment;
import eu.spod.isislab.spodapp.entities.CocreationRoom;
import eu.spod.isislab.spodapp.fragments.cocreation.CocreationRoomFragment;

public class CocreationRoomsAdapter extends BaseAdapter{

    public static final String[] ROOM_TYPES = {"all", "media", "data", "knowledge"};

    ArrayList<CocreationRoom> rooms;
    ArrayList<CocreationRoom> allRooms;
    Context context;

    private static LayoutInflater inflater = null;

    public CocreationRoomsAdapter(Activity mainActivity, ArrayList<CocreationRoom> rooms){
        this.rooms    = rooms;
        this.allRooms = new ArrayList<>(rooms);
        this.context  = mainActivity;
        inflater      = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CocreationRoomFragment roomFragment = null;
                switch (rooms.get(position).getType()) {
                    case "media":
                        roomFragment = new CocreationMediaRoomGridFragment();
                        break;
                    case "data":
                        roomFragment = new CocreationDataRoomFragment();
                        break;
                    case "knowledge":
                        roomFragment = new CocreationKnowledgeRoomFragment();
                        break;
                }
                roomFragment.setRoom(rooms.get(position));
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, roomFragment, "cocreation_room")
                        .addToBackStack("cocreation_room")
                        .commit();
            }
        });

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

    private class Holder{
        TextView  name;
        TextView  description;
        TextView  ownerName;
        ImageView ownerImage;
        TextView  date;
    }


}
