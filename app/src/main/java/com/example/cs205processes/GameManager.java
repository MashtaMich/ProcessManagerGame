package com.example.cs205processes;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {
    private static final String TAG = "GameManager";
    //private static final int POINTS_PER_COMPLETED_PROCESS = 100;
    //private static final int POINTS_DEDUCTION_FOR_DEAD_PROCESS = 300;
    private static final int MAX_DEAD_PROCESSES = 3;
    private static final int TIMER_INTERVAL_MS = 16; // Update more frequently for smoother animation (~60 FPS)
    private static final int MAX_ACTIVE_PROCESSES = 5; // Maximum number of active processes

    // Timer utilities for smooth timing
    private final ElapsedTimer elapsedTimer = new ElapsedTimer();
    private final DeltaStepper timerStepper;

    // Simple mutex for thread synchronization
    private final Object mutex = new Object();

    // Process collections
    private final List<Order> activeOrders;
    private final List<Order> pendingRemovals;

    private final List<Recipe> availableRecipes;
    private int score;
    private int streakCount;
    private long lastCompletionTime = 0L; // in milliseconds
    private static final long STREAK_TIME_LIMIT = 10_000L; // 10 seconds in milliseconds
    private static final int BASE_POINTS = 100;
    private int deadProcessCount;
    private boolean isGameOver;
    private final Random random;
    private final Handler mainHandler;
    private final Handler gameTickHandler;
    private Runnable processSpawnRunnable;
    private Runnable gameTickRunnable;
    private final Context context;
    private final GameListener gameListener;
    private boolean isPaused = false;
    public interface GameListener {
        void onProcessAdded(Order order);
        void onProcessCompleted(Order order);
        void onProcessDied(Order order);
        void onScoreChanged(int newScore);
        void onGameOver(int finalScore);
        void onTimerTick(); // Notify UI of every timer tick
    }

    public GameManager(Context context, GameListener listener,List<Recipe> recipeList) {
        this.context = context;
        this.gameListener = listener;
        this.activeOrders = new ArrayList<>();
        this.pendingRemovals = new ArrayList<>();
        this.availableRecipes = recipeList;
        this.score = 0;
        this.deadProcessCount = 0;
        this.isGameOver = false;
        this.random = new Random();

        this.timerStepper = new DeltaStepper(1000, this::tickUpdate);

        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gameTickHandler = new Handler(Looper.getMainLooper());
    }

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
            activeOrders.clear();
            pendingRemovals.clear();
        }

        // Start the process spawning logic
        scheduleNextProcess();

        // Start the game tick for updating timers
        startGameTick();
    }

    private void scheduleNextProcess() {
        int spawnDelay = 5000 + random.nextInt(8000); // 5-13 seconds between processes

        Log.d(TAG, "Scheduling next process in " + spawnDelay + "ms");

        processSpawnRunnable = () -> {
            if (!isGameOver & !isPaused) {
                generateNewProcess();
                scheduleNextProcess();
            }
        };

        mainHandler.postDelayed(processSpawnRunnable, spawnDelay);
    }

    private void generateNewProcess() {
        Order newOrder = Order.generateRandomProcess(availableRecipes);

        synchronized (mutex) {
            // Check if we can add directly to active processes
            if (activeOrders.size() < MAX_ACTIVE_PROCESSES) {
                activeOrders.add(newOrder);
                Log.d(TAG, "New process added directly: " + newOrder.getName());

                if (gameListener != null) {
                    gameListener.onProcessAdded(newOrder);
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
                if (!isGameOver && !isPaused) {
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
        List<Order> processesToUpdate;

        // First, get a snapshot of current processes
        synchronized (mutex) {
            processesToUpdate = new ArrayList<>(activeOrders);
        }

        // Process the snapshot without holding the lock
        for (Order order : processesToUpdate) {
            if (!order.isComplete() && !order.isDead()) {
                // Update each process's timer
                order.updateTime();

                // Check if process died during this update
                if (order.isDead()) {
                    handleDeadProcess(order);
                }
            }
        }

        // Now handle any pending removals
        handlePendingRemovals();
    }

    private void handleDeadProcess(Order order) {
        Log.d(TAG, "Process died: " + order.getName());

        deadProcessCount++;
//        score -= POINTS_DEDUCTION_FOR_DEAD_PROCESS;
//        if (score < 0) score = 0;

        // Add to pending removals
        synchronized (mutex) {
            pendingRemovals.add(order);
        }

        if (gameListener != null) {
            gameListener.onProcessDied(order);
            gameListener.onScoreChanged(score);
        }

        // Check game over condition
        if (deadProcessCount >= MAX_DEAD_PROCESSES) {
            endGame();
        }
    }

    public void setDeadProcessCount(int count) {
        this.deadProcessCount = count;

        if (deadProcessCount >= MAX_DEAD_PROCESSES && !isGameOver) {
            endGame();
        }
    }
    private void handlePendingRemovals() {
        synchronized (mutex) {
            if (!pendingRemovals.isEmpty()) {
                // Remove all pending processes
                activeOrders.removeAll(pendingRemovals);
                pendingRemovals.clear();
            }
        }
    }

    public void completeProcess(String processId) {
        Order orderToComplete = null;

        synchronized (mutex) {
            for (Order order : activeOrders) {
                if (order.getId().equals(processId) && !order.isComplete() && !order.isDead()) {
                    orderToComplete = order;
                    break;
                }
            }
        }

        // Complete the process if found
        if (orderToComplete != null) {
            orderToComplete.completeProcess();
//            score += POINTS_PER_COMPLETED_PROCESS;
            // Check if the process was successfully completed
            if (!orderToComplete.isDead()) {
                long currentTime = System.currentTimeMillis();

                if (lastCompletionTime > 0 && (currentTime - lastCompletionTime <= STREAK_TIME_LIMIT)) {
                    streakCount++;
                } else {
                    streakCount = 1; // reset streak to 1 (not 0) because we still scored
                }

                int pointsEarned = BASE_POINTS * streakCount;
                score += pointsEarned;
                lastCompletionTime = currentTime;
            } else {
                streakCount = 0;
                lastCompletionTime = 0;
            }

            // Add to pending removals to be cleared next tick
            synchronized (mutex) {
                pendingRemovals.add(orderToComplete);
            }

            Log.d(TAG, "Process completed: " + orderToComplete.getName() + ", New score: " + score);

            if (gameListener != null) {
                gameListener.onProcessCompleted(orderToComplete);
                gameListener.onScoreChanged(score);
            }
        } else{
            streakCount = 0;
            lastCompletionTime= 0;
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
        if (processSpawnRunnable != null) {
            mainHandler.removeCallbacks(processSpawnRunnable);
        }
        if (gameTickRunnable != null) {
            gameTickHandler.removeCallbacks(gameTickRunnable);
        }
        elapsedTimer.pause();

        // Pause all active process timers
        synchronized (mutex) {
            for (Order order : activeOrders) {
                order.pauseTimer();
            }
        }
        
        isPaused = true;
    }

    public void resumeGame() {
        if (!isGameOver && isPaused) {
            Log.d(TAG, "Game resumed");
            elapsedTimer.resume();
            
            // Resume all active process timers
            synchronized (mutex) {
                for (Order order : activeOrders) {
                    order.resumeTimer();
                }
            }
            
            scheduleNextProcess();
            startGameTick();
            isPaused = false;
        }
    }

    public void stopGame() {
        Log.d(TAG, "Game stopped");
        mainHandler.removeCallbacks(processSpawnRunnable);
        gameTickHandler.removeCallbacks(gameTickRunnable);
    }

    public List<Order> getActiveProcesses() {
        List<Order> processesCopy;
        synchronized (mutex) {
            processesCopy = new ArrayList<>(activeOrders);
        }
        return processesCopy;
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

    public boolean isRunning() {
        return !isPaused && !isGameOver;
    }

    public void addProcessDirectly(Order order) {
        synchronized (mutex) {
            activeOrders.add(order);
        }
    }

    public void setScore(int newScore) {
        this.score = newScore;
        if (gameListener != null) {
            gameListener.onScoreChanged(newScore);
        }
    }
}