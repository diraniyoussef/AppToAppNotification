package com.youssefdirani.citizen;

//import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

//import android.content.DialogInterface;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.youssefdirani.citizen.Service.RequestLocationWorkManager;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    Toasting toasting = new Toasting(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("إشعار بالاستخدام")
                .setMessage("هذا التطبيق على درجة من الحساسية" +
                        "\n" +
                        "جهازك هو جزء من هويتك" +
                        "\n" +
                        "معلومات هاتفك هي التالية :" +
                        "\n" +
                        DeviceInformation.getInfosAboutDevice(MainActivity.this) +
                        "\n" +
                        "لطفاً حسن الإستخدام و التعاطي به بجدية" +
                        "\n" +
                        "شكراً")
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                //.setPositiveButton(android.R.string.no, null)
                //.setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
         */
        //Log.i("MainActivity","My device info are : " + DeviceInformation.getInfosAboutDevice(MainActivity.this) );
        //startService( new Intent( MainActivity.this, MyFirebaseMessagingService.class ) ); //no need to
        if (ActivityCompat.checkSelfPermission( this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission( this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1); /*last parameter requestCode is
             * related to this app itself.
             * https://developer.android.com/reference/androidx/core/app/ActivityCompat.html#requestDragAndDropPermissions(android.app.Activity,%20android.view.DragEvent)
             */
        } else {
            Log.i("Youssef MainAct", "permissions are fine in onConnected");
            setRequestLocationWorkManager();
            //Can I avoid setting work manager if it has already been set ?
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allRequestsAreGranted = true;
        if( requestCode == 1 ) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allRequestsAreGranted = false;
                    }
                }
            }
            if( allRequestsAreGranted ) {
                Log.i("Youssef MainAct", "in onRequestPermissionsResult");
                setRequestLocationWorkManager();
            } else {
                //requestPermissions(new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_CODE);
                toasting.toast("عليك أن تعطي الإذن. يمكنك الذهاب إلى settings ثم apps" +
                        " و تختار هذا التطبيق و تعطي الإذن.", Toast.LENGTH_LONG);
                this.finish();
            }
        }
    }

    private void setRequestLocationWorkManager() {
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(false)
                .build();

        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(RequestLocationWorkManager.class, 30, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();
        /*The value of PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS is fixed and it is equivalent to 15 minutes
        Log.i("Youssef MainAct", String.valueOf(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS ));
         */
        WorkManager instance = WorkManager.getInstance( getApplicationContext() );
        instance.enqueueUniquePeriodicWork("work_RequestLocation", ExistingPeriodicWorkPolicy.KEEP , saveRequest);
/*
        WorkManager.getInstance(this)
                .enqueue(saveRequest); //using enqueueUniquePeriodicWork it won't run again if we issue again the same command. https://stackoverflow.com/questions/51612274/check-if-workmanager-is-scheduled-already
 */

    }
}
