package eu.spod.isislab.spodapp.fragments.newsfeed;


import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.NewsfeedImageInfo;
import eu.spod.isislab.spodapp.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.NewsfeedUtils;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class ImageVisualizationFragment extends Fragment implements Observer {

    public static final String FRAGMENT_NAME = "ImageVisualizationFragment";

    private static final String ARG_IMAGE_IDS = "image_ids";
    private static final String ARG_CURRENT_IMAGE_URI = "current_image_url";
    private static final String ARG_CURRENT_IMAGE_ID = "current_image_id";

    private static final String TAG = "ImageVisualizationFr";
    private int[] imageIds;
    private Uri uri;
    private int currentImageId;

    private ImageView mImageContainer;
    private TextView mUserNameText;
    private TextView mAlbumNameText;
    private TextView mTimeText;
    private TextView mDescriptionText;
    private View mDescriptionContainer;
    private View mUpperContainer;

    private ActionBar mActionBar;

    private SparseArray<NewsfeedImageInfo> mImages;

    private boolean mUiVisible;

    public ImageVisualizationFragment() {}

    public static ImageVisualizationFragment newInstance(int currentImageId, Uri uri, int[] images) {
        ImageVisualizationFragment fragment = new ImageVisualizationFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CURRENT_IMAGE_URI, uri);
        args.putIntArray(ARG_IMAGE_IDS, images);
        args.putInt(ARG_CURRENT_IMAGE_ID, currentImageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageIds = getArguments().getIntArray(ARG_IMAGE_IDS);
            uri = getArguments().getParcelable(ARG_CURRENT_IMAGE_URI);
            currentImageId = getArguments().getInt(ARG_CURRENT_IMAGE_ID);
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }*/
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
                    hideUI();
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

    private void hideUI() {
        mDescriptionContainer.setVisibility(View.GONE);
        mUpperContainer.setVisibility(View.GONE);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
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

        if(uri != null) {
            loadImage(uri);
            hideUI();
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
        //TODO: implementare la richiesta di multiple foto con view pager
        Log.d(TAG, "update: " + o.toString());

        boolean handled = true;

        if(NetworkChannel.NEWSFEED_SERVICE_GET_PHOTOS.equals(NetworkChannel.getInstance().getCurrentService())){
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

                if(currentImageId >= 0) {
                    toLoad = mImages.get(currentImageId);
                } else if(mImages.size() > 0){
                    toLoad = mImages.valueAt(0);
                }

                if(toLoad != null) {
                    loadImage(Uri.parse(toLoad.getUrl()));
                    fillInfoFields(toLoad);
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
        mImages = new SparseArray<>(imageList.size());

        for (NewsfeedImageInfo imageInfo : imageList) {
            mImages.put(imageInfo.getId(), imageInfo);
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

        mDescriptionText.setText(image.getDescription());
        showUI();
    }

    private void loadImage(Uri uri) {
        Glide.with(getContext())
                .load(uri)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        startPostponedTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        startPostponedTransition();
                        return false;
                    }
                })
                .dontAnimate()
                .dontTransform()
                .into(mImageContainer);
    }

    private void startPostponedTransition() {
        /*mImageContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mImageContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().startPostponedEnterTransition();
                }
                return true;
            }
        });*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().startPostponedEnterTransition();
        }
    }
}
