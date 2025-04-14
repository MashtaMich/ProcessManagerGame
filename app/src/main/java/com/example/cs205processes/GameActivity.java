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
        IngredientFetchWorker.ingredientFetchListener{

    private static final String TAG = "GameActivity";
    private MediaPlayer mediaPlayer;
    private GameManager gameManager;
    private ProcessAdapter processAdapter;
    private RecyclerView processRecyclerView;
    private TextView scoreTextView;
    private TextView deadProcessCountTextView;
    private TextView queueSizeTextView;

    private GameView gameView;
    private Game game;
    private int highScore = 0;

    private List<ImageView> inventoryViews;
    private List<ImageView> availableIngredientsViews;
    private LinearLayout swapOptionsLayout;

    private IngredientFetchWorker ingredientFetcher;
    private Inventory inventory;
    View ingredientBlocker;
    private int selectedIngredientIndex=-1;
    private int selectedSwapIndex=-1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_game);
        mediaPlayer = MediaPlayer.create(this, R.raw.overcooked);
        mediaPlayer.setLooping(true); // Enable looping
        mediaPlayer.start(); // Start playing the audio
        hideSystemUI();

        try {
            // 🌟 Init GameView + Game logic
            gameView = findViewById(R.id.gameView);
            game = new Game(gameView, this);
            gameView.init(game);

            // 🌟 Hook joystick controls to movement
            findViewById(R.id.btnUp).setOnClickListener(v -> game.moveUp());
            findViewById(R.id.btnDown).setOnClickListener(v -> game.moveDown());
            findViewById(R.id.btnLeft).setOnClickListener(v -> game.moveLeft());
            findViewById(R.id.btnRight).setOnClickListener(v -> game.moveRight());

            // 🌟 Init RecyclerView + Stats UI
            processRecyclerView = findViewById(R.id.processRecyclerView);
            scoreTextView = findViewById(R.id.scoreTextView);
            deadProcessCountTextView = findViewById(R.id.deadProcessCountTextView);
            queueSizeTextView = findViewById(R.id.queueSizeTextView);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            processRecyclerView.setLayoutManager(layoutManager);
            processAdapter = new ProcessAdapter(this, new ArrayList<>(), this);
            processRecyclerView.setAdapter(processAdapter);

            //Init inventory Views
            ingredientFetcher=new IngredientFetchWorker();
            inventory =new Inventory();
            inventoryViews = new ArrayList<>(List.of(
                    findViewById(R.id.ingredientSlot1),
                    findViewById(R.id.ingredientSlot2),
                    findViewById(R.id.ingredientSlot3)
            ));
            availableIngredientsViews=new ArrayList<>(List.of(
                    findViewById(R.id.swapOption1),
                    findViewById(R.id.swapOption2)
            ));
            swapOptionsLayout = findViewById(R.id.swapOptionsLayout);
            ingredientBlocker=findViewById(R.id.ingredientBlockerOverlay);
            initIngredientViews();

            // 🌟 Init GameManager
            gameManager = new GameManager(this, this);
            gameManager.startGame();

            updateScoreDisplay(0);
            updateDeadProcessCountDisplay(0);
            updateQueueSizeDisplay(0);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateQueueSizeDisplay(int queueSize) {
        if (queueSizeTextView != null) {
            queueSizeTextView.setText(getString(R.string.queue_size_format, queueSize));
        }
    }

    private void updateScoreDisplay(int score) {
        if (scoreTextView != null) {
            scoreTextView.setText(getString(R.string.score_format, score));
        }
    }

    private void updateDeadProcessCountDisplay(int count) {
        if (deadProcessCountTextView != null) {
            deadProcessCountTextView.setText(getString(R.string.failed_processes_format, count));
        }
    }

    private void initIngredientViews(){
        List<Ingredient> initialList=ingredientFetcher.generateIngredientsRandom(inventory.max_cap);
        inventory.setInitialList(initialList);
        updateInventoryUI();
        updateAvailableUI();
    }

    private void updateAvailableUI(){
        List<Ingredient> swappableIngredients = ingredientFetcher.getAvailableList();
        for (int i=0;i<availableIngredientsViews.size();i++){
            final int index = i;
            ImageView avIngView=availableIngredientsViews.get(i);
            avIngView.setImageResource(swappableIngredients.get(i).getIconResourceId());
            avIngView.setBackgroundResource(R.drawable.swap_options_normal);
            avIngView.setOnClickListener(v->{
                if (selectedIngredientIndex!=-1){
                    List<Ingredient> swapOptions = ingredientFetcher.getAvailableList();
                    Ingredient dropIngredient=inventory.getIngredientByIndex(selectedIngredientIndex);
                    availableIngredientsViews.get(index).setBackgroundResource(R.drawable.swap_options_selected);
                    selectedSwapIndex=index;
                    for (View imageView:availableIngredientsViews){
                        imageView.setEnabled(false);
                    }
                    for (View imageView:inventoryViews){
                        imageView.setEnabled(false);
                    }
                    ingredientBlocker.setVisibility(VISIBLE);
                    ingredientFetcher.exchangeIngredient(dropIngredient,swapOptions.get(index), this);
                }
            });
        }
    }

    private void updateInventoryUI(){
        List<Ingredient> invHeld=inventory.getHeld();
        for (int i=0;i<inventoryViews.size();i++){
            final int index = i;
            ImageView invView=inventoryViews.get(i);
            invView.setImageResource(invHeld.get(i).getIconResourceId());
            invView.setBackgroundResource(R.drawable.inventory_slot_normal);
            invView.setOnClickListener(v->{
                if (selectedIngredientIndex==index){
                    selectedIngredientIndex=-1;
                    swapOptionsLayout.setVisibility(INVISIBLE);
                    inventoryViews.get(index).setBackgroundResource(R.drawable.inventory_slot_normal);
                    for (View imageView:availableIngredientsViews){
                        imageView.setEnabled(false);
                    }
                }else{
                    selectedIngredientIndex = index;
                    inventoryViews.get(index).setBackgroundResource(R.drawable.inventory_slot_selected);
                    swapOptionsLayout.setVisibility(VISIBLE);
                    for (View imageView:availableIngredientsViews){
                        imageView.setEnabled(true);
                    }
                }
            });
        }
    }

    @Override
    public void receiveNewIngredient(Ingredient newIngredient){
        runOnUiThread(()->{
            if (newIngredient!=null){
                inventory.swapIngredientAtIndex(selectedIngredientIndex,newIngredient);
            }
            updateInventoryUI();
            updateAvailableUI();
            inventoryViews.get(selectedIngredientIndex).setBackgroundResource(R.drawable.inventory_slot_normal);
            availableIngredientsViews.get(selectedSwapIndex).setBackgroundResource(R.drawable.swap_options_normal);
            selectedSwapIndex=-1;
            selectedIngredientIndex=-1;
            for (View imageView:inventoryViews){
                imageView.setEnabled(true);
            }
            ingredientBlocker.setVisibility(GONE);
            swapOptionsLayout.setVisibility(INVISIBLE);
        });
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (gameManager != null) {
            gameManager.pauseGame();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // Pause the audio
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameManager != null) {
            gameManager.resumeGame();
        }
        if (mediaPlayer != null) {
            mediaPlayer.start(); // Resume playing if it was paused
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameManager != null) {
            gameManager.stopGame();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onQueueChanged(int queueSize) {
        runOnUiThread(() -> updateQueueSizeDisplay(queueSize));
    }

    @Override
    public void onTimerTick() {
        runOnUiThread(() -> {
            if (gameManager != null && processAdapter != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
            }
        });
    }

    @Override
    public void onProcessAdded(Process process) {
        runOnUiThread(() -> {
            if (processAdapter != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
            }
        });
    }

    @Override
    public void onProcessCompleted(Process process) {
        runOnUiThread(() -> {
            if (processAdapter != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
            }
        });
    }

    @Override
    public void onProcessDied(Process process) {
        runOnUiThread(() -> {
            if (processAdapter != null && gameManager != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
                updateDeadProcessCountDisplay(gameManager.getDeadProcessCount());
                // Update media playback speed based on dead process count
                updateMediaPlaybackSpeed(gameManager.getDeadProcessCount());
            }
        });
    }
    private void hideSystemUI() {
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
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

    @Override
    public void onProcessAboutToDie(Process process) {
        // Optional: vibration is already handled by GameManager
    }

    @Override
    public void onScoreChanged(int newScore) {
        runOnUiThread(() -> updateScoreDisplay(newScore));
    }

    @Override
    public void onGameOver(int finalScore) {
        runOnUiThread(() -> {
            try {
                GameOverDialog gameOverDialog = new GameOverDialog(this, finalScore);
                gameOverDialog.show();

                // Store the score
                saveHighScore(finalScore);
            } catch (Exception e) {
                Log.e(TAG, "Error showing game over dialog: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    private void saveHighScore(int score) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyGamePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve the current high score
        int highScore = sharedPreferences.getInt("highScore", Integer.MIN_VALUE); // Use Integer.MIN_VALUE as the default

        // Check if the score is negative and if the current high score is the default
        if (score < 0 && highScore == Integer.MIN_VALUE) {
            // Store the negative score as high score
            editor.putInt("highScore", score);
            editor.apply();
        } else if (score > highScore) {
            // Update high score if the new score is greater
            editor.putInt("highScore", score);
            editor.apply();
        }
    }
    @Override
    public void onCompleteButtonClicked(Process process) {
        if (gameManager != null) {
            gameManager.completeProcess(process.getId());
        }
    }
}
