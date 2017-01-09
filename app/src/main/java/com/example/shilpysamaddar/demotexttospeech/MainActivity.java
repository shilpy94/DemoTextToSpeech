package com.example.shilpysamaddar.demotexttospeech;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {


    private TextToSpeech tts;
    private Button btnSpeak;
    private EditText txtText;
    String googleTtsPackage = "com.google.android.tts", picoPackage = "com.svox.pico";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, this);

        btnSpeak = (Button) findViewById(R.id.btnSpeak);

        txtText = (EditText) findViewById(R.id.txtText);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                speakOut();
            }

        });

    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void checkTTS(){
        try {
            if(!isPackageInstalled(getPackageManager(), googleTtsPackage)){
                confirmDialogtts();
            } else if(tts.getDefaultEngine()!="com.google.android.tts"){
                confirmDialogSetting();
            }
        }catch (Exception e){
            e.getMessage();
        }

    }

    private void confirmDialogSetting(){
        AlertDialog.Builder d = new AlertDialog.Builder(MainActivity.this);
        d.setTitle("Please select recommeded speech engine?");
        d.setMessage("Your device isn't using the recommended speech engine. Do you wish to select it?");
        d.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int arg1){
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }});
        d.setNegativeButton("No,later", new android.content.DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int arg1){
                if(isPackageInstalled(getApplicationContext().getPackageManager(), picoPackage));
            }
        });
        d.show();
    }

    private void confirmDialogtts(){
        AlertDialog.Builder d = new AlertDialog.Builder(MainActivity.this);
        d.setTitle("Install recommeded speech engine?");
        d.setMessage("Your device isn't using the recommended speech engine. Do you wish to install it?");
        d.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int arg1){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + googleTtsPackage)));
            }});
        d.setNegativeButton("No,later", new android.content.DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int arg1){
                if(isPackageInstalled(getApplicationContext().getPackageManager(), picoPackage));
            }
        });
        d.show();
    }

    public static boolean isPackageInstalled(PackageManager pm, String packageName) {
        try {
            pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(new Locale("hin", "IND", "variant"));
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut() {

        String text = txtText.getText().toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}

//
//public class MainActivity extends AppCompatActivity {
//
//    private TextToSpeech tts;
//    private Button btnSpeak;
//    private EditText txtText;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        txtText = (EditText) findViewById(R.id.txtText);
//        btnSpeak = (Button) findViewById(R.id.btnSpeak);
//
//        btnSpeak.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                loadSpeakingLanguages(txtText.getText().toString());
//            }
//        });
//
//    }
//
//
//    private void loadSpeakingLanguages(String textToTranslate) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            tts.setLanguage(Locale.forLanguageTag("hin"));
//            ttsGreater21(textToTranslate);
//        } else {
//            ttsUnder20(textToTranslate);
//        }
//    }
//
//    @SuppressWarnings("deprecation")
//    private void ttsUnder20(String text) {
//        HashMap<String, String> map = new HashMap<>();
//        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
//        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
//    }
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void ttsGreater21(String text) {
//        String utteranceId = this.hashCode() + "";
//        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
//    }
//
//
//    @Override
//    protected void onResume() {
//        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status != TextToSpeech.ERROR) {
//                    tts.setLanguage(Locale.ENGLISH);
//                }
//            }
//        });
//        super.onResume();
//    }
//
//    public void onPause() {
//        if (tts != null) {
//            tts.stop();
//            tts.shutdown();
//            tts = null;
//        }
//        super.onPause();
//    }}
