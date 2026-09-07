package com.gritacademy.draftlifecycle.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/* läser/skriver profilen på users/<uid>, skapas bara när någon är inloggad. */
public class ProfileRepository {

    // ProfileActivity implementerar för att kunna anropa
    public interface Listener {
        void onLoaded(@Nullable UserProfile profile); // null = ingen profil sparad än
        void onError(DatabaseError error);
    }

    private final DatabaseReference ref;

    public ProfileRepository() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // ej säkrat mot null här men man kommer bara hit om inloggad
        ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
    }

    // skriver profile-objektet till rätt path i firebase (ref)
    // Task för att kunna visa fel...
    public Task<Void> save(UserProfile profile) {
        return ref.setValue(profile);
    }

    // körs vid varje ändring
    // returnerar inte UserProfile till ProfileActivity utan ValueEventListener
    // (datan kommer som callback)
    public ValueEventListener load(Listener listener) {
        ValueEventListener registration = new ValueEventListener() {
            @Override
            public void onDataChange(@Nullable DataSnapshot snapshot) {
                listener.onLoaded(snapshot.getValue(UserProfile.class));
            }
            // om permission saknas (ej om internet saknas, då finns cache)
            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error);
            }
        };
        ref.addValueEventListener(registration); // automatisk uppdatering vid ändring av data
        return registration;
    }

    public void stop(ValueEventListener registration) {
        ref.removeEventListener(registration);
    }
}
