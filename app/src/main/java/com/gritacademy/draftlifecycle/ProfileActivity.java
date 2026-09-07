package com.gritacademy.draftlifecycle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class ProfileActivity extends BottomNavActivity {
    // extends klassen för nav bar som i sin tur extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setUpBottomNav(R.id.profileNav);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ((TextView) findViewById(R.id.profileEmail))
                .setText(user != null ? user.getEmail() : "");

        // övriga användaruppgifter //

        // formulär / knapp för formulär //


    }
}
