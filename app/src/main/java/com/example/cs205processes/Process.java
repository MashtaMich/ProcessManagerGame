package com.example.cs205processes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Process {
    private String id;
    private String name;
    private int timeLimit; // in seconds
    private int timeRemaining; // in seconds
    private boolean isComplete;
    private boolean isDead;
    private Recipe recipe;
    private static final String[] PROCESS_NAMES = {"P1", "P2", "P3",
            "P4", "P5", "P6",
            "P7", "P8", "P9"};

    public Process(Recipe recipe, int timeLimit) {
        this.id = UUID.randomUUID().toString();
        this.name = PROCESS_NAMES[(int)(Math.random() * PROCESS_NAMES.length)];
        this.recipe = recipe;
        this.timeLimit = timeLimit;
        this.timeRemaining = timeLimit;
        this.isComplete = false;
        this.isDead = false;
    }

    public static Process generateRandomProcess(List<Recipe> availableRecipes) {
        // Random time between 30-60 seconds
        int randomTime = 30 + (int)(Math.random() * 31);
        Recipe randomRecipe = availableRecipes.get((int)(Math.random() * availableRecipes.size()));
        return new Process(randomRecipe, randomTime);
    }

    public void updateTime(int secondsElapsed) {
        if (!isComplete && !isDead) {
            timeRemaining -= secondsElapsed;
            if (timeRemaining <= 0) {
                isDead = true;
                timeRemaining = 0;
            }
        }
    }

    public void completeProcess() {
        this.isComplete = true;
    }

    public boolean isAboutToDie() {
        return timeRemaining <= 10 && !isComplete && !isDead;
    }

    // Getters and setters
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
        return timeRemaining;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean isDead() {
        return isDead;
    }

    public Recipe getRecipe() {
        return recipe;
    }
}