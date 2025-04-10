package com.example.cs205processes;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameManager {
    private static final int POINTS_PER_COMPLETED_PROCESS = 100;
    private static final int POINTS_DEDUCTION_FOR_DEAD_PROCESS = 500;
    private static final int MAX_DEAD_PROCESSES = 3;

    private List<Process> activeProcesses;
    private List<Recipe> availableRecipes;
    private int score;
    private int deadProcessCount;
    private boolean isGameOver;
    private Random random;
    private Handler processSpawnHandler;
    private Runnable processSpawnRunnable;
    private Handler gameTickHandler;
    private Runnable gameTickRunnable;
    private Context context;
    private GameListener gameListener;

    public interface GameListener {
        void onProcessAdded(Process process);
        void onProcessCompleted(Process process);
        void onProcessDied(Process process);
        void onProcessAboutToDie(Process process);
        void onScoreChanged(int newScore);
        void onGameOver(int finalScore);
    }

    public GameManager(Context context, GameListener listener) {
        this.context = context;
        this.gameListener = listener;
        this.activeProcesses = new ArrayList<>();
        this.availableRecipes = Recipe.getDefaultRecipes();
        this.score = 0;
        this.deadProcessCount = 0;
        this.isGameOver = false;
        this.random = new Random();

        processSpawnHandler = new Handler();
        gameTickHandler = new Handler();
    }

    public void startGame() {
        isGameOver = false;
        score = 0;
        deadProcessCount = 0;
        activeProcesses.clear();

        // Start the process spawning logic
        scheduleNextProcess();

        // Start the game tick for updating timers
        startGameTick();
    }

    private void scheduleNextProcess() {
        int spawnDelay = 5000 + random.nextInt(8000); // 5-13 seconds between processes

        processSpawnRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    spawnNewProcess();
                    scheduleNextProcess();
                }
            }
        };

        processSpawnHandler.postDelayed(processSpawnRunnable, spawnDelay);
    }

    private void spawnNewProcess() {
        Process newProcess = Process.generateRandomProcess(availableRecipes);
        activeProcesses.add(newProcess);
        if (gameListener != null) {
            gameListener.onProcessAdded(newProcess);
        }
    }

    private void startGameTick() {
        gameTickRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    updateProcesses();
                    gameTickHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };

        gameTickHandler.post(gameTickRunnable);
    }

    private void updateProcesses() {
        Iterator<Process> iterator = activeProcesses.iterator();
        while (iterator.hasNext()) {
            Process process = iterator.next();

            if (!process.isComplete() && !process.isDead()) {
                process.updateTime(1);

                if (process.isAboutToDie()) {
                    // Notify that process is about to die - for vibration
                    if (gameListener != null) {
                        gameListener.onProcessAboutToDie(process);
                    }

                    // Vibrate the device
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null && vibrator.hasVibrator()) {
                        vibrator.vibrate(500); // Vibrate for 500ms
                    }
                }

                if (process.isDead()) {
                    deadProcessCount++;
                    score -= POINTS_DEDUCTION_FOR_DEAD_PROCESS;

                    if (gameListener != null) {
                        gameListener.onProcessDied(process);
                        gameListener.onScoreChanged(score);
                    }

                    if (deadProcessCount >= MAX_DEAD_PROCESSES) {
                        endGame();
                    }
                }
            }
        }
    }

    public void completeProcess(String processId) {
        for (Process process : activeProcesses) {
            if (process.getId().equals(processId) && !process.isComplete() && !process.isDead()) {
                process.completeProcess();
                score += POINTS_PER_COMPLETED_PROCESS;

                if (gameListener != null) {
                    gameListener.onProcessCompleted(process);
                    gameListener.onScoreChanged(score);
                }
                break;
            }
        }
    }

    private void endGame() {
        isGameOver = true;
        processSpawnHandler.removeCallbacks(processSpawnRunnable);
        gameTickHandler.removeCallbacks(gameTickRunnable);

        if (gameListener != null) {
            gameListener.onGameOver(score);
        }

        Toast.makeText(context, "Game Over! Final Score: " + score, Toast.LENGTH_LONG).show();
    }

    public void pauseGame() {
        processSpawnHandler.removeCallbacks(processSpawnRunnable);
        gameTickHandler.removeCallbacks(gameTickRunnable);
    }

    public void resumeGame() {
        if (!isGameOver) {
            scheduleNextProcess();
            startGameTick();
        }
    }

    public void stopGame() {
        processSpawnHandler.removeCallbacks(processSpawnRunnable);
        gameTickHandler.removeCallbacks(gameTickRunnable);
    }

    // Getters
    public List<Process> getActiveProcesses() {
        return activeProcesses;
    }

    public int getScore() {
        return score;
    }

    public int getDeadProcessCount() {
        return deadProcessCount;
    }

    public boolean isGameOver() {
        return isGameOver;
    }
}