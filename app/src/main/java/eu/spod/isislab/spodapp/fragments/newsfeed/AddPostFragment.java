package eu.spod.isislab.spodapp.fragments.newsfeed;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.adapters.NewsfeedImageChooserAdapter;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.Tooltip;
import eu.spod.isislab.spodapp.utils.UserManager;

public class AddPostFragment extends DialogFragment implements PopupMenu.OnMenuItemClickListener, Observer {

    private static final String TAG = "AddPostFragment";

    public static final String EXTRA_DATA_POST_ATTACHMENT_URI = "eu.spod.isislab.spodapp.fragments.AddPostFragment.EXTRA_DATA_POST_ATTACHMENT_URI";
    public static final String EXTRA_DATA_POST_LINK_CONTENT = "eu.spod.isislab.spodapp.fragments.AddPostFragment.EXTRA_DATA_POST_LINK_CONTENT";
    public static final String EXTRA_DATA_POST_MESSAGE = "eu.spod.isislab.spodapp.fragments.AddPostFragment.EXTRA_DATA_POST_MESSAGE";

    public static final String ARGUMENT_TYPE = "eu.spod.isislab.spodapp.fragments.AddPostFragment.ARGUMENT_TYPE";

    public static final String ARGUMENT_INPUT_LINK = "eu.spod.isislab.spodapp.fragments.AddPostFragment.ARGUMENT_INPUT_LINK";
    public static final String ARGUMENT_TYPE_FILE = "eu.spod.isislab.spodapp.fragments.AddPostFragment.ARGUMENT_TYPE_FILE";

    public static final String ARGUMENT_TYPE_LINK = "eu.spod.isislab.spodapp.fragments.AddPostFragment.ARGUMENT_TYPE_LINK";

    public static final int ADD_POST_FILE_REQUEST_CODE = 999;
    public static final int ADD_POST_LINK_REQUEST_CODE = 998;

    private static final int ACTIVITY_REQUEST_IMAGE_CHOSEN = 1;
    private static final int ACTIVITY_REQUEST_PHOTO_TAKEN = 2;

    private EditText mMessageEditText;
    private ImageButton mAddAttachmentButton;

    private ImageView mAttachmentImageView;
    private FrameLayout mAttachmentContainer;
    private View mAttachmentLink;

    private ImageView mUserImageView;
    private TextView mUserNameTextView;

    //FRAGMENT TYPE PHOTO
    private Uri mCurrentAttachmentUri = null;

    //FRAGMENT TYPE LINK
    private HashMap<String, String> mCurrentAttachmentMap;

    private String mFragmentType;
    public AddPostFragment() {
    }

