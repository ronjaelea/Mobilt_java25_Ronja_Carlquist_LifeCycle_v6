package com.gritacademy.draftlifecycle;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gritacademy.draftlifecycle.model.Gender;
import com.gritacademy.draftlifecycle.model.ProfileRepository;
import com.gritacademy.draftlifecycle.model.UserProfile;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import java.util.Calendar;
import java.util.Locale;

/** formuläret för att uppdatera användaruppgifter */
public class EditProfileActivity extends AppCompatActivity {

    private ProfileRepository repo;

    private TextInputEditText editName, editBirthdate, editHeight, editWeight;
    private RadioGroup editGender;
    private CheckBox editNewsletter;
    // riktigt newsletter finns ej, endast för att använda checkbox i uppgiften
    private String selectedBirthdate; // "yyyy-mm-dd" eller null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        repo = new ProfileRepository();

        editName = findViewById(R.id.editName);
        editBirthdate = findViewById(R.id.editBirthdate);
        editHeight = findViewById(R.id.editHeight);
        editWeight = findViewById(R.id.editWeight);
        editGender = findViewById(R.id.editGender);
        editNewsletter = findViewById(R.id.editNewsletter);

        editBirthdate.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.saveProfileBtn).setOnClickListener(v -> save());

        prefill();
    }

    /** laddar en gång för att fylla i fälten med nuvarande värden. */
    private void prefill() {
        repo.loadOnce(new ProfileRepository.Listener() {
            @Override
            public void onLoaded(UserProfile p) {
                if (p == null) {
                    editGender.check(R.id.genderUnspecified);
                    return;
                }
                editName.setText(p.getName());
                selectedBirthdate = p.getBirthdate();
                editBirthdate.setText(p.getBirthdate());
                if (p.getHeight() != null) editHeight.setText(String.valueOf(p.getHeight()));
                if (p.getWeight() != null) editWeight.setText(String.valueOf(p.getWeight()));
                editNewsletter.setChecked(p.isNewsletter());
                checkGender(p.getGender());
            }

            @Override
            public void onError(DatabaseError error) {
                Toast.makeText(EditProfileActivity.this,
                        getString(R.string.profile_load_failed, error.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void showDatePicker() {
        Calendar start = Calendar.getInstance();
        String seed = currentBirthdate();
        if (seed != null) {
            String[] parts = seed.split("-");
            if (parts.length == 3) {
                try {
                    start.set(Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]) - 1, // DatePicker-månad är 0-baserad
                            Integer.parseInt(parts[2]));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        /** detta objekt finns ej i xml-filen, måste instansieras här, renderas med dialog.show() */
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedBirthdate = String.format(Locale.US, "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    editBirthdate.setText(selectedBirthdate);
                },
                start.get(Calendar.YEAR),
                start.get(Calendar.MONTH),
                start.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // visa ej framtida datum
        dialog.show();
    }

    private void save() {
        String name = textOf(editName);
        String birthdate = currentBirthdate();
        if (TextUtils.isEmpty(name) || birthdate == null) {
            Toast.makeText(this, R.string.edit_validation, Toast.LENGTH_LONG).show();
            return; // minst name & birthdate måste finnas
        }

        UserProfile p = new UserProfile();
        p.setName(name);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        p.setEmail(user != null ? user.getEmail() : null);
        p.setBirthdate(birthdate);
        p.setGender(selectedGender());
        p.setHeight(intOrNull(editHeight));
        p.setWeight(intOrNull(editWeight));
        p.setNewsletter(editNewsletter.isChecked());

        repo.save(p)
                .addOnSuccessListener(u -> {
                    Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
                    finish(); // tillbaka till Profile; dess live-lyssnare repaint:ar
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this, getString(R.string.profile_save_failed, e.getMessage()),
                        Toast.LENGTH_LONG).show());
    }

    /**
     * för att kunna tilldela rätt enum value till rätt radiobtn
     */
    private Gender selectedGender() {
        int id = editGender.getCheckedRadioButtonId();
        if (id == R.id.genderWoman) return Gender.WOMAN;
        if (id == R.id.genderMan) return Gender.MAN;
        if (id == R.id.genderNonBinary) return Gender.NON_BINARY;
        return Gender.UNSPECIFIED;
    }

    /** om gender == null i databas väljs unspecified */
    private void checkGender(Gender g) {
        if (g == null) {
            editGender.check(R.id.genderUnspecified);
            return;
        }
        switch (g) {
            case WOMAN: editGender.check(R.id.genderWoman); break;
            case MAN: editGender.check(R.id.genderMan); break;
            case NON_BINARY: editGender.check(R.id.genderNonBinary); break;
            default: editGender.check(R.id.genderUnspecified);
        }
    }

    /** vald födelsedag: det sparade fältet i db, annars vad som väljs i picker  */
    private String currentBirthdate() {
        if (selectedBirthdate != null) return selectedBirthdate;
        String shown = textOf(editBirthdate);
        return shown.isEmpty() ? null : shown;
    }

    /** hjälpmetod för att trimma name och birthdate */
    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

/** om age elr weight är empty sparas null.
  * user input är tekniskt sett String, måste köra parseInt(s) för att matcha objektet */
    private Integer intOrNull(TextInputEditText field) {
        String s = textOf(field);
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
