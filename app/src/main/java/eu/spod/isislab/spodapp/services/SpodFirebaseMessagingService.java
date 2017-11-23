package eu.spod.isislab.spodapp.services;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import eu.spod.isislab.spodapp.MainActivity;
import eu.spod.isislab.spodapp.utils.SpodNotificationManager;

public class SpodFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String body = remoteMessage.getNotification().getBody();
        String title = remoteMessage.getNotification().getTitle();
        if (!body.isEmpty() && !title.isEmpty()) {
            try {
                sendPushNotification(title, body, "null");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPushNotification(String title, String body, String imageUrl)
    {
        SpodNotificationManager mNotificationManager = new SpodNotificationManager(getApplicationContext());
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(SpodNotificationManager.NOTIFICATION_INTENT_EXTRA_BODY, body);
        intent.putExtra(SpodNotificationManager.NOTIFICATION_INTENT_EXTRA_TITLE, title);

        if(imageUrl.equals("null")){
            mNotificationManager.showSmallNotification(title, body, intent);
        }else{
            mNotificationManager.showBigNotification(title, body, imageUrl, intent);
        }
    }

    @Override
    public void handleIntent(Intent intent) {
        if (intent.getExtras() != null) {

            String title = intent.getExtras().get("gcm.notification.title").toString();
            String body  = intent.getExtras().get("gcm.notification.body").toString();
            (new SpodNotificationManager(getApplicationContext())).showSmallNotification(title, body, intent);
        }
    }
}
