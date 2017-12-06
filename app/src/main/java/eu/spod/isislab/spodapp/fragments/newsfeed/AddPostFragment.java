package eu.spod.isislab.spodapp.fragments.newsfeed;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

import eu.spod.isislab.spodapp.utils.NewsfeedUtils;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.utils.UserManager;

public class AddPostFragment extends DialogFragment implements PopupMenu.OnMenuItemClickListener {

    public static final String EXTRA_DATA_POST_ATTACHMENT_URI = "eu.spod.isislab.spodapp.fragments.AddPostFragment.POST_TITLE_EXTRA";
    public static final String EXTRA_DATA_POST_MESSAGE = "eu.spod.isislab.spodapp.fragments.AddPostFragment.POST_BODY_EXTRA";

    public static final int ADD_POST_REQUEST_CODE = 999;
    private static final int ACTIVITY_REQUEST_IMAGE_CHOSEN = 1;
    private static final int ACTIVITY_REQUEST_PHOTO_TAKEN = 2;

    private EditText mMessageEditText;
    private ImageButton mAddAttachmentButton;
    private ImageView mAttachmentImageView;

    private ImageView mUserImageView;
    private TextView mUserNameTextView;

    private Uri mCurrentAttachmentUri = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_add_post, null);

        mMessageEditText = (EditText) v.findViewById(R.id.newsfeed_add_post_content_text);
        mAddAttachmentButton = (ImageButton) v.findViewById(R.id.newsfeed_add_post_add_attachment_button);
        mAttachmentImageView = (ImageView) v.findViewById(R.id.newsfeed_add_post_add_attachment_imageview);

        mUserImageView = (ImageView) v.findViewById(R.id.newsfeed_add_post_header_user_image);
        mUserNameTextView = (TextView) v.findViewById(R.id.newsfeed_add_post_header_user_name);

        final PopupMenu menu = new PopupMenu(getContext(), mAddAttachmentButton);
        MenuInflater menuInflater = menu.getMenuInflater();

        menuInflater.inflate(R.menu.newsfeed_photo_attachment_add, menu.getMenu());
        menu.setOnMenuItemClickListener(this);

        mAddAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.show();
            }
        });

        Glide.with(getContext())
                .load(UserManager.getInstance().getAvatarImage())
                .placeholder(R.drawable.user_placeholder)
                .into(mUserImageView);


        mUserNameTextView.setText(UserManager.getInstance().getUsername());
        dialogBuilder.setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_DATA_POST_ATTACHMENT_URI, mCurrentAttachmentUri);
                        intent.putExtra(EXTRA_DATA_POST_MESSAGE, mMessageEditText.getText().toString());
                        dialogInterface.dismiss();
                        getTargetFragment().onActivityResult(ADD_POST_REQUEST_CODE, Activity.RESULT_OK, intent);
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
                    startActivityForResult(captureIntent, ACTIVITY_REQUEST_PHOTO_TAKEN);
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
            Glide.with(getContext())
                    .load(mCurrentAttachmentUri)
                    .into(mAttachmentImageView);
            mAttachmentImageView.setVisibility(View.VISIBLE);
            mAddAttachmentButton.setVisibility(View.GONE);
        }
    }
}

