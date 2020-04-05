package com.youssefdirani.citizen.Service;
//check copyrights here https://github.com/android/location-samples/tree/df8fa498cdd859aa5c49fd7d0dc3d329c6e01591/LocationUpdatesPendingIntent/app/src/main/java/com/google/android/gms/location/sample/locationupdatespendingintent
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;


import java.util.List;

import com.google.android.gms.location.LocationResult;
import com.youssefdirani.citizen.SendLocationToServer;

import static android.content.Context.MODE_PRIVATE;

public class LocationUpdatesReceiver extends BroadcastReceiver {
    private static final String TAG = "Youssef-LUBR";

    public static final String ACTION_PROCESS_UPDATES =
            //"Hi"; //weird thing is that the value of this variable doesn't matter, and intent action always gets this same string, but how ??
            "package com.youssefdirani.citizen.action.PROCESS_UPDATES";

    @Override
    public void onReceive( Context context, Intent intent ) {
        //Toast.makeText(context, log, Toast.LENGTH_LONG).show();
        Log.i(TAG,"Broadcast receiver has received something !");
        if (intent != null) {
            final String action = intent.getAction();
/*
            Log.i(TAG,"context className of onReceive is : " + context.getApplicationInfo().className);
            Log.i(TAG,"intent package of onReceive is : " + intent.getPackage());
            Log.i(TAG,"intent data of onReceive is : " + intent.getData());
            Log.i(TAG,"intent action of onReceive is : " + intent.getAction());
 */
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    for (final Location location: locations) {
                        Log.i(TAG,"A location !");
                        final SharedPreferences client_app_data = context //this is the context in which the onReceive method runs which is the application context I believe. Is this determined in the PendingIntent.getBroadcast ? IDK
                                .getSharedPreferences("client_app_data", MODE_PRIVATE);
                        int port = client_app_data.getInt( "server_port_to_send_locations_on", 3552 );
                        SendLocationToServer sendLocationToServer = new SendLocationToServer( location, port );
                        sendLocationToServer.start();
                    }
                }
            } else {
                Log.i(TAG,"Logically, it's false... Intent action is different ??");
            }
        }
    }

}
