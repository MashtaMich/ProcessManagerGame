package com.example.cs205processes;

import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class Player {
    private PlayerInventory inventory;
    private Bitmap sprite;
    private Bitmap scaledSprite;
    private float x, y;
    private float targetX, targetY;
    private boolean isMoving = false;
    private final float moveSpeed = 12f; // pixels per frame (~10 frames = 1 tile)
    private int queuedDX = 0, queuedDY = 0;
    private boolean movementHeld = false;
    private int tileSize = 120;
    private Game game;



    public Player(float x, float y, Bitmap sprite, int tileSize, Game game) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.sprite = sprite;
        this.tileSize = tileSize;
        this.game = game;
        this.inventory = new PlayerInventory(); // Default inventory, will be replaced
    }

    public void setInventory(PlayerInventory inventory) {
        this.inventory = inventory;
    }
    public PlayerInventory getInventory() {
        return inventory;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void draw(Canvas canvas, Paint paint, int tileSize) {
        if (sprite == null) return;
        
        // Create scaled sprite if it doesn't exist or if tileSize changed
        if (scaledSprite == null || scaledSprite.getWidth() != tileSize) {
            if (scaledSprite != null) {
                scaledSprite.recycle(); // Recycle old bitmap to free memory
            }
            scaledSprite = Bitmap.createScaledBitmap(sprite, tileSize, tileSize, true);
        }
        
        canvas.drawBitmap(scaledSprite, x, y, paint);
    }


    public boolean isNear(Interactable obj, int tileSize) {
        float dx = Math.abs(x - obj.x);
        float dy = Math.abs(y - obj.y);
        
        // Use a more lenient distance check (1.5 tiles)
        float threshold = tileSize * 1.5f;
        boolean isNear = dx < threshold && dy < threshold;
        
        Log.d("Player", "isNear check: dx=" + dx + ", dy=" + dy + ", threshold=" + threshold + ", result=" + isNear);
        return isNear;
    }

    public void move(int dx, int dy) {
        movementHeld = true;
        queuedDX = dx;
        queuedDY = dy;

        if (!isMoving) {
            moveToNextTile();
        }
    }
    public void stopMovement() {
        movementHeld = false;
    }


    private void moveToNextTile() {
        float nextX = x + queuedDX * tileSize;
        float nextY = y + queuedDY * tileSize;

        // Convert to tile indices
        int tileCol = (int) (nextX / tileSize);
        int tileRow = (int) (nextY / tileSize);

        // Boundary check
        if (tileCol < 0 || tileCol >= Game.MAP_WIDTH || tileRow < 0 || tileRow >= Game.MAP_HEIGHT) {
            bounce(queuedDX, queuedDY);
            isMoving = false;
            return;
        }

        if (game.canMoveTo(nextX, nextY)) {
            targetX = nextX;
            targetY = nextY;
            isMoving = true;
        } else {
            bounce(queuedDX, queuedDY);
            isMoving = false;
        }
    }


    public void update() {
        if (!isMoving) return;

        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < moveSpeed) {
            x = targetX;
            y = targetY;
            isMoving = false;

            if (movementHeld) {
                moveToNextTile();
            }
        } else {
            x += moveSpeed * dx / dist;
            y += moveSpeed * dy / dist;
        }
    }



    public void bounce(float dx, float dy) {
        final float bounceDist = 10f; // how much to nudge
        final int duration = 50;      // how fast it returns

        x += dx * bounceDist;
        y += dy * bounceDist;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            x -= dx * bounceDist;
            y -= dy * bounceDist;
        }, duration);
    }

    public Game getGame() {
        return game;
    }


}
