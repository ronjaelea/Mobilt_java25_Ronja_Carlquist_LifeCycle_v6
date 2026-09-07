package com.gritacademy.draftlifecycle;

import android.os.Bundle;

public class StepCounterActivity extends BottomNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        setUpBottomNav(R.id.stepCounterNav);
    }
}
