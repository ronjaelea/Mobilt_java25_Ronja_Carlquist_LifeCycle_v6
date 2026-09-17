package com.gritacademy.draftlifecycle.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gritacademy.draftlifecycle.model.UserProfile;

/** läser/skriver profilen på users/uid (Firebase) */
public class ProfileRepository {

    /** ProfileActivity implementerar interface för att kunna hämta uppgifter vid onStart() */
    public interface Listener {
        void onLoaded(@Nullable UserProfile profile); // null = ingen profil sparad än
        void onError(DatabaseError error);
    }

    private final DatabaseReference ref;

    public ProfileRepository() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // ej säkrat mot null här men man kommer bara hit om inloggad
        ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
    }

    /** skriver profile-objektet till rätt path i firebase (ref)
     * task för att kunna visa fel...
     * anropas i EditProfileActivity */
    public Task<Void> save(UserProfile profile) {
        return ref.setValue(profile);
    }

     /** körs vid varje ändring
      * returnerar inte UserProfile till ProfileActivity utan ValueEventListener
      * (datan kommer som callback) */
    public ValueEventListener load(Listener listener) {
        ValueEventListener registration = new ValueEventListener() {
            @Override
            public void onDataChange(@Nullable DataSnapshot snapshot) {
                listener.onLoaded(snapshot.getValue(UserProfile.class));
            }
            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error);
            }
        };  // om permission saknas (ej om internet saknas, då finns cache)
        ref.addValueEventListener(registration); // automatisk uppdatering vid ändring av data
        return registration;
    }

    public void stop(ValueEventListener registration) {
        ref.removeEventListener(registration);
    }

    /** läser bara en gång för att kunna prefill formuläret */
    public void loadOnce(Listener listener) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onLoaded(snapshot.getValue(UserProfile.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error);
            }
        });
    }

    /** skriver bara ett fält (users/uid/weight), inte hela objektet.
     * (för att kunna ha weight spinner direkt på profilsidan utan att behöva öppna form */
    public Task<Void> updateWeight(int kg) {
        return ref.child("weight").setValue(kg);
    }
}
