package com.youssefdirani.androidfcmnew.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.youssefdirani.androidfcmnew.MainActivity;
import com.youssefdirani.androidfcmnew.R;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    final private String TAG = "MyFirebaseMessag...";

    @Override
    public void onCreate() {
        Log.d(TAG, "inside onCreate" );
        super.onCreate();
    }

    @Override
    public void onNewToken(String token) {
        /*
        https://firebase.google.com/docs/cloud-messaging/android/client
        The registration token may change when:
            The app deletes Instance ID
            The app is restored on a new device
            The user uninstalls/reinstall the app
            The user clears app data.
         */
        Log.d(TAG, "inside onNewToken" );

        if(token == null) {
            return;
        }

        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        //sendRegistrationToServer(token);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //According to the video "Android Push Notification using Firebase Console"
        Intent intent = new Intent( this, MainActivity.class);
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id));
        notificationBuilder.setContentTitle("FCM Notification");
        notificationBuilder.setContentText(remoteMessage.getNotification().getBody());
        notificationBuilder.setAutoCancel(true);
        //notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_ic_notification);
        notificationBuilder.setContentIntent( pendingIntent ); //here is the reason for PendingIntent (https://stackoverflow.com/questions/2808796/what-is-an-android-pendingintent)
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());


        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "Inside onMessageReceived.   From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.

                //scheduleJob();
            } else {
                // Handle message within 10 seconds

                //handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

}

