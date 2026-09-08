package com.gritacademy.draftlifecycle;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

    /** för att kunna ha cachad data om offline */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}