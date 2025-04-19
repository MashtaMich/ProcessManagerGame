package com.example.cs205processes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread gameThread;
    private boolean isRunning = false;
    private Game game;

    private int screenWidth;//To help scale the map
    private int screenHeight;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void init(Game game) {
        this.game = game;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Get device screen's dimensions
//        this.screenHeight=getHeight();
//        this.screenWidth=getWidth();

        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            game.update();
            game.draw();
            try {
                Thread.sleep(game.getSleepTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean useCanvas(Game.CanvasCallback callback) {
        Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            if (canvas != null) {
                callback.draw(canvas);
                return true;
            }
        } finally {
            if (canvas != null) {
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
        return false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // No-op for now. Required by SurfaceHolder.Callback.
    }

}
