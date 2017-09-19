package eu.spod.isislab.spodapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.AgoraRoom;
import eu.spod.isislab.spodapp.fragments.AgoraRoomFragment;

public class AgoraRoomsAdapter extends BaseAdapter{

    ArrayList<AgoraRoom> rooms;
    Context context;
    private static LayoutInflater inflater = null;

    public AgoraRoomsAdapter(Activity mainActivity, ArrayList<AgoraRoom> rooms) {
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
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView  = inflater.inflate(R.layout.agora_room_row, null);
        holder.timestamp  = (TextView) rowView.findViewById(R.id.agora_row_date);
        holder.name       = (TextView) rowView.findViewById(R.id.agora_row_name);
        holder.body       = (TextView) rowView.findViewById(R.id.agora_row_body);
        holder.views      = (ProgressBar) rowView.findViewById(R.id.agora_row_views);
        holder.comments   = (ProgressBar) rowView.findViewById(R.id.agora_row_comments);
        holder.opendata   = (ProgressBar) rowView.findViewById(R.id.agora_row_opendata);

        holder.timestamp.setText(rooms.get(position).getTimestamp());
        holder.name.setText(rooms.get(position).getSubject());
        holder.body.setText(rooms.get(position).getBody());
        holder.views.setProgress(Integer.parseInt(rooms.get(position).getViews()));
        holder.comments.setProgress(Integer.parseInt(rooms.get(position).getComments()));
        holder.opendata.setProgress(Integer.parseInt(rooms.get(position).getOpendata()));

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "You Clicked "+ rooms[position][1], Toast.LENGTH_LONG).show();
                //CocreationRoomPagedFragment roomFragment = new CocreationRoomPagedFragment();
                AgoraRoomFragment roomFragment = new AgoraRoomFragment();
                roomFragment.setRoom(rooms.get(position));
                ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, roomFragment, "agora_room" )
                        .addToBackStack("agora_room")
                        .commit();
            }
        });

        return rowView;
    }

    private class Holder{
        TextView timestamp;
        TextView name;
        TextView body;
        ProgressBar views;
        ProgressBar comments;
        ProgressBar opendata;
    }
}
