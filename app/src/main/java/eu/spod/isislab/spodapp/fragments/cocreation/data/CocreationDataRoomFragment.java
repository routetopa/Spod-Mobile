package eu.spod.isislab.spodapp.fragments.cocreation.data;

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
import eu.spod.isislab.spodapp.fragments.cocreation.CocreationCommentsFragment;
import eu.spod.isislab.spodapp.fragments.cocreation.CocreationRoomFragment;
import eu.spod.isislab.spodapp.fragments.cocreation.CocreationWebContentFragment;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationDataRoomFragment extends CocreationRoomFragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG         = "CocreationDataRoomFragment";
    public static final String DATASET_TAG = "CocreationDataRoomFragmentDataset";
    public static final String NOTE_TAG    = "CocreationDataRoomFragmentNote";
    public static final String COMMENT_TAG = "CocreationDataRoomFragmentComment";
    public static final String WEB_TAG     = "CocreationDataRoomFragmentWeb";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.cocreation_room_data_fragment, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
        rootView.findViewById(R.id.cocreation_room_data_list_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        CocreationWebContentFragment datasetFragment = new CocreationWebContentFragment();
        datasetFragment.setResourceUrl(NetworkChannel.getInstance().getSpodEndpoint() + Consts.COCREATION_DATASET_ENDPOINT + room.getSheetId());
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, datasetFragment).addToBackStack(DATASET_TAG).commit();

        ((MainActivity)getActivity()).setToolbarTitle(room.getName());

        return rootView;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        String code = "";
        switch (item.getItemId()) {
            case R.id.cocreation_room_data_menu_dataset:
                CocreationWebContentFragment datasetFragment = new CocreationWebContentFragment();
                datasetFragment.setResourceUrl(NetworkChannel.getInstance().getSpodEndpoint() + Consts.COCREATION_DATASET_ENDPOINT + room.getSheetId());
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, datasetFragment).addToBackStack(DATASET_TAG).commit();
                break;
            case R.id.cocreation_room_data_menu_metadata:
                NetworkChannel.getInstance().addObserver(this);
                NetworkChannel.getInstance().getCocreationMetadata(room.getId());
                break;
            case R.id.cocreation_room_data_menu_notes:
                CocreationWebContentFragment noteFragment = new CocreationWebContentFragment();
                noteFragment.setResourceUrl(NetworkChannel.getInstance().getSpodEndpoint() + Consts.COCREATION_DOCUMENT_ENDPOINT + room.getDocs().get(0));
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, noteFragment).addToBackStack(NOTE_TAG).commit();
                break;
            case R.id.cocreation_room_data_menu_discussion:
                CocreationCommentsFragment commentFragment = new CocreationCommentsFragment();
                commentFragment.setRoom(room);
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, commentFragment).addToBackStack(COMMENT_TAG).commit();
                break;
            case R.id.cocreation_room_data_menu_datalets:
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
                    case Consts.SERVICE_COCREATION_GET_METADATA:
                        webFragment.setTemplate(CocreationWebContentFragment.METADATA_TEMPLATE, res.getString("metadata"), "");
                        break;
                    case Consts.SERVICE_COCREATION_GET_DATALETS:
                        webFragment.setTemplate(CocreationWebContentFragment.DATALETS_TEMPLATE, res.getString("datalets"), res.getString("datalets_definition"));
                        break;
                }

                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_data_container, webFragment).addToBackStack(WEB_TAG).commit();

            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        NetworkChannel.getInstance().deleteObserver(this);
    }

}
