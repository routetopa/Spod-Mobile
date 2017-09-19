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

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.fragments.CocreationRoomGridFragment;
import eu.spod.isislab.spodapp.entities.CocreationRoom;

/**
 * Created by Utente on 28/06/2017.
 */
public class CocreationRoomsAdapter extends BaseAdapter{
    ArrayList<CocreationRoom> rooms;
    Context context;
    private static LayoutInflater inflater = null;

    public CocreationRoomsAdapter(Activity mainActivity, ArrayList<CocreationRoom> rooms){
        this.rooms    = rooms;
        this.context  = mainActivity;
        inflater      = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        View rowView  = inflater.inflate(R.layout.cocreation_room_row, null);
        holder.name        = (TextView) rowView.findViewById(R.id.cocreation_room_row_name);
        holder.description = (TextView) rowView.findViewById(R.id.cocreation_room_row_description);
        holder.ownerName   = (TextView) rowView.findViewById(R.id.cocreation_room_owner_name);
        holder.ownerImage  = (ImageView)rowView.findViewById(R.id.cocreation_room_owner_image);
        holder.date        = (TextView) rowView.findViewById(R.id.cocreation_room_row_date);

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


       /* new DownloadImageTask(holder.ownerImage)
                .execute(rooms[position][5]);*/

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "You Clicked "+ rooms[position][1], Toast.LENGTH_LONG).show();
                //CocreationRoomPagedFragment roomFragment = new CocreationRoomPagedFragment();
                CocreationRoomGridFragment roomFragment = new CocreationRoomGridFragment();
                roomFragment.setRoom(rooms.get(position));
                ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, roomFragment, "cocreation_room" )
                        .addToBackStack("cocreation_room")
                        .commit();
            }
        });

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
