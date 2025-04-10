package com.example.cs205processes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button startGameButton;
    private Button howToPlayButton;
    private Button highScoresButton;
    private Button settingsButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set orientation to landscape
        // Note: This is now better handled in the AndroidManifest.xml

        // Initialize preferences
        preferences = getSharedPreferences("ProcessManagerPrefs", MODE_PRIVATE);

        // Initialize buttons
        startGameButton = findViewById(R.id.startGameButton);
        howToPlayButton = findViewById(R.id.howToPlayButton);
        highScoresButton = findViewById(R.id.highScoresButton);
        settingsButton = findViewById(R.id.settingsButton);

        // Set button click listeners
        setupButtonListeners();
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

        // High Scores button - Shows high scores dialog
        highScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighScoresDialog();
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

    private void showHighScoresDialog() {
        int highScore = preferences.getInt("highScore", 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("High Scores");
        builder.setMessage("Your best score: " + highScore);
        builder.setPositiveButton("Close", null);
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
}
