package com.example.cs205processes;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;

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

    private GameView gameView;
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            // 🌟 Init GameView + Game logic
            gameView = findViewById(R.id.gameView);
            game = new Game(gameView, this);
            gameView.init(game);

            // 🌟 Hook joystick controls to movement
            findViewById(R.id.btnUp).setOnClickListener(v -> game.moveUp());
            findViewById(R.id.btnDown).setOnClickListener(v -> game.moveDown());
            findViewById(R.id.btnLeft).setOnClickListener(v -> game.moveLeft());
            findViewById(R.id.btnRight).setOnClickListener(v -> game.moveRight());

            // 🌟 Init RecyclerView + Stats UI
            processRecyclerView = findViewById(R.id.processRecyclerView);
            scoreTextView = findViewById(R.id.scoreTextView);
            deadProcessCountTextView = findViewById(R.id.deadProcessCountTextView);
            queueSizeTextView = findViewById(R.id.queueSizeTextView);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            processRecyclerView.setLayoutManager(layoutManager);
            processAdapter = new ProcessAdapter(this, new ArrayList<>(), this);
            processRecyclerView.setAdapter(processAdapter);

            // 🌟 Init GameManager
            gameManager = new GameManager(this, this);
            gameManager.startGame();

            updateScoreDisplay(0);
            updateDeadProcessCountDisplay(0);
            updateQueueSizeDisplay(0);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateQueueSizeDisplay(int queueSize) {
        if (queueSizeTextView != null) {
            queueSizeTextView.setText(getString(R.string.queue_size_format, queueSize));
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

    @Override
    public void onQueueChanged(int queueSize) {
        runOnUiThread(() -> updateQueueSizeDisplay(queueSize));
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
        // Optional: vibration is already handled by GameManager
    }

    @Override
    public void onScoreChanged(int newScore) {
        runOnUiThread(() -> updateScoreDisplay(newScore));
    }

    @Override
    public void onGameOver(int finalScore) {
        runOnUiThread(() -> {
            try {
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
}
