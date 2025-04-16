package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;

public abstract class Interactable {
    public float x, y;
    public Bitmap sprite;

    public abstract void onInteract(Player player);
    public void draw(Canvas canvas, Paint paint, int TILE_SIZE) {
        if (sprite == null) {
            Log.e("DrawDebug", "Missing sprite for " + getClass().getSimpleName());
            return;
        }
        canvas.drawBitmap(Bitmap.createScaledBitmap(sprite, TILE_SIZE, TILE_SIZE, true), x, y, paint);
    }

    protected Bitmap loadSprite(Context context, String filename) {
        Log.d("LoadSprite", "Attempting to load sprite: " + filename);
        try {
            return BitmapFactory.decodeStream(context.getAssets().open("tiles/" + filename));
        } catch (Exception e) {
            Log.e("LoadSprite", "FAILED to load sprite: " + filename);
            e.printStackTrace();
            return fallbackRedSquareBitmap();
        }
    }
    private Bitmap fallbackRedSquareBitmap() {
        int size = 120;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawRect(0, 0, size, size, paint);
        return bmp;
    }
}
