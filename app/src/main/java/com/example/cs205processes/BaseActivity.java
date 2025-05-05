package com.example.cs205processes;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.function.Consumer;

import androidx.appcompat.app.AppCompatActivity;


public abstract class BaseActivity extends AppCompatActivity {
    protected final String TAG = getClass().getSimpleName();
    protected static final float JOYSTICK_SCALE_SMALL = 1.0f;
    protected static final float JOYSTICK_SCALE_LARGE = 1.4f;
    protected static final float JOYSTICK_SCALE_DEFAULT = JOYSTICK_SCALE_SMALL;
    protected static final int JOYSTICK_BASE_SIZE_DP = 60;

    protected float getSavedJoystickScale() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        return prefs.getFloat("joystickScale", JOYSTICK_SCALE_DEFAULT); // Default to Small
    }

    protected void saveJoystickScale(float scale) {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        prefs.edit().putFloat("joystickScale", scale).apply();
    }

    protected void setupJoystickSizeListener(RadioGroup group, int smallId, int largeId) {
        setupJoystickSizeListener(group, smallId, largeId, null);
    }

    protected void setupJoystickSizeListener(RadioGroup group, int smallId, int largeId, Consumer<Float> onChange) {
        float savedScale = getSavedJoystickScale();

        group.post(() -> {
            if (savedScale == JOYSTICK_SCALE_SMALL && group.findViewById(smallId) != null) {
                ((RadioButton) group.findViewById(smallId)).setChecked(true);
            } else if (savedScale == JOYSTICK_SCALE_LARGE && group.findViewById(largeId) != null) {
                ((RadioButton) group.findViewById(largeId)).setChecked(true);
            }
        });

        group.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            float selectedScale = (checkedId == smallId) ? JOYSTICK_SCALE_SMALL : JOYSTICK_SCALE_LARGE;
            saveJoystickScale(selectedScale);
            if (onChange != null) {
                onChange.accept(selectedScale); // Apply immediately if callback provided
            }
        });
    }

    protected void applyJoystickScale(View rootView) {
        float scale = getSavedJoystickScale();

        int newSizePx = (int) (scale * dpToPx());

        int[] ids = { R.id.btnUp, R.id.btnDown, R.id.btnLeft, R.id.btnRight};
        for (int id : ids) {
            View v = rootView.findViewById(id);
            if (v != null) {
                ViewGroup.LayoutParams params = v.getLayoutParams();
                params.width = newSizePx;
                params.height = newSizePx;
                v.setLayoutParams(params);
            }
        }
    }
    private int dpToPx() {
        return Math.round(JOYSTICK_BASE_SIZE_DP * getResources().getDisplayMetrics().density);
    }



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
