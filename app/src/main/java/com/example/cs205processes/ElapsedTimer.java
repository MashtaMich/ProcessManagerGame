package com.example.cs205processes;

public class ElapsedTimer {
    private long updateStartTime = 0L;
    private boolean initialized = false;

    public long getUpdateStartTime() {
        return updateStartTime;
    }

    public long progress() {
        final long now = System.currentTimeMillis();
        if (!initialized) {
            initialized = true;
            updateStartTime = now;
        }
        final long delta = now - updateStartTime;
        updateStartTime = now;
        return delta;
    }
}