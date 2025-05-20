package com.example.cs205processes;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

    private Button startGameButton;
    private Button howToPlayButton;
    private Button settingsButton;
    private Button loadGameButton;
    private Button creditsButton;
    private static final String PREFS_NAME = "MyGamePrefs";
    private MediaPlayer mediaPlayer;
    private TextView highScoreTextView;
    private LinearLayout settingMenu;
    private LinearLayout creditsPage;
    private ImageButton back;
    private ImageButton backFromCredits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        setContentView(R.layout.activity_main);

        //preload resources at main menu
        new Thread(() -> {
            ResourceLoader.preloadFloorTiles(getApplicationContext());
            ResourceLoader.preloadInteractableSprites(getApplicationContext());

            for (String key : ResourceLoader.getAllSpriteKeys()) {
                Log.d("MainActivity", "Preloaded: " + key);
            }
        }).start();

        //audio setup
        mediaPlayer = setupMediaPlayer(R.raw.overcooked, sharedPreferences);
        setupVolumeSeekBar(findViewById(R.id.volumeSeekBar), mediaPlayer, sharedPreferences);

        //load high score
        highScoreTextView = findViewById(R.id.highScore1);
        loadHighScore();

        //enable immersive UI
        enableImmersiveMode();

        //link buttons and views
        startGameButton = findViewById(R.id.StartGame);
        howToPlayButton = findViewById(R.id.HowToPlay);
        loadGameButton = findViewById(R.id.LoadGame);
        settingsButton = findViewById(R.id.Settings);
        settingMenu = findViewById(R.id.SettingsMenu);
        creditsButton = findViewById(R.id.Credits);
        creditsPage = findViewById(R.id.CreditsPage);
        back = findViewById(R.id.backButton);
        backFromCredits = findViewById(R.id.backFromCredits);
        TextView contributorsText = findViewById(R.id.contributorsText);
        contributorsText.setText(Html.fromHtml(
                "<ul>" +
                        "<li><a href='https://github.com/matthew-ngzc'>Matthew Ng</a> - Backend</li>" +
                        "<li><a href='https://github.com/MashtaMich'>Michael Suteja</a> - Backend</li>" +
                        "<li><a href='https://github.com/Cutcuu'>Jeremy Lin</a> - Art & Assets</li>" +
                        "<li><a href='https://github.com/jeffreygoh2023'>Jeffrey Goh</a> - Backend, Sound & Music</li>" +
                        "<li><a href='https://github.com/ChewWaiMunDamien'>Damien Chew</a> - Backend</li>" +
                        "<li><a href='https://github.com/reiwening'>Ing Reiwen</a> - Backend</li>" +
                        "</ul>", Html.FROM_HTML_MODE_LEGACY));
        contributorsText.setMovementMethod(LinkMovementMethod.getInstance());
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
        startGameButton.setOnClickListener(v -> {
            Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(gameIntent);
        });

        howToPlayButton.setOnClickListener(v -> showHowToPlayDialog());

        settingsButton.setOnClickListener(v -> showSettingsDialog());

        creditsButton.setOnClickListener(v -> {
            creditsPage.setVisibility(View.VISIBLE);
            backFromCredits.setVisibility(View.VISIBLE);
        });

        backFromCredits.setOnClickListener(v -> {
            creditsPage.setVisibility(View.GONE);
            backFromCredits.setVisibility(View.GONE);
        });

        loadGameButton.setOnClickListener(v -> {
            // Check if a save exists
            SharedPreferences prefs = getSharedPreferences("GameSave", MODE_PRIVATE);
            if (prefs.contains("score")) {
                Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                gameIntent.putExtra("loadSavedGame", true);
                startActivity(gameIntent);
            } else {
                Toast.makeText(MainActivity.this, "No saved game found", Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(v -> {
            settingMenu.setVisibility(GONE);
            back.setVisibility(GONE);
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
    }

    private void showSettingsDialog() {
        // In a real app, you might implement a more complex settings dialog
        // or launch a separate Settings Activity
        settingMenu.setVisibility(View.VISIBLE);
        back.setVisibility(VISIBLE);
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