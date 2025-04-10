package com.example.cs205processes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.util.DisplayMetrics;

public class Game {

    public interface CanvasCallback {
        void draw(Canvas canvas);
    }

    private final GameView gameView;
    private final Paint paint = new Paint();

    private final Bitmap playerBitmap;
    private final Bitmap mapBitmap;
    private float playerX, playerY;

    private final int TILE_SIZE;

    public Game(GameView gameView, Context context) {
        this.gameView = gameView;

        // Get screen size
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        int TILES_PER_ROW = 13;


        // Use smaller dimension to calculate tile size
        TILE_SIZE = (int)Math.ceil(Math.min(screenWidth, screenHeight) / (double)TILES_PER_ROW);

        // Scale player to 1 tile
        Bitmap rawPlayer = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
        playerBitmap = Bitmap.createScaledBitmap(rawPlayer, TILE_SIZE, TILE_SIZE, true);

        // Load map and scale it to 13x13 tiles
        Bitmap rawMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.kitchen);
        mapBitmap = Bitmap.createScaledBitmap(rawMap, TILE_SIZE * 13, TILE_SIZE * 13, true);

        // Initial player position (top-left tile)
        playerX = TILE_SIZE;
        playerY = TILE_SIZE;
    }

    public void update() {
        // Add logic like collision here later
    }

    public void draw() {
        gameView.useCanvas(canvas -> {
            canvas.drawColor(0xFFFFFFFF); // Clear screen
            canvas.drawBitmap(mapBitmap, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
            canvas.drawBitmap(playerBitmap, playerX, playerY, paint);
        });
    }

    public long getSleepTime() {
        return 16; // ~60 FPS
    }

    // Movement controls (1 tile per tap)
    public void moveUp()    { playerY -= TILE_SIZE; }
    public void moveDown()  { playerY += TILE_SIZE; }
    public void moveLeft()  { playerX -= TILE_SIZE; }
    public void moveRight() { playerX += TILE_SIZE; }
}
