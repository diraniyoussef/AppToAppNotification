package com.youssefdirani.citizen.Service;
//check copyrights here https://github.com/android/location-samples/tree/df8fa498cdd859aa5c49fd7d0dc3d329c6e01591/LocationUpdatesPendingIntent/app/src/main/java/com/google/android/gms/location/sample/locationupdatespendingintent
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;


import java.util.List;

import com.google.android.gms.location.LocationResult;
import com.youssefdirani.citizen.SendLocationToServer;

public class LocationUpdatesReceiver extends BroadcastReceiver {
    private static final String TAG = "Youssef-LUBR";

    public static final String ACTION_PROCESS_UPDATES =
            //"Hi"; //weird thing is that the value of this variable doesn't matter, and intent action always gets this same string, but how ??
            "package com.youssefdirani.citizen.action.PROCESS_UPDATES";
            //"package com.youssefdirani.citizen.LocationUpdatesReceiver.action.PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Broadcast receiver has received something !");
        if (intent != null) {
            final String action = intent.getAction();
            Log.i(TAG,"context className of onReceive is : " + context.getApplicationInfo().className);
            Log.i(TAG,"intent package of onReceive is : " + intent.getPackage());
            Log.i(TAG,"intent data of onReceive is : " + intent.getData());
            Log.i(TAG,"intent action of onReceive is : " + intent.getAction());
/*
            final Context applicationContext = context.getApplicationContext();
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    new Intent(), // add this
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(applicationContext, "whatever");
            notificationBuilder.setContentText("hi");
            notificationBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if( notificationManager != null ) {
                notificationManager.notify(0, notificationBuilder.build());
            }
*/
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    for (final Location location: locations) {
                        Log.i(TAG,"A location !");
                        //Now to send this location to server, along with the token.
                        //Toast.makeText(,"asd", Toast.LENGTH_LONG).show();
/*
                        Activity activity = (Activity) context;
                        activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        //Do something on UiThread
                                        activity.updateLocation( location );
                                    }
                                });
                        if( activity != null ) {
                            if( activity.mapSetup.isConnected ) {
                                activity.mapSetup.updateLocation(location);
                            }
                        }

*/
                        SendLocationToServer sendLocationToServer = new SendLocationToServer( location );
                        sendLocationToServer.start();
                    }
                }
            } else {
                Log.i(TAG,"Logically, it's false... Intent action is different ??");
            }
        }
    }

}
