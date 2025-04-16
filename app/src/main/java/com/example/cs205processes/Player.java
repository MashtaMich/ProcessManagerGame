package com.example.cs205processes;

import android.graphics.*;

public class Player {
    private float x, y;
    private Bitmap sprite;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
        this.sprite = null; // Load later
    }
    public float getX() { return x; }
    public float getY() { return y; }

    public void draw(Canvas canvas, Paint paint, int tileSize) {
        if (sprite == null) return;
        canvas.drawBitmap(Bitmap.createScaledBitmap(sprite, tileSize, tileSize, true), x, y, paint);
    }

    public boolean isNear(Interactable obj, int tileSize) {
        float dx = Math.abs(x - obj.x);
        float dy = Math.abs(y - obj.y);
        return dx < tileSize && dy < tileSize;
    }

    public void tryMove(int dx, int dy, int tileSize, Game game) {
        float nextX = x + dx * tileSize;
        float nextY = y + dy * tileSize;
        if (game.canMoveTo(nextX, nextY)) {
            x = nextX;
            y = nextY;
        }
    }

}
