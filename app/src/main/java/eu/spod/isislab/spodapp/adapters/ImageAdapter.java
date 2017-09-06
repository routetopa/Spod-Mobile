package eu.spod.isislab.spodapp.adapters;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.fragments.MediaGalleryScreenSliderFragment;
import eu.spod.isislab.spodapp.utils.DownloadImageTask;
import eu.spod.isislab.spodapp.utils.MediaGalleryItem;

public class ImageAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<MediaGalleryItem> items = new ArrayList<>();

    public ImageAdapter(Context c) {
        mContext = c;
    }



    public void setData(JSONArray data){

        items = new ArrayList<>();
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
                        row.getString("Date"))
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder = new ItemHolder();
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            /*holder.webView = new WebView(mContext);
            holder.webView.setLayoutParams(new GridView.LayoutParams(200, 200));
            //webView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.webView.setPadding(8, 8, 8, 8);
            holder.webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            holder.webView.setInitialScale(1);
            holder.webView.getSettings().setJavaScriptEnabled(true);
            holder.webView.getSettings().setLoadWithOverviewMode(true);
            holder.webView.getSettings().setUseWideViewPort(true);
            holder.webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            holder.webView.setScrollbarFadingEnabled(false);*/
            holder.imageView = new ImageView(mContext);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            new DownloadImageTask(holder.imageView)
                    .execute(items.get(position).getImage());

        } else {
            holder.imageView = (ImageView) convertView;
        }

        return holder.imageView;
    }

}

class ItemHolder{
    public ImageView imageView;;
}
