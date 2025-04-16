package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

public class RubbishBin extends Interactable {
    private Bitmap openSprite, closedSprite;
    private boolean isClosed = true;

    public RubbishBin(Context context, float x, float y, JSONObject props) {
        this.x = x;
        this.y = y;

        try {
            closedSprite = loadSprite(context, props.getString("closed_sprite"));
            openSprite = loadSprite(context, props.getString("open_sprite"));
            sprite = closedSprite;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInteract(Player player) {
        if (isClosed) {
            isClosed = false;
            sprite = openSprite;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isClosed = true;
                sprite = closedSprite;
            }, 500);
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int TILE_SIZE) {
        if (sprite == null) {
            Log.e("DrawDebug", "Missing sprite for " + getClass().getSimpleName());
            return;
        }
        canvas.drawBitmap(Bitmap.createScaledBitmap(sprite, TILE_SIZE, TILE_SIZE, true), x, y, paint);
    }
}
