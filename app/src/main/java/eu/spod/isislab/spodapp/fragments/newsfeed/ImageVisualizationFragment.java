package eu.spod.isislab.spodapp.fragments.newsfeed;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.entities.NewsfeedImageInfo;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class ImageVisualizationFragment extends Fragment implements Observer {

    public static final String FRAGMENT_NAME = "ImageVisualizationFragment";

    private static final String ARG_IMAGE_IDS = "image_ids";
    private static final String ARG_CURRENT_IMAGE = "current_image";
    private static final String ARG_CURRENT_IMAGE_URI = "current_image_uri";

    private static final String TAG = "ImageVisualizationFr";
    private String[] imageIds;
    private NewsfeedImageInfo currentImage;
    private Uri currentImageUri;

    private ImageView mImageContainer;
    private TextView mUserNameText;
    private TextView mAlbumNameText;
    private TextView mTimeText;
    private TextView mDescriptionText;
    private View mDescriptionContainer;
    private View mUpperContainer;

    private ActionBar mActionBar;

    private Map<String, NewsfeedImageInfo> mImages;

    private boolean mUiVisible;

    public ImageVisualizationFragment() {}

    public static ImageVisualizationFragment newInstance(Uri currentImageURI, NewsfeedImageInfo localImageInfo, String[] images) {
        ImageVisualizationFragment fragment = new ImageVisualizationFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CURRENT_IMAGE_URI, currentImageURI);
        args.putSerializable(ARG_CURRENT_IMAGE, localImageInfo);
        args.putStringArray(ARG_IMAGE_IDS, images);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageIds = getArguments().getStringArray(ARG_IMAGE_IDS);
            currentImage = (NewsfeedImageInfo) getArguments().getSerializable(ARG_CURRENT_IMAGE);
            currentImageUri = getArguments().getParcelable(ARG_CURRENT_IMAGE_URI);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_visualizator, container, false);

        mImageContainer = (ImageView) view.findViewById(R.id.image_visualizator_container);
        mUserNameText = (TextView) view.findViewById(R.id.image_visualizator_user_name_text);
        mAlbumNameText = (TextView) view.findViewById(R.id.image_visualizator_album_text);
        mTimeText = (TextView) view.findViewById(R.id.image_visualizator_time_text);
        mDescriptionText = (TextView) view.findViewById(R.id.image_visualizator_description_text);
        mDescriptionContainer = view.findViewById(R.id.image_visualizator_description_layout);
        mUpperContainer = view.findViewById(R.id.image_visualizator_upper_container);


        mImageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUiVisible) {
                    hideUI(true);
                } else {
                    showUI();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Color.BLACK);
        }

        return view;
    }

    private void showUI() {
        mUpperContainer.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(mDescriptionText.getText())) {
            mDescriptionContainer.setVisibility(View.VISIBLE);
        }

        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        mUiVisible = true;
    }

    private void hideUI(boolean immersive) {
        mDescriptionContainer.setVisibility(View.GONE);
        mUpperContainer.setVisibility(View.GONE);
        if(immersive) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
        mUiVisible = false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if(mActionBar != null) {
            mActionBar.hide();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if(currentImageUri != null) {
            loadImage(currentImageUri);
            hideUI(false);
        }

        if(currentImage != null) {
            fillInfoFields(currentImage);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showUI();
                }
            }, 300);
        }

        if (imageIds != null) {
            NetworkChannel.getInstance().addObserver(this);
            NetworkChannel.getInstance().getPhotos(imageIds);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        NetworkChannel.getInstance().deleteObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mActionBar != null) {
            mActionBar.show();
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d(TAG, "update: " + o.toString());

        boolean handled = true;

        if(Consts.NEWSFEED_SERVICE_GET_PHOTOS.equals(NetworkChannel.getInstance().getCurrentService())){
            JSONArray a;
            try {
                String errorMessage = NewsfeedJSONHelper.getErrorMessage(((String) o));
                if(errorMessage != null) {
                    return;
                }

                a = new JSONArray(((String) o));
                List<NewsfeedImageInfo> imageList = NewsfeedJSONHelper.createImageInfoList(a);
                createImagesMap(imageList);

                NewsfeedImageInfo toLoad = null;

                if(currentImage != null) {
                    toLoad = mImages.get(currentImage.getId());
                } else if(mImages.size() > 0){
                    toLoad = null;
                }

                if(toLoad != null) {
                    loadImage(Uri.parse(toLoad.getUrl()));
                    fillInfoFields(toLoad);
                    showUI();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                handled = false;
            }

            if(handled) {
                NetworkChannel.getInstance().deleteObserver(this);
            }
        }
    }


    private void createImagesMap(List<NewsfeedImageInfo> imageList) {
        mImages = new HashMap<>(imageList.size());

        for (NewsfeedImageInfo imageInfo : imageList) {
            mImages.put(String.valueOf(imageInfo.getId()), imageInfo);
        }
    }


    private void fillInfoFields(NewsfeedImageInfo image) {
        mUserNameText.setText(image.getUserName());
        mTimeText.setText(NewsfeedUtils.timeToString(getContext(), image.getTime()));

        if(image.getAlbumName() != null) {
            mAlbumNameText.setVisibility(View.VISIBLE);
            mAlbumNameText.setText(image.getAlbumName());
        } else {
            mAlbumNameText.setVisibility(View.GONE);
        }

        mDescriptionText.setText(NewsfeedUtils.htmlToSpannedText(image.getDescription()));
    }

    private void loadImage(Uri uri) {
        Glide.with(getContext())
                .load(uri)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        startPostponedTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        startPostponedTransition();
                        return false;
                    }
                })
                .apply(new RequestOptions().dontAnimate().dontTransform())
                .into(mImageContainer);
    }

    private void startPostponedTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().startPostponedEnterTransition();
        }
    }
}
