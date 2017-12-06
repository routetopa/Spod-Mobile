package eu.spod.isislab.spodapp.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class CompressBitmapTask extends AsyncTask<Bitmap, Void, byte[]> {

    public interface ResultHandler {
        void onCompress(byte[] result);
    }

    private ResultHandler mListener;

    public CompressBitmapTask() {}

    public void setResultHandler(ResultHandler handler) {
        this.mListener = handler;
    }

    @Override
    protected byte[] doInBackground(Bitmap... bitmaps) {
        return ImageUtils.getInstance().compressBitmap(bitmaps[0], 1, 100);
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        this.mListener.onCompress(bytes);
    }


}
