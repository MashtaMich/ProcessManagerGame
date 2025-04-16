package com.example.cs205processes;

public class ElapsedTimer {
    private long lastTimestamp = System.currentTimeMillis();
    private boolean paused = false;
    private long pauseTimestamp = 0;

    public long progress() {
        if (paused) return 0;

        long now = System.currentTimeMillis();
        long delta = now - lastTimestamp;
        lastTimestamp = now;
        return delta;
    }

    public void pause() {
        if (!paused) {
            pauseTimestamp = System.currentTimeMillis();
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            // When resuming, update the lastTimestamp to the current time
            // This effectively ignores the time that passed while paused
            lastTimestamp = System.currentTimeMillis();
            paused = false;
        }
    }
    
    public boolean isPaused() {
        return paused;
    }
}
