package com.gritacademy.draftlifecycle;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.gritacademy.draftlifecycle.steps.StepDetector;
import com.gritacademy.draftlifecycle.steps.StepStore;

public class StepCounterActivity extends BottomNavActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private StepDetector detector;
    private StepStore store;

    private int steps;

    private TextView stepCountView;
    private Button resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        setUpBottomNav(R.id.stepCounterNav);

        store = new StepStore(this);
        detector = new StepDetector();

        stepCountView = findViewById(R.id.currentStepCount);
        resetBtn = findViewById(R.id.resetStepsBtn);
        resetBtn.setOnClickListener(v -> {
            store.resetNow();
            steps = 0;
            detector.reset();
            updateStepView();
        });

        sensorManager = getSystemService(SensorManager.class);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            resetBtn.setEnabled(false);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.sensor_unavailable_title)
                    .setMessage(R.string.no_step_sensor)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        store.resetIfNewDay(); // om ny dag medan appen var stängd
        steps = store.getSteps();
        updateStepView();

        if (accelerometer != null) {
            // registrera lyssnaren först när skärmen är i förgrunden -> sparar batteri
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // avregistrera direkt när vi lämnar foreground, annars drar sensorn ström i onödan
        sensorManager.unregisterListener(this);
        store.setSteps(steps);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        if (store.resetIfNewDay()) { // om klockan slog midnatt medan skärmen var på
            steps = 0;
            detector.reset();
        }

        boolean stepTaken = detector.onSample(
                event.values[0], event.values[1], event.values[2],
                SystemClock.elapsedRealtime());
        if (stepTaken) {
            steps++;
            store.setSteps(steps);
            updateStepView();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // behövs inte för stegräkning
    }

    private void updateStepView() {
        stepCountView.setText(String.valueOf(steps));
    }
}
