package com.youssefdirani.citizen;

//import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.room.Room;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

//import android.content.DialogInterface;
import android.Manifest;
import android.content.Context;
//import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.youssefdirani.citizen.Service.RequestLocationWorkManager;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    Toasting toasting = new Toasting(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //addShortcut( getApplicationContext() ); //didn't work
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
                        PackageManager.PERMISSION_GRANTED
        //      ||  ActivityCompat.checkSelfPermission( this.getApplicationContext(), Manifest.permission.INSTALL_SHORTCUT) !=
        //                PackageManager.PERMISSION_GRANTED
            ) {
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
        (new Thread() { //opening the database needs to be on a separate thread.
            public void run() {
                AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "my_db").build();
                List<AnnouncementEntity> announcementEntity_list = db.announcementDao().getAll();
                if (announcementEntity_list.size() == 0) {
                    //make a textview stating that there is no recorded announcements yet.
                    final LinearLayout mainLinearLayout = (LinearLayout) findViewById(R.id.main_linearlayout);
                    final TextView textView = new TextView(MainActivity.this);
                    textView.setLayoutParams( new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
                    textView.setText("لا يوجد أي تعميم مسجّل في قاعدة البيانات.");
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mainLinearLayout.addView(textView);
                        }
                    });

                } else {
                    makeBoxes( announcementEntity_list );
                }
                db.close();
            }
        }).start();
    }

    public void makeBoxes( List<AnnouncementEntity> announcementEntity_list ) {
        final SharedPreferences client_app_data = getApplicationContext()
                .getSharedPreferences("client_app_data", MODE_PRIVATE);
        int lastDbIndex = client_app_data.getInt( "last_db_index", 0 );
        int maxDbRows = client_app_data.getInt( "max_db_rows", 20 );
        //getPreviousDbIndex( lastDbIndex, maxDbRows, announcementEntity_list.size() ); //will be in a loop with a max iteration maxDbRows (not more, or else we might do it forever)
        //now we want to show all of the recorded announcements
        int index = lastDbIndex;
        for( int i = 0; i < maxDbRows; i++ ) {
            try {
                new visualEntry( announcementEntity_list.get( index ), MainActivity.this,
                        Position_of_button.bottom ).build();
                index = getPreviousDbIndex( index, maxDbRows, announcementEntity_list.size() );
                if( index == 0 ) {
                    break;
                }
            } catch( IndexOutOfBoundsException e ) { //should not normally happen. announcementEntity_list.get would make this error
                continue;
            }
        }
    }

    public enum Position_of_button { //https://stackoverflow.com/questions/9246934/working-with-enums-in-android
        top,
        bottom
    }

    private class visualEntry { //this builds the visual announcements on after another
        AnnouncementEntity announcementEntity;
        Context context;
        Position_of_button position_of_button;
        visualEntry( AnnouncementEntity announcementEntity, Context context, Position_of_button position_of_button ) {
            this.announcementEntity = announcementEntity;
            this.context = context;
            this.position_of_button = position_of_button;
        }

        void build() {
            final LinearLayout mainLinearLayout = (LinearLayout) findViewById(R.id.main_linearlayout);
            final Button buttonView = new Button(context);
            LayoutParams params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0,16,0,0);
            buttonView.setLayoutParams( params );
            buttonView.setLines(4);
            //setting the date string
            Date date = new Date( announcementEntity.receiptDateOfAnnouncement );
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd/MM ''yy 'at' HH:mm");
            final String announcement_receiptDate_str = simpleDateFormat.format(date);
            buttonView.setText( announcementEntity.announcer + "\n" +
                    announcementEntity.announcement.substring(0,60) + "...\n" +
                    announcement_receiptDate_str );
            if( position_of_button == Position_of_button.bottom ) { //it was ok to use .equals(...) https://stackoverflow.com/questions/1750435/comparing-java-enum-members-or-equals
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        buttonView.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view) {
                                //later we may show a map activity containing some info about the announcement, but it's really not relevant for the user
                                //Now a simple dialog alert is enough.
                                new AlertDialog.Builder(context)
                                        .setTitle(announcementEntity.announcer)
                                        .setMessage(announcementEntity.announcement)
                                        .setNeutralButton(android.R.string.ok, null)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show(); //https://stackoverflow.com/questions/10135353/when-may-we-need-to-use-runonuithread-in-android-application
                            }
                        });
                        mainLinearLayout.addView(buttonView);
                    }
                });
            } else {

            }
        }
    }

    public static int getPreviousDbIndex( int lastDbIndex, int maxDbRows, int announcementEntity_listSize ) {
        //1 is the minimum normal value of the index
        if( announcementEntity_listSize == 0 ) { //won't happen, already checked before we called makeBoxes(...)
            return 0;
        } else if( announcementEntity_listSize > 1 ) {
            return announcementEntity_listSize - 1 ;
        } else {
            if( announcementEntity_listSize < maxDbRows ) {
                return 0; //0 means we don't have any previous row
            } else {
                return maxDbRows;
            }
        }
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
/*
    private void addShortcut(Context context) { ////didn't work
        Intent shortcutIntent = new Intent();
        shortcutIntent.setClassName("com.youssefdirani.citizen", "MainActivity");
//shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra( Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent );
        addIntent.putExtra( Intent.EXTRA_SHORTCUT_NAME, "Announcement" );
        addIntent.putExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, R.drawable.logo) );
        addIntent.putExtra("duplicate", false);
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        Log.i("Youssef MainAct", "shortcut should be established by now...");
        context.sendBroadcast(addIntent);
    }

 */
}
