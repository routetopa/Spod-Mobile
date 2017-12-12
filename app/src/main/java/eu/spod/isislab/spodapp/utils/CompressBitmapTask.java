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
        byte[] compressBitmap;
        int sampleSize = 1;
        int quality = 100;
        while (true) {
            try {
                compressBitmap = ImageUtils.getInstance().compressBitmap(bitmaps[0], sampleSize, quality);
                break;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                sampleSize = sampleSize * 2;
                quality = (int)(quality / 1.2);
            }
        }

        bitmaps[0].recycle();
        return compressBitmap;
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        this.mListener.onCompress(bytes);
    }


}
