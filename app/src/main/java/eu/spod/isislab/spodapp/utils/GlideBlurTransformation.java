package eu.spod.isislab.spodapp.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class GlideBlurTransformation extends BitmapTransformation{

    private RenderScript renderScript;
    private float radius;

    public GlideBlurTransformation(Context ctx, float radius) {
        this.renderScript = RenderScript.create(ctx);
        this.radius = radius;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Bitmap blurred = toTransform.copy(Bitmap.Config.ARGB_8888, true);

            Allocation input = Allocation.createFromBitmap(renderScript, blurred);
            Allocation output = Allocation.createTyped(renderScript, input.getType());
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

            blur.setInput(input);
            blur.setRadius(radius);
            blur.forEach(output);

            output.copyTo(blurred);

            return blurred;
        }
        return toTransform;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
