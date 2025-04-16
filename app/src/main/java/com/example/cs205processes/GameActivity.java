package com.example.cs205processes;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements
        GameManager.GameListener,
        ProcessAdapter.OnProcessInteractionListener,
        IngredientFetchWorker.ingredientFetchListener {

    private static final String TAG = "GameActivity";
    private MediaPlayer mediaPlayer;
    private GameManager gameManager;
    private ProcessAdapter processAdapter;
    private RecyclerView processRecyclerView;
    private TextView scoreTextView;
    private TextView deadProcessCountTextView;

    private GameView gameView;
    private Game game;
    private int highScore = 0;

    private List<ImageView> inventoryViews;
    private List<ImageView> availableIngredientsViews;
    private LinearLayout swapOptionsLayout;

    private List<ImageView> basketViews;

    private IngredientFetchWorker ingredientFetcher;
    private IngredientInventory ingredientInventory;
    private View ingredientBlocker;
    private int selectedIngredientIndex=-1;
    private int selectedSwapIndex=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.activity_game);
            mediaPlayer = MediaPlayer.create(this, R.raw.overcooked);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            hideSystemUI();
            initializeGameComponents();

            // Link buttons
            Button togglePauseButton = findViewById(R.id.togglePauseButton);
            togglePauseButton.setText("Pause"); // Default state

            // Set listeners
            togglePauseButton.setOnClickListener(v -> {
                if (gameManager.isGameOver()) return; // Don’t allow toggling if game is over

                if (gameManager.isRunning()) {
                    gameManager.pauseGame();
                    togglePauseButton.setText("Resume");
                } else {
                    gameManager.resumeGame();
                    togglePauseButton.setText("Pause");
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            finish(); // Safely exit the activity if initialization fails
        }
    }

    private void initializeGameComponents() {
            // Initialize game view and logic
            gameView = findViewById(R.id.gameView);

            game = new Game(gameView, this);
            gameView.init(game);

            // Set up movement controls
            setupMovementControls();

            // Initialize UI components
            initializeUIComponents();

            // Initialize ingredientInventory and ingredients
            initializeInventory();

            // Initialize game manager
            gameManager = new GameManager(this, this);
            gameManager.startGame();

            // Set initial values for statistics
            updateScoreDisplay(0);
            updateDeadProcessCountDisplay(0);
    }

    private void setupMovementControls() {

            View btnUp = findViewById(R.id.btnUp);
            View btnDown = findViewById(R.id.btnDown);
            View btnLeft = findViewById(R.id.btnLeft);
            View btnRight = findViewById(R.id.btnRight);

            if (btnUp != null) btnUp.setOnClickListener(v -> game.moveUp());
            if (btnDown != null) btnDown.setOnClickListener(v -> game.moveDown());
            if (btnLeft != null) btnLeft.setOnClickListener(v -> game.moveLeft());
            if (btnRight != null) btnRight.setOnClickListener(v -> game.moveRight());
    }

    private void initializeUIComponents() {
        try {
            // Initialize statistics text views
            scoreTextView = findViewById(R.id.scoreTextView);
            deadProcessCountTextView = findViewById(R.id.deadProcessCountTextView);

            // Initialize process list
            processRecyclerView = findViewById(R.id.processRecyclerView);
            if (processRecyclerView == null) {
                Log.e(TAG, "processRecyclerView is null");
                return;
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            processRecyclerView.setLayoutManager(layoutManager);
            processAdapter = new ProcessAdapter(this, new ArrayList<>(), this);
            processRecyclerView.setAdapter(processAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UI components: " + e.getMessage(), e);
        }
    }

    private void initializeInventory() {
        try {
            // Initialize ingredient fetcher and ingredientInventory
            ingredientFetcher = new IngredientFetchWorker();
            ingredientInventory = new IngredientInventory();

            // Initialize view lists
            initializeViewLists();

            // Initialize ingredient views
            initIngredientViews();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ingredientInventory: " + e.getMessage(), e);
        }
    }

    private void initializeViewLists() {
        try {
            // Initialize ingredientInventory slots
            inventoryViews = new ArrayList<>();
            View slot1 = findViewById(R.id.ingredientSlot1);
            View slot2 = findViewById(R.id.ingredientSlot2);
            View slot3 = findViewById(R.id.ingredientSlot3);

            if (slot1 != null) inventoryViews.add((ImageView) slot1);
            if (slot2 != null) inventoryViews.add((ImageView) slot2);
            if (slot3 != null) inventoryViews.add((ImageView) slot3);

            // Initialize available ingredients view
            availableIngredientsViews = new ArrayList<>();
            View option1 = findViewById(R.id.swapOption1);
            View option2 = findViewById(R.id.swapOption2);

            if (option1 != null) availableIngredientsViews.add((ImageView) option1);
            if (option2 != null) availableIngredientsViews.add((ImageView) option2);

            // Initialize basket views
            basketViews = new ArrayList<>();
            View basket1 = findViewById(R.id.basket1);
            View basket2 = findViewById(R.id.basket2);
            View basket3 = findViewById(R.id.basket3);

            if (basket1 != null) basketViews.add((ImageView) basket1);
            if (basket2 != null) basketViews.add((ImageView) basket2);
            if (basket3 != null) basketViews.add((ImageView) basket3);

            // Get other UI elements
            swapOptionsLayout = findViewById(R.id.swapOptionsLayout);
            ingredientBlocker = findViewById(R.id.ingredientBlockerOverlay);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing view lists: " + e.getMessage(), e);
        }
    }

    private void updateScoreDisplay(int score) {
        try {
            if (scoreTextView != null) {
                scoreTextView.setText(getString(R.string.score_format, score));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating score display: " + e.getMessage(), e);
        }
    }

    private void updateDeadProcessCountDisplay(int count) {
        try {
            if (deadProcessCountTextView != null) {
                deadProcessCountTextView.setText(getString(R.string.failed_processes_format, count));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating dead process count: " + e.getMessage(), e);
        }
    }

    private void initIngredientViews() {
        try {
            if (ingredientInventory == null || ingredientFetcher == null) {
                Log.e(TAG, "ingredientInventory or ingredientFetcher is null");
                return;
            }

            List<Ingredient> initialList = ingredientFetcher.generateIngredientsRandom(ingredientInventory.maxCap);
            Log.d(TAG, "Initial ingredients: " + initialList.size());

            if (initialList.isEmpty()) {
                Log.e(TAG, "Initial ingredient list is empty");
                return;
            }

            ingredientInventory.setInitialList(initialList);
            updateInventoryUI();
            updateAvailableUI();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ingredient views: " + e.getMessage(), e);
        }
    }

    private void updateAvailableUI() {
        try {
            if (ingredientFetcher == null || availableIngredientsViews == null || availableIngredientsViews.isEmpty()) {
                Log.e(TAG, "ingredientFetcher or availableIngredientsViews is null/empty");
                return;
            }

            List<Ingredient> swappableIngredients = ingredientFetcher.getAvailableList();
            if (swappableIngredients == null || swappableIngredients.isEmpty()) {
                Log.e(TAG, "Swappable ingredients list is null/empty");
                return;
            }

            for (int i = 0; i < availableIngredientsViews.size(); i++) {
                if (i >= swappableIngredients.size()) {
                    break;
                }

                final int index = i;
                ImageView avIngView = availableIngredientsViews.get(i);
                Ingredient ingredient = swappableIngredients.get(i);

                if (ingredient != null) {
                    avIngView.setImageResource(ingredient.getIconResourceId());
                    avIngView.setBackgroundResource(R.drawable.swap_options_normal);

                    avIngView.setOnClickListener(v -> {
                        handleAvailableIngredientClick(index);
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating available UI: " + e.getMessage(), e);
        }
    }

    private void handleAvailableIngredientClick(int index) {
        try {
            if (selectedIngredientIndex != -1 && ingredientInventory != null && ingredientFetcher != null) {
                List<Ingredient> swapOptions = ingredientFetcher.getAvailableList();

                if (swapOptions != null && index < swapOptions.size() && selectedIngredientIndex < ingredientInventory.heldItemCount()) {
                    Ingredient dropIngredient = ingredientInventory.getByIndex(selectedIngredientIndex);

                    if (index < availableIngredientsViews.size()) {
                        availableIngredientsViews.get(index).setBackgroundResource(R.drawable.swap_options_selected);
                    }

                    selectedSwapIndex = index;
                    disableAllViews();

                    if (ingredientBlocker != null) {
                        ingredientBlocker.setVisibility(VISIBLE);
                    }

                    ingredientFetcher.exchangeIngredient(dropIngredient, swapOptions.get(index), this);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling available ingredient click: " + e.getMessage(), e);
            resetSelectionState();
        }
    }

    private void disableAllViews() {
        try {
            disableAll(availableIngredientsViews);
            disableAll(inventoryViews);
        } catch (Exception e) {
            Log.e(TAG, "Error disabling views: " + e.getMessage(), e);
        }
    }

    private void updateInventoryUI() {
        try {
            if (ingredientInventory == null) {
                Log.e(TAG, "ingredientInventory is null");
                return;
            }

            List<Ingredient> invHeld = ingredientInventory.getHeld();
            Log.d(TAG, "Running updateInventoryUI() with size = " + invHeld.size());

            updateInventoryIcons(invHeld);
            updateBasketIcons(invHeld);
            setupInventoryClickListeners(invHeld);

            Log.d(TAG, "Finished updateInventoryUI()");
        } catch (Exception e) {
            Log.e(TAG, "Error in updateInventoryUI: " + e.getMessage(), e);
        }
    }

    private void updateInventoryIcons(List<Ingredient> invHeld) {
        try {
            if (inventoryViews == null || invHeld == null) {
                return;
            }

            for (int i = 0; i < inventoryViews.size(); i++) {
                if (i >= invHeld.size()) {
                    break;
                }

                ImageView view = inventoryViews.get(i);
                if (view != null) {
                    view.setImageResource(invHeld.get(i).getIconResourceId());
                    view.setBackgroundResource(R.drawable.inventory_slot_normal);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating ingredientInventory icons: " + e.getMessage(), e);
        }
    }

    private void updateBasketIcons(List<Ingredient> invHeld) {
        try {
            if (basketViews == null || invHeld == null) {
                return;
            }

            for (int i = 0; i < basketViews.size(); i++) {
                ImageView basketView = basketViews.get(i);
                if (basketView == null) continue;

                if (i >= invHeld.size()) {
                    // Set default basket image
                    basketView.setImageResource(R.drawable.basket);
                    continue;
                }

                Ingredient ingredient = invHeld.get(i);
                if (ingredient == null) {
                    basketView.setImageResource(R.drawable.basket);
                    continue;
                }

                Log.d(TAG, "Updating basket " + i + " with " + ingredient.getName());
                int resId;

                try {
                    switch (ingredient.getName()) {
                        case "carrot":
                            resId = R.drawable.carrot_basket;
                            break;
                        case "potato":
                            resId = R.drawable.potato_basket;
                            break;
                        case "onion":
                            resId = R.drawable.onion_basket;
                            break;
                        case "cabbage":
                            resId = R.drawable.cabbage_basket;
                            break;
                        case "tomato":
                            resId = R.drawable.tomato_basket;
                            break;
                        default:
                            Log.d(TAG, "SHOWING EMPTY BASKET");
                            resId = R.drawable.basket;
                            break;
                    }
                    basketView.setImageResource(resId);
                } catch (Exception e) {
                    Log.e(TAG, "Error setting basket image: " + e.getMessage(), e);
                    // Fallback to default basket
                    try {
                        basketView.setImageResource(R.drawable.basket);
                    } catch (Exception ex) {
                        Log.e(TAG, "Error setting fallback basket image: " + ex.getMessage(), ex);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating basket icons: " + e.getMessage(), e);
        }
    }

    private void setupInventoryClickListeners(List<Ingredient> invHeld) {
        try {
            if (inventoryViews == null || invHeld == null) {
                return;
            }

            for (int i = 0; i < inventoryViews.size(); i++) {
                if (i >= invHeld.size()) {
                    continue;
                }

                final int index = i;
                ImageView invView = inventoryViews.get(i);

                if (invView == null) continue;

                Log.d(TAG, "Setting listener for ingredientInventory index " + i + " (" + invHeld.get(i).getName() + ")");

                invView.setOnClickListener(v -> {
                    handleInventoryItemClick(index);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up ingredientInventory click listeners: " + e.getMessage(), e);
        }
    }

    private void handleInventoryItemClick(int index) {
        try {
            Log.d(TAG, "Clicked ingredientInventory index: " + index);

            if (selectedIngredientIndex == index) {
                // Deselect the current item
                selectedIngredientIndex = -1;

                if (swapOptionsLayout != null) {
                    swapOptionsLayout.setVisibility(INVISIBLE);
                }

                if (index < inventoryViews.size()) {
                    inventoryViews.get(index).setBackgroundResource(R.drawable.inventory_slot_normal);
                }

                disableAll(availableIngredientsViews);
            } else {
                // Deselect previous selection if there was one
                if (selectedIngredientIndex != -1 && selectedIngredientIndex < inventoryViews.size()) {
                    inventoryViews.get(selectedIngredientIndex).setBackgroundResource(R.drawable.inventory_slot_normal);
                }

                // Select new item
                selectedIngredientIndex = index;

                if (index < inventoryViews.size()) {
                    inventoryViews.get(index).setBackgroundResource(R.drawable.inventory_slot_selected);
                }

                if (swapOptionsLayout != null) {
                    swapOptionsLayout.setVisibility(VISIBLE);
                }

                enableAll(availableIngredientsViews);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling ingredientInventory item click: " + e.getMessage(), e);
            resetSelectionState();
        }
    }

    private void enableAll(List<? extends View> views) {
        try {
            if (views == null) return;

            for (View v : views) {
                if (v != null) {
                    v.setEnabled(true);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error enabling views: " + e.getMessage(), e);
        }
    }

    private void disableAll(List<? extends View> views) {
        try {
            if (views == null) return;

            for (View v : views) {
                if (v != null) {
                    v.setEnabled(false);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disabling views: " + e.getMessage(), e);
        }
    }

    @Override
    public void receiveNewIngredient(Ingredient newIngredient) {
        runOnUiThread(() -> {
            try {
                if (newIngredient != null && ingredientInventory != null && selectedIngredientIndex >= 0 && selectedIngredientIndex < ingredientInventory.heldItemCount()) {
                    ingredientInventory.swapIngredientAtIndex(selectedIngredientIndex, newIngredient);
                }

                Log.d(TAG, "Calling updateInventoryUI()");
                updateInventoryUI();
                Log.d(TAG, "Completed updateInventoryUI()");
                updateAvailableUI();

                // Reset selection states safely
                resetSelectionState();

                // Re-enable ingredientInventory items
                enableAll(inventoryViews);

                // Hide UI elements
                if (ingredientBlocker != null) {
                    ingredientBlocker.setVisibility(GONE);
                }

                if (swapOptionsLayout != null) {
                    swapOptionsLayout.setVisibility(INVISIBLE);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in receiveNewIngredient: " + e.getMessage(), e);
                resetSelectionState();
            }
        });
    }

    private void resetSelectionState() {
        try {
            // Reset background for selected ingredientInventory item if valid
            if (selectedIngredientIndex >= 0 && selectedIngredientIndex < inventoryViews.size()) {
                inventoryViews.get(selectedIngredientIndex).setBackgroundResource(R.drawable.inventory_slot_normal);
            }

            // Reset background for selected swap option if valid
            if (selectedSwapIndex >= 0 && selectedSwapIndex < availableIngredientsViews.size()) {
                availableIngredientsViews.get(selectedSwapIndex).setBackgroundResource(R.drawable.swap_options_normal);
            }

            // Reset selection indices
            selectedSwapIndex = -1;
            selectedIngredientIndex = -1;

            // Re-enable all views
            enableAll(inventoryViews);

            // Hide UI elements
            if (ingredientBlocker != null) {
                ingredientBlocker.setVisibility(GONE);
            }

            if (swapOptionsLayout != null) {
                swapOptionsLayout.setVisibility(INVISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resetting selection state: " + e.getMessage(), e);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (gameManager != null) {
                gameManager.pauseGame();
            }

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onPause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            hideSystemUI();

            if (gameManager != null) {
                gameManager.resumeGame();
            }

            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (gameManager != null) {
                gameManager.stopGame();
            }

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }

    private void hideSystemUI() {
        try {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding system UI: " + e.getMessage(), e);
        }
    }

    private void updateMediaPlaybackSpeed(int deadProcessCount) {
        if (deadProcessCount >= 3) {
            if (mediaPlayer != null) {
                mediaPlayer.stop(); // Stop the music
                mediaPlayer.reset(); // Reset the MediaPlayer to prepare for reuse if needed
                mediaPlayer = MediaPlayer.create(this, R.raw.gameover);
                mediaPlayer.start(); // Start playing the audio
            }
            return; // Exit the method
        }

        float speed;
        switch (deadProcessCount) {
            case 0:
                speed = 1.0f; // 0/3 speed
                break;
            case 1:
                speed = 2.0f; // 1/3 speed
                break;
            case 2:
                speed = 3.0f; // 2/3 speed
                break;
            default:
                speed = 1.0f; // Fallback (should not occur)
                break;
        }

        if (mediaPlayer != null) {
            PlaybackParams playbackParams = new PlaybackParams();
            playbackParams.setSpeed(speed);
            mediaPlayer.setPlaybackParams(playbackParams);
            Log.d(TAG, "Playback speed updated to: " + speed);
        }
    }

    // GameManager.GameListener implementation
    @Override
    public void onTimerTick() {
        runOnUiThread(() -> {
            try {
                if (gameManager != null && processAdapter != null) {
                    processAdapter.updateProcesses(gameManager.getActiveProcesses());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onTimerTick: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void onProcessAdded(Process process) {
        runOnUiThread(() -> {
            try {
                if (processAdapter != null && gameManager != null) {
                    processAdapter.updateProcesses(gameManager.getActiveProcesses());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onProcessAdded: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void onProcessCompleted(Process process) {
        runOnUiThread(() -> {
            try {
                if (processAdapter != null && gameManager != null) {
                    processAdapter.updateProcesses(gameManager.getActiveProcesses());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onProcessCompleted: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void onProcessDied(Process process) {
        runOnUiThread(() -> {
            if (processAdapter != null && gameManager != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
                updateDeadProcessCountDisplay(gameManager.getDeadProcessCount());
                updateMediaPlaybackSpeed(gameManager.getDeadProcessCount());
            }
        });
    }

    @Override
    public void onProcessAboutToDie(Process process) {
        // Optional: vibration is already handled by GameManager
    }

    @Override
    public void onScoreChanged(int newScore) {
        runOnUiThread(() -> {
            try {
                updateScoreDisplay(newScore);
            } catch (Exception e) {
                Log.e(TAG, "Error in onScoreChanged: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void onGameOver(int finalScore) {
        runOnUiThread(() -> {
            GameOverDialog gameOverDialog = new GameOverDialog(this, finalScore);
            gameOverDialog.show();
            saveHighScore(finalScore);
        });
    }

    private void saveHighScore(int score) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyGamePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int highScore = sharedPreferences.getInt("highScore", Integer.MIN_VALUE);

        if (score > highScore || highScore == Integer.MIN_VALUE) {
            editor.putInt("highScore", score);
            editor.apply();
        }
    }

    @Override
    public void onCompleteButtonClicked(Process process) {
        if (gameManager != null && process != null) {
            gameManager.completeProcess(process.getId());
        }
    }
}
