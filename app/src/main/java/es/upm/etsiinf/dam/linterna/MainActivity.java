package es.upm.etsiinf.dam.linterna;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private boolean isFlashOn=false;
    private CameraManager mCameraManager;
    private String mCameraId;
    private SharedPreferences sp;
    private ImageButton imageButton;

    public static boolean isOpen = false;
    private BroadcastReceiver mReceiver;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isOpen = true;

        sp = getSharedPreferences("flashlight_status",MODE_PRIVATE);
        isFlashOn = sp.getBoolean("onoff",false);
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, TorchService.class);
        startService(intent);

        imageButton = findViewById(R.id.flashlight_button);

        if(isFlashOn){
            imageButton.setImageResource(R.drawable.ic_baseline_flash_off_24);
            imageButton.setBackground(ContextCompat.getDrawable(this,R.drawable.flashlight_button_background2));
        }else{
            imageButton.setImageResource(R.drawable.ic_baseline_flash_on);
            imageButton.setBackground(ContextCompat.getDrawable(this,R.drawable.flashlight_button_background));
        }

        imageButton.setOnClickListener(view -> {
            isFlashOn = sp.getBoolean("onoff",false);
            if(!isFlashOn){
                turnOnFlash();
            }else{
                turnOffFlash();
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive (Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "UPDATE_IMAGE_BUTTON_ON":
                        imageButton.setImageResource(R.drawable.ic_baseline_flash_off_24);
                        imageButton.setBackground(ContextCompat.getDrawable(context,R.drawable.flashlight_button_background2));
                        Toast.makeText(context,"Linterna ON", Toast.LENGTH_SHORT).show();
                        break;
                    case "UPDATE_IMAGE_BUTTON_OFF":
                        imageButton.setImageResource(R.drawable.ic_baseline_flash_on);
                        imageButton.setBackground(ContextCompat.getDrawable(context,R.drawable.flashlight_button_background));
                        Toast.makeText(context,"Linterna OFF", Toast.LENGTH_SHORT).show();
                        break;
                    default:

                        break;
                }
            }
        };


    }

    private void turnOnFlash() {
        try {
            mCameraManager.setTorchMode(mCameraId, true);
            Toast.makeText(this,"Linterna ON", Toast.LENGTH_SHORT).show();
            isFlashOn = true;
            imageButton.setImageResource(R.drawable.ic_baseline_flash_off_24);
            imageButton.setBackground(ContextCompat.getDrawable(this,R.drawable.flashlight_button_background2));
            sp.edit()
                    .putBoolean("onoff",true)
                    .apply();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void turnOffFlash() {
        try {
            mCameraManager.setTorchMode(mCameraId, false);
            Toast.makeText(this,"Linterna OFF", Toast.LENGTH_SHORT).show();
            isFlashOn = false;
            imageButton.setImageResource(R.drawable.ic_baseline_flash_on);
            imageButton.setBackground(ContextCompat.getDrawable(this,R.drawable.flashlight_button_background));
            sp.edit()
                    .putBoolean("onoff",false)
                    .apply();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        isOpen = false;
    }

    @Override
    protected void onResume () {
        super.onResume();
        IntentFilter filter = new IntentFilter("UPDATE_IMAGE_BUTTON_ON");
        IntentFilter filter2 = new IntentFilter("UPDATE_IMAGE_BUTTON_OFF");
        registerReceiver(mReceiver, filter);
        registerReceiver(mReceiver, filter2);
    }


    @Override
    protected void onPause () {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}