package eu.spod.isislab.spodapp.fragments.cocreation.knowledge;

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

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.fragments.cocreation.CocreationRoomFragment;
import eu.spod.isislab.spodapp.fragments.cocreation.CocreationWebContentFragment;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class CocreationKnowledgeRoomFragment extends CocreationRoomFragment implements BottomNavigationView.OnNavigationItemSelectedListener
{

    public static final String TAG     = "CocreationKnowledgeRoomFragment";
    public static final String WEB_TAG = "CocreationDataRoomFragmentWeb";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.cocreation_room_knowledge_fragment, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                rootView.findViewById(R.id.cocoreation_room_knowledge_list_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        ((MainActivity)getActivity()).setToolbarTitle(room.getName());

        laodDocumentFragment(0, false);

        return rootView;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String code = "";
        switch (item.getItemId()) {
            case R.id.cocoreation_room_knowledge_menu_explore:
                laodDocumentFragment(0, true);
                break;
            case R.id.cocoreation_room_knowledge_menu_ideas:
                laodDocumentFragment(1, true);
                break;
            case R.id.cocoreation_room_knowledge_menu_outcome:
                laodDocumentFragment(2, true);
                break;
            case R.id.cocoreation_room_knowledge_menu_datalets:
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
                webFragment.setTemplate(CocreationWebContentFragment.DATALETS_TEMPLATE, res.getString("datalets"), res.getString("datalets_definition"));
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_knowledge_container, webFragment).addToBackStack(WEB_TAG).commit();

            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        NetworkChannel.getInstance().deleteObserver(this);
    }

    private void laodDocumentFragment(int doc, boolean pop){
        CocreationWebContentFragment fragment = new CocreationWebContentFragment();
        fragment.setResourceUrl(NetworkChannel.getInstance().getSpodEndpoint() + Consts.COCREATION_DOCUMENT_ENDPOINT + room.getDocs().get(doc));
        if(pop) getActivity().getSupportFragmentManager().popBackStack();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.room_knowledge_container, fragment).addToBackStack(WEB_TAG).commit();
    }


}
