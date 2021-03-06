package eu.spod.isislab.spodapp.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.entities.ContextActionMenuItem;

public class NewsfeedUtils {

    public static final int DEFAULT_ITEM_PER_PAGE_COUNT = 10;
    public static final int DEFAULT_COMMENTS_PER_PAGE_COUNT = 10;
    public static final String FEED_TYPE_MY = "my";
    public static final String FEED_TYPE_SITE = "site";
    public static final String FEED_TYPE_USER = "user";

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

    /**
     *
     * @param color The color to make darker
     * @param factor How much color have to be darker. Note must be between 0.0 and 1.0
     * @return The darker color
     */
    public static int makeColorDarker(int color, float factor) {
        int alpha = Color.alpha(color);
        int red = Math.round(Color.red(color) * factor);
        int green = Math.round(Color.green(color) * factor);
        int blue = Math.round(Color.blue(color) * factor);

        return Color.argb(alpha, red, green, blue);
    }

    public static int getUserColor(Context ctx, int userId) {
        String colors[] = ctx.getResources().getStringArray(R.array.userColors);
        String color = colors[userId % colors.length];
        return Color.parseColor(color);
    }

    public static TextDrawable getTextDrawableForUser(Context ctx, int userId, String userName) {
        String text;
        userName = userName.toUpperCase();
        String[] names = userName.split(" ");
        if(names.length == 2) {
            text = names[0].substring(0,1) + names[1].substring(0,1);
        } else {
            text = names[0].substring(0,1);
        }


        TextDrawable drawable = new TextDrawable(new OvalShape());
        drawable.setBackgroundColor(getUserColor(ctx, userId));
        drawable.setTextColor(Color.WHITE);
        drawable.setBold(false);
        drawable.setSmallText(true);

        drawable.setText(text);
        return drawable;
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

    public static CharSequence truncateString(CharSequence str, int limit) {
        if(str == null || str.length() <= limit) {
            return str;
        }

        return TextUtils.substring(str, 0, limit) + "...";
    }

    public static String truncateString(String str, int limit) { //method for backward compatibility
        return (String) truncateString((CharSequence) str, limit);
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

    public static void truncateWithViewMore(Context ctx, CharSequence text, int limit, final TextView target) {
        if(text == null || text.length() <= 1000) {
            target.setText(text);
            return;
        }

        CharSequence truncatedStr = truncateString(text, limit);

        String viewMore = NewsfeedUtils.getStringResource(ctx, R.string.newsfeed_view_more);
        String viewLess = NewsfeedUtils.getStringResource(ctx, R.string.newsfeed_view_less);

        final Spannable spannableMore = new SpannableString(truncatedStr + " " + viewMore);
        final Spannable spannableLess = new SpannableString(text + " " + viewLess);

        spannableLess.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                target.setText(spannableMore);
            }
        }, text.length() + 1, spannableLess.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableMore.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                target.setText(spannableLess);
            }
        }, truncatedStr.length() + 1, spannableMore.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        target.setText(spannableMore);
    }

    public static View viewVisibleIfNotNull(Object value, View v) {
        if(value instanceof CharSequence) {
            if(!TextUtils.isEmpty((CharSequence) value)) {
                v.setVisibility(View.VISIBLE);
                return v;
            } else {
                v.setVisibility(View.GONE);
                return v;
            }
        }

        if(value != null) {
            v.setVisibility(View.VISIBLE);
            return v;
        }

        v.setVisibility(View.GONE);
        return v;
    }

    public static int[] getRelativeDimensions(Context ctx, int[] dimens) {
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        int width = dimens[0];
        int height = dimens[1];

        if(width == 0) {
            return new int[] {width, height};
        }

        int relativeWidth = width > metrics.widthPixels ? metrics.widthPixels : width;
        int relativeHeight = (height * relativeWidth) / width;

        return new int[]{relativeWidth, relativeHeight};
    }

    public static boolean isDefaultAvatar(String avatarUrl) {
        return avatarUrl.contains("no-avatar");
    }
}
