package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;

import org.json.JSONObject;

public class Pot extends Interactable {
    private enum State { EMPTY, COOKING, DONE }
    private State state;
    private Bitmap emptySprite, cookingSprite, doneSprite;
    private long cookingStartTime;
    private int cookingDuration = 3000; // ms

    public Pot(Context context, float x, float y, JSONObject props) {
        this.x = x;
        this.y = y;

        try {
            this.state = State.valueOf(props.optString("state", "empty").toUpperCase());
            this.cookingDuration = props.optInt("cooking_time", 3000);

            emptySprite = loadSprite(context, props.getString("empty_sprite"));
            cookingSprite = loadSprite(context, props.getString("cooking_sprite"));
            doneSprite = loadSprite(context, props.getString("done_sprite"));


            updateSprite();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInteract(Player player) {
        switch (state) {
            case EMPTY:
                state = State.COOKING;
                cookingStartTime = System.currentTimeMillis();
                updateSprite();
                break;
            case DONE:
                state = State.EMPTY;
                updateSprite();
                break;
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int TILE_SIZE) {
        if (sprite == null) {
            Log.e("DrawDebug", "Missing sprite for " + getClass().getSimpleName());
            return;
        }
        if (state == State.COOKING && System.currentTimeMillis() - cookingStartTime >= cookingDuration) {
            state = State.DONE;
            updateSprite();
        }
        canvas.drawBitmap(Bitmap.createScaledBitmap(sprite, TILE_SIZE, TILE_SIZE, true), x, y, paint);
    }

    private void updateSprite() {
        switch (state) {
            case EMPTY: sprite = emptySprite; break;
            case COOKING: sprite = cookingSprite; break;
            case DONE: sprite = doneSprite; break;
        }
    }

}
