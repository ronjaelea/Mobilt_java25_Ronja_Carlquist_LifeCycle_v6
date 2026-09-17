package com.gritacademy.draftlifecycle;

import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BottomNavActivity extends AppCompatActivity {

    protected void setUpBottomNav(@IdRes int currentItemId) {
        BottomNavigationView nav = findViewById(R.id.viewNavBar);

        // markera nuvarande activity's nav tab
        nav.setSelectedItemId(currentItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // checka först om det är current för att ej köra startActivity i onödan
            if (id == currentItemId) {
                return true;
            } else if (id == R.id.profileNav) {
                openScreen(ProfileActivity.class);
                return true;
            } else if (id == R.id.stepCounterNav) {
                openScreen(StepCounterActivity.class);
                return true;
            } else if (id == R.id.logoutNav) {
                confirmLogout();
                return false;
            }
            return false;
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.log_out, (dialog, which) -> logOut())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void openScreen(Class<?> target) {
        startActivity(new Intent(this, target));
        finish(); // lägg inte i back stack
        overridePendingTransition(0, 0); // ingen slide..
            // DEPRECATED!?
    }
    private void logOut() {
        stopService(new Intent(this, StepCounterService.class));
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        // efter logout kan man ej backa "tillbaka in"
        startActivity(intent);
        finish();
    }
}
