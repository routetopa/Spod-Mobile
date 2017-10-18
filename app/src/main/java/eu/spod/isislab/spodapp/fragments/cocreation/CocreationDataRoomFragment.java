package eu.spod.isislab.spodapp.fragments.cocreation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationDataRoomFragment extends CocreationRoomFragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.cocreation_room_data_fragment, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                rootView.findViewById(R.id.cocoreation_room_data_list_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        CocreationWebContentFragment datasetFragment = new CocreationWebContentFragment();
        datasetFragment.setResourceUrl(NetworkChannel.getInstance().getSpodEndpoint() + NetworkChannel.COCREATION_DATASET_ENDPOINT + room.getSheetId());
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, datasetFragment).addToBackStack("cocreation_data_room_dataset_fragment").commit();

        ((MainActivity)getActivity()).setToolbarTitle(room.getName());

        return rootView;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        String code = "";
        switch (item.getItemId()) {
            case R.id.cocoreation_room_data_menu_dataset:
                CocreationWebContentFragment datasetFragment = new CocreationWebContentFragment();
                datasetFragment.setResourceUrl(NetworkChannel.getInstance().getSpodEndpoint() + NetworkChannel.COCREATION_DATASET_ENDPOINT + room.getSheetId());
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, datasetFragment).addToBackStack("cocreation_data_room_dataset_fragment").commit();
                break;
            case R.id.cocoreation_room_data_menu_metadata:
                NetworkChannel.getInstance().addObserver(this);
                NetworkChannel.getInstance().getCocreationMetadata(room.getId());
                break;
            case R.id.cocoreation_room_data_menu_notes:
                CocreationWebContentFragment noteFragment = new CocreationWebContentFragment();
                noteFragment.setResourceUrl(NetworkChannel.getInstance().getSpodEndpoint() + NetworkChannel.COCREATION_DOCUMENT_ENDPOINT + room.getDocs().get(0));
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, noteFragment).addToBackStack("cocreation_data_room_note_fragment").commit();
                break;
            case R.id.cocoreation_room_data_menu_discussion:
                CocreationCommentsFragment commentFragment = new CocreationCommentsFragment();
                commentFragment.setRoom(room);
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, commentFragment).addToBackStack("cocreation_data_room_comment_fragment").commit();
                break;
            case R.id.cocoreation_room_data_menu_datalets:
                NetworkChannel.getInstance().addObserver(this);
                NetworkChannel.getInstance().getCocreationDatalets(room.getId());
                break;
        }

        return true;
    }

    @Override
    public void update(Observable o, Object response) {
        try {

            JSONObject res = new JSONObject((String)response);
            Boolean status = res.getBoolean("status");

            if(status)
            {
                CocreationWebContentFragment webFragment = new CocreationWebContentFragment();
                switch(NetworkChannel.getInstance().getCurrentService()) {
                    case NetworkChannel.SERVICE_COCREATION_GET_METADATA:
                        webFragment.setTemplate("metadata", res.getString("metadata"), "");
                        break;
                    case NetworkChannel.SERVICE_COCREATION_GET_DATALETS:
                        webFragment.setTemplate("datalets", res.getString("datalets"), res.getString("datalets_definition"));
                        break;
                }

                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, webFragment).addToBackStack("cocreation_data_room_web_fragment").commit();

            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        NetworkChannel.getInstance().deleteObserver(this);
    }

}
