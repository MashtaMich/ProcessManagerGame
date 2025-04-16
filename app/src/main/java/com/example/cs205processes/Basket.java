package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;

import org.json.JSONObject;

public class Basket extends Interactable {
    private String ingredient;

    public Basket(Context context, float x, float y, JSONObject props) {
        this.x = x;
        this.y = y;

        try {
            ingredient = props.getString("contents").toLowerCase();
            String spriteName = props.getString("basket_" + ingredient + "_sprite");
            sprite = loadSprite(context, spriteName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInteract(Player player) {
        System.out.println("Player took a " + ingredient);
        // TODO: Add to player's inventory or similar logic
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
