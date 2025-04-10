package com.example.cs205processes;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class GameOverDialog extends Dialog {

    private int finalScore;

    public GameOverDialog(Context context, int finalScore) {
        super(context);
        this.finalScore = finalScore;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_game_over);

        TextView scoreTextView = findViewById(R.id.finalScoreTextView);
        Button restartButton = findViewById(R.id.restartButton);
        Button mainMenuButton = findViewById(R.id.mainMenuButton);

        scoreTextView.setText("Final Score: " + finalScore);

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                // Restart the game
                Intent intent = new Intent(getContext(), GameActivity.class);
                getContext().startActivity(intent);
                // Close current activity
                if (getContext() instanceof GameActivity) {
                    ((GameActivity) getContext()).finish();
                }
            }
        });

        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                // Go back to main menu
                if (getContext() instanceof GameActivity) {
                    ((GameActivity) getContext()).finish();
                }
            }
        });

        setCancelable(false);
    }
}