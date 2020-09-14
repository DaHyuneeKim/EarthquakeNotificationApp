package com.example.warning;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service implements Runnable{


    Object lock = new Object();
    boolean rcv_mes = false;
    public String message = new String();
    public static String receive_message = new String();
    double magnitude;
        public void onCreate() {
            super.onCreate();
            registerRestartAlarm(false);
            Thread ReceiveWarning = new Thread(this);
            ReceiveWarning.start();

        }

        public void onDestroy()
        {
            super.onDestroy();
            registerRestartAlarm(true);
        }

        int count = 0;
        public void run() {
            try
            {
                int portNumber = 11001;
                ServerSocket aServerSocket = new ServerSocket(portNumber);
                while(true)
                {

                    //Socket으로 임의로 발생시킨 지진 메시지 받음.
                    Socket sock = aServerSocket.accept();

                    ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
                    Object obj = instream.readObject();
                    message = obj.toString();
                    receive_message = message;

                    if(message.contains("지진"))
                    {
                        magnitude = magnitude = Float.parseFloat(message.replaceAll("[^0-9]", "").substring(10));

                        magnitude /= 10;
                        System.out.println("magnitude"+magnitude);

                        if(magnitude >= 5.0)
                        {

                            Intent intent = new Intent(this,WarningEarthquake.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        rcv_mes = false;
                    }

                    message = "";




                }
            }
            catch(Exception e)
                {
                    e.printStackTrace();
                }
        }

    public void registerRestartAlarm(boolean isOn){

        Intent intent = new Intent(MyService.this, RestartReceiver.class);

        intent.setAction(RestartReceiver.ACTION_RESTART_SERVICE);

        PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);



        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

        if(isOn){

            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 100, 10000, sender);

        }else{

            am.cancel(sender);

        }

    }






    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
