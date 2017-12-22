package eu.spod.isislab.spodapp.fragments.newsfeed;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.utils.CompressBitmapTask;
import eu.spod.isislab.spodapp.entities.ContextActionMenuItem;
import eu.spod.isislab.spodapp.activities.FullscreenActivity;
import eu.spod.isislab.spodapp.entities.NewsfeedComment;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;
import eu.spod.isislab.spodapp.utils.NewsfeedUtils;
import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.NewsfeedCommentsAdapter;
import eu.spod.isislab.spodapp.adapters.NewsfeedPostsAdapter;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.UserManager;

public class PostCommentsFragment extends Fragment implements Observer, PopupMenu.OnMenuItemClickListener, NewsfeedCommentsAdapter.CommentsAdapterInteractionListener {

    private static final String TAG = "PostCommentsFragment";
    public static final String FRAGMAENT_NAME = "PostCommentsFragment";

    private static final int ACTIVITY_REQUEST_IMAGE_CHOSEN = 1;
    private static final int ACTIVITY_REQUEST_PHOTO_TAKEN = 2;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mCommentsList;

    private NewsfeedCommentsAdapter mNewsfeedCommentsAdapter;
    private ImageView userImageView;
    private Button sendCommentButton;
    private EditText commentTextEditor;
    private RelativeLayout noCommentsLayout;
    private ImageButton addAttachmentButton;
    private ImageView mAttachmentImageView;

    private Context mContext;

    private int mCurrentEntityId;
    private String mCurrentEntityType;
    private String mCurrentPluginKey;
    private int mCurrentOwnerId;

    private Uri currentAttachedImageUri = null;

    private int mCurrentPage = -1;
    private int mPageItemCount = NewsfeedUtils.DEFAULT_ITEM_PER_PAGE_COUNT;
    private boolean isLoadingPosts = false;

    private static final String ARG_ENTITY_TYPE = "eu.spod.isislab.spodapp.PostCommentFragment.ARGUMENT_ENTITY_TYPE";
    private static final String ARG_ENTITY_ID   = "eu.spod.isislab.spodapp.PostCommentFragment.ARGUMENT_ENTITY_ID";
    private static final String ARG_PLUGIN_KEY  = "eu.spod.isislab.spodapp.PostCommentFragment.ARG_PLUGIN_KEY";
    private static final String ARG_OWNER_ID    = "eu.spod.isislab.spodapp.PostCommentFragment.ARG_OWNER_ID";

    public PostCommentsFragment() {
    }


    public static PostCommentsFragment newInstance(String entityType, int entityId, String pluginKey, int ownerId) {
        PostCommentsFragment frag = new PostCommentsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ENTITY_ID, entityId);
        args.putString(ARG_ENTITY_TYPE, entityType);
        args.putString(ARG_PLUGIN_KEY, pluginKey);
        args.putInt(ARG_OWNER_ID, ownerId);
        frag.setArguments(args);
        return frag;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View contentView = inflater.inflate(R.layout.fragment_postcomments_list,  container, false);

        mContext = getContext();

