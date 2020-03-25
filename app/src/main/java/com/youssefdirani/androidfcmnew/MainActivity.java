package com.youssefdirani.androidfcmnew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    //    startService( new Intent( MainActivity.this, MyFirebaseMessagingService.class ) ); //no need to

        //this is to get the "current" registration token.
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        final String TAG = "FirebaseInstanceId";
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = "no token !";
                        InstanceIdResult iir = task.getResult();
                        if( iir != null ) {
                            token = iir.getToken();
                        }
                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token); //Didn't find its signature
                        String msg = token;
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
