package eu.spod.isislab.spodapp.adapters;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.fragments.CocreationRoomFragment;
import eu.spod.isislab.spodapp.fragments.CocreationRoomGridFragment;
import eu.spod.isislab.spodapp.fragments.CocreationRoomPagedFragment;
import eu.spod.isislab.spodapp.utils.DownloadImageTask;

/**
 * Created by Utente on 28/06/2017.
 */
public class CocreationRoomsAdapter extends BaseAdapter{

    String[][] rooms;
    Context context;
    private static LayoutInflater inflater = null;

    public CocreationRoomsAdapter(Activity mainActivity, String[][] rooms){
        this.rooms    = rooms;
        this.context  = mainActivity;
        inflater      = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return rooms.length;
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
        Holder holder = new Holder();
        View rowView  = inflater.inflate(R.layout.cocreation_room_row, null);
        holder.name        = (TextView)  rowView.findViewById(R.id.room_name);
        holder.description  = (TextView) rowView.findViewById(R.id.room_description);
        holder.ownerName   = (TextView)  rowView.findViewById(R.id.room_owner_name);
        holder.ownerImage = (ImageView)  rowView.findViewById(R.id.room_owner_image);

        holder.name.setText(rooms[position][0]);
        holder.description.setText(rooms[position][1]);
        holder.ownerName.setText(rooms[position][4]);

        new DownloadImageTask(holder.ownerImage)
                .execute(rooms[position][5]);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "You Clicked "+ rooms[position][1], Toast.LENGTH_LONG).show();
                CocreationRoomPagedFragment roomFragment = new CocreationRoomPagedFragment();
                //CocreationRoomGridFragment roomFragment = new CocreationRoomGridFragment();
                roomFragment.setRoom(rooms[position][0], rooms[position][2], rooms[position][3]);
                ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, roomFragment, "cocreation_room" )
                        .addToBackStack("cocreation_room")
                        .commit();
            }
        });

        return rowView;
    }

    public class Holder{
        TextView  name;
        TextView  description;
        TextView  ownerName;
        ImageView ownerImage;
    }


}
