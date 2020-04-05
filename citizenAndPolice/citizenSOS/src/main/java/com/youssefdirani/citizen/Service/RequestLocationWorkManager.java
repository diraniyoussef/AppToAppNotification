package com.youssefdirani.citizen.Service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
/*https://developer.android.com/guide/background
https://developer.android.com/topic/libraries/architecture/workmanager/basics
https://developer.android.com/topic/libraries/architecture/workmanager/how-to/recurring-work
 */

public class RequestLocationWorkManager extends Worker implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public RequestLocationWorkManager(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NotNull
    @Override
    public Result doWork() {
        // Do the work here--in this case, upload the images.
        connectAttemptToGetCurrentLocation();
        //Log.i("Youssef", "instead of Location !");

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }

    private void connectAttemptToGetCurrentLocation() {
        Log.i("Youssef Goog API", "entering Connect attempt to get current location.");
        /* There's something similar to GoogleApiClient in
         * https://stackoverflow.com/questions/22733661/googleapiclient-and-googleplayservicesclient-can-you-preserve-a-separation-of-c
         * which is GooglePlayServicesClient.
         * */
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder( this.getApplicationContext() )
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //if (mGoogleApiClient != null) { //always true
        if( !mGoogleApiClient.isConnected() ) {
            Log.i("Youssef Goog API", "Connecting...");
            mGoogleApiClient.connect(); //this will usually mean to find move to last location.
        }
        //}
        //unregisterConnectionCallbacks we don't want t do it though
    }

    private GoogleApiClient mGoogleApiClient;


    private PendingIntent pendingIntent;
    private void setPendingIntent() {
        Intent intent = new Intent( this.getApplicationContext(), LocationUpdatesReceiver.class );
        intent.setAction( LocationUpdatesReceiver.ACTION_PROCESS_UPDATES );
        //intent.setData(Uri.parse("NotSureWhatIsThat://"))
        //this is lovely in case you want to make a pending intent or whatever : queryBroadcastReceivers
        pendingIntent = PendingIntent.getBroadcast( this.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    private LocationRequest locationRequest;
    private void setLocationRequest(long updateInterval, long fastestUpdateInterval) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval( updateInterval );
        locationRequest.setFastestInterval( fastestUpdateInterval );
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("Youssef  Goog API", "Google API map is Connected !");
        mapOperations();
    }

    private void mapOperations() {
        FusedLocationProviderClient fusedLocationApi = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());
        /*another way is : https://developer.android.com/training/location/receive-location-updates#get-last-location
        //same as the second answer here https://stackoverflow.com/questions/46481789/android-locationservices-fusedlocationapi-deprecated
        // But I doubt it will work.
        */
        //1 minute
        long LOCATION_UPDATE_INTERVAL = 21 * 60 * 1000;
        //half a minute
        long LOCATION_FASTEST_UPDATE_INTERVAL = 20 * 60 * 1000;

        setLocationRequest(LOCATION_UPDATE_INTERVAL, LOCATION_FASTEST_UPDATE_INTERVAL);
        //LocationUpdatesReceiver.activity = activity;
        setPendingIntent();
        fusedLocationApi.requestLocationUpdates(locationRequest, pendingIntent);
        /*
        Task m = fusedLocationApi.requestLocationUpdates(locationRequest, pendingIntent);
        if( m.isSuccessful() ) {
            Log.i("Youssef MainActivity","requestLocationUpdates is successful" );
        } else {
            Log.i("Youssef MainActivity","requestLocationUpdates is not successful" );
        }
        if( m.isComplete() ) {
            Log.i("Youssef MainActivity","requestLocationUpdates is complete" );
        } else {
            Log.i("Youssef MainActivity","requestLocationUpdates is not complete" );
        }
         */
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
