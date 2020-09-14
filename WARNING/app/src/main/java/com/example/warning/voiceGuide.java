package com.example.warning;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;



import java.util.Locale;

//음성안내 부분.
public class voiceGuide extends Thread {

    public String shelterPosition;
    public voiceGuide(String sp)
    {
        shelterPosition = new String(sp);
    }

    public void run() {

        while (true) {
            WarningEarthquake.Speech();

            if (WarningEarthquake.voiceExecute == false) {
                break;
            }

        }
    }


}