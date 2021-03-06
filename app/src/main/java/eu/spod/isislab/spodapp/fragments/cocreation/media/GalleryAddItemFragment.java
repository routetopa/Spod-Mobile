package eu.spod.isislab.spodapp.fragments.cocreation.media;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.services.SpodLocationService;
import eu.spod.isislab.spodapp.utils.DownloadImageTask;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.UserManager;

public class GalleryAddItemFragment extends Fragment implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener, Observer{

    public static final String TAG = "GalleryAddItemFragment";

    private static final int PHOTO_REQUEST_CODE = 1;
    private ImageView image;
    private Uri mImageUri;

    ViewGroup rootView;
    Bitmap bp = null;
    String sheetId;
    int rotation = 0, orientation = Configuration.ORIENTATION_PORTRAIT;

    CocreationMediaRoomGridFragment cocreationRoomGridFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        rootView = (ViewGroup) inflater.inflate(R.layout.gallery_add_item_fragment, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                rootView.findViewById(R.id.add_item_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        new DownloadImageTask((ImageView)rootView.findViewById(R.id.new_item_avatar))
                .execute(UserManager.getInstance().getAvatarImage());

        image = (ImageView)rootView.findViewById(R.id.new_item_image);
        image.setOnClickListener(this);

        ((EditText)rootView.findViewById(R.id.new_item_description)).setImeOptions(EditorInfo.IME_ACTION_DONE);
        ((EditText)rootView.findViewById(R.id.new_item_description)).setRawInputType(InputType.TYPE_CLASS_TEXT);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPhoto();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==PHOTO_REQUEST_CODE && resultCode == getActivity().RESULT_OK)
        {
            /*bp = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(bp);
            image.setScaleType(ImageView.ScaleType.FIT_XY);*/
            this.rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            this.orientation = getActivity().getResources().getConfiguration().orientation;
            this.grabImage(image);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
        NetworkChannel.getInstance().addObserver(this);
    }

    @Override
    public void onDestroy() {
        NetworkChannel.getInstance().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        getPhoto();
    }

    public void setCocreationRoomGridFragment(CocreationMediaRoomGridFragment cocreationRoomGridFragment) {
        this.cocreationRoomGridFragment = cocreationRoomGridFragment;
    }

    public void grabImage(ImageView imageView)
    {
        getActivity().getContentResolver().notifyChange(mImageUri, null);
        try
        {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager)getActivity(). getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            bp = BitmapFactory.decodeFile(mImageUri.getPath(), options);
            //bp = this.rotateBitmap(bp);

            if(bp.getByteCount() > mi.availMem){
                options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bp.compress(Bitmap.CompressFormat.JPEG, 0, bos);
            }else {
                imageView.setImageBitmap(bp);
            }
        }
        catch (Exception e)
        {
            Snackbar.make(rootView, "Failed to load the image!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public Bitmap rotateBitmap(Bitmap source)
    {
        int angle;
        switch (rotation) {
            case Surface.ROTATION_90:
                angle = -90;
                break;
            case Surface.ROTATION_180:
                angle = 180;
                break;
            case Surface.ROTATION_270:
                angle = 90;
                break;
            default:
                angle = 0;
                break;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void getPhoto()
    {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo;
        try
        {
            // place where to store camera taken picture
            photo = this.createTemporaryFile("picture", ".jpg");
            photo.delete();

            mImageUri = Uri.fromFile(photo);
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(photoIntent, PHOTO_REQUEST_CODE);
        }
        catch(Exception e)
        {
            Snackbar.make(rootView, "Please check SD card!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath() + "/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottom_bar_add_item_upload:
                String title       = ((TextView)rootView.findViewById(R.id.new_item_title)).getText().toString();
                String description = ((TextView)rootView.findViewById(R.id.new_item_description)).getText().toString();
                Location location  = SpodLocationService.getCurrentLocation();

                if(title.isEmpty() || description.isEmpty() || location == null){
                    Snackbar.make(getActivity().findViewById(R.id.container), "Please fill form correctly, maybe the location is not accessible, check it!!!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }else{
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String strDate = sdf.format(c.getTime());
                    //Log.e("ADDPHOTO", "Call network service");
                    NetworkChannel.getInstance().addRowToSheet(sheetId, title, description, strDate, bp);
                    getActivity().getSupportFragmentManager().popBackStack();
                }

                break;
        }
        return true;
    }

    @Override
    public void update(Observable o, Object response)
    {
        try {
            JSONObject result = new JSONObject((String)response);
            Boolean status = result.getBoolean("status");
            String message = result.getString("message");

            if(status){
                Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                cocreationRoomGridFragment.refreshData();
                //this.getActivity().getSupportFragmentManager().popBackStack();

            }else{
                Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
