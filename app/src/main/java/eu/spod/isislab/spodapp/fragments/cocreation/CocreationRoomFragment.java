package eu.spod.isislab.spodapp.fragments.cocreation;

import android.support.v4.app.Fragment;
import org.json.JSONArray;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.entities.CocreationRoom;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationRoomFragment extends Fragment implements Observer {

    public CocreationRoom room;
    public JSONArray response;

    public CocreationRoomFragment(){ }

    public void setRoom(CocreationRoom room){
        this.room = room;
    }

    public void refreshData(){
        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getSheetData(this.room.getId());
    }

    @Override
    public void onPause() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onPause();
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            response = new JSONArray((String) arg);
        }catch (Exception e){
            e.printStackTrace();
        }
        NetworkChannel.getInstance().deleteObserver(this);
    }
}
