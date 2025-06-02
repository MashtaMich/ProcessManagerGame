package com.game.cookingspree;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;
import java.util.function.Consumer;

import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.Credential;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import android.os.CancellationSignal;
import android.widget.Toast;


import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

public class AccountManager {
    private final Activity activity;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final CredentialManager credentialManager;
    private final String TAG = "AccountManager";

    public AccountManager(Activity activity) {
        this.activity = activity;
        FirebaseApp.initializeApp(activity);
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.credentialManager = CredentialManager.create(activity);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signIn(Runnable onSuccess) {
        Log.d("AccountManager", "signIn() called");

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
            activity,
            request,
            new CancellationSignal(),
            ContextCompat.getMainExecutor(activity),
            new CredentialManagerCallback<>() {
                @Override
                public void onResult(@NonNull GetCredentialResponse result) {
                    Log.d("AccountManager", "CredentialManager returned a result");
                    Credential credential = result.getCredential();
                    if (credential instanceof CustomCredential) {
                        handleGoogleCredential((CustomCredential) credential, onSuccess);
                    }
                }

                @Override
                public void onError(@NonNull GetCredentialException e) {
                    Log.e("AccountManager", "CredentialManager failed", e);
                    Toast.makeText(activity, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        );

    }

    private void handleGoogleCredential(CustomCredential credential, Runnable onSuccess) {
        Bundle data = credential.getData();
        GoogleIdTokenCredential googleCred = GoogleIdTokenCredential.createFrom(data);
        String idToken = googleCred.getIdToken();

        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase sign-in successful");
                        createChefProfileIfNeeded(onSuccess);
                    } else {
                        Log.e(TAG, "Firebase sign-in failed", task.getException());
                    }
                });
    }

    private void createChefProfileIfNeeded(Runnable onSuccess) {
        FirebaseUser user = getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        String email = user.getEmail();
        String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

        db.collection("chefs").document(uid).get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                promptForChefName(chefName ->
                        generateUniqueChefCode(chefName, chefCode ->
                                db.collection("chefs").document(uid).set(
                                        new Chef(uid, email, chefName, chefCode, photoUrl)
                                ).addOnSuccessListener(unused -> onSuccess.run())
                        )
                );
            } else {
                onSuccess.run();
            }
        });
    }

    private void promptForChefName(Consumer<String> callback) {
        EditText input = new EditText(activity);
        input.setHint("Enter your Chef name");

        new AlertDialog.Builder(activity)
                .setTitle("Welcome, Chef!")
                .setMessage("Choose a name that others will see.")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) callback.accept(name);
                    else promptForChefName(callback); // try again
                })
                .show();
    }

    private void generateUniqueChefCode(String baseName, Consumer<String> callback) {
        tryGenerate(baseName, callback);
    }

    private void tryGenerate(String baseName, Consumer<String> callback) {
        String tag = String.format(Locale.US, "#%04d", new Random().nextInt(10000));

        String code = baseName + tag;

        db.collection("chefs").whereEqualTo("chefCode", code)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        callback.accept(code);
                    } else {
                        tryGenerate(baseName, callback); // try another
                    }
                });
    }

    public void syncPreferencesToLocal() {
        FirebaseUser user = getCurrentUser();
        if (user == null) return;

        SharedPreferences prefs = activity.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        db.collection("chefs").document(user.getUid()).get().addOnSuccessListener(doc -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> prefMap = (Map<String, Object>) doc.get("preferences");
            if (prefMap != null) {
                Object joystickScale = prefMap.get("joystickScale");
                float scale = joystickScale instanceof Number ? ((Number) joystickScale).floatValue() : 1.0f;

                Object soundPref = prefMap.get("soundEnabled");
                boolean sound = soundPref instanceof Boolean ? (Boolean) soundPref : true;


                prefs.edit()
                        .putFloat("joystickScale", scale)
                        .putBoolean("soundEnabled", sound)
                        .apply();
            }
        });
    }

    public void uploadPreferencesFromLocal() {
        FirebaseUser user = getCurrentUser();
        if (user == null) return;

        SharedPreferences prefs = activity.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        float scale = prefs.getFloat("joystickScale", 1.0f);
        boolean sound = prefs.getBoolean("soundEnabled", true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("preferences.joystickScale", scale);
        updates.put("preferences.soundEnabled", sound);

        db.collection("chefs").document(user.getUid()).update(updates);
    }

    public void followByChefCode(String inputCode, Consumer<Boolean> callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            callback.accept(false);
            return;
        }

        String myUid = user.getUid();

        db.collection("chefs")
            .whereEqualTo("chefCode", inputCode)
            .limit(1)
            .get()
            .addOnSuccessListener(snapshot -> {
                if (!snapshot.isEmpty()) {
                    String targetUid = snapshot.getDocuments().get(0).getString("uid");
                    if (myUid.equals(targetUid)) {
                        callback.accept(false);
                        return;
                    }

                    db.collection("chefs").document(myUid)
                            .update("following", FieldValue.arrayUnion(targetUid))
                            .addOnSuccessListener(unused -> callback.accept(true))
                            .addOnFailureListener(e -> callback.accept(false));
                } else {
                    callback.accept(false);
                }
            })
            .addOnFailureListener(e -> callback.accept(false));
    }
}
