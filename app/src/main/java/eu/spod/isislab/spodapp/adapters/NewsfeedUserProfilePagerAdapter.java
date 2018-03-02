package eu.spod.isislab.spodapp.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.spod.isislab.spodapp.R;


public class NewsfeedUserProfilePagerAdapter extends PagerAdapter {

    private Context mContext;

    public NewsfeedUserProfilePagerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;
        if(position == 0) {
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_profile_view_feed_page, container, false);
            view.setTag("FeedPage");

            container.addView(view);
        } else if (position == 1) {
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_profile_view_info_page, container, false);
            view.setTag("InfoPage");
            container.addView(view);
        }

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(((View) object));
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;

        if(position == 0) {
            title = mContext.getString(R.string.newsfeed_user_info_feed_tab);
        } else if (position == 1) {
            title = mContext.getString(R.string.newsfeed_user_info_info_tab);
        }

        return title;
    }
}
