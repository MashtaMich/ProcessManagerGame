package com.game.cookingspree;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

/** @noinspection BusyWait*/
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread gameThread;
    private boolean isRunning = false;
    private Game game;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void init(Game game) {
        this.game = game;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("GameView","Failed to handle surfaceDestroyed:"+e.getLocalizedMessage());
            //e.printStackTrace();
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
                Log.e("GameView","Failed to handle run:"+e.getLocalizedMessage());
            }
        }
    }

    public void useCanvas(Game.CanvasCallback callback) {
        Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            if (canvas != null) {
                callback.draw(canvas);
            }
        } finally {
            if (canvas != null) {
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }
}