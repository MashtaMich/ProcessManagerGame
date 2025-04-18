package com.example.cs205processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // Player position
    private float playerX;
    private float playerY;

    // Game score and state
    private int score;
    private int deadProcessCount;

    // Minimal recipe and process state needed for restoration
    private List<ProcessInfo> activeProcesses = new ArrayList<>();

    // Basic getters and setters
    public float getPlayerX() { return playerX; }
    public void setPlayerX(float x) { playerX = x; }

    public float getPlayerY() { return playerY; }
    public void setPlayerY(float y) { playerY = y; }

    public int getScore() { return score; }
    public void setScore(int s) { score = s; }

    public int getDeadProcessCount() { return deadProcessCount; }
    public void setDeadProcessCount(int count) { deadProcessCount = count; }

    public List<ProcessInfo> getActiveProcesses() { return activeProcesses; }
    public void setActiveProcesses(List<ProcessInfo> processes) { activeProcesses = processes; }

    // Simple inner class to store process information
    public static class ProcessInfo implements Serializable {
        private String recipeName;
        private int timeRemaining;
        private int timeLimit;

        public ProcessInfo(String name, int remaining, int limit) {
            recipeName = name;
            timeRemaining = remaining;
            timeLimit = limit;
        }

        public String getRecipeName() { return recipeName; }
        public int getTimeRemaining() { return timeRemaining; }
        public int getTimeLimit() { return timeLimit; }
    }
}