    public static AddPostFragment getTextInstance() {
        AddPostFragment fragment = new AddPostFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_TYPE, ARGUMENT_TYPE_FILE);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static AddPostFragment getLinkInstance(String link) {
        AddPostFragment fragment = new AddPostFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_TYPE, ARGUMENT_TYPE_LINK);
        arguments.putString(ARGUMENT_INPUT_LINK, link);
        fragment.setArguments(arguments);
        return fragment;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_add_post, null);

        mFragmentType = getArguments().getString(ARGUMENT_TYPE, ARGUMENT_TYPE_FILE);

        mMessageEditText = (EditText) v.findViewById(R.id.newsfeed_add_post_content_text);
        mAddAttachmentButton = (ImageButton) v.findViewById(R.id.newsfeed_add_post_add_attachment_button);
        mAttachmentContainer = (FrameLayout) v.findViewById(R.id.newsfeed_add_post_add_attachment_container);
        mAttachmentLink = inflater.inflate(R.layout.newsfeed_attachment_link_view, mAttachmentContainer, false);

        mUserImageView = (ImageView) v.findViewById(R.id.newsfeed_add_post_header_user_image);
        mUserNameTextView = (TextView) v.findViewById(R.id.newsfeed_add_post_header_user_name);

        final PopupMenu menu = new PopupMenu(getContext(), mAddAttachmentButton);
        MenuInflater menuInflater = menu.getMenuInflater();

        menuInflater.inflate(R.menu.newsfeed_photo_attachment_add, menu.getMenu());
        menu.setOnMenuItemClickListener(this);

        mAttachmentImageView = new ImageView(getContext());
        mAttachmentImageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mAttachmentImageView.setMaxHeight(NewsfeedUtils.pxToDp(getContext(), 400));


        if(ARGUMENT_TYPE_FILE.equals(mFragmentType)) {
            mAddAttachmentButton.setVisibility(View.VISIBLE);
            mAddAttachmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.show();
                }
            });
            mAttachmentContainer.addView(mAttachmentImageView);
        }

        Glide.with(getContext())
                .load(UserManager.getInstance().getAvatarImage())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.user_placeholder)
                        .circleCrop())
                .transition(new DrawableTransitionOptions().crossFade())
                .into(mUserImageView);


        mUserNameTextView.setText(UserManager.getInstance().getUsername());

        dialogBuilder.setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AddPostFragment.this.getDialog().cancel();
                    }
                });

        mMessageEditText.requestFocus();

        return dialogBuilder.create();
    }

    private void sendResult() {
        Intent intent = new Intent();
        if(ARGUMENT_TYPE_FILE.equals(mFragmentType)) {
            intent.putExtra(EXTRA_DATA_POST_ATTACHMENT_URI, mCurrentAttachmentUri);
            intent.putExtra(EXTRA_DATA_POST_MESSAGE, mMessageEditText.getText().toString());
            getTargetFragment().onActivityResult(ADD_POST_FILE_REQUEST_CODE, Activity.RESULT_OK, intent);
        } else if(ARGUMENT_TYPE_LINK.equals(mFragmentType)) {
            intent.putExtra(EXTRA_DATA_POST_MESSAGE, mMessageEditText.getText().toString());
            intent.putExtra(EXTRA_DATA_POST_LINK_CONTENT, mCurrentAttachmentMap);
            getTargetFragment().onActivityResult(ADD_POST_LINK_REQUEST_CODE, Activity.RESULT_OK, intent);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(ARGUMENT_TYPE_LINK.equals(mFragmentType)) {
            String link = getArguments().getString(ARGUMENT_INPUT_LINK);
            if(link != null) {
                mMessageEditText.setText(link);
                mAttachmentContainer.addView(new ProgressBar(getContext()));
                NetworkChannel.getInstance().addObserver(this);
                NetworkChannel.getInstance().getLinkContent(link);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newsfeed_photo_attachment_add_menu_choose:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, NewsfeedUtils.getStringResource(getContext(), R.string.newsfeed_choose_image)), ACTIVITY_REQUEST_IMAGE_CHOSEN);
                return true;
            case R.id.newsfeed_photo_attachment_add_menu_camera:
                try {
                    File tempDir = getActivity().getExternalCacheDir();
                    if(tempDir == null) {
                        tempDir = getActivity().getCacheDir();
                    }
                    tempDir = new File(tempDir.getAbsolutePath());
                    File tmpPhoto = File.createTempFile("photo", ".jpg", tempDir);
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mCurrentAttachmentUri = Uri.fromFile(tmpPhoto);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentAttachmentUri);
                    try {
                        startActivityForResult(captureIntent, ACTIVITY_REQUEST_PHOTO_TAKEN);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getContext(), R.string.newsfeed_no_camera_app, Toast.LENGTH_LONG).show();
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == ACTIVITY_REQUEST_IMAGE_CHOSEN) {
            mCurrentAttachmentUri = data.getData();
        }

        if (mCurrentAttachmentUri != null) {
            Glide.with(this)
                    .load(mCurrentAttachmentUri)
                    .apply(new RequestOptions()
                            .centerInside())
                    .transition(new DrawableTransitionOptions()
                            .crossFade())
                    .into(mAttachmentImageView);
            mAttachmentImageView.setVisibility(View.VISIBLE);
            mAddAttachmentButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onDismiss(dialog);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(isDetached()){
            return;
        }
        String result = (String) arg;
        if(NetworkChannel.getInstance().getCurrentService().equals(Consts.NEWSFEED_SERVICE_GET_LINK_CONTENT)) {
            mAttachmentContainer.removeAllViews();
            String errorMessage = NewsfeedJSONHelper.getErrorMessage(result);
            if(errorMessage != null) {
                Log.e(TAG, errorMessage);
                return;
            }

            try {
                JSONObject linkContent = new JSONObject(result);
                mCurrentAttachmentMap = (HashMap<String, String>) NewsfeedJSONHelper.convertToMap(linkContent);

                if(!mCurrentAttachmentMap.containsKey(NewsfeedJSONHelper.TYPE)) {
                    mCurrentAttachmentMap = null;
                    return;
                }

                String type = mCurrentAttachmentMap.get(NewsfeedJSONHelper.TYPE);

                if(type.equals(NewsfeedJSONHelper.PHOTO)) {
                    String url = mCurrentAttachmentMap.get(NewsfeedJSONHelper.HREF);
                    mAttachmentContainer.addView(mAttachmentImageView);
                    Glide.with(getContext())
                            .load(url)
                            .into(mAttachmentImageView);
                } else if(type.equals(NewsfeedJSONHelper.LINK)) {
                    TextView linkTitle = (TextView) mAttachmentLink.findViewById(R.id.newsfeed_attachment_link_view_title);
                    TextView linkDescription = (TextView) mAttachmentLink.findViewById(R.id.newsfeed_attachment_link_view_description);
                    final ImageView linkImage = (ImageView) mAttachmentLink.findViewById(R.id.newsfeed_attachment_link_view_image);

                    String title = mCurrentAttachmentMap.get(NewsfeedJSONHelper.TITLE);
                    String description = mCurrentAttachmentMap.get(NewsfeedJSONHelper.DESCRIPTION);
                    final String thumbnail = mCurrentAttachmentMap.get(NewsfeedJSONHelper.THUMBNAIL_URL);

                    linkTitle.setText(title);
                    linkDescription.setText(NewsfeedUtils.truncateString(description, 150));

                    Glide.with(this)
                            .load(thumbnail)
                            .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_link_darker_gray_24dp))
                            .into(linkImage);

                    NewsfeedUtils.viewVisibleIfNotNull(title, linkTitle);
                    NewsfeedUtils.viewVisibleIfNotNull(description, linkDescription);
                    //NewsfeedUtils.viewVisibleIfNotNull(thumbnail, linkImage);

                    String allImages = mCurrentAttachmentMap.get(NewsfeedJSONHelper.ALL_IMAGES);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mAttachmentLink.setElevation(5f);
                    }
                    mAttachmentContainer.addView(mAttachmentLink);

                    if(allImages != null) {
                        final String[] strings = NewsfeedJSONHelper.convertToStringArray(new JSONArray(allImages));
                        linkImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final NewsfeedImageChooserFragment instance = NewsfeedImageChooserFragment.getInstance(strings);
                                instance.setOnItemClickListener(new NewsfeedImageChooserAdapter.ItemClickListener() {
                                    @Override
                                    public void onItemClick(String link) {
                                        mCurrentAttachmentMap.put(NewsfeedJSONHelper.THUMBNAIL_URL, link);
                                        Glide.with(getContext())
                                                .load(link)
                                                .into(linkImage);
                                        instance.dismiss();
                                    }
                                });
                                instance.show(getFragmentManager(), instance.getTag());
                            }
                        });

                        Tooltip.create(getContext())
                                .rootView(getDialog().getWindow().getDecorView())
                                .tip("Click here to choose an image")
                                .on(linkImage)
                                .show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mAttachmentContainer.setVisibility(View.GONE);
            }

            NetworkChannel.getInstance().deleteObserver(this);
        }
    }
}

