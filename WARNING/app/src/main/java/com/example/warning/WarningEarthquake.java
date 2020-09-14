package com.example.warning;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;

public class WarningEarthquake extends AppCompatActivity implements OnMapReadyCallback {

    static boolean voiceExecute = false;
    double nowlongitude = 0;
    double nowlatitude = 0;
    int minIndex = -1;
    static String minDistanceLocation = new String();
    ShelterInfo[] shelterinfo = new ShelterInfo[63];


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            nowlongitude = location.getLongitude();
            nowlatitude = location.getLatitude();
            double altitude = location.getAltitude();
            float accuracy = location.getAccuracy();
            String provider = location.getProvider();

            System.out.println("위치정보 : " + provider + "\n위도 : " + nowlongitude + "\n경도 : " + nowlatitude
                    + "\n고도 : " + altitude + "\n정확도 : " + accuracy);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("test", "onProviderEnabled, provider:" + provider);

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }
    };

    private AlertDialog createDialogBox(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("[지진 알리미]");
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setNeutralButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();

        return dialog;
    }



    public static TextToSpeech myTTS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning_earthquake);

        AlertDialog dialog = createDialogBox(MyService.receive_message);
        dialog.show();

        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                int result = myTTS.setLanguage(Locale.KOREA);
            }
        });

        //음성 정지 버튼을 눌렀을 때
        Button btn_2 = (Button) findViewById(R.id.button2);
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceExecute = false;
                OnStop();

            }
        });
        //현재 GPS 값 받아오기
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, mLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, mLocationListener);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {

        Thread.sleep(2000);
    }
        catch(Exception e)
    {

    }
    FragmentManager fm = getSupportFragmentManager();
    MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
        mapFragment = MapFragment.newInstance();
        fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
    }

        mapFragment.getMapAsync(this);




    //길안내 버튼을 눌렀을 때
    Button btn_1 = (Button)findViewById(R.id.button1);
        btn_1.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (minIndex != -1) {
                    String url = "nmap://route/walk?" + "slat=" + Double.toString(nowlatitude) + "&slng=" + Double.toString(nowlongitude) +
                            "&sname=" + URLEncoder.encode("내위치", "UTF-8") + "&dlat=" + Double.toString(shelterinfo[minIndex].locshelter.latitude) + "&dlng="
                            + Double.toString(shelterinfo[minIndex].locshelter.longitude) + "&dname=" + URLEncoder.encode(shelterinfo[minIndex].name, "UTF-8") + "&appname=com.example.warning";
                    System.out.println(url);


                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);

                    List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (list == null || list.isEmpty()) {
                        WarningEarthquake.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")));
                    } else {
                        WarningEarthquake.this.startActivity(intent);
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }
    });


}


    @UiThread
        @Override
        public void onMapReady(@NonNull NaverMap naverMap) {
            // ...
            //엑셀로부터 대피소 이름 및 위치 받아오기.

            Workbook workbook = null;
            Sheet sheet = null;
        try {
            InputStream inputstream = getBaseContext().getResources().getAssets().open("shelter.xls");
            workbook = Workbook.getWorkbook(inputstream);
            sheet = workbook.getSheet(0);
            int MaxColumn = 2, RowStart = 0, RowEnd = sheet.getColumn(MaxColumn - 1).length - 1;
            for (int row = RowStart; row <= RowEnd; row++) {
                String name = sheet.getCell(0, row).getContents();

                NumberCell nc = (NumberCell) sheet.getCell(1, row);
                nc.getNumberFormat().setMaximumFractionDigits(8);
                double latitude = nc.getValue();

                NumberCell nc2 = (NumberCell) sheet.getCell(2, row);
                nc2.getNumberFormat().setMaximumFractionDigits(7);
                double longitude = nc2.getValue();

                shelterinfo[row] = new ShelterInfo(name, latitude, longitude);
                // System.out.println(name + ' ' + latitude + ' ' + longitude);
                //shelterinfo[row].printInfo();
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workbook.close();
        }






        //현재 GPS 값으로 데이터베이스 내에 든 지진 대피소 중 네이버 API를 이용해 직선거리로 가장 가까운 대피소를 찾아냄.




        System.out.println("위도 : " + nowlatitude + " 경도 : " + nowlongitude);
        LatLng myPosition = new LatLng(nowlatitude, nowlongitude);
        //Marker marker2 = new Marker(myPosition);
        //marker2.setMap(naverMap);
        double minDistance = 99999999;
        for(int i = 0 ;i<shelterinfo.length;i++)
        {
            double distance = myPosition.distanceTo(shelterinfo[i].locshelter);
            System.out.println(distance);
            if(minDistance >= distance)
            {
                minDistance = distance;
                minIndex = i;
            }
        }

        minDistanceLocation = shelterinfo[minIndex].name;
        System.out.println("minDistance : " + minDistance + " minDistanceLocation : " + minDistanceLocation + " minIndex : " + minIndex);

        //해당 위치로 지도 Camera 옮김.
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(shelterinfo[minIndex].locshelter);
        naverMap.moveCamera(cameraUpdate);
        //네이버 지도에 해당 위치 Pin 찍음.
        Marker marker = new Marker(shelterinfo[minIndex].locshelter);
        marker.setCaptionText(shelterinfo[minIndex].name);
        marker.setCaptionColor(Color.RED);
        marker.setMap(naverMap);




        //음성 안내 Thread 실행
        voiceExecute = true;
        voiceGuide vg = new voiceGuide("음성 실행");
        vg.start();
       // Speech();


    }

    public static void Speech()
    {
        String voice = "현재 위치에서 가장 가까운 대피소는 " + minDistanceLocation + "입니다. 진동 멈춘 후 " + minDistanceLocation + "대피소로 신속히 대피하세요";
        myTTS.speak(voice,TextToSpeech.QUEUE_ADD,null);

    }

    private void OnStop()
    {
        super.onStop();
        myTTS.stop();
        myTTS.shutdown();
    }







}