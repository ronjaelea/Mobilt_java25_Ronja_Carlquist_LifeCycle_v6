package com.gritacademy.draftlifecycle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.gritacademy.draftlifecycle.model.Gender;
import com.gritacademy.draftlifecycle.model.ProfileRepository;
import com.gritacademy.draftlifecycle.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

/** extends nav-bar-klassen som i sin tur extends AppCompatActivity */
public class ProfileActivity extends BottomNavActivity {

    /** nedanstående 2 ints endast för att begränsa spinnerns intervall,
     *  i formuläret kan vilket nummer som helst sparas */
    private static final int WEIGHT_MIN = 30;
    private static final int WEIGHT_MAX = 250;
    private ProfileRepository repo;
    private ValueEventListener profileUpdate;
    private TextView valueName, valueEmail, valueAge, valueGender, valueHeight, valueBmi;
    private NumberPicker weightPicker;
    private Button saveWeightBtn;
    private UserProfile lastProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setUpBottomNav(R.id.profileNav);

        repo = new ProfileRepository();

        valueName = findViewById(R.id.valueName);
        valueEmail = findViewById(R.id.valueEmail);
        valueAge = findViewById(R.id.valueAge);
        valueGender = findViewById(R.id.valueGender);
        valueHeight = findViewById(R.id.valueHeight);
        valueBmi = findViewById(R.id.valueBmi);

        weightPicker = findViewById(R.id.weightPicker);
        weightPicker.setMinValue(WEIGHT_MIN);
        weightPicker.setMaxValue(WEIGHT_MAX);
        weightPicker.setWrapSelectorWheel(false);

        saveWeightBtn = findViewById(R.id.saveWeightBtn);
        saveWeightBtn.setOnClickListener(v -> saveWeight());
        setWeightControlsEnabled(false); // tills en profil laddats

        findViewById(R.id.editProfileBtn).setOnClickListener(
                v -> startActivity(new Intent(this, EditProfileActivity.class)));
                // ingen finish() här, denna sida hamnar i back stack
                // så bakåt från formuläret blir i princip cancel + backa till profilen

        /** till skillnad fr övr fält hämtas email inte från de sparade fälten i db
         * utan från FirebaseUser */
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        valueEmail.setText(getString(R.string.label_email,
                user != null ? user.getEmail() : getString(R.string.value_none)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // live-lyssnare, uppdaterar automatiskt även när man kommer tillbaka från formuläret
        profileUpdate = repo.load(new ProfileRepository.Listener() {
            @Override
            public void onLoaded(UserProfile profile) {
                showProfile(profile);
            }
            @Override
            public void onError(DatabaseError error) {
                Toast.makeText(ProfileActivity.this,
                        getString(R.string.profile_load_failed, error.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (profileUpdate != null) {
            repo.stop(profileUpdate);
            profileUpdate = null;
        }
    }

    private void showProfile(UserProfile p) {
        lastProfile = p;
        String none = getString(R.string.value_none);

        if (p == null) {
            valueName.setText(getString(R.string.label_name, none));
            valueAge.setText(getString(R.string.label_age, none));
            valueGender.setText(getString(R.string.label_gender, none));
            valueHeight.setText(getString(R.string.label_height, none));
            valueBmi.setText(getString(R.string.label_bmi, none));
            setWeightControlsEnabled(false);
            return;
        }

        valueName.setText(getString(R.string.label_name,
                p.getName() != null ? p.getName() : none));

        int age = p.getAge();
        valueAge.setText(getString(R.string.label_age,
                age >= 0 ? String.valueOf(age) : none));

        valueGender.setText(getString(R.string.label_gender, genderLabel(p.getGender())));

        valueHeight.setText(getString(R.string.label_height,
                p.getHeight() != null ? String.valueOf(p.getHeight()) : none));

        double bmi = p.getBmi();
        valueBmi.setText(getString(R.string.label_bmi,
                Double.isNaN(bmi) ? none : String.format(Locale.US, "%.1f", bmi)));

        setWeightControlsEnabled(true);
        if (p.getWeight() != null) {
            int clamped = Math.max(WEIGHT_MIN, Math.min(WEIGHT_MAX, p.getWeight()));
            weightPicker.setValue(clamped);
        }
    }

    /** weight kan sparas direkt på profilsidan utan att öppna formuläret */
    private void saveWeight() {
        if (lastProfile == null) return;
        repo.updateWeight(weightPicker.getValue())
                .addOnSuccessListener(u -> Toast.makeText(
                        this, R.string.weight_updated, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(
                        this, getString(R.string.profile_save_failed, e.getMessage()),
                        Toast.LENGTH_LONG).show());
    }

    private void setWeightControlsEnabled(boolean enabled) {
        weightPicker.setEnabled(enabled);
        saveWeightBtn.setEnabled(enabled);
    }

    /** enum values ska alltid vara UPPERCASE men vill
     * visa det snyggare så anger custom strings */
    private String genderLabel(Gender g) {
        if (g == null) return getString(R.string.value_none);
        switch (g) {
            case WOMAN: return getString(R.string.gender_woman);
            case MAN: return getString(R.string.gender_man);
            case NON_BINARY: return getString(R.string.gender_non_binary);
            default: return getString(R.string.gender_unspecified);
        }
    }
}