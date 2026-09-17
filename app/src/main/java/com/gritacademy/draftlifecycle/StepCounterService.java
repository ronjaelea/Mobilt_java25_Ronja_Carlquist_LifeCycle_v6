package com.gritacademy.draftlifecycle;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gritacademy.draftlifecycle.util.StepDetector;
import com.gritacademy.draftlifecycle.util.StepStore;

/**
 * räknar steg i bakgrunden. foreground service har sin egen livscykel,
 * även om den inte har någon activity
 */
public class StepCounterService extends Service implements SensorEventListener {

    public static final String ACTION_STOP = "com.gritacademy.draftlifecycle.STOP";
    public static final String ACTION_RESET = "com.gritacademy.draftlifecycle.RESET";

    public static volatile boolean RUNNING = false;

    private static final String CHANNEL_ID = "steps";
    private static final int NOTIF_ID = 1;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private StepDetector detector;
    private StepStore store;

    @Override
    public void onCreate() {
        super.onCreate();
        store = new StepStore(this);
        detector = new StepDetector();
        sensorManager = getSystemService(SensorManager.class);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;

        if (ACTION_STOP.equals(action)) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (ACTION_RESET.equals(action)) {
            store.resetNow();
            detector.reset();
            updateNotification();
            return START_STICKY;
        }

        if (accelerometer == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        store.resetIfNewDay();
        startForeground(NOTIF_ID, buildNotification(store.getSteps()));
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        RUNNING = true;
        return START_STICKY; // starta om ifall systemet dödar tjänsten
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        RUNNING = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        if (store.resetIfNewDay()) {   // midnatt passerades medan tjänsten körde
            detector.reset();
        }
        boolean stepTaken = detector.onSample(
                event.values[0], event.values[1], event.values[2],
                SystemClock.elapsedRealtime());
        if (stepTaken) {
            store.setSteps(store.getSteps() + 1); // aktiviteten lyssnar på denna ändring
            updateNotification();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // används ej, bara abstrakt metod från interfacet
    }


    /**
     * notifikation
     */
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notif_channel_steps),
                    NotificationManager.IMPORTANCE_LOW); // ingen ljud/vibration
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(int steps) {
        PendingIntent openApp = PendingIntent.getActivity(
                this, 0,
                new Intent(this, StepCounterActivity.class),
                PendingIntent.FLAG_IMMUTABLE);

        PendingIntent stop = PendingIntent.getService(
                this, 0,
                new Intent(this, StepCounterService.class).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_text, steps))
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(openApp)
                .addAction(0, getString(R.string.stop), stop)
                .build();
    }

    private void updateNotification() {
        getSystemService(NotificationManager.class)
                .notify(NOTIF_ID, buildNotification(store.getSteps()));
    }
}