package com.example.cs205processes;

public class ElapsedTimer {
    private long lastTimestamp = System.currentTimeMillis();
    private boolean paused = false;

    public long progress() {
        if (paused) return 0;

        long now = System.currentTimeMillis();
        long delta = now - lastTimestamp;
        lastTimestamp = now;
        return delta;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        lastTimestamp = System.currentTimeMillis(); // Reset time tracking
        paused = false;
    }
}
