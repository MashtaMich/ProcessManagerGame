package com.example.cs205processes;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button startGameButton;
    private Button howToPlayButton;
    private Button settingsButton;
    private Button loadGameButton;
    private static final String PREFS_NAME = "MyGamePrefs";
    private MediaPlayer mediaPlayer;
    private TextView highScoreTextView;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        highScoreTextView = findViewById(R.id.highScore1);
        loadHighScore();

        mediaPlayer = MediaPlayer.create(this, R.raw.overcooked);
        mediaPlayer.setLooping(true); // Enable looping
        mediaPlayer.start(); // Start playing the audio
        hideSystemUI();

        // Initialize preferences
        preferences = getSharedPreferences("ProcessManagerPrefs", MODE_PRIVATE);

        // Initialize buttons
        startGameButton = findViewById(R.id.StartGame);
        howToPlayButton = findViewById(R.id.HowToPlay);
        loadGameButton = findViewById(R.id.LoadGame);
        settingsButton = findViewById(R.id.Settings);

        // Set button click listeners
        setupButtonListeners();
    }

    private void loadHighScore() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int highScore = sharedPreferences.getInt("highScore", Integer.MIN_VALUE); // Default to Integer.MIN_VALUE

        // Display the high score, or leave it empty if it's the initial value
        if (highScore == Integer.MIN_VALUE) {
            highScoreTextView.setText(""); // Set as empty
        } else {
            highScoreTextView.setText(String.valueOf(highScore)); // Display the high score
        }
    }

    private void setupButtonListeners() {
        // Start Game button - Launches the Game Activity
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(gameIntent);
            }
        });

        // How to Play button - Shows instructions dialog
        howToPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHowToPlayDialog();
            }
        });

        // Settings button - Shows settings dialog
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        });
    }

    private void showHowToPlayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How to Play");
        builder.setMessage(
                "Process Manager: Cooking under CPU pressure!\n\n" +
                        "• You're the Process Manager in a busy kitchen.\n" +
                        "• Processes (customers) arrive randomly with orders.\n" +
                        "• Move your character around the kitchen to collect ingredients.\n" +
                        "• Cook dishes according to recipes before the process times out.\n" +
                        "• Each completed order gives you 100 points.\n" +
                        "• Each failed order costs you 500 points.\n" +
                        "• Game ends when 3 processes time out.\n\n" +
                        "Controls:\n" +
                        "• Use on-screen joystick to move your character.\n" +
                        "• Tap on ingredients or tools to interact with them.\n"
        );
        builder.setPositiveButton("Got it!", null);
        builder.show();
    }

    private void showSettingsDialog() {
        // In a real app, you might implement a more complex settings dialog
        // or launch a separate Settings Activity
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        builder.setMessage("Settings options would go here.");
        builder.setPositiveButton("Close", null);
        builder.show();
    }
    private void hideSystemUI() {
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // Pause the audio
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.start(); // Resume playing if it was paused
        }
    }
}
