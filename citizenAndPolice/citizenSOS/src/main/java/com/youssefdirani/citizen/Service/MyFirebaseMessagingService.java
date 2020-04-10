package com.youssefdirani.citizen.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.youssefdirani.citizen.AnnouncementEntity;
import com.youssefdirani.citizen.AppDatabase;
import com.youssefdirani.citizen.MainActivity;
import com.youssefdirani.citizen.R;

import java.util.List;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public MutableLiveData<Integer> port = new MutableLiveData<>();
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
        if( remoteMessage.getData().size() <= 0 || notification == null || notification.getBody() == null ||
                notification.getBody().equals("") ) {
            return;
        }

        Log.d( TAG, "Message data payload: " + data_map );

        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.receiptDateOfAnnouncement = remoteMessage.getSentTime();
        final SharedPreferences client_app_data = getApplicationContext()
                .getSharedPreferences("client_app_data", MODE_PRIVATE);
        if( !isMessageDataConsistent( data_map, announcement, client_app_data ) ) {
            return;
        }
/*
        if (true) { //Check if data needs to be processed by long running job
            // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.

            //scheduleJob();
        } else {
            // Handle message within 10 seconds

            //handleNow();
        }
 */
        /*Since the message is recognizable and well filled, then We have to :
        * 1) add the announcement data in the SQLite database
        * 2) show the notification (in the notification tray e.g. or as a dot in the icon launcher)
        * 3) open the app on the foreground if the user presses on the notification (maybe this is a default behavior)
        * 3') if the user had already opened the app, and is in the MainActivity then update the visual rows in front of him.
        */

        AppDatabase db = Room.databaseBuilder( getApplicationContext(),
                AppDatabase.class, "my_db" ).build();
        updateDb( db, client_app_data, announcement );

        showNotification( notification );

        db.close(); //can this collide with the closing of the database in MainActivity.java ?  
    }

    private boolean isMessageDataConsistent( Map<String, String> data_map, AnnouncementEntity announcement,
            SharedPreferences client_app_data ) {
        //checking if the received message is a request to change the port this device sends its location on.
        String new_server_port_to_send_locations_on = data_map.get("new_server_port_to_send_locations_on");
        if( new_server_port_to_send_locations_on != null ) {
            try {
                 int port = Integer.parseInt(new_server_port_to_send_locations_on);
                //Integer.parseUnsignedInt(...); // this requires higher min api level
                if( port >= 0 ) {
                    //SendLocationToServer.port = port;
                    SharedPreferences.Editor prefs_editor = client_app_data.edit();
                    prefs_editor.putInt( "server_port_to_send_locations_on", port ).apply();
                    return true;
                } else { //should never happpen
                    return false;
                }
            } catch (NumberFormatException e) { //if the number is not valid. Usually should never happen
                return false;
            } catch (Exception e) {//I'm afraid this can happen if SendLocationToServer.port threw an error
                return false;
            }
        }

        //Now trying with the usual message type of announcement
        announcement.announcer = data_map.get("announcer");
        announcement.announcement = data_map.get("announcement");
        String center_lat = data_map.get("center_lat");
        String center_lng = data_map.get("center_lng");
        String radius = data_map.get("radius");
        if( center_lat == null || center_lng == null || radius == null ) {
            return false;
        }
        announcement.centerLat = Double.parseDouble( center_lat );
        announcement.centerLng = Double.parseDouble( center_lng );
        announcement.radius = Double.parseDouble( radius );
        return true;
    }

    private void updateDb( AppDatabase db, SharedPreferences client_app_data, AnnouncementEntity announcement ) {
        /* If we're less than 20 records, then we simply insert our new row, WITH the uid being the index (it may be
        * correctly auto incremented - little loose concept than the SQLite Autoincrement specific behavior term - nevertheless
        * I prefer to be on the safe side).
        * If we've reached 20 records then we set a cyclic index which can be found in the shared preferences e.g.,
        * this index will refer to the last inserted row. So if we want to insert a new one we get the next "cyclic" index
        * And update our row there. We won't be deleting.
        * ( One old mechanism was : we shall fetch all existing rows, then seek the smallest in time,
        *  and update it. I won't be deleting. - but I won't follow this mechanism)
         */
        int maxDbRows = client_app_data.getInt( "max_db_rows", 20 );
        announcement.uid = getNewDbIndex(
                client_app_data.getInt( "last_db_index", 0 ), maxDbRows );
        List<AnnouncementEntity> announcementEntity_list = db.announcementDao().getAll();
        if( announcementEntity_list.size() < maxDbRows ) {
            db.announcementDao().insert( announcement );
        } else {
            db.announcementDao().update( announcement );
        }
        SharedPreferences.Editor prefs_editor = client_app_data.edit();
        prefs_editor.putInt( "last_db_index", announcement.uid ).apply();
    }
    private int getNewDbIndex( int lastDbIndex, int maxDbRows ) {
        if( lastDbIndex < maxDbRows ) {
            return lastDbIndex + 1;
        } else { //normally lastDbIndex is 20 here
            return 1;
        }
    }

    private void showNotification(@org.jetbrains.annotations.NotNull RemoteMessage.Notification notification ) {
    //private void showNotificationIfPossible( RemoteMessage.Notification notification ) {
        //According to the video "Android Push Notification using Firebase Console"
        Intent intent = new Intent( this, MainActivity.class);
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id));

        Log.d(TAG, "Message Notification Body: " + notification.getBody());
        String s = notification.getBody(); //body is not empty - this has already been taken care of
        notificationBuilder.setContentText(s);
        //notificationBuilder.setContentTitle(notification.getTitle()); //there is no title to send
        notificationBuilder.setContentTitle( getString(R.string.app_name) );
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

