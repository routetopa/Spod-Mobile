package eu.spod.isislab.spodapp.activities;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.NewsfeedImageInfo;
import eu.spod.isislab.spodapp.fragments.newsfeed.DataletVisualizationFragment;
import eu.spod.isislab.spodapp.fragments.newsfeed.ImageVisualizationFragment;

public class FullscreenActivity extends AppCompatActivity {


    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;


    public static final String FRAGMENT_TYPE_ARGUMENT = "eu.spod.isislab.spodapp.FullscreenActivity.fragment_type_argument";
    public static final String URI_ARGUMENT = "eu.spod.isislab.spodapp.FullscreenActivity.uri_argument";
    public static final String IMAGES_ARGUMENT = "eu.spod.isislab.spodapp.FullscreenActivity.image_argument";
    public static final String CURRENT_IMAGE_INFO_ARGUMENT = "eu.spod.isislab.spodapp.FullscreenActivity.current_image_info_argument";

    public static final int FRAGMENT_TYPE_IMAGE = 0;
    public static final int FRAGMENT_TYPE_DATALET = 1;

    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            mContentView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private boolean mFullscreenModeEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.activity_image_visualization_fragment_container);

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }*/

        supportPostponeEnterTransition();

        int fragmentType = getIntent().getIntExtra(FRAGMENT_TYPE_ARGUMENT, -1);
        Fragment fragment = null;
        String backstackString = null;
        switch (fragmentType) {
            case FRAGMENT_TYPE_IMAGE:
                mFullscreenModeEnabled = false;

                NewsfeedImageInfo currentImage = (NewsfeedImageInfo) getIntent().getSerializableExtra(CURRENT_IMAGE_INFO_ARGUMENT);
                String[] images = getIntent().getStringArrayExtra(IMAGES_ARGUMENT);
                Uri imageUrl = getIntent().getParcelableExtra(URI_ARGUMENT);
                fragment = ImageVisualizationFragment.newInstance(imageUrl, currentImage, images);
                backstackString = ImageVisualizationFragment.FRAGMENT_NAME;
                break;
            case FRAGMENT_TYPE_DATALET:
                mFullscreenModeEnabled = true;
                String url = getIntent().getStringExtra(URI_ARGUMENT);
                fragment = DataletVisualizationFragment.newInstance(url);
                backstackString = "datalet_viusualizator";
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.activity_image_visualization_fragment_container, fragment, backstackString)
                .commit();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        if(mFullscreenModeEnabled) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            mControlsView.setVisibility(View.GONE);
            mVisible = false;

            // Schedule a runnable to remove the status and navigation bar after a delay
            mHideHandler.removeCallbacks(mShowPart2Runnable);
            mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
        }
    }

    private void show() {
        if(mFullscreenModeEnabled) {
            // Show the system bar
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            mVisible = true;

            // Schedule a runnable to display UI elements after a delay
            mHideHandler.removeCallbacks(mHidePart2Runnable);
            mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
        }
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        if(mFullscreenModeEnabled) {
            mHideHandler.removeCallbacks(mHideRunnable);
            mHideHandler.postDelayed(mHideRunnable, delayMillis);
        }
    }
}
