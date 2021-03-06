package eu.spod.isislab.spodapp.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import eu.spod.isislab.spodapp.R;

public class SpodNotificationManager {

    public static final String NOTIFICATION_INTENT_EXTRA_BODY  = "NOTIFICATION_INTENT_EXTRA_BODY";
    public static final String NOTIFICATION_INTENT_EXTRA_TITLE = "NOTIFICATION_INTENT_EXTRA_TITLE";

    private static final int ID_BIG_NOTIFICATION   = 234;
    private static final int ID_SMALL_NOTIFICATION = 235;

    private Context mContext;

    public SpodNotificationManager(Context mCtx) {
        this.mContext = mCtx;
    }

    public void showBigNotification(String title, String body, String url, Intent intent) {
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        ID_BIG_NOTIFICATION,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        //bigPictureStyle.setSummaryText(Html.fromHtml(getNotificationMessage(body)).toString());
        //bigPictureStyle.bigPicture(getBitmapFromURL(url));
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        Notification notification;
        notification = mBuilder.setSmallIcon(R.drawable.logo).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setStyle(bigPictureStyle)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo))
                .setContentText(getNotificationMessage(body))
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID_BIG_NOTIFICATION, notification);
    }

    public void showSmallNotification(String title, String body, Intent intent) {
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        ID_SMALL_NOTIFICATION,
                        intent,
                        PendingIntent.FLAG_ONE_SHOT
                );

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        Notification notification;
        notification = mBuilder.setSmallIcon(R.drawable.logo).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo))
                .setContentText(Html.fromHtml(this.getNotificationMessage(body)))
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(Html.fromHtml(this.getNotificationMessage(body))))
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    private String getNotificationMessage(String body)
    {
        String message = "";
        try {
            JSONObject notificationBody = new JSONObject(body);
            message = notificationBody.getString("message");
            message = message.split("\n")[0];

            Log.e("NOTIFICATION MANAGER", "MESSAGE : \"" + message + "\"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }
}
