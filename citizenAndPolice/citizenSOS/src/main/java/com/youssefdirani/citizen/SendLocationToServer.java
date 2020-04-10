package com.youssefdirani.citizen;

import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import android.location.Location;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;

public class SendLocationToServer extends Thread {
    private Location location;
    private int port;
    //final Task<InstanceIdResult> getInstanceId_failed;
    //getInstanceId_failed = FirebaseInstanceId.getInstance().getInstanceId()

    public SendLocationToServer( Location location, int port ) {
        this.location = location;
        this.port = port;
    }

    @Override
    public void run() {
        //this is to get the "current" registration token.
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        final String TAG = "Youssef FirebaseIId";
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        InstanceIdResult iir = task.getResult();
                        String token = "no token !";
                        if (iir != null) {
                            token = iir.getToken();
                        }
                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token); //Didn't find its signature
                        Log.d(TAG, "token is " + token);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        byte[] message = prepareMessage( token );
                        sendUDPMessageToServer( message ); //Can the port be set using a message I get from the intermediate server ?
                    }
                });
    }

    private byte[] prepareMessage( String token ) {
        //ArrayList<String> list = new ArrayList<String>();
        //list.size()   list.add()      list.remove()       list.contains(...)
        //list.add("lat" + location.getLatitude() + "lng" + location.getLongitude() + "token" + token);
        String str_message = "lat" + location.getLatitude() + "lng" + location.getLongitude() + "token" + token;
        byte[] message = str_message.getBytes();
        Log.i("Youssef SendLoc..", "message length is " + message.length );
        return message;
    }
/*
    private void sendTCPMessageToServer( int port ) {

        //client = new Socket(wiFiConnection.chosenIPConfig.staticIP, port); //should be further developed.
        //client.connect(new InetSocketAddress(wiFiConnection.chosenIPConfig.staticIP, port), 1500);

        Socket client = new Socket();
        PrintWriter printWriter;
        try {
            //client.connect(new InetSocketAddress("192.168.4.201", port),1500);
            if( client.isConnected() ) {
                client.close();
            }
            client.connect( new InetSocketAddress( "192.168.0.21", port ), 1500); //this blocks execution for a timeout interval of time
            //When we reach here, then socket did really connect

            //client.setSoTimeout(0); //no need to set it to infinite since all it does, if it were not infinite, is to throw an exception; it does not affect the socket.
            printWriter = new PrintWriter( client.getOutputStream() );
            Log.i("Youssef sock...", "New outputStreams is made, for panel index ");
            printWriter.write(message);
            printWriter.flush();

        } catch (Exception e) {//I hope this includes the IOException or the UnknownHostException because this will be thrown
            //in case the IP is wrong or the electricity on module is down.
            e.printStackTrace();
            Log.i("Youssef sock...", "Error: socket connection to port (and " +
                    "in a lesser probablity, error in socket closure.)" + port);
            try {
                client.close();
            } catch (Exception exc) {
                //Thread.currentThread().interrupt();
                exc.printStackTrace();
                Log.i("Youssef sock...", "Error: closing socket.");
            }

            //it's probably better to call      destroySocket(index);       but to be checked later.
            //Log.i("Youssef sock...", "Exception is thrown on port " + getPortFromIndex(index));
            //Now turn off the WiFi
        }
    }
*/

    private void sendUDPMessageToServer( byte[] message ) {
        /*About passing 'port' as argument : In case the intermediate server congested and didn't receive some requests
        * then the server might prefer to receive some values on a different port. And there's no way to tell this android
        * device to switch on another port, this is why I'm able to change the port using FirebaseMessagingService.
        * Thus the intermediate server might e.g. send the change port request to a random number of devices.
        */
        try {
            InetAddress server_IP = InetAddress.getByName("192.168.1.21");
            DatagramPacket p = new DatagramPacket(message, message.length, server_IP, port);
            DatagramSocket s = new DatagramSocket();
            s.send(p);
            Log.i("Youssef SendLoc..", "datagram packet is sent to server on port " + port);
            /* //I changed my  mind because I will close the UDP in the server side right after I get the datagram there.
            //now closing the udp socket after 2 seconds
            new Handler().postDelayed(new Runnable() {
                public void run() {

                }
            }, 2000);
            */
        } catch (Exception e) {//I hope this includes the IOException or the UnknownHostException because this will be thrown
            //in case the IP is wrong or the electricity on module is down.
            e.printStackTrace();
            Log.i("Youssef SendLoc..", "Error: UDP connection to " + port + " or maybe in datagram sent.");
        }
    }

}

