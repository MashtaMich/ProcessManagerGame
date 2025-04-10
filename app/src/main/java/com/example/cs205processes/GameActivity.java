package com.example.cs205processes;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements
        GameManager.GameListener,
        ProcessAdapter.OnProcessInteractionListener {

    private static final String TAG = "GameActivity";

    private GameManager gameManager;
    private ProcessAdapter processAdapter;
    private RecyclerView processRecyclerView;
    private TextView scoreTextView;
    private TextView deadProcessCountTextView;
    private TextView queueSizeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize views
        try {
            processRecyclerView = findViewById(R.id.processRecyclerView);
            scoreTextView = findViewById(R.id.scoreTextView);
            deadProcessCountTextView = findViewById(R.id.deadProcessCountTextView);
            queueSizeTextView = findViewById(R.id.queueSizeTextView);

            // Setup RecyclerView with LinearLayoutManager for horizontal scrolling
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            processRecyclerView.setLayoutManager(layoutManager);

            processAdapter = new ProcessAdapter(this, new ArrayList<>(), this);
            processRecyclerView.setAdapter(processAdapter);

            // Initialize game manager
            gameManager = new GameManager(this, this);

            // Start the game
            gameManager.startGame();

            // Initialize displays
            updateScoreDisplay(0);
            updateDeadProcessCountDisplay(0);
            updateQueueSizeDisplay(0);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // [Rest of your GameActivity implementation]

    // Make this method safely handle null queueSizeTextView
    private void updateQueueSizeDisplay(int queueSize) {
        if (queueSizeTextView != null) {
            queueSizeTextView.setText(getString(R.string.queue_size_format, queueSize));
        }
    }

    // Other methods remain the same...

    @Override
    protected void onPause() {
        super.onPause();
        if (gameManager != null) {
            gameManager.pauseGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameManager != null) {
            gameManager.resumeGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameManager != null) {
            gameManager.stopGame();
        }
    }

    // Handle queue size changes
    @Override
    public void onQueueChanged(int queueSize) {
        runOnUiThread(() -> {
            updateQueueSizeDisplay(queueSize);
        });
    }

    @Override
    public void onTimerTick() {
        runOnUiThread(() -> {
            if (gameManager != null && processAdapter != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
            }
        });
    }

    @Override
    public void onProcessAdded(Process process) {
        runOnUiThread(() -> {
            if (processAdapter != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
            }
        });
    }

    @Override
    public void onProcessCompleted(Process process) {
        runOnUiThread(() -> {
            if (processAdapter != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
            }
        });
    }

    @Override
    public void onProcessDied(Process process) {
        runOnUiThread(() -> {
            if (processAdapter != null && gameManager != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
                updateDeadProcessCountDisplay(gameManager.getDeadProcessCount());
            }
        });
    }

    @Override
    public void onProcessAboutToDie(Process process) {
        // Handled by GameManager (vibration)
    }

    @Override
    public void onScoreChanged(int newScore) {
        runOnUiThread(() -> {
            updateScoreDisplay(newScore);
        });
    }

    @Override
    public void onGameOver(int finalScore) {
        runOnUiThread(() -> {
            try {
                // Show game over dialog
                GameOverDialog gameOverDialog = new GameOverDialog(this, finalScore);
                gameOverDialog.show();
            } catch (Exception e) {
                Log.e(TAG, "Error showing game over dialog: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onCompleteButtonClicked(Process process) {
        if (gameManager != null) {
            gameManager.completeProcess(process.getId());
        }
    }

    private void updateScoreDisplay(int score) {
        if (scoreTextView != null) {
            scoreTextView.setText(getString(R.string.score_format, score));
        }
    }

    private void updateDeadProcessCountDisplay(int count) {
        if (deadProcessCountTextView != null) {
            deadProcessCountTextView.setText(getString(R.string.failed_processes_format, count));
        }
    }
}