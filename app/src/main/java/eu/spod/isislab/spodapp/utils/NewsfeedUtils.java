package eu.spod.isislab.spodapp.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.IOException;
import java.util.regex.Pattern;

import eu.spod.isislab.spodapp.entities.ContextActionMenuItem;

public class NewsfeedUtils {

    public static final int GET_POST_LIMIT_NUMBER = 20;
    public static final int DEFAULT_ITEM_PER_PAGE_COUNT = 10;

    public static final String FEED_TYPE_MY = "my";
    public static final String FEED_TYPE_SITE = "site";

    private static Pattern mHtmlPattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>"); //for check if a string is a html string

    private static boolean isAndroidApiGreaterThan(int sdkInt) {
        return Build.VERSION.SDK_INT >= sdkInt;
    }

    public static int getColorResource(Context ctx, int id, @Nullable Resources.Theme theme) {
        return isAndroidApiGreaterThan(Build.VERSION_CODES.M)
                ? ctx.getResources().getColor(id, theme)
                : ctx.getResources().getColor(id);

    }

    public static int getColorResource(Context ctx, int id) {
        return getColorResource(ctx, id, null);
    }

    public static String getStringResource(Context ctx, int id) {
        return ctx.getResources().getString(id);
    }

    public static String getStringByResourceName(Context ctx, String packageName, String prefix, String key) {
        int stringId = ctx.getResources().getIdentifier(prefix+key, "string", packageName);
        return NewsfeedUtils.getStringResource(ctx, stringId);
    }

    public static Drawable getDrawableResource(Context ctx, int id, @Nullable Resources.Theme theme) {
        return isAndroidApiGreaterThan(Build.VERSION_CODES.LOLLIPOP)
                ? ctx.getResources().getDrawable(id, theme)
                : ctx.getResources().getDrawable(id); //AppCompatResources.getDrawable(ctx, id);
    }
    public static Drawable getDrawableResource(Context ctx, int id) {
        return getDrawableResource(ctx, id, null);

    }
    public static Spanned htmlToSpannedText(String html) {

        if(html == null) {
            return new SpannableString("");
        }

        boolean matches = mHtmlPattern.matcher(html).find();

        if(!matches) { //if the input string doesn't contain html we return it
            return new SpannableString(html);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    public static String truncateString(String str, int limit) {
        if(str == null || str.length() <= limit) {
            return str;
        }

        return str.substring(0, limit) + "...";
    }

    public static String timeToString(Context ctx, long time) {
        long now = System.currentTimeMillis();

        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH;
        String dateString;
        if(DateUtils.isToday(time)) {
            dateString = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS, flags).toString();
        } else {
            dateString = DateUtils.getRelativeDateTimeString(ctx, time, DateUtils.MINUTE_IN_MILLIS, 2* DateUtils.DAY_IN_MILLIS, flags).toString();
        }
        return dateString;
    }

    public static String uriToPath(Context ctx, Uri uri) throws IOException {
        if (uri == null) {
           return null;
        }

        if ("file".equals(uri.getScheme())) {
            return uri.getPath();
        }
        //Get content information
        String[] proj = new String[] {"_data"};
        Cursor cursor = ctx.getContentResolver().query(uri, proj, null, null, null);

        if(cursor == null) throw new IOException("Uri not found in database");

        cursor.moveToFirst();
        Log.d("BlaBla", "uriToPath: " + DatabaseUtils.dumpCurrentRowToString(cursor));
        String path = cursor.getString(0);
        cursor.close();

        return path;
    }

    public static int ContextActionTypeToId(ContextActionMenuItem.ContextActionType actionType) {
        return actionType.ordinal();
    }

    public static ContextActionMenuItem.ContextActionType idToContextActionType(int id) {
        return ContextActionMenuItem.ContextActionType.values()[id];
    }


    public static Bitmap loadBitmap(Context ctx, String path) {
        Bitmap bitmap;

        try {
            bitmap = BitmapFactory.decodeFile(path);
        } catch (OutOfMemoryError error) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            bitmap = BitmapFactory.decodeFile(path, options);
        }

        return bitmap;
    }

    public static int pxToDp(Context ctx, int px) {
        float density = ctx.getResources().getDisplayMetrics().density;

        return (int) (px * density);
    }
}