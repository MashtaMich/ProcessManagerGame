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
        while (deltaSum > deltaLimit) { // update only if certain time has passed
            if (!step.test(deltaSum)) { // executes the step function
                // if step returns false, we consume the entire delta at once
                deltaSum %= deltaLimit;
                break;
            }
            // if step returns true, we consume one deltaLimit unit at a time
            deltaSum -= deltaLimit;
        }
    }
}