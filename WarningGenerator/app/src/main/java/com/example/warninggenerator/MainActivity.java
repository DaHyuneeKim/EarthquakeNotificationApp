package com.example.warninggenerator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.AndroidException;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_1 = (Button)findViewById(R.id.button1);

        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override


            public void onClick(View v) {

                String addr = "127.0.0.1";
                String message = "[기상청] 00월00일 00:00 서울특별시 마포구 x쪽 11km 지역 규모 5.5 지진 발생/낙하물로부터 몸 보호, 진동 멈춘 후 야외 대피하며 여진 주의";
                connectThread thread = new connectThread(addr,message);
                thread.start();
            }
        });

    }

    class connectThread extends Thread {
        String hostname;
        String message;

        public connectThread(String addr, String mes) {
            hostname = addr;
            message = mes;
        }


        public void run() {
            int portnumber = 11001;

            try {

                Socket sock = new Socket(hostname, portnumber);
                ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());
                outstream.writeObject(message);
                outstream.flush();
                System.out.println("전송");

                sock.close();


            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }



}
