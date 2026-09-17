package com.gritacademy.draftlifecycle;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.gritacademy.draftlifecycle.steps.StepStore;

/**
 * visar stegräkningen och startar/stoppar tjänsten som sköter sensorn & räkningen
 */
public class StepCounterActivity extends BottomNavActivity {

    private StepStore store;
    private SharedPreferences stepPrefs;

    private TextView stepCountView;
    private Button toggleBtn;
    private Button resetBtn;

    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            (prefs, key) -> {
                if (StepStore.KEY_COUNT.equals(key)) updateStepView();
            };

    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> startCounting());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        setUpBottomNav(R.id.stepCounterNav);

        store = new StepStore(this);
        stepPrefs = StepStore.prefs(this);

        stepCountView = findViewById(R.id.currentStepCount);
        toggleBtn = findViewById(R.id.toggleCountingBtn);
        resetBtn = findViewById(R.id.resetStepsBtn);

        toggleBtn.setOnClickListener(v -> {
            if (StepCounterService.RUNNING) {
                stopCounting();
            } else {
                ensurePermissionThenStart();
            }
        });

        resetBtn.setOnClickListener(v -> {
            if (StepCounterService.RUNNING) {
                startService(new Intent(this, StepCounterService.class)
                        .setAction(StepCounterService.ACTION_RESET));
            } else {
                store.resetNow();
            }
            updateStepView();
        });

        SensorManager sm = getSystemService(SensorManager.class);
        if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            toggleBtn.setEnabled(false);
            resetBtn.setEnabled(false);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.sensor_unavailable_title)
                    .setMessage(R.string.no_step_sensor)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        stepPrefs.registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        store.resetIfNewDay();
        updateStepView();
        syncToggleLabel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stepPrefs.unregisterOnSharedPreferenceChangeListener(prefListener);
    }

    private void ensurePermissionThenStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            startCounting();
        }
    }

    private void startCounting() {
        ContextCompat.startForegroundService(this,
                new Intent(this, StepCounterService.class));
        toggleBtn.setText(R.string.stop_counting);
    }

    private void stopCounting() {
        startService(new Intent(this, StepCounterService.class)
                .setAction(StepCounterService.ACTION_STOP));
        toggleBtn.setText(R.string.start_counting);
    }

    private void syncToggleLabel() {
        toggleBtn.setText(StepCounterService.RUNNING
                ? R.string.stop_counting : R.string.start_counting);
    }

    private void updateStepView() {
        stepCountView.setText(String.valueOf(store.getSteps()));
    }
}
