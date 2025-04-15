package com.example.cs205processes;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class GameManager {
    private static final String TAG = "GameManager";
    private static final int POINTS_PER_COMPLETED_PROCESS = 100;
    private static final int POINTS_DEDUCTION_FOR_DEAD_PROCESS = 500;
    private static final int MAX_DEAD_PROCESSES = 3;
    private static final int TIMER_INTERVAL_MS = 16; // Update more frequently for smoother animation (~60 FPS)
    private static final int MAX_ACTIVE_PROCESSES = 5; // Maximum number of active processes

    // Timer utilities for smooth timing
    private final ElapsedTimer elapsedTimer = new ElapsedTimer();
    private final DeltaStepper timerStepper;

    // Simple mutex for thread synchronization
    private final Object mutex = new Object();

    // Process collections
    private final List<Process> activeProcesses;
    private final Queue<Process> processQueue; // Queue for pending processes
    private final List<Process> pendingRemovals;

    private final List<Recipe> availableRecipes;
    private int score;
    private int deadProcessCount;
    private boolean isGameOver;
    private final Random random;
    private final Handler mainHandler;
    private final Handler gameTickHandler;
    private Runnable processSpawnRunnable;
    private Runnable gameTickRunnable;
    private final Context context;
    private final GameListener gameListener;

    public interface GameListener {
        void onProcessAdded(Process process);
        void onProcessCompleted(Process process);
        void onProcessDied(Process process);
        void onProcessAboutToDie(Process process);
        void onScoreChanged(int newScore);
        void onGameOver(int finalScore);
        void onTimerTick(); // Notify UI of every timer tick
        void onQueueChanged(int queueSize); // Notify about changes in the queue
    }

    public GameManager(Context context, GameListener listener) {
        this.context = context;
        this.gameListener = listener;
        this.activeProcesses = new ArrayList<>();
        this.processQueue = new LinkedList<>();
        this.pendingRemovals = new ArrayList<>();
        this.availableRecipes = Recipe.getDefaultRecipes();
        this.score = 0;
        this.deadProcessCount = 0;
        this.isGameOver = false;
        this.random = new Random();

        // Initialize timer stepper
        this.timerStepper = new DeltaStepper(1000, this::tickUpdate);

        // Create handlers on the main thread
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gameTickHandler = new Handler(Looper.getMainLooper());
    }

    // This method updates the UI every second
    private boolean tickUpdate(long deltaTime) {
        if (gameListener != null) {
            gameListener.onTimerTick();
        }
        return true;
    }

    public void startGame() {
        Log.d(TAG, "Starting game");
        isGameOver = false;
        score = 0;
        deadProcessCount = 0;

        // Clear any existing processes
        synchronized (mutex) {
            activeProcesses.clear();
            processQueue.clear();
            pendingRemovals.clear();
        }

        // Start the process spawning logic
        scheduleNextProcess();

        // Start the game tick for updating timers
        startGameTick();
    }

    private void scheduleNextProcess() {
        // Randomize spawn delay for variety but keep it game-appropriate
        int spawnDelay = 5000 + random.nextInt(8000); // 5-13 seconds between processes

        Log.d(TAG, "Scheduling next process in " + spawnDelay + "ms");

        processSpawnRunnable = () -> {
            if (!isGameOver) {
                generateNewProcess();
                scheduleNextProcess();
            }
        };

        mainHandler.postDelayed(processSpawnRunnable, spawnDelay);
    }

    private void generateNewProcess() {
        Process newProcess = Process.generateRandomProcess(availableRecipes);

        synchronized (mutex) {
            // Check if we can add directly to active processes
            if (activeProcesses.size() < MAX_ACTIVE_PROCESSES) {
                activeProcesses.add(newProcess);
                Log.d(TAG, "New process added directly: " + newProcess.getName());

                if (gameListener != null) {
                    gameListener.onProcessAdded(newProcess);
                }
            } else {
                // Otherwise, add to the queue
                processQueue.add(newProcess);
                Log.d(TAG, "New process added to queue: " + newProcess.getName() +
                        ", Queue size: " + processQueue.size());

                if (gameListener != null) {
                    gameListener.onQueueChanged(processQueue.size());
                }
            }
        }
    }

    private void checkProcessQueue() {
        synchronized (mutex) {
            // Check if we can move processes from queue to active
            while (!processQueue.isEmpty() && activeProcesses.size() < MAX_ACTIVE_PROCESSES) {
                Process process = processQueue.poll();
                activeProcesses.add(process);

                Log.d(TAG, "Process moved from queue to active: " + process.getName() +
                        ", Queue size: " + processQueue.size());

                if (gameListener != null) {
                    gameListener.onProcessAdded(process);
                    gameListener.onQueueChanged(processQueue.size());
                }
            }
        }
    }

    private void startGameTick() {
        // Reset the elapsed timer
        elapsedTimer.progress();

        // Use a higher frequency timer for smoother updates
        gameTickRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    updateProcesses();

                    // Schedule the next update
                    gameTickHandler.postDelayed(this, TIMER_INTERVAL_MS);
                }
            }
        };

        // Start immediately
        gameTickHandler.post(gameTickRunnable);
    }

    private void updateProcesses() {
        // Get elapsed time since last update
        long delta = elapsedTimer.progress();

        // Update the timer stepper for UI updates
        timerStepper.update(delta);

        // Create a local list of processes to handle in this update
        List<Process> processesToUpdate = new ArrayList<>();

        // First, get a snapshot of current processes
        synchronized (mutex) {
            processesToUpdate.addAll(activeProcesses);
        }

        // Process the snapshot without holding the lock
        for (Process process : processesToUpdate) {
            if (!process.isComplete() && !process.isDead()) {
                // Update each process's timer
                process.updateTime();

                // Check if process is about to die
                if (process.isAboutToDie()) {
                    if (gameListener != null) {
                        gameListener.onProcessAboutToDie(process);
                    }

                    // Vibrate the device
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null && vibrator.hasVibrator()) {
                        vibrator.vibrate(500); // Vibrate for 500ms
                    }
                }

                // Check if process died during this update
                if (process.isDead()) {
                    handleDeadProcess(process);
                }
            }
        }

        // Now handle any pending removals
        handlePendingRemovals();

        // Check if we can add processes from the queue
        checkProcessQueue();
    }

    private void handleDeadProcess(Process process) {
        Log.d(TAG, "Process died: " + process.getName());

        deadProcessCount++;
        score -= POINTS_DEDUCTION_FOR_DEAD_PROCESS;

        // Add to pending removals
        synchronized (mutex) {
            pendingRemovals.add(process);
        }

        if (gameListener != null) {
            gameListener.onProcessDied(process);
            gameListener.onScoreChanged(score);
        }

        // Check game over condition
        if (deadProcessCount >= MAX_DEAD_PROCESSES) {
            endGame();
        }
    }

    private void handlePendingRemovals() {
        synchronized (mutex) {
            if (!pendingRemovals.isEmpty()) {
                // Remove all pending processes
                activeProcesses.removeAll(pendingRemovals);
                pendingRemovals.clear();
            }
        }
    }

    public void completeProcess(String processId) {
        Process processToComplete = null;

        synchronized (mutex) {
            for (Process process : activeProcesses) {
                if (process.getId().equals(processId) && !process.isComplete() && !process.isDead()) {
                    processToComplete = process;
                    break;
                }
            }
        }

        // Complete the process if found
        if (processToComplete != null) {
            processToComplete.completeProcess();
            score += POINTS_PER_COMPLETED_PROCESS;

            // Add to pending removals to be cleared next tick
            synchronized (mutex) {
                pendingRemovals.add(processToComplete);
            }

            Log.d(TAG, "Process completed: " + processToComplete.getName() + ", New score: " + score);

            if (gameListener != null) {
                gameListener.onProcessCompleted(processToComplete);
                gameListener.onScoreChanged(score);
            }
        }
    }

    private void endGame() {
        Log.d(TAG, "Game over! Final score: " + score);
        isGameOver = true;

        // Remove callbacks to prevent further updates
        mainHandler.removeCallbacks(processSpawnRunnable);
        gameTickHandler.removeCallbacks(gameTickRunnable);

        // Save high score if applicable
        saveHighScore();

        if (gameListener != null) {
            gameListener.onGameOver(score);
        }

        Toast.makeText(context, "Game Over! Final Score: " + score, Toast.LENGTH_LONG).show();
    }

    private void saveHighScore() {
        // Get current high score
        int currentHighScore = context.getSharedPreferences("ProcessManagerPrefs", Context.MODE_PRIVATE)
                .getInt("highScore", 0);

        // Update if new score is higher
        if (score > currentHighScore) {
            context.getSharedPreferences("ProcessManagerPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("highScore", score)
                    .apply();

            Log.d(TAG, "New high score: " + score);
        }
    }

    public void pauseGame() {
        Log.d(TAG, "Game paused");
        mainHandler.removeCallbacks(processSpawnRunnable);
        gameTickHandler.removeCallbacks(gameTickRunnable);
    }

    public void resumeGame() {
        if (!isGameOver) {
            Log.d(TAG, "Game resumed");
            // Reset the elapsed timer
            elapsedTimer.progress();
            scheduleNextProcess();
            startGameTick();
        }
    }

    public void stopGame() {
        Log.d(TAG, "Game stopped");
        mainHandler.removeCallbacks(processSpawnRunnable);
        gameTickHandler.removeCallbacks(gameTickRunnable);
    }

    // Getters with thread-safe access
    public List<Process> getActiveProcesses() {
        List<Process> processesCopy = new ArrayList<>();
        synchronized (mutex) {
            processesCopy.addAll(activeProcesses);
        }
        return processesCopy;
    }

    public int getQueueSize() {
        synchronized (mutex) {
            return processQueue.size();
        }
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