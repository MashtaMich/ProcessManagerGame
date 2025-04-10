package com.example.cs205processes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;

public class Game {

    public interface CanvasCallback {
        void draw(Canvas canvas);
    }

    private final GameView gameView;
    private final Bitmap playerBitmap;
    private final Bitmap mapBitmap;
    private final Paint paint = new Paint();

    private float playerX = 300, playerY = 300;
    private final float speed = 10;

    private long lastUpdateTime;

    public Game(GameView gameView, Context context) {
        this.gameView = gameView;
        this.playerBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);

        this.mapBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.kitchen); // Your image
    }

    public void update() {
        // game logic (e.g., collision detection) here
    }

    public void draw() {
        gameView.useCanvas(canvas -> {
            canvas.drawColor(0xFFFFFFFF); // Clear to white
            canvas.drawBitmap(mapBitmap, 0, 0, paint); // Map stays fixed
            canvas.drawBitmap(playerBitmap, playerX, playerY, paint); // Player
        });
    }

    public long getSleepTime() {
        return 16; // ~60 FPS
    }

    // Movement controls
    public void moveUp() { playerY -= speed; }
    public void moveDown() { playerY += speed; }
    public void moveLeft() { playerX -= speed; }
    public void moveRight() { playerX += speed; }
}
