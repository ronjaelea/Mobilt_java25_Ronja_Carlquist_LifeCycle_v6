package com.gritacademy.draftlifecycle;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.gritacademy.draftlifecycle.model.Gender;
import com.gritacademy.draftlifecycle.model.ProfileRepository;
import com.gritacademy.draftlifecycle.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends BottomNavActivity {
    // extends klassen för nav bar som i sin tur extends AppCompatActivity

    private ProfileRepository repo;
    private ValueEventListener registration;

    private TextView profileData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setUpBottomNav(R.id.profileNav);

        repo = new ProfileRepository();
        profileData = findViewById(R.id.profileData);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ((TextView) findViewById(R.id.profileEmail))
                .setText(user != null ? user.getEmail() : "");

        findViewById(R.id.saveTestProfileBtn).setOnClickListener(v -> saveTestProfile());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // börja lyssna när skärmen blir synlig
        registration = repo.load(new ProfileRepository.Listener() {
            @Override
            public void onLoaded(UserProfile profile) {
                showProfile(profile);
            }

            @Override
            public void onError(DatabaseError error) {
                String msg = getString(R.string.profile_load_failed, error.getMessage());
                profileData.setText(msg);
                Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // sluta lyssna när skärmen inte längre är synlig -> ingen läcka, inga
        // callbacks till en död vy
        if (registration != null) {
            repo.stop(registration);
            registration = null;
        }
    }

    private void saveTestProfile() {
        UserProfile p = new UserProfile();
        p.setName("Ro Ca");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        p.setEmail(user != null ? user.getEmail() : null);
        p.setBirthdate("1995-06-15");
        p.setGender(Gender.WOMAN);
        p.setHeight(181);
        p.setWeight(70);
        p.setNewsletter(true);

        repo.save(p)
                .addOnSuccessListener(unused -> Toast.makeText(
                        this, R.string.profile_saved, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(
                        this, getString(R.string.profile_save_failed, e.getMessage()),
                        Toast.LENGTH_LONG).show());
    }

    private void showProfile(UserProfile p) {
        if (p == null) {
            profileData.setText(R.string.profile_none);
            return;
        }
        // tillfällig diagnostik-vy, Phase 4 ersätter med riktiga fält + labels
        String text = "Name: " + p.getName()
                + "\nEmail: " + p.getEmail()
                + "\nBirthdate: " + p.getBirthdate()
                + "\nGender: " + p.getGender()
                + "\nHeight: " + p.getHeight() + " cm"
                + "\nWeight: " + p.getWeight() + " kg"
                + "\nNewsletter: " + p.isNewsletter();
        profileData.setText(text);
    }
}
