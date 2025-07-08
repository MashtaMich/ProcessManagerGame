package com.game.cookingspree;

import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.game.cookingspree.util.PrefsHelper;
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
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.Credential;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
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
        // clear credentials first then sign in to new account
        clearCredentials(() -> startSignInFlow(onSuccess));
    }

    private void clearCredentials(Runnable afterClear){
        credentialManager.clearCredentialStateAsync(
                new ClearCredentialStateRequest(),
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(@NonNull Void result) {
                        Log.d("AccountManager", "Credential state cleared, starting sign-in");
                        afterClear.run();
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.e("AccountManager", "Failed to clear credential state", e);
                        Toast.makeText(activity, "Failed to clear old credentials", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void startSignInFlow(Runnable onSuccess){
        // setup google sign in option
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true) //change to true for checking if alr have accounts used to sign in before?
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)  //for automatic sign in
//                .setNonce(<nonce string to use when generating a Google ID token>) //for security?
                .build();

        // prepare credential request
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // call credential manager
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

    @SuppressWarnings("unused") // Optional: suppress false warning
    private void handleGoogleCredential(CustomCredential credential, Runnable onSuccess) {
        Bundle data = credential.getData();
        GoogleIdTokenCredential googleCred = GoogleIdTokenCredential.createFrom(data);
        String idToken = googleCred.getIdToken();

        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase sign-in successful");
                        createChefProfileIfNeeded(() -> {
                            syncFromFirestoreToPrefs(() -> {
                                if (onSuccess != null) onSuccess.run();
                            });
                        });
                    } else {
                        Log.e(TAG, "Firebase sign-in failed", task.getException());
                    }
                });
    }

    /*
=======================================
📦 Firestore Database Structure (chefs/{uid})
=======================================

Each chef document is organized into nested maps for modularity, clarity, and scalability:

- profile:        // Basic account information
    - uid          (String)
    - email        (String)
    - chefName     (String)
    - chefCode     (String)
    - photoUrl     (String)

- stats:          // Game performance statistics
    - gamesPlayed  (int)
    - highScore    (int)
    - averageScore (float)

- settings:       // Device-specific preferences (synced for convenience)
    - joystickScale (float: 1.0f for small, 1.4f for large)
    - volume         (int: 0–100)

- following:      // Social features (friend/follow system)
    - {uid}: true   (Map of followed users by their UID)
*/

    private void createChefProfileIfNeeded(Runnable onSuccess) {
        FirebaseUser user = getCurrentUser();
        if (user == null) return;

        // Fetch user stats
        String uid = user.getUid();
        String email = user.getEmail();
        String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

        db.collection("chefs").document(uid).get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                promptForChefName(chefName ->
                    generateUniqueChefCode(chefName, chefCode -> {
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("uid", uid);
                        profile.put("email", email);
                        profile.put("chefName", chefName);
                        profile.put("chefCode", chefCode);
                        profile.put("photoUrl", photoUrl);
                        profile.put("dailyStreak", 0);

                        Map<String, Object> stats = new HashMap<>();
                        stats.put("gamesPlayed", 0);
                        stats.put("highScore", 0);
                        stats.put("averageScore", 0f);

                        Map<String, Object> settings = new HashMap<>();
                        settings.put("joystickScale", PrefsHelper.getJoystickScale());
                        settings.put("volume", PrefsHelper.getVolume());
                        settings.put("language", PrefsHelper.getLanguage());

                        Map<String, Object> chefDoc = new HashMap<>();
                        chefDoc.put("profile", profile);
                        chefDoc.put("stats", stats);
                        chefDoc.put("settings", settings);

                        db.collection("chefs").document(uid).set(chefDoc)
                                .addOnSuccessListener(unused -> onSuccess.run());
                    })
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

    // run on app start
    public void syncFromFirestoreToPrefs(Runnable onDone) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            if (onDone != null) onDone.run();
            return;
        }

        db.collection("chefs").document(user.getUid()).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // Profile
                Map<String, Object> profile = (Map<String, Object>) doc.get("profile");
                if (profile != null) {
                    Object name = profile.get("chefName");
                    if (name != null) {
                        PrefsHelper.setChefName(name.toString());
                        Log.d("Prefs_debug AccM", "chefName is " + name.toString());
                    }else{
                        Log.d("Prefs_debug AccM", "chefName is null from firebase");
                    }
                    Number streak = (Number) profile.get("dailyStreak");
                    if (streak != null) PrefsHelper.setDailyStreak(streak.intValue());
                    if (profile.get("chefCode") != null)
                        PrefsHelper.setChefCode(Objects.requireNonNull(profile.get("chefCode")).toString());

                    if (profile.get("photoUrl") != null)
                        PrefsHelper.setPhotoUrl(Objects.requireNonNull(profile.get("photoUrl")).toString());
                    }

                // Stats
                Map<String, Object> stats = (Map<String, Object>) doc.get("stats");
                if (stats != null) {
                    Number gamesPlayed = (Number) stats.get("gamesPlayed");
                    Number highScore = (Number) stats.get("highScore");
                    Number averageScore = (Number) stats.get("averageScore");

                    if (gamesPlayed != null) PrefsHelper.setGamesPlayed(gamesPlayed.intValue());
                    if (highScore != null) PrefsHelper.setHighScore(highScore.intValue());
                    if (averageScore != null) PrefsHelper.setAverageScore(averageScore.floatValue());
                }

                // Settings
                Map<String, Object> settings = (Map<String, Object>) doc.get("settings");
                if (settings != null) {
                    Object scale = settings.get("joystickScale");
                    Object volume = settings.get("volume");
                    String lang = (String) settings.get("language");
                    if (lang != null) PrefsHelper.setLanguage(lang);

                    if (scale instanceof Float) PrefsHelper.setJoystickScale((float) scale);
                    if (volume instanceof Number) PrefsHelper.setVolume(((Number) volume).intValue());
                }
            }
            if (onDone != null) onDone.run();
        })
                .addOnFailureListener(e -> {
                    Log.e("Prefs_debug AccM", "Failed to fetch Firestore", e);
                    if (onDone != null) onDone.run();
                });
    }

    public void updateSetting(String key, Object value) {
        db.collection("chefs").document(getCurrentUser().getUid())
                .update("settings." + key, value);
    }

    public void updateStat(String key, Object value) {
        db.collection("chefs").document(getCurrentUser().getUid())
                .update("stats." + key, value);
    }

    public void updateProfileField(String key, Object value) {
        db.collection("chefs").document(getCurrentUser().getUid())
                .update("profile." + key, value);
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
