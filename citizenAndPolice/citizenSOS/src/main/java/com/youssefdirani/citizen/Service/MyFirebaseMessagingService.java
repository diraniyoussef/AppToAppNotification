package com.youssefdirani.citizen.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.youssefdirani.citizen.Announcement;
import com.youssefdirani.citizen.MainActivity;
import com.youssefdirani.citizen.R;

import java.util.Map;

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
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "Inside onMessageReceived.   From: " + remoteMessage.getFrom());
        // Checking if message contains a data payload and if it has a notification (this is my convention)
        Map<String, String> data_map = remoteMessage.getData();
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if( remoteMessage.getData().size() <= 0 || notification == null ) {
            return;
        }

        Log.d( TAG, "Message data payload: " + data_map );
        Announcement announcement = new Announcement();
        if( !isMessageDataConsistent( data_map, announcement ) ) {
            return;
        }

        /*Since the message is recognizable and well filled, then We have to :
        * 1) open the app on the foreground if not yet
        * 2) show the announcement in the MapAnnouncement activity
         */
        appToForeground();


        if (true) { //Check if data needs to be processed by long running job
            // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.

            //scheduleJob();
        } else {
            // Handle message within 10 seconds

            //handleNow();
        }

        //showNotificationIfPossible( notification );
    }

    private boolean isMessageDataConsistent( Map<String, String> data_map, Announcement announcement ) {
        announcement.announcer = data_map.get("announcer");
        announcement.announcement = data_map.get("announcement");
        String center_lat = data_map.get("center_lat");
        String center_lng = data_map.get("center_lng");
        String radius = data_map.get("radius");
        if( center_lat == null || center_lng == null || radius == null ) {
            return false;
        }
        announcement.center_lat = Double.parseDouble( center_lat );
        announcement.center_lng = Double.parseDouble( center_lng );
        announcement.radius = Double.parseDouble( radius );
        return true;
    }

    private void appToForeground() {


    }

    private void showNotificationIfPossible(@org.jetbrains.annotations.NotNull RemoteMessage.Notification notification ) {
    //private void showNotificationIfPossible( RemoteMessage.Notification notification ) {
        //According to the video "Android Push Notification using Firebase Console"
        Intent intent = new Intent( this, MainActivity.class);
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id));

        Log.d(TAG, "Message Notification Body: " + notification.getBody());
        String s = notification.getBody(); //assuming body is not empty
        notificationBuilder.setContentText(s);
        notificationBuilder.setContentTitle(notification.getTitle()); //assuming title is not empty
        notificationBuilder.setAutoCancel(true);
        //notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_ic_notification);
        notificationBuilder.setContentIntent(pendingIntent); //here is the reason for PendingIntent (https://stackoverflow.com/questions/2808796/what-is-an-android-pendingintent)
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if( notificationManager != null ) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}

