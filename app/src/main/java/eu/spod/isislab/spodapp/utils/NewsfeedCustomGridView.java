package eu.spod.isislab.spodapp.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.GridView;

public class NewsfeedCustomGridView extends GridView {

    private long mStartClickTime = 0;

    public NewsfeedCustomGridView(Context context) {
        super(context);
    }

    public NewsfeedCustomGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NewsfeedCustomGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mStartClickTime = System.currentTimeMillis();
        } else if (event.getAction() == MotionEvent.ACTION_UP ||
                event.getActionMasked() == MotionEvent.ACTION_CANCEL) {

            if (System.currentTimeMillis() - mStartClickTime < ViewConfiguration.getTapTimeout()) {
                // Touch was a simple tap.
                return false;
            } else {
                // Touch was a not a simple tap.
                return true;
            }

        }

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (canScrollVertically(this)) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(event);
    }

    public boolean canScrollVertically (AbsListView view) {
        boolean canScroll = false;

        if (view !=null && view.getChildCount ()> 0) {
            boolean isOnTop = view.getFirstVisiblePosition() != 0 || view.getChildAt(0).getTop() != 0;
            boolean isAllItemsVisible = isOnTop && view.getLastVisiblePosition() == view.getChildCount();

            if (isOnTop || isAllItemsVisible) {
                canScroll = true;
            }
        }

        return  canScroll;
    }
}
