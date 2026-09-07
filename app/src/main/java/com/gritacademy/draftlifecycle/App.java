package com.gritacademy.draftlifecycle;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

    // för att cacha data om offline
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // måste ske 1 gång, före anv av databasen
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}