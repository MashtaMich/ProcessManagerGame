package com.game.cookingspree;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TutorialActivity extends GameActivity {

    private LinearLayout tutorialOverlay;
    private TextView tutorialText;
    private Button tutorialNextButton;

    private int currentStep = 0;
    private List<TutorialStep> tutorialSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set tutorial layout instead of game layout
        setContentView(R.layout.activity_tutorial);

        // Re-initialize components after setting tutorial layout
        reinitializeComponents();

        // Initialize tutorial UI
        initializeTutorial();
    }

    private void reinitializeComponents() {
        // Since we changed the layout, we need to reinitialize some components
        try {
            // Re-setup joystick
            setupJoystickSizeListener(
                    findViewById(R.id.joystickSizeGroup),
                    R.id.smallSize,
                    R.id.largeSize,
                    scale -> applyJoystickScale(findViewById(android.R.id.content))
            );

            // Re-setup audio
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = setupMediaPlayer(R.raw.overcooked);
            setupVolumeSeekBar(findViewById(R.id.volumeSeekBar), mediaPlayer);

            // Re-initialize game components
            initializeGameComponents();
            setupInteractButton();
            setupPauseMenuButtons();

        } catch (Exception e) {
            Log.e("TutorialActivity", "Error reinitializing components: " + e.getMessage(), e);
        }
    }

    private void initializeTutorial() {
        // Get tutorial overlay views with null checks
        tutorialOverlay = findViewById(R.id.tutorialOverlay);
        tutorialText = findViewById(R.id.tutorialText);
        tutorialNextButton = findViewById(R.id.tutorialNextButton);
        Button tutorialSkipButton = findViewById(R.id.tutorialSkipButton);

        if (tutorialOverlay == null || tutorialText == null ||
                tutorialNextButton == null || tutorialSkipButton == null) {
            Log.e("TutorialActivity", "Tutorial UI elements not found!");
            return;
        }

        // Setup tutorial steps
        setupTutorialSteps();

        // Setup button listeners
        tutorialNextButton.setOnClickListener(v -> nextStep());
        tutorialSkipButton.setOnClickListener(v -> skipTutorial());

        // Start tutorial
        showCurrentStep();

        // Pause the game initially
        GameManager tutorialGameManager = getTutorialGameManager();
        if (tutorialGameManager != null) {
            tutorialGameManager.pauseGame();
        }
    }

    // Helper method to access gameManager through game instance
    private GameManager getTutorialGameManager() {
        // Access gameManager through the game instance
        if (game != null && game.getGameManager() != null) {
            return game.getGameManager();
        }
        return null;
    }

    private void setupTutorialSteps() {
        tutorialSteps = new ArrayList<>(Arrays.asList(
                new TutorialStep(
                        "Welcome to Cooking Spree!",
                        "This is your kitchen. Let's learn the basics!"
                ),
                new TutorialStep(
                        "Movement",
                        "Use the joystick on the bottom left to move around. Try moving your character!"
                ),
                new TutorialStep(
                        "Orders",
                        "Orders appear at the top. They show what recipe to cook and time remaining. More orders = more points!"
                ),
                new TutorialStep(
                        "Ingredients",
                        "On the right are your available ingredients. Click one to select it, then click a swap option to exchange ingredients."
                ),
                new TutorialStep(
                        "Baskets",
                        "Walk to a basket and press Interact to pick up ingredients. You can only hold one item at a time."
                ),
                new TutorialStep(
                        "Cooking Pots",
                        "Add 3 ingredients to a pot to start cooking. Make sure they match a recipe! Wrong ingredients = waste."
                ),
                new TutorialStep(
                        "Tables",
                        "Use tables to store items temporarily. Press Interact to place or pick up items."
                ),
                new TutorialStep(
                        "Submission Zone",
                        "Bring completed dishes here to submit orders and score points. Wrong dish = no points!"
                ),
                new TutorialStep(
                        "Trash Bin",
                        "Made a mistake? Throw unwanted items in the trash bin."
                ),
                new TutorialStep(
                        "Ready to Cook!",
                        "You now know the basics! Cook fast, submit orders, and don't let them expire. Good luck, Chef!"
                )
        ));
    }

    private void showCurrentStep() {
        if (currentStep >= tutorialSteps.size()) {
            completeTutorial();
            return;
        }

        TutorialStep step = tutorialSteps.get(currentStep);
        tutorialText.setText(step.getDescription());
        tutorialOverlay.setVisibility(View.VISIBLE);

        // Update button text for last step
        if (currentStep == tutorialSteps.size() - 1) {
            tutorialNextButton.setText("Start Game!");
        }

        // Special handling for certain steps
        handleSpecialSteps();
    }

    private void handleSpecialSteps() {
        GameManager tutorialGameManager = getTutorialGameManager();

        switch (currentStep) {
            case 1: // Movement step
                // Resume game briefly to allow movement
                if (tutorialGameManager != null) {
                    tutorialGameManager.resumeGame();
                }
                break;
            case 2: // Orders step
                // Ensure game is paused to focus on UI
                if (tutorialGameManager != null) {
                    tutorialGameManager.pauseGame();
                }
                break;
            default:
                // Keep game paused for most steps
                if (tutorialGameManager != null) {
                    tutorialGameManager.pauseGame();
                }
                break;
        }
    }

    private void nextStep() {
        currentStep++;
        showCurrentStep();
    }

    private void skipTutorial() {
        completeTutorial();
    }

    private void completeTutorial() {
        tutorialOverlay.setVisibility(View.GONE);

        // Resume the game
        GameManager tutorialGameManager = getTutorialGameManager();
        if (tutorialGameManager != null) {
            tutorialGameManager.resumeGame();
        }

        // Optionally go back to main menu or continue playing
        // For now, let's continue in tutorial mode
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Simple tutorial step class
    private static class TutorialStep {
        private final String description;

        public TutorialStep(String title, String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Override to disable pause menu during tutorial
    @Override
    protected void setupPauseMenuButtons() {
        try {
            super.setupPauseMenuButtons();

            // Disable pause button during tutorial
            Button togglePauseButton = findViewById(R.id.togglePauseButton);
            if (togglePauseButton != null && currentStep < tutorialSteps.size()) {
                togglePauseButton.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("TutorialActivity", "Error setting up pause menu: " + e.getMessage(), e);
        }
    }
}