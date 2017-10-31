package eu.spod.isislab.spodapp.services;

import android.content.Intent;

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
        intent.putExtra(SpodNotificationManager.NOTIFICATION_INTENT_EXTRA_BODY, body);

        if(imageUrl.equals("null")){
            mNotificationManager.showSmallNotification(title, body, intent);
        }else{
            mNotificationManager.showBigNotification(title, body, imageUrl, intent);
        }
    }
}
