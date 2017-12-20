package eu.spod.isislab.spodapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import eu.spod.isislab.spodapp.R;

public class NewsfeedImageChooserAdapter extends BaseAdapter {

    private String[] mImageLinks;
    private Context mContext;
    private ItemClickListener mListener;

    public NewsfeedImageChooserAdapter(Context context, String[] imageLinks) {
        this.mImageLinks = imageLinks;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mImageLinks.length;
    }

    @Override
    public Object getItem(int position) {
        return mImageLinks[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String link = mImageLinks[position];


        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.newsfeed_image_chooser_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.newsfeed_image_chooser_item);

        Glide.with(mContext)
                .load(link)
                .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onItemClick(link);
                }
            }
        });

        return convertView;
    }

    public void setOnItemClickListener(ItemClickListener listener){
        this.mListener = listener;
    }

    public interface ItemClickListener {
        void onItemClick(String link);
    }
}
