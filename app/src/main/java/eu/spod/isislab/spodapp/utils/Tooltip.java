package eu.spod.isislab.spodapp.utils;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import eu.spod.isislab.spodapp.R;

public class Tooltip {

    private Context mContext;
    private View mAnchor;
    private PopupWindow mTooltip;
    private String mTip;

    private TextView textView;
    private ImageView upArrow;
    private ImageView downArrow;
    private View tooltipLayout;

    private View mRootView;

    public static Tooltip create(Context context) {
        return new Tooltip(context);
    }

    private Tooltip(Context context) {
        this.mContext = context;
        mTooltip = new PopupWindow(mContext);
    }

    public Tooltip on(View anchor) {
        mAnchor = anchor;
        return this;
    }

    public Tooltip rootView(View root) {
        mRootView = root;
        return this;
    }

    public Tooltip onDismiss(PopupWindow.OnDismissListener listener) {
        this.mTooltip.setOnDismissListener(listener);
        return this;
    }

    public Tooltip tip(String tip){
        mTip = tip;
        return this;
    }

    public Tooltip tip(int resource) {
        mTip = mContext.getString(resource);
        return this;
    }

    private void setupWindow() {
        tooltipLayout = LayoutInflater.from(mContext).inflate(R.layout.newsfeed_tooltip_baloon, null, false);

        textView = (TextView) tooltipLayout.findViewById(R.id.newsfeed_tooltip_text);
        upArrow = (ImageView) tooltipLayout.findViewById(R.id.newsfeed_tooltip_arrow_up);
        downArrow = (ImageView) tooltipLayout.findViewById(R.id.newsfeed_tooltip_arrow_down);

        textView.setText(mTip);

        mTooltip.setContentView(tooltipLayout);

        mTooltip.setOutsideTouchable(true);
        mTooltip.setBackgroundDrawable(new ColorDrawable());
        mTooltip.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mTooltip.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mTooltip.setClippingEnabled(false);
        mTooltip.setTouchable(true);
        mTooltip.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mTooltip.dismiss();
                return true;
            }
        });
    }

    public void show() {
        if(mAnchor == null){
            return;
        }

        mAnchor.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                createView();
                mAnchor.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void createView() {
        if(mAnchor == null) {
            return;
        }
        setupWindow();
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();

        int screenHeight = dm.heightPixels;
        int screenWidth = dm.widthPixels;

        int[] location = new int[2];
        mAnchor.getLocationOnScreen(location);

        Rect rect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1] + mAnchor.getHeight());

        boolean onTop = (rect.top > screenHeight / 2);

        View arrow = onTop ? downArrow : upArrow;
        View hideArrow = onTop ? upArrow : downArrow;
        hideArrow.setVisibility(View.GONE);

        tooltipLayout.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        int tooltipWidth = tooltipLayout.getMeasuredWidth();
        int tooltipHeight = tooltipLayout.getMeasuredHeight();
        int arrowWidth = arrow.getMeasuredWidth();

        int yPos;
        int xPos;

        if(onTop) {
            yPos = rect.top - tooltipHeight;
        } else {
            yPos = rect.bottom;
        }

        int anchorCenterX = rect.centerX();

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) arrow.getLayoutParams();

        xPos = anchorCenterX - (tooltipWidth / 2);

        if(xPos < 0) {
            xPos = rect.left;
        } else if(xPos > screenWidth) {
            xPos = screenWidth - tooltipWidth;
        }

        int leftMargin = 10 + anchorCenterX - xPos - (arrowWidth / 2);
        layoutParams.setMargins(leftMargin,0,0,0);
        arrow.setLayoutParams(layoutParams);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTooltip.setElevation(5.0f);
        }

        if(onTop) {
            mTooltip.setAnimationStyle(R.style.TopTooltipAnimation);
        } else {
            mTooltip.setAnimationStyle(R.style.BottomTooltipAnimation);
        }

        View root = mRootView == null ? mAnchor : mRootView;
        mTooltip.showAtLocation(root, Gravity.NO_GRAVITY, xPos, yPos);
        mTooltip.update();
    }
}
