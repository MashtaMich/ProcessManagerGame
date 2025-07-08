package com.game.cookingspree.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

public class ScreenUtils {
    private static DisplayMetrics displayMetrics;
    private static float density;
    private static int screenWidth;
    private static int screenHeight;
    private static float scaleFactor = 1.0f;
    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (isInitialized) return;

        displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        density = displayMetrics.density;
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        // Calculate scale factor based on screen density and size
        calculateScaleFactor();
        isInitialized = true;
    }

    private static void calculateScaleFactor() {
        // Base measurements (designed for ~1080x1920 at 420dpi)
        float baseWidth = 1080f;
        float baseDensity = 2.625f; // 420dpi / 160dpi

        // Calculate scale factor based on both screen size and density
        float widthScale = screenWidth / baseWidth;
        float densityScale = density / baseDensity;

        // Use a weighted average, favoring density scaling
        scaleFactor = (densityScale * 0.7f) + (widthScale * 0.3f);

        // Clamp scale factor to reasonable bounds
        scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 2.0f));
    }

    public static int dpToPx(float dp) {
        if (!isInitialized) throw new IllegalStateException("ScreenUtils not initialized");
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics));
    }

    public static int spToPx(float sp) {
        if (!isInitialized) throw new IllegalStateException("ScreenUtils not initialized");
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics));
    }

    public static int getScaledTileSize() {
        // Base tile size in dp
        int baseTileSizeDp = 60;
        return Math.round(dpToPx(baseTileSizeDp) * scaleFactor);
    }

    public static int getScaledSize(int baseSizeDp) {
        return Math.round(dpToPx(baseSizeDp) * scaleFactor);
    }

    public static float getScaleFactor() {
        return scaleFactor;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK)
                >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static String getScreenCategory() {
        if (!isInitialized) return "normal";

        float screenInches = (float) Math.sqrt(
                Math.pow(screenWidth / displayMetrics.xdpi, 2) +
                        Math.pow(screenHeight / displayMetrics.ydpi, 2)
        );

        if (screenInches < 5.0) return "small";
        else if (screenInches < 7.0) return "normal";
        else return "large";
    }

    public static int calculateOptimalTileSize(int mapWidth, int mapHeight, int reservedUiHeight) {
        int availableWidth = screenWidth;
        int availableHeight = screenHeight - reservedUiHeight;

        // Calculate tile size to fit the map optimally
        int tileByWidth = availableWidth / mapWidth;
        int tileByHeight = availableHeight / mapHeight;

        // Use the smaller dimension to ensure the map fits
        int calculatedTileSize = Math.min(tileByWidth, tileByHeight);

        // Apply minimum and maximum bounds
        int minTileSize = dpToPx(40);
        int maxTileSize = dpToPx(100);

        return Math.max(minTileSize, Math.min(calculatedTileSize, maxTileSize));
    }

    public static boolean isLandscape() {
        return screenWidth > screenHeight;
    }

    public static float getDensity() {
        return density;
    }
}