        mCommentsList = (RecyclerView) contentView.findViewById(R.id.newsfeed_comments_container);
        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.newsfeed_postcomments_swipe_refresh_layout);
        userImageView = (ImageView) contentView.findViewById(R.id.newsfeed_comment_editor_user_image);
        sendCommentButton = (Button) contentView.findViewById(R.id.newsfeed_comment_editor_send_button);
        commentTextEditor = (EditText) contentView.findViewById(R.id.newsfeed_comment_editor_edit_text);
        noCommentsLayout = (RelativeLayout) contentView.findViewById(R.id.newsfeed_no_comments_layout);
        addAttachmentButton = (ImageButton) contentView.findViewById(R.id.newsfeed_comment_editor_add_attachment_button);
        mAttachmentImageView = (ImageView) contentView.findViewById(R.id.newsfeed_comment_editor_attachment_image_view);

        RecyclerView.LayoutManager lm = new LinearLayoutManager(mContext);

        mCommentsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy == 0) { //sometimes onScrolled is called on other layout changes
                    return;
                }

                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int lastItemPosition = recyclerView.getAdapter().getItemCount() - 1;

                if(lastVisibleItemPosition >= lastItemPosition && lastItemPosition > 0) {
                    if (!isLoadingPosts) {
                        loadComments(mCurrentEntityType, mCurrentEntityId, false);
                    }
                }
            }


        });


        mNewsfeedCommentsAdapter = new NewsfeedCommentsAdapter(this.mContext);
        mNewsfeedCommentsAdapter.setInteractionListener(this);
        mNewsfeedCommentsAdapter.setHasFooter(false);

        mCommentsList.setLayoutManager(lm);
        mCommentsList.setAdapter(mNewsfeedCommentsAdapter);

        mCurrentEntityId = getArguments().getInt(ARG_ENTITY_ID);
        mCurrentEntityType = getArguments().getString(ARG_ENTITY_TYPE);
        mCurrentPluginKey =  getArguments().getString(ARG_PLUGIN_KEY);
        mCurrentOwnerId = getArguments().getInt(ARG_OWNER_ID);

        initCommentEditor();

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadComments(mCurrentEntityType, mCurrentEntityId, true);
            }
        });
        if(NewsfeedUtils.isDefaultAvatar(UserManager.getInstance().getAvatarImage())) {
            userImageView.setImageDrawable(NewsfeedUtils.getTextDrawableForUser(mContext, Integer.parseInt(UserManager.getInstance().getId()), UserManager.getInstance().getName()));
        } else {
            Glide.with(mContext)
                    .load(UserManager.getInstance().getAvatarImage())
                    .apply(new RequestOptions()
                            .placeholder(NewsfeedUtils.getTextDrawableForUser(mContext, Integer.parseInt(UserManager.getInstance().getId()), UserManager.getInstance().getName()))
                            .circleCrop())
                    .into(userImageView);
        }
        return contentView;
    }

    private void initCommentEditor() {

        final PopupMenu attachmentMenu = new PopupMenu(mContext, addAttachmentButton);
        attachmentMenu.inflate(R.menu.newsfeed_photo_attachment_add);
        attachmentMenu.setOnMenuItemClickListener(this);
        addAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachmentMenu.show();
            }
        });

        sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComment();
            }
        });

        commentTextEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                refreshSendButtonState();
            }
        });

        commentTextEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                refreshSendButtonState();
            }
        });

        mAttachmentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageVisualizator(currentAttachedImageUri, ((ImageView) view));
            }
        });

        mAttachmentImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == DialogInterface.BUTTON_POSITIVE) {
                            currentAttachedImageUri = null;
                            mAttachmentImageView.setImageDrawable(null);
                            mAttachmentImageView.setVisibility(View.GONE);
                            refreshSendButtonState();
                        } else {
                            dialogInterface.dismiss();
                        }
                    }
                };

                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setMessage(NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_delete_attachment_message))
                        .setPositiveButton(
                                NewsfeedUtils.getStringResource(mContext, R.string.yes),
                                onClickListener)
                        .setNegativeButton(
                                NewsfeedUtils.getStringResource(mContext, R.string.no),
                                onClickListener
                        );

                alert.create().show();
                return true;
            }
        });
    }

    private void refreshSendButtonState() {
        if (TextUtils.isEmpty(commentTextEditor.getText().toString()) && currentAttachedImageUri == null) {
            sendCommentButton.setEnabled(false);
        } else {
            sendCommentButton.setEnabled(true);
        }

        if(commentTextEditor.hasFocus() || sendCommentButton.isEnabled()) {
            sendCommentButton.setVisibility(View.VISIBLE);
        } else {
            sendCommentButton.setVisibility(View.GONE);
        }
    }

    private void resetCommentEditor() {
        commentTextEditor.setText("");
        commentTextEditor.clearFocus();

        sendCommentButton.setVisibility(View.GONE);

        mAttachmentImageView.setImageDrawable(null);
        mAttachmentImageView.setVisibility(View.GONE);
        currentAttachedImageUri = null;
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.toggleSoftInput(0, 0);
        }
    }

    private void loadComments(String entityType, int entityId, boolean reset) {
        isLoadingPosts = true;

        if(reset) {
            mSwipeRefreshLayout.setRefreshing(true);
            mNewsfeedCommentsAdapter.clearList();
            mNewsfeedCommentsAdapter.setHasFooter(false);
            mCurrentPage = -1;
        }

        mCurrentPage++;

        NetworkChannel.getInstance().addObserver(this);
        NetworkChannel.getInstance().getPostComments(entityType, entityId, mCurrentPage, mPageItemCount);
    }

    private void sendComment() {
        final String comment = commentTextEditor.getText().toString();
        if(TextUtils.isEmpty(comment)){
            commentTextEditor.setError(NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_empty_field));
            return;
        }

        String fileName;
        if (currentAttachedImageUri != null) {
            final ProgressDialog loading = ProgressDialog.show(mContext, "SPOD Mobile", mContext.getString(R.string.wait_network_message), false, false);
            try {
                String path = NewsfeedUtils.uriToPath(mContext, currentAttachedImageUri);
                Bitmap bitmap = NewsfeedUtils.loadBitmap(getContext(), path);

                if(bitmap == null) {
                    throw new IOException();
                }

                int index = path.lastIndexOf(File.separatorChar);
                fileName = path.substring(index + 1);

                CompressBitmapTask compressTask = new CompressBitmapTask();
                final String finalFileName = fileName;
                compressTask.setResultHandler(new CompressBitmapTask.ResultHandler() {
                    @Override
                    public void onCompress(byte[] result) {
                        loading.dismiss();
                        NetworkChannel.getInstance().addObserver(PostCommentsFragment.this);
                        NetworkChannel.getInstance().addComment(mCurrentEntityType, mCurrentEntityId, mCurrentPluginKey, mCurrentOwnerId, comment, result, finalFileName);
                    }
                });

                compressTask.execute(bitmap);
                bitmap = null;
            } catch (IOException e) {
                loading.dismiss();
                Log.e(TAG, "sendComment: " + e.getMessage(), e);
                Toast.makeText(mContext, "Failed image loading", Toast.LENGTH_SHORT).show(); //TODO: add to string.xml
                return;
            }
        } else {
            NetworkChannel.getInstance().addObserver(PostCommentsFragment.this);
            NetworkChannel.getInstance().addComment(mCurrentEntityType, mCurrentEntityId, mCurrentPluginKey, mCurrentOwnerId, comment, null, null);
        }

        resetCommentEditor();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadComments(mCurrentEntityType, mCurrentEntityId, true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: " + resultCode);
        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == ACTIVITY_REQUEST_IMAGE_CHOSEN) {
            currentAttachedImageUri = data.getData();
        }

        if (currentAttachedImageUri != null) {
            Glide.with(mContext)
                    .load(currentAttachedImageUri)
                    .into(mAttachmentImageView);
            mAttachmentImageView.setVisibility(View.VISIBLE);
        }
        refreshSendButtonState();
    }

    @Override
    public void update(Observable observable, Object o) {

        Log.d(TAG, "update: " + o);
        boolean handled = false;
        switch (NetworkChannel.getInstance().getCurrentService()) {
            case Consts.NEWSFEED_SERVICE_GET_POST_COMMENTS:
                mSwipeRefreshLayout.setRefreshing(false);
                ArrayList<NewsfeedComment> commentsList = null;
                try {
                    JSONArray comments = new JSONArray((String) o);
                    commentsList = NewsfeedJSONHelper.createCommentsList(comments);
                } catch (JSONException e) {
                    Log.e(TAG, "Error on response parsing", e);
                }

                if(commentsList != null && commentsList.size() == 0) {
                    mNewsfeedCommentsAdapter.setHasFooter(false);
                    mNewsfeedCommentsAdapter.notifyItemChanged(mNewsfeedCommentsAdapter.getItemCount());
                }
                if (commentsList != null && commentsList.size() > 0) {
                    mNewsfeedCommentsAdapter.appendData(commentsList);
                    if(commentsList.size() >= mPageItemCount) {
                        mNewsfeedCommentsAdapter.setHasFooter(true);
                    }
                } else if(mNewsfeedCommentsAdapter.getItemCount() == 0) {
                    mCommentsList.setVisibility(View.GONE);
                    noCommentsLayout.setVisibility(View.VISIBLE);
                }

                isLoadingPosts = false;
                handled = true;
                break;
            case Consts.NEWSFEED_SERVICE_ADD_COMMENT:
                try {
                    JSONObject response = new JSONObject((String) o);
                    NewsfeedComment comment = NewsfeedJSONHelper.createComment(response);
                    if(mCommentsList.getVisibility() == View.GONE) {
                        mCommentsList.setVisibility(View.VISIBLE);
                        noCommentsLayout.setVisibility(View.GONE);
                    }
                    mNewsfeedCommentsAdapter.addBottom(comment);
                    mCommentsList.scrollToPosition(mCommentsList.getAdapter().getItemCount() - 1);
                    updateNewsfeedFragment(mCurrentEntityType, mCurrentEntityId);
                } catch (JSONException e) {
                    Log.e(TAG, "Error on response parsing", e);
                }
                handled = true;
                break;
            case Consts.NEWSFEED_SERVICE_DELETE_COMMENT:
                updateNewsfeedFragment(mCurrentEntityType, mCurrentEntityId);
                break;
        }
        if (handled) {
            NetworkChannel.getInstance().deleteObserver(this);
        }
    }

    private void updateNewsfeedFragment(String entityType, int entityId) {
        ((NewsfeedFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(NewsfeedFragment.FRAGMENT_NAME))
                .refreshPost(entityType, entityId, NewsfeedPostsAdapter.AdapterUpdateType.COMMENTS);
    }

    private void openImageVisualizator(Uri uri, ImageView view) {
        Intent intent = new Intent(mContext, FullscreenActivity.class);
        intent.putExtra(FullscreenActivity.URI_ARGUMENT, uri);
        intent.putExtra(FullscreenActivity.FRAGMENT_TYPE_ARGUMENT, FullscreenActivity.FRAGMENT_TYPE_IMAGE);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, NewsfeedUtils.getStringResource(getActivity(), R.string.newsfeed_image_transition_name));

        startActivity(intent, options.toBundle());
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newsfeed_photo_attachment_add_menu_choose:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, NewsfeedUtils.getStringResource(mContext, R.string.newsfeed_choose_image)), ACTIVITY_REQUEST_IMAGE_CHOSEN);
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
                    currentAttachedImageUri = Uri.fromFile(tmpPhoto);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentAttachedImageUri);
                    try {
                        startActivityForResult(captureIntent, ACTIVITY_REQUEST_PHOTO_TAKEN);
                    }catch (ActivityNotFoundException e) {
                        Toast.makeText(mContext, R.string.newsfeed_no_camera_app, Toast.LENGTH_LONG).show();
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }


    //ADAPTER INTERACTION LISTENER
    @Override
    public void onImageClicked(NewsfeedComment c, ImageView view) {
        String url = c.getAttachment().get(NewsfeedJSONHelper.URL);
        Uri uri = Uri.parse(url);
        openImageVisualizator(uri, view);
    }

    @Override
    public void onContextActionMenuItemClicked(final NewsfeedComment comment, ContextActionMenuItem.ContextActionType actionType) {
        final ContextActionMenuItem item = comment.getContextActionMenuItem(actionType);

        switch (item.getActionType()) {
            case DELETE_COMMENT:
                new AlertDialog.Builder(this.getContext())
                        .setMessage(R.string.newsfeed_delete_confirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NetworkChannel.getInstance().deleteComment(item.getParams());
                                NetworkChannel.getInstance().addObserver(PostCommentsFragment.this);
                                mNewsfeedCommentsAdapter.deleteItemAtPosition(mNewsfeedCommentsAdapter.findItemPosition(comment.getId()));
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                break;
            case FLAG_CONTENT:
                final String[] reasonKeys = getResources().getStringArray(R.array.newsfeed_flag_reason);
                final String[] reasonsLabel = new String[reasonKeys.length];

                for(int i = 0; i<reasonKeys.length; i++) {
                    reasonsLabel[i] = NewsfeedUtils.getStringByResourceName(getContext(), getActivity().getPackageName(), "newsfeed_", reasonKeys[i]);
                }

                final ArrayAdapter<String> chooseList = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_list_item_1,
                        reasonsLabel
                );
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.newsfeed_select_reason)
                        .setSingleChoiceItems(chooseList, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String reason = reasonKeys[i];
                                dialogInterface.dismiss();
                                NetworkChannel.getInstance().flagContent(item.getParams(), reason);
                                NetworkChannel.getInstance().addObserver(PostCommentsFragment.this);
                            }
                        })
                        .show();
                break;
        }

    }

    @Override
    public void onDataletClicked(NewsfeedComment c) {
        Log.d(TAG, "onDataletClicked: ");
        String dataletUrl = c.getAttachment().get("dataletUrl");

        if(dataletUrl != null) {
            Intent intent = new Intent(getContext(), FullscreenActivity.class);
            Log.d(TAG, "onClick: sending url:" + dataletUrl);
            intent.putExtra(FullscreenActivity.FRAGMENT_TYPE_ARGUMENT, FullscreenActivity.FRAGMENT_TYPE_DATALET);
            intent.putExtra(FullscreenActivity.URI_ARGUMENT, dataletUrl);

            startActivity(intent);
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public void onLinkClicked(String href) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(href));
        startActivity(browserIntent);
    }

}
