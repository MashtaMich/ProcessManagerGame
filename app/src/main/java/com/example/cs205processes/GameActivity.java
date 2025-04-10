package com.example.cs205processes;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements
        GameManager.GameListener,
        ProcessAdapter.OnProcessInteractionListener {

    private GameManager gameManager;
    private ProcessAdapter processAdapter;
    private RecyclerView processRecyclerView;
    private TextView scoreTextView;
    private TextView deadProcessCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize views
        processRecyclerView = findViewById(R.id.processRecyclerView);
        scoreTextView = findViewById(R.id.scoreTextView);
        deadProcessCountTextView = findViewById(R.id.deadProcessCountTextView);

        // Setup RecyclerView with GridLayoutManager for landscape orientation
        int spanCount = 2; // Number of columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        processRecyclerView.setLayoutManager(layoutManager);

        processAdapter = new ProcessAdapter(this, new ArrayList<>(), this);
        processRecyclerView.setAdapter(processAdapter);

        // Initialize game manager
        gameManager = new GameManager(this, this);

        // Start the game
        gameManager.startGame();

        // Initialize score display
        updateScoreDisplay(0);
        updateDeadProcessCountDisplay(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameManager.pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameManager.resumeGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameManager.stopGame();
    }

    // GameManager.GameListener implementation
    @Override
    public void onProcessAdded(Process process) {
        runOnUiThread(() -> {
            processAdapter.updateProcesses(gameManager.getActiveProcesses());
        });
    }

    @Override
    public void onProcessCompleted(Process process) {
        runOnUiThread(() -> {
            processAdapter.updateProcesses(gameManager.getActiveProcesses());
        });
    }

    @Override
    public void onProcessDied(Process process) {
        runOnUiThread(() -> {
            processAdapter.updateProcesses(gameManager.getActiveProcesses());
            updateDeadProcessCountDisplay(gameManager.getDeadProcessCount());
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
            // You could show a game over dialog here
            GameOverDialog gameOverDialog = new GameOverDialog(this, finalScore);
            gameOverDialog.show();
        });
    }

    // ProcessAdapter.OnProcessInteractionListener implementation
    @Override
    public void onCompleteButtonClicked(Process process) {
        gameManager.completeProcess(process.getId());
    }

    private void updateScoreDisplay(int score) {
        scoreTextView.setText(getString(R.string.score_format, score));
    }

    private void updateDeadProcessCountDisplay(int count) {
        deadProcessCountTextView.setText(getString(R.string.failed_processes_format, count));
    }
}