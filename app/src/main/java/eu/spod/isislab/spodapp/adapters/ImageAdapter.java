package eu.spod.isislab.spodapp.adapters;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.fragments.cocreation.media.GalleryItemFragment;
import eu.spod.isislab.spodapp.entities.MediaGalleryItem;

public class ImageAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<MediaGalleryItem> items = new ArrayList<>();
    int tileWidth;

    public ImageAdapter(Context c) {
        mContext = c;
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        tileWidth = size.x / 2;
    }

    public void setData(JSONArray data){

        items.clear();
        int length = data.length();
        for (int i = 0; i < length; i++) {
            try {
                JSONObject row = data.getJSONObject(i);
                Location loc = new Location("");
                loc.setLatitude(Double.parseDouble(row.getString("Location").split(",")[0]));
                loc.setLongitude(Double.parseDouble(row.getString("Location").split(",")[1]));

                items.add(new MediaGalleryItem(row.getString("Title"),
                        row.getString("Description"),
                        row.getString("Image"),
                        loc,
                        row.getString("Date"),
                        row.getString("UserManager"))
                );
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
    }

    public int getCount() {
        return this.items.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        ItemHolder holder = new ItemHolder();
        if (convertView == null) {
            holder.imageView = new ImageView(mContext);
            holder.imageView.setLayoutParams(new GridView.LayoutParams(tileWidth, tileWidth));
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            holder.imageView = (ImageView) convertView;
        }

        Glide.with(mContext)
                .load(items.get(position).getImage())
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                GalleryItemFragment galleryItemFragment = new GalleryItemFragment();
                galleryItemFragment.setData(items.get(position).getTitle(),
                                            items.get(position).getDescription(),
                                            items.get(position).getImage(),
                                            items.get(position).getLocation(),
                                            items.get(position).getDate(),
                                            items.get(position).getUsername());
                ((MainActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,android.R.anim.slide_out_right, android.R.anim.slide_in_left)
                        .add(R.id.container, galleryItemFragment)
                        .addToBackStack("gallery_item_fragment")
                        .commit();
            }
        });

        return holder.imageView;
    }

}

class ItemHolder{
    public ImageView imageView;;
}
