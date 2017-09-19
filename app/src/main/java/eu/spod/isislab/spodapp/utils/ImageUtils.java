package eu.spod.isislab.spodapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImageUtils
{
    private int IMAGE_SIZE_LIMIT = 1048576;
    private static ImageUtils ourInstance = new ImageUtils();
    public static ImageUtils getInstance() {
        return ourInstance;
    }

    private ImageUtils() {}

    public byte[] getByteImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public ByteArrayOutputStream compressPass(Bitmap bitmap, int sampleSize,int quality){
        ByteArrayOutputStream baos    = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        Bitmap bm = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        bm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos;
    }

    public byte[] compressBitmap(Bitmap bitmap, int sampleSize, int quality) {
        ByteArrayOutputStream baos = null;
        try {
            baos = compressPass(bitmap, sampleSize, quality);
            long lengthInByte = baos.toByteArray().length;
            while (lengthInByte > IMAGE_SIZE_LIMIT) {
                sampleSize *= 2;
                quality /= 4;
                baos = compressPass(bitmap, sampleSize, quality);
                lengthInByte = baos.toByteArray().length;
            }
            //bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

}
