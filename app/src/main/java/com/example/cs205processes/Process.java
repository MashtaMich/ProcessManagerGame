package com.example.cs205processes;

import android.util.Log;

import java.util.List;
import java.util.UUID;

public class Process {
    private static final String TAG = "Process";
    private static final long TIME_INTERVAL_MS = 1000; // 1 second interval

    private final String id;
    private final String name;
    private final int timeLimit; // in seconds
    private int timeRemaining; // in seconds
    private boolean isComplete;
    private boolean isDead;
    private final Recipe recipe;
    private final Object mutex = new Object();

    private final ElapsedTimer elapsedTimer = new ElapsedTimer();
    private final DeltaStepper timeStepper;

    private static final String[] CUSTOMER_NAMES = {
            "Customer A", "Customer B", "Customer C"
    };

    public Process(Recipe recipe, int timeLimit) {
        this.id = UUID.randomUUID().toString();
        this.name = CUSTOMER_NAMES[(int)(Math.random() * CUSTOMER_NAMES.length)];
        this.recipe = recipe;
        this.timeLimit = timeLimit;
        this.timeRemaining = timeLimit;
        this.isComplete = false;
        this.isDead = false;

        // Initialize the DeltaStepper with our time update logic
        this.timeStepper = new DeltaStepper(TIME_INTERVAL_MS, this::timeStep);

        Log.d(TAG, "New process created: " + name + ", Recipe: " + recipe.getName() + ", Time: " + timeLimit + "s");
    }

    public static Process generateRandomProcess(List<Recipe> availableRecipes) {
        // Random time between 30-60 seconds
        int randomTime = 30 + (int)(Math.random() * 31);
        Recipe randomRecipe = availableRecipes.get((int)(Math.random() * availableRecipes.size()));
        return new Process(randomRecipe, randomTime);
    }
    public static Process generateRandomProcess(Recipe recipe, int timeLimit, int timeRemaining) {
        Process process = new Process(recipe, timeLimit);
        // Access the private field via reflection or add a package-private setter
        process.timeRemaining = timeRemaining;
        return process;
    }

    // This is called by the DeltaStepper
    private boolean timeStep(long deltaTime) {
        synchronized (mutex) {
            if (!isComplete && !isDead) {
                timeRemaining -= 1; // Decrement by 1 second

                // Check for death
                if (timeRemaining <= 0) {
                    timeRemaining = 0;
                    isDead = true;
                    Log.d(TAG, "Process died: " + name);
                }
            }
        }
        return true; // Continue processing time steps
    }

    // This will be called from GameManager
    public void updateTime() {
        // Only update if the timer is not paused
        if (!elapsedTimer.isPaused()) {
            long delta = elapsedTimer.progress();
            if (delta > 0) {
                timeStepper.update(delta);
            }
        }
    }
    
    // Add methods to pause and resume the process timer
    public void pauseTimer() {
        elapsedTimer.pause();
    }
    
    public void resumeTimer() {
        elapsedTimer.resume();
    }

    public void completeProcess() {
        synchronized (mutex) {
            if (!isComplete && !isDead) {
                isComplete = true;
                Log.d(TAG, "Process completed: " + name);
            }
        }
    }

    // Getters with thread-safe access
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getTimeRemaining() {
        synchronized (mutex) {
            return timeRemaining;
        }
    }

    public boolean isComplete() {
        synchronized (mutex) {
            return isComplete;
        }
    }

    public boolean isDead() {
        synchronized (mutex) {
            return isDead;
        }
    }

    public Recipe getRecipe() {
        return recipe;
    }

    // For debugging
    @Override
    public String toString() {
        synchronized (mutex) {
            return "Process{" + "name='" + name + '\'' + ", recipe=" + recipe.getName() + ", timeRemaining=" + timeRemaining + ", isComplete=" + isComplete + ", isDead=" + isDead + '}';
        }
    }
}
