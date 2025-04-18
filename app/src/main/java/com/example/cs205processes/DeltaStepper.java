package com.example.cs205processes;

import java.util.function.LongPredicate;

public class DeltaStepper {

    private final long deltaLimit;
    private final LongPredicate step;
    private long deltaSum = 0L;

    public DeltaStepper(final long deltaLimit, final LongPredicate step) {
        this.deltaLimit = deltaLimit;
        this.step = step;
    }

    public void update(long delta) {
        deltaSum += delta;
        while (deltaSum > deltaLimit) {
            if (!step.test(deltaSum)) {
                deltaSum %= deltaLimit;
                break;
            }
            deltaSum -= deltaLimit;
        }
    }
}