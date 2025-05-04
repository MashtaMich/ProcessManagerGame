package com.example.cs205processes;

import android.util.Log;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected final String TAG = getClass().getSimpleName();

    protected void enableImmersiveMode() {
        //hide system UI for the 1st time
        hideSystemUI();
        // Re-hide system UI for future after swipe gestures
        getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null && insets.isVisible(WindowInsets.Type.systemBars())) {
                hideSystemUI();
            }
            return v.onApplyWindowInsets(insets);
        });
    }

    protected void hideSystemUI() {
        try {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                //allows system bars to show temporarily by swiping
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding system UI: " + e.getMessage(), e);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}
