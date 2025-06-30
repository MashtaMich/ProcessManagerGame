package com.game.cookingspree.util;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.game.cookingspree.AccountManager;
import com.game.cookingspree.BaseActivity;

import java.util.Map;

public class PrefsHelper {
    private static SharedPreferences prefs;
    private static SharedPreferences gameSavePrefs;
    private static AccountManager accountManager;

    public static void init(Context context, AccountManager am) {
        prefs = context.getSharedPreferences("chef_prefs", Context.MODE_PRIVATE);
        gameSavePrefs = context.getSharedPreferences("GameSave", Context.MODE_PRIVATE);
        accountManager = am;
    }

// SETTINGS
    // Volume setting (0–100)
    public static void setVolume(int volume) {
        prefs.edit().putInt("settings_volume", volume).apply();
        if (accountManager != null) {
            accountManager.updateSetting("volume", volume);
        }else{
            Log.d("Prefs_debug", "Null account manager when setting volume");
        }
    }

    public static int getVolume() {
        return prefs.getInt("settings_volume", 100);
    }

    // Joystick size setting ("small", "large")
    public static void setJoystickScale(float scale) {
        prefs.edit().putFloat("settings_joystick_scale", scale).apply();
        if (accountManager != null) {
            accountManager.updateSetting("joystickScale", scale);
        }
    }

    public static float getJoystickScale() {
        return prefs.getFloat("settings_joystick_scale", BaseActivity.JOYSTICK_SCALE_DEFAULT);
    }
    // Language
    public static void setLanguage(String langCode) {
        prefs.edit().putString("settings_language", langCode).apply();
        if (accountManager != null) {
            accountManager.updateSetting("language", langCode);
        }

    }
    public static String getLanguage() {
        return prefs.getString("settings_language", "en"); // default English
    }

// STATS
    // Games played
    public static void setGamesPlayed(int count) {
        prefs.edit().putInt("stats_games_played", count).apply();
        if (accountManager != null) {
            accountManager.updateStat("gamesPlayed", count);
        }
    }

    public static int getGamesPlayed() {
        return prefs.getInt("stats_games_played", 0);
    }

    // Average score
    public static void setAverageScore(float score) {
        prefs.edit().putFloat("stats_average_score", score).apply();
        if (accountManager != null) {
            accountManager.updateStat("averageScore", score);
        }
    }

    public static float getAverageScore() {
        return prefs.getFloat("stats_average_score", 0f);
    }

    // High score
    public static void setHighScore(int score) {
        prefs.edit().putInt("stats_high_score", score).apply();
        if (accountManager != null) {
            accountManager.updateStat("highScore", score);
        }
    }

    public static int getHighScore() {
        return prefs.getInt("stats_high_score", 0);
    }

// PROFILE
    public static void setChefName(String name) {
        prefs.edit().putString("profile_chef_name", name).apply();
        if (accountManager != null) {
            accountManager.updateProfileField("chefName", name);
        }
    }

    public static String getChefName() {
        return prefs.getString("profile_chef_name", "No chef name");
    }

    public static void clearChefName() {
        prefs.edit().remove("profile_chef_name").apply();
    }
    // Chef Code
    public static void setChefCode(String code) {
        prefs.edit().putString("profile_chef_code", code).apply();
        if (accountManager != null) {
            accountManager.updateProfileField("chefCode", code);
        }
    }
    public static String getChefCode() {
        return prefs.getString("profile_chef_code", ""); // fallback to empty
    }

    // Photo URL
    public static void setPhotoUrl(String url) {
        prefs.edit().putString("profile_photo_url", url).apply();
        if (accountManager != null) {
            accountManager.updateProfileField("photoUrl", url);
        }
    }
    public static String getPhotoUrl() {
        return prefs.getString("profile_photo_url", ""); // fallback to default
    }

    public static void setDailyStreak(int count) {
        prefs.edit().putInt("profile_daily_streak", count).apply();
        if (accountManager != null) {
            accountManager.updateProfileField("dailyStreak", count);
        }
    }
    public static int getDailyStreak() {
        return prefs.getInt("profile_daily_streak", 0);
    }

//RESET
    // Clear everything (e.g. on logout or reset)
    public static void clearAll() {
        prefs.edit().clear().apply();
        clearSaveState();
    }

//GAME SAVE
    public static void clearSaveState() {
        gameSavePrefs.edit().clear().apply();
    }

    public static boolean hasSyncedThisSession() {
        return prefs.getBoolean("synced_once", false);
    }

    public static void setSyncedThisSession(boolean synced) {
        prefs.edit().putBoolean("synced_once", synced).apply();
    }
    public static Map<String, ?> getAll() {
        return prefs.getAll();
    }


}
