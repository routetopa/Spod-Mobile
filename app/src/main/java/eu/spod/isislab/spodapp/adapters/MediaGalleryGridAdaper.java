package eu.spod.isislab.spodapp.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.fragments.cocreation.GalleryItemFragment;
import eu.spod.isislab.spodapp.entities.MediaGalleryItem;
import eu.spod.isislab.spodapp.R;

public class MediaGalleryGridAdaper extends BaseAdapter {

    public static final int NUM_IMAGES_PER_PAGE = 4;

    private static LayoutInflater inflater = null;

    private Context mContext;
    private ArrayList<MediaGalleryItem> items = new ArrayList<>();

    public MediaGalleryGridAdaper(Context mContext) {
        this.mContext = mContext;
        inflater      = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItems(ArrayList<MediaGalleryItem> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        return this.items.size();
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

        GalleryItemHolder holder = new GalleryItemHolder();
        RelativeLayout itemView = (RelativeLayout) inflater.inflate(R.layout.gallery_item, null);

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        //float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpHeight = (parent.getMeasuredHeight() -  140)/ 2;
        //float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        try {
            holder.image_webview = (WebView) itemView.findViewById(R.id.image_webview);
            holder.image_webview.setLayoutParams(new RelativeLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, (int)dpHeight  ));
            holder.image_title   = (TextView) itemView.findViewById(R.id.image_title);
            holder.image_description = (TextView) itemView.findViewById(R.id.image_description);
            holder.image_date = (TextView) itemView.findViewById(R.id.image_date);

            holder.image_webview.loadUrl(items.get(position).getImage());
            holder.image_title.setText(items.get(position).getTitle());
            holder.image_description.setText(items.get(position).getDescription());
            holder.image_date.setText(items.get(position).getDate());

            holder.image_webview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            holder.image_webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            holder.image_webview.setInitialScale(1);
            holder.image_webview.getSettings().setJavaScriptEnabled(true);
            holder.image_webview.getSettings().setLoadWithOverviewMode(true);
            holder.image_webview.getSettings().setUseWideViewPort(true);
            holder.image_webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            holder.image_webview.setScrollbarFadingEnabled(false);

        }catch (Exception e){
            e.printStackTrace();
        }

        itemView.findViewById(R.id.image_detail).setOnClickListener(new View.OnClickListener() {

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

        return itemView;
    }
}

class GalleryItemHolder{
    public WebView  image_webview;
    public TextView image_title;
    public TextView image_description;
    public TextView image_date;
}
