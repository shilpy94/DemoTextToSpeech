package com.example.shilpysamaddar.demotexttospeech;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {


    private RelativeLayout main_layout;
    private TextInputLayout txtText_hint;
    private ImageView speak,stop_speak;
    private TextToSpeech tts;
    private Button btnSpeak;
    private EditText txtText;
    String googleTtsPackage = "com.google.android.tts", picoPackage = "com.svox.pico";
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        main_layout= (RelativeLayout) findViewById(R.id.main_layout);
        txtText_hint= (TextInputLayout) findViewById(R.id.txtText_hint);
        speak= (ImageView) findViewById(R.id.speak);
        stop_speak= (ImageView) findViewById(R.id.stop_speak);
        txtText_hint.setHint("Text to speak");
        // onCreate
        AnimationDrawable animationDrawable =(AnimationDrawable)main_layout.getBackground();
        animationDrawable.setEnterFadeDuration(5000);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        tts = new TextToSpeech(this, this);
        btnSpeak = (Button) findViewById(R.id.btnSpeak);
        txtText = (EditText) findViewById(R.id.txtText);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakOut();
                startAnimations();
                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f,0.94f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f,0.96f);
                scaleDownX.setDuration(150);
                scaleDownY.setDuration(150);
                scaleDownX.setRepeatCount(1);
                scaleDownY.setRepeatCount(1);
                scaleDownX.setRepeatMode(ValueAnimator.REVERSE);
                scaleDownY.setRepeatMode(ValueAnimator.REVERSE);
                scaleDownX.setInterpolator(new DecelerateInterpolator());
                scaleDownY.setInterpolator(new DecelerateInterpolator());
                AnimatorSet scaleDown = new AnimatorSet();
                scaleDown.play(scaleDownX).with(scaleDownY);
                scaleDown.start();

            }

        });
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        stop_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAnimation();
                if (tts != null) {
                    tts.stop();
                }
                if(spokenString!=null) {
                    spokenString="";
                    txtText.setText(spokenString);
                    txtText.setSelection(0);
                }
            }
        });
    }

    private void startAnimations(){
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(stop_speak, "alpha",  0f, 1f);
        fadeOut.setDuration(1500);
        fadeOut.start();
        stop_speak.setVisibility(View.VISIBLE);
    }

    private void stopAnimation(){
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(stop_speak, "alpha",  1f, 10f);
        fadeOut.setDuration(1200);
        fadeOut.start();
        stop_speak.setVisibility(View.GONE);
    }

    private String spokenString="";

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "say something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),"Your device does not support this language",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    spokenString=" "+result.get(0);
                    if(spokenString!=null) {
                        txtText.setText(spokenString);
                        txtText.setSelection(txtText.getText().length());
                    }
                }
                break;
            }

        }
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
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
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
