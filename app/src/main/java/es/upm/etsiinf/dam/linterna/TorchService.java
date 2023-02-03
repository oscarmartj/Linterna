package es.upm.etsiinf.dam.linterna;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class TorchService extends Service {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private CameraManager mCameraManager;
    private ShakeDetector mShakeDetector;
    private String mCameraId;

    private boolean torchOn = false;
    private Notification notification;
    private boolean isFlashOn = false;
    private long mLastShakeTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "linterna_channel";
        CharSequence channelName = "Linterna";
        int importance = NotificationManager.IMPORTANCE_MIN;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);

        notificationManager.createNotificationChannel(channel);


         notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Linterna")
                .setContentText("El servicio de linterna está ejecutándose en segundo plano")
                .setSmallIcon(R.drawable.ic_flashlight)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
        startForeground(123, notification);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        mShakeDetector = new ShakeDetector();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (Exception e) {
            Toast.makeText(this, "Error al acceder a la cámara", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake (int count) {
                long currentTime = System.currentTimeMillis();
                if(currentTime - mLastShakeTime < 1000){
                    return;
                }
                mLastShakeTime = currentTime;
                if(isFlashOn){
                    turnOffFlash();
                }else{
                    turnOnFlash();
                }
            }
        });
        return START_STICKY;
    }

    private void turnOnFlash() {
        try {
            mCameraManager.setTorchMode(mCameraId, true);
            isFlashOn = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void turnOffFlash() {
        try {
            mCameraManager.setTorchMode(mCameraId, false);
            isFlashOn = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

