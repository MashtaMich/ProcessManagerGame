package com.example.cs205processes;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button startGameButton;
    private Button howToPlayButton;
    private Button settingsButton;
    private Button loadGameButton;
    private static final String PREFS_NAME = "MyGamePrefs";
    private MediaPlayer mediaPlayer;
    private TextView highScoreTextView;
    private SeekBar volumeSeekBar;
    private LinearLayout settingMenu;
    private ImageButton back;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        int savedVolume = sharedPreferences.getInt("volume", 100);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        highScoreTextView = findViewById(R.id.highScore1);
        loadHighScore();

        mediaPlayer = MediaPlayer.create(this, R.raw.overcooked);
        mediaPlayer.setLooping(true); // Enable looping
        mediaPlayer.setVolume(savedVolume / 100f, savedVolume / 100f); // Set initial volume
        mediaPlayer.start(); // Start playing the audio
        hideSystemUI();

        // Initialize SeekBar
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setProgress(savedVolume);
        // Set up SeekBar listener to update volume
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update MediaPlayer volume as user changes the SeekBar
                mediaPlayer.setVolume(progress / 100f, progress / 100f);

                // Save the new volume setting to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("volume", progress);
                editor.apply(); // Apply changes to SharedPreferences asynchronously
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: Handle start of touch on SeekBar (if needed)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: Handle stop of touch on SeekBar (if needed)
            }
        });

        // Initialize buttons
        startGameButton = findViewById(R.id.StartGame);
        howToPlayButton = findViewById(R.id.HowToPlay);
        loadGameButton = findViewById(R.id.LoadGame);
        settingsButton = findViewById(R.id.Settings);
        settingMenu = findViewById(R.id.SettingsMenu);
        back = findViewById(R.id.backButton);

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

        loadGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if a save exists
                SharedPreferences prefs = getSharedPreferences("GameSave", MODE_PRIVATE);
                if (prefs.contains("score")) {
                    // Pass a load flag to GameActivity
                    Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                    gameIntent.putExtra("loadSavedGame", true);
                    startActivity(gameIntent);
                } else {
                    Toast.makeText(MainActivity.this, "No saved game found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Back button - goes back to main menu
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingMenu.setVisibility(GONE);
                back.setVisibility(GONE);
            }
        });
    }

    private void showHowToPlayDialog() {
        setContentView(R.layout.tutorial);
        LinearLayout instructions1 = findViewById(R.id.step1);
        LinearLayout instructions2 = findViewById(R.id.step2);
        LinearLayout instructions3 = findViewById(R.id.step3);
        LinearLayout instructions4 = findViewById(R.id.step4);
        LinearLayout instructions5 = findViewById(R.id.step5);
        LinearLayout instructions6 = findViewById(R.id.step6);
        LinearLayout instructions7 = findViewById(R.id.step7);
        LinearLayout instructions8 = findViewById(R.id.step8);
        LinearLayout instructions9 = findViewById(R.id.step9);
        LinearLayout instructions10 = findViewById(R.id.step10);
        LinearLayout instructions11 = findViewById(R.id.step11);
        Button step1 = findViewById(R.id.nextButton1);
        Button step2 = findViewById(R.id.nextButton2);
        Button step3 = findViewById(R.id.nextButton3);
        Button step4 = findViewById(R.id.nextButton4);
        Button step5 = findViewById(R.id.nextButton5);
        Button step6 = findViewById(R.id.nextButton6);
        Button step7 = findViewById(R.id.nextButton7);
        Button step8 = findViewById(R.id.nextButton8);
        Button step9 = findViewById(R.id.nextButton9);
        Button step10 = findViewById(R.id.nextButton10);
        Button step11 = findViewById(R.id.nextButton11);
        step1.setOnClickListener(v -> {
            instructions1.setVisibility(View.GONE);
            instructions2.setVisibility(VISIBLE);
        });
        step2.setOnClickListener(v -> {
            instructions2.setVisibility(View.GONE);
            instructions3.setVisibility(VISIBLE);
        });
        step3.setOnClickListener(v -> {
            instructions3.setVisibility(View.GONE);
            instructions4.setVisibility(VISIBLE);
        });
        step4.setOnClickListener(v -> {
            instructions4.setVisibility(View.GONE);
            instructions5.setVisibility(VISIBLE);
        });
        step5.setOnClickListener(v -> {
            instructions5.setVisibility(View.GONE);
            instructions6.setVisibility(VISIBLE);
        });
        step6.setOnClickListener(v -> {
            instructions6.setVisibility(View.GONE);
            instructions7.setVisibility(VISIBLE);
        });
        step7.setOnClickListener(v -> {
            instructions7.setVisibility(View.GONE);
            instructions8.setVisibility(VISIBLE);
        });
        step8.setOnClickListener(v -> {
            instructions8.setVisibility(View.GONE);
            instructions9.setVisibility(VISIBLE);
        });
        step9.setOnClickListener(v -> {
            instructions9.setVisibility(View.GONE);
            instructions10.setVisibility(VISIBLE);
        });
        step10.setOnClickListener(v -> {
            instructions10.setVisibility(View.GONE);
            instructions11.setVisibility(VISIBLE);
        });
        step11.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("How to Play");
//        builder.setMessage(
//                "Process Manager: Cooking under CPU pressure!\n\n" +
//                        "• You're the Process Manager in a busy kitchen.\n" +
//                        "• Processes (customers) arrive randomly with orders.\n" +
//                        "• Move your character around the kitchen to collect ingredients.\n" +
//                        "• Cook dishes according to recipes before the process times out.\n" +
//                        "• Each completed order gives you 100 points.\n" +
//                        "• Each failed order costs you 500 points.\n" +
//                        "• Game ends when 3 processes time out.\n\n" +
//                        "Controls:\n" +
//                        "• Use on-screen joystick to move your character.\n" +
//                        "• Tap on ingredients or tools to interact with them.\n"
//        );
//        builder.setPositiveButton("Got it!", null);
//        builder.show();
    }

    private void showSettingsDialog() {
        // In a real app, you might implement a more complex settings dialog
        // or launch a separate Settings Activity
        settingMenu.setVisibility(View.VISIBLE);
        back.setVisibility(VISIBLE);
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Settings");
//        builder.setMessage("Settings options would go here.");
//        builder.setPositiveButton("Close", null);
//        builder.show();
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