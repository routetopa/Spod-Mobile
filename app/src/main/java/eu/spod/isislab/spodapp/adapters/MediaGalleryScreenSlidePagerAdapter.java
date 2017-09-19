package eu.spod.isislab.spodapp.adapters;

import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.entities.MediaGalleryItem;
import eu.spod.isislab.spodapp.fragments.MediaGalleryScreenSliderFragment;


public class MediaGalleryScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    private int num_pages;
    private JSONArray data;
    private MediaGalleryScreenSliderFragment[] pages;

    public void setData(JSONArray data, String sheetId){

        JSONArray newJsonArray = new JSONArray();
        try {
            for (int i = data.length() - 1; i >= 0; i--) {
                newJsonArray.put(data.get(i));
            }
        }catch (JSONException e){ e. printStackTrace(); }

        this.data = newJsonArray;
        this.num_pages = (this.data.length() / MediaGalleryGridAdaper.NUM_IMAGES_PER_PAGE)
                       + ((this.data.length() % MediaGalleryGridAdaper.NUM_IMAGES_PER_PAGE != 0) ? 1 : 0);

        if(this.data.length() == 0) {
            this.num_pages = 1;
        }

        this.pages = new MediaGalleryScreenSliderFragment[num_pages];

        //try {
            /*String[] headers = new String[data.getJSONObject(0).length()];
            Iterator<String> iter = data.getJSONObject(0).keys();
            int i = 0;
            while(iter.hasNext()){
                headers[i] = iter.next();
                i++;
            }*/
        /* }catch (JSONException e) {
            e.printStackTrace();
        }*/

            ArrayList<MediaGalleryItem> items = new ArrayList<>();

            int length = data.length();
            for (int i = 0; i < length; i++) {
                try {
                    JSONObject row = this.data.getJSONObject(i);

                    Location loc = new Location("");
                    loc.setLatitude(Double.parseDouble(row.getString("Location").split(",")[0]));
                    loc.setLongitude(Double.parseDouble(row.getString("Location").split(",")[1]));

                    items.add(new MediaGalleryItem(row.getString("Title"),
                            row.getString("Description"),
                            row.getString("Image"),
                            loc,
                            row.getString("Date"),
                            row.getString("User"))
                    );

                    if ((i + 1) % MediaGalleryGridAdaper.NUM_IMAGES_PER_PAGE == 0) {
                        this.pages[i / MediaGalleryGridAdaper.NUM_IMAGES_PER_PAGE] = new MediaGalleryScreenSliderFragment();
                        this.pages[i / MediaGalleryGridAdaper.NUM_IMAGES_PER_PAGE].setSheetId(sheetId);
                        this.pages[i / MediaGalleryGridAdaper.NUM_IMAGES_PER_PAGE].setItems(items);
                        items = new ArrayList<>();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
            }

            //last page if not empty
            if(this.data.length() % MediaGalleryGridAdaper.NUM_IMAGES_PER_PAGE != 0 || this.data.length() == 0) {
                this.pages[num_pages - 1] = new MediaGalleryScreenSliderFragment();
                this.pages[num_pages - 1].setSheetId(sheetId);
                this.pages[num_pages - 1].setItems(items);
            }
    }

    public MediaGalleryScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return this.pages[position];
    }

    @Override
    public int getCount() {
        return num_pages;
    }

}
