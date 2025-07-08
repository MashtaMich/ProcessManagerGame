package com.game.cookingspree;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SimpleTutorialActivity extends GameActivity {

    private LinearLayout tutorialOverlay;
    private TextView tutorialText;
    private Button tutorialNextButton;
    private Button tutorialSkipButton;

    private int currentStep = 0;
    private String[] tutorialTexts = {
            "Welcome to Cooking Spree! This is your kitchen.",
            "Use the joystick to move around. Try it now!",
            "Orders appear at the top. Cook what they request!",
            "Click ingredients on the right to swap them out.",
            "Walk to baskets and press Interact to get ingredients.",
            "Add 3 ingredients to pots to start cooking.",
            "Use tables to store items temporarily.",
            "Bring finished dishes to the submission zone.",
            "Throw mistakes in the trash bin.",
            "You're ready! Have fun cooking!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d("SimpleTutorial", "Starting tutorial initialization");

            // Wait a moment for everything to initialize
            findViewById(android.R.id.content).post(() -> {
                initializeTutorial();
            });

        } catch (Exception e) {
            Log.e("SimpleTutorial", "Error in onCreate: " + e.getMessage(), e);
            finish(); // Go back if there's an error
        }
    }

    private void initializeTutorial() {
        try {
            // Create tutorial overlay programmatically if it doesn't exist
            createTutorialOverlay();
            showCurrentStep();

        } catch (Exception e) {
            Log.e("SimpleTutorial", "Error initializing tutorial: " + e.getMessage(), e);
        }
    }

    private void createTutorialOverlay() {
        // Create overlay if it doesn't exist
        tutorialOverlay = findViewById(R.id.tutorialOverlay);
        if (tutorialOverlay == null) {
            // Create programmatically
            tutorialOverlay = new LinearLayout(this);
            tutorialOverlay.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            tutorialOverlay.setOrientation(LinearLayout.VERTICAL);
            tutorialOverlay.setBackgroundColor(0xB0000000); // Semi-transparent black

            // Add to root view
            findViewById(android.R.id.content).post(() -> {
                ((android.view.ViewGroup) findViewById(android.R.id.content)).addView(tutorialOverlay);
                setupTutorialViews();
            });
        }
    }

    private void setupTutorialViews() {
        // Create text view
        tutorialText = new TextView(this);
        tutorialText.setTextSize(18);
        tutorialText.setTextColor(0xFFFFFFFF);
        tutorialText.setPadding(50, 50, 50, 50);

        // Create next button
        tutorialNextButton = new Button(this);
        tutorialNextButton.setText("Next");
        tutorialNextButton.setOnClickListener(v -> nextStep());

        // Create skip button
        tutorialSkipButton = new Button(this);
        tutorialSkipButton.setText("Skip");
        tutorialSkipButton.setOnClickListener(v -> skipTutorial());

        // Add to overlay
        tutorialOverlay.addView(tutorialText);
        tutorialOverlay.addView(tutorialNextButton);
        tutorialOverlay.addView(tutorialSkipButton);
    }

    private void showCurrentStep() {
        if (currentStep >= tutorialTexts.length) {
            completeTutorial();
            return;
        }

        if (tutorialText != null) {
            tutorialText.setText(tutorialTexts[currentStep]);
        }

        if (tutorialNextButton != null && currentStep == tutorialTexts.length - 1) {
            tutorialNextButton.setText("Start Game!");
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
        if (tutorialOverlay != null) {
            tutorialOverlay.setVisibility(View.GONE);
        }
        Log.d("SimpleTutorial", "Tutorial completed");
    }
}