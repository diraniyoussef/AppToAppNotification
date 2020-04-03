package com.youssefdirani.citizen;

import android.util.Log;
import android.widget.Toast;
import android.location.Location;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SendLocationToServer extends Thread {
    int index, i;
    byte[] message;
    private Location location;

    public SendLocationToServer( Location location ) {
        this.location = location;
    }

    @Override
    public void run() {
        i++; //only for debugging
        if( i == 10 ) {
            i = 0;//I don't want i to grow indefinitely and potentially cause an overflow.
        }
        Log.i("Youssef sock...", "This is the " + i + "th time we enter in CreateSocket");
        //client = new Socket(wiFiConnection.chosenIPConfig.staticIP, port); //should be further developed.
        //client.connect(new InetSocketAddress(wiFiConnection.chosenIPConfig.staticIP, port), 1500);
        newSocketIsCurrentlyUnderCreation = true;
        try {
            client[index] = new Socket();
            Log.i("Youssef sock...", "client[" + index + "] will try to connect to server on port " + getPortFromIndex(index) );
            //client.connect(new InetSocketAddress("192.168.4.201", port),1500);
            client[index].connect( new InetSocketAddress( StaticIP , getPortFromIndex( index ) ) , SocketTimeout ); //this blocks execution for a timeout interval of time
            //When we reach here, then socket did really connect
            activeSocketIndex = index;

            //client.setSoTimeout(0); //no need to set it to infinite since all it does, if it were not infinite, is to throw an exception; it does not affect the socket.
            Log.i("Youssef sock...", "Socket " + index + " " +
                    "is connected,  on port " + getPortFromIndex(index));

            outputStreams[index] = client[index].getOutputStream();
            Log.i("Youssef sock...", "New outputStreams is made, for panel index ");

            communication.renew_InputStreamThread(index);
            //Log.i("Youssef sock...", "New bufferThread is made." + " For panel  index " + selectedServerConfig.panel_index);
            if( atTheBeginning ) {
                callback_socketConnectedAtTheBeginning(message);
            } else {
                callback_switchSocket();
            }

        } catch (Exception e) {//I hope this includes the IOException or the UnknownHostException because this will be thrown
            //in case the IP is wrong or the electricity on module is down.
            e.printStackTrace();
            if( client[index] != null ) {
                try {
                    client[index].close();
                } catch (Exception exc) {
                    //Thread.currentThread().interrupt();
                    exc.printStackTrace();
                    Log.i("Youssef sock...", "Error: closing socket.");
                }
            }
            //it's probably better to call      destroySocket(index);       but to be checked later.
            Log.i("Youssef sock...", "Exception is thrown on port " + getPortFromIndex(index));
            //Now turn off the WiFi
/*
                if (WiFiConnection.isWiFiOn()) {
                    WiFiConnection.turnWiFiOff(); //this sometimes solves a problem..............
                }
                Generic.toasting.toast("Couldn't connect.\nPlease turn on the WiFi to refresh...", Toast.LENGTH_LONG, silentToast);
*/
            if( atTheBeginning ) {
                toasting.toast("لم يتمكّن التطبيق من الاتصال." +
                        "\nهل الانترنت منقطع ؟", Toast.LENGTH_LONG, silentToast);
            }

            e.printStackTrace();
        }
        newSocketIsCurrentlyUnderCreation = false;
    }

}
