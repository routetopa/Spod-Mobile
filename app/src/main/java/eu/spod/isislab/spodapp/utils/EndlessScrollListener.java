package eu.spod.isislab.spodapp.utils;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

public abstract class EndlessScrollListener implements AbsListView.OnScrollListener, View.OnTouchListener {
    public final static int SCROLL_DIRECTION_UP = 0;
    public final static int SCROLL_DIRECTION_DOWN = 1;

    private int visibleThreshold       = 5;
    private int currentPage            = 0;
    private int previousTotalItemCount = 0;
    private boolean loading            = true;
    private int startingPageIndex      = 0;
    private int scrollDirection        = SCROLL_DIRECTION_DOWN;
    private boolean isUserScrolling    = false;

    public EndlessScrollListener() {
    }

    public EndlessScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    public EndlessScrollListener(int visibleThreshold, int startPage) {
        this.visibleThreshold = visibleThreshold;
        this.startingPageIndex = startPage;
        this.currentPage = startPage;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        Log.v("EndlessScroll", "firstVisibleItem: "+firstVisibleItem);
        Log.v("EndlessScroll", "visibleItemCount: "+visibleItemCount);
        Log.v("EndlessScroll", "totalItemCount: "+totalItemCount);

        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) { this.loading = true; }
        }

        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
            currentPage++;
        }

        if (!loading)
        {
            if( scrollDirection == SCROLL_DIRECTION_DOWN && (totalItemCount - visibleItemCount)<=(firstVisibleItem + visibleThreshold)) {
                onLoadMore(currentPage + 1, totalItemCount);
                loading = true;
            }
            else if( scrollDirection == SCROLL_DIRECTION_UP && firstVisibleItem<=visibleThreshold && isUserScrolling) {
                onLoadMore(currentPage + 1, totalItemCount);
                loading = true;
            }
        }
    }

    public abstract void onLoadMore(int page, int totalItemsCount);

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) { }

    public int getScrollDirection() {
        return scrollDirection;
    }

    public void setScrollDirection(int scrollDirection) {
        if (scrollDirection == SCROLL_DIRECTION_DOWN || scrollDirection == SCROLL_DIRECTION_UP)
        { this.scrollDirection = scrollDirection; }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        isUserScrolling  = event.getAction() == MotionEvent.ACTION_MOVE;
        return false;
    }

    public boolean isLoading() {
        return loading;
    }

    public void finishedLoading() {
        this.loading = false;
    }

}