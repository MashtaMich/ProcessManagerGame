package com.example.cs205processes;

import android.util.Log;

import java.util.List;
import java.util.UUID;

public class Process {
    private static final String TAG = "Process";

    private final String id;
    private final String name;
    private final int timeLimit; // in seconds
    private int timeRemaining; // in seconds
    private boolean isComplete;
    private boolean isDead;
    private final Recipe recipe;
    private final Object mutex = new Object();

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

        Log.d(TAG, "New process created: " + name + ", Recipe: " + recipe.getName() + ", Time: " + timeLimit + "s");
    }

    public static Process generateRandomProcess(List<Recipe> availableRecipes) {
        // Random time between 30-60 seconds
        int randomTime = 30 + (int)(Math.random() * 31);
        Recipe randomRecipe = availableRecipes.get((int)(Math.random() * availableRecipes.size()));
        return new Process(randomRecipe, randomTime);
    }

    public void updateTime(int secondsElapsed) {
        synchronized (mutex) {
            // Only update if not complete and not dead
            if (!isComplete && !isDead) {
                timeRemaining -= secondsElapsed;

                // Check for death
                if (timeRemaining <= 0) {
                    timeRemaining = 0;
                    isDead = true;
                    Log.d(TAG, "Process died: " + name);
                }
            }
        }
    }

    public void completeProcess() {
        synchronized (mutex) {
            if (!isComplete && !isDead) {
                isComplete = true;
                Log.d(TAG, "Process completed: " + name);
            }
        }
    }

    public boolean isAboutToDie() {
        synchronized (mutex) {
            return timeRemaining <= 10 && !isComplete && !isDead;
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