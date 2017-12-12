package eu.spod.isislab.spodapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.graphics.Palette;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.LinkedList;
import java.util.List;

public class GlidePalette implements RequestListener<Drawable>{

    static class PaletteCache {

        private static final PaletteCache ourInstance = new PaletteCache();
        static PaletteCache getInstance() {
            return ourInstance;
        }

        private final LruCache<String, Palette> cache;

        private PaletteCache() {
            this.cache = new LruCache<>(20);
        }

        public Palette get(String key) {
            synchronized (cache) {
                return cache.get(key);
            }
        }

        public void put(String key, Palette palette) {
            synchronized (cache){
                cache.put(key, palette);
            }
        }

    }

    private class PaletteTarget<V> {
        private V targetView;
        private PaletteType paletteType;
        private ColorType colorType;

        PaletteTarget(V targetView, PaletteType paletteType, ColorType colorType) {
            this.targetView = targetView;
            this.paletteType = paletteType;
            this.colorType = colorType;
        }
    }

    private static final String TAG = "GlidePaletteColorized";

    private List<PaletteTarget<View>> backgroundTargetViews;
    private List<PaletteTarget<TextView>> textTargetViews;
    private RequestListener<Drawable> callback;



    private PaletteCache cache;
    public enum PaletteType{
        VIBRANT,
        VIBRANT_DARK,
        VIBRANT_LIGHT,
        MUTED,
        MUTED_DARK,
        MUTED_LIGHT


    }
    public enum ColorType{
        RGB,
        TITLE,
        BODY
    }

    public static GlidePalette painter() {
        return new GlidePalette();
    }

    private GlidePalette() {
        this.backgroundTargetViews = new LinkedList<>();
        this.textTargetViews = new LinkedList<>();
        this.cache = PaletteCache.getInstance();
    }


    public GlidePalette paintBackground(View target, PaletteType paletteType) {
        backgroundTargetViews.add(new PaletteTarget<>(target, paletteType, ColorType.RGB));
        return this;
    }

    public GlidePalette paintText(TextView target, PaletteType paletteType, ColorType colorType) {
        textTargetViews.add(new PaletteTarget<>(target, paletteType, colorType));
        return this;
    }

    public GlidePalette listener(RequestListener<Drawable> listener) {
        this.callback = listener;
        return this;
    }

    private void colorizeViews(Palette palette) {
        for (PaletteTarget<View> target : backgroundTargetViews) {
            Palette.Swatch requestedSwatch = getSwatchByType(palette, target.paletteType);
            crossFadeBackgroundColor(target.targetView, getColorByType(requestedSwatch, target.colorType));
        }

        for (PaletteTarget<TextView> target : textTargetViews) {
            Palette.Swatch requestedSwatch = getSwatchByType(palette, target.paletteType);
            if(requestedSwatch != null) {
                target.targetView.setTextColor(getColorByType(requestedSwatch, target.colorType));
            }
        }
    }

    private Palette.Swatch getSwatchByType(Palette palette, PaletteType request) {
        Palette.Swatch toReturn = null;

        switch (request) {
            case VIBRANT:
                toReturn = palette.getVibrantSwatch();
                break;
            case VIBRANT_DARK:
                toReturn = palette.getDarkVibrantSwatch();
                break;
            case VIBRANT_LIGHT:
                toReturn = palette.getLightVibrantSwatch();
                break;
            case MUTED:
                toReturn = palette.getMutedSwatch();
                break;
            case MUTED_DARK:
                toReturn = palette.getDarkMutedSwatch();
                break;
            case MUTED_LIGHT:
                toReturn = palette.getLightMutedSwatch();
                break;
        }

        return (toReturn == null) ? palette.getDominantSwatch() : toReturn;
    }

    private int getColorByType(Palette.Swatch swatch, ColorType colorType){
        switch (colorType) {
            case TITLE:
                return swatch.getTitleTextColor();
            case BODY:
                return swatch.getBodyTextColor();
            default:
                return swatch.getRgb();
        }
    }


    private void crossFadeBackgroundColor(View target, int newColor){
        Drawable old = target.getBackground();

        Drawable newDrawable = new ColorDrawable(newColor);
        Drawable[] drawables = new Drawable[] {old, newDrawable};
        TransitionDrawable transition = new TransitionDrawable(drawables);

        target.setBackground(transition);

        transition.startTransition(300);
    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
        return callback != null && callback.onLoadFailed(e, model, target, isFirstResource);
    }

    @Override
    public boolean onResourceReady(Drawable resource, final Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        Bitmap b;
        if(resource instanceof BitmapDrawable) {
            b = ((BitmapDrawable) resource).getBitmap();
        } else if(resource instanceof GifDrawable){
            b = ((GifDrawable) resource).getFirstFrame();
        } else {
            return callback != null && callback.onResourceReady(resource, model, target, dataSource, isFirstResource);
        }

        Palette cachedPalette = cache.get(model.toString());

        if(cachedPalette != null) { //cache hit
            colorizeViews(cachedPalette);
        } else { //cache miss
            Palette.from(b).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    cache.put(model.toString(), palette);
                    colorizeViews(palette);
                }
            });
        }

        return callback != null && callback.onResourceReady(resource, model, target, dataSource, isFirstResource);
    }

}
