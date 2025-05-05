package com.example.cs205processes;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends BaseActivity implements
        GameManager.GameListener,
        IngredientFetchWorker.ingredientFetchListener,
        IngredientBasketFiller.BasketFillListener,
        PotFunctions.PotListener
        {

    private final Handler moveHandler = new Handler(Looper.getMainLooper());
    private Runnable moveRunnable;
    private MediaPlayer mediaPlayer;
    private GameManager gameManager;
    private ProcessAdapter processAdapter;
            private TextView scoreTextView;
    private TextView deadProcessCountTextView;

            private Game game;

    private List<ImageView> inventoryViews;
    private List<ImageView> availableIngredientsViews;
    private LinearLayout swapOptionsLayout;

    private IngredientFetchWorker ingredientFetcher;
    private View ingredientBlocker;
    private PlayerInventory playerInventory;
    private int selectedIngredientIndex=-1;
    private int selectedSwapIndex=-1;
            private final int maxIngredients=3;
    private PotThreadPool potThreadPool;
    private ImageView playerInventoryView;
            private SharedPreferences sharedPreferences;
    private BasketManager basketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_game);
            sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
            setupJoystickSizeListener(
                    findViewById(R.id.joystickSizeGroup),
                    R.id.smallSize,
                    R.id.largeSize,
                    scale -> applyJoystickScale(findViewById(android.R.id.content))
            );


            enableImmersiveMode();
            setupMediaPlayer();
            setupVolumeSeekBar();
            initializeGameComponents();
            setupInteractButton();
            setupPauseMenuButtons();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            finish();
        }
    }
    private void setupVolumeSeekBar() {
        SeekBar volumeSeekBar = findViewById(R.id.volumeSeekBar);
        int savedVolume = sharedPreferences.getInt("volume", 100);
        volumeSeekBar.setProgress(savedVolume);
        float volume = savedVolume / 100f;
        mediaPlayer.setVolume(volume, volume);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float newVolume = progress / 100f;
                mediaPlayer.setVolume(newVolume, newVolume);
                sharedPreferences.edit().putInt("volume", progress).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    private void setupMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.overcooked);
        mediaPlayer.setLooping(true);
        int savedVolume = sharedPreferences.getInt("volume", 100);
        float volume = savedVolume / 100f;
        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.start();
    }


    private void setupInteractButton() {
        Button interactButton = findViewById(R.id.interactButton);
        Log.d("Interact", "Setting up interact button listener");
        interactButton.setOnClickListener(v -> {
            if (game != null) {
                game.interact();
                updatePlayerInventoryView();
            } else {
                Log.w("Interact", "Game not initialized yet!");
            }
        });
        Log.d("Interact", "Interact listener assigned");
    }
    private void setupPauseMenuButtons(){
        // Link buttons
        Button togglePauseButton = findViewById(R.id.togglePauseButton);
        togglePauseButton.setText(R.string.pause); // Default state
        LinearLayout pauseMenu = findViewById(R.id.pauseMenu);
        LinearLayout settingsMenu = findViewById(R.id.SettingsMenu);
        Button resume = findViewById(R.id.btnResume);
        Button save = findViewById(R.id.btnSave);
        Button settings = findViewById(R.id.btnSettings);
        Button mainMenuButton = findViewById(R.id.btnMainMenu);
        ImageButton back = findViewById(R.id.backButton);

        // Set listeners
        togglePauseButton.setOnClickListener(v -> {
            if (gameManager.isGameOver()) return; // Don’t allow toggling if game is over

            if (gameManager.isRunning()) {
                gameManager.pauseGame();
                pauseMenu.setVisibility(View.VISIBLE);
            }
        });
        resume.setOnClickListener(v -> {
            gameManager.resumeGame();
            pauseMenu.setVisibility(View.GONE);  // Hide the pause menu
        });
        save.setOnClickListener(v -> {
            saveGameState();
            Toast.makeText(this, "Game saved", Toast.LENGTH_SHORT).show();
        });
        settings.setOnClickListener(v -> {
            settingsMenu.setVisibility(View.VISIBLE);
            pauseMenu.setVisibility(GONE);
            back.setVisibility(VISIBLE);
        });
        mainMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close the current activity
        });
        back.setOnClickListener(v -> {
            settingsMenu.setVisibility(View.GONE);
            pauseMenu.setVisibility(View.VISIBLE);
            back.setVisibility(View.GONE);
        });
    }

    private void saveGameState() {
        try {
            // Get SharedPreferences and create an editor
            SharedPreferences prefs = getSharedPreferences("GameSave", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Save player position
            Player player = game.getPlayer();
            editor.putFloat("playerX", player.getX());
            editor.putFloat("playerY", player.getY());

            // Save score and progress
            int score = gameManager.getScore();
            int deadProcessCount = gameManager.getDeadProcessCount();
            editor.putInt("score", score);
            editor.putInt("deadProcessCount", deadProcessCount);

            // Save player inventory
            FoodItem heldItem = playerInventory.getHeld();
            if (heldItem != null) {
                int itemType = playerInventory.checkHeldType();
                editor.putInt("heldItemType", itemType);
                editor.putInt("heldItemId", heldItem.getId());
                editor.putString("heldItemName", heldItem.getName());

                // For cooked food, save ingredients
                if (itemType == PlayerInventory.COOKED && heldItem instanceof CookedFood) {
                    CookedFood cookedFood = (CookedFood) heldItem;
                    List<Ingredient> ingredients = cookedFood.getMadeWith();

                    if (ingredients != null && !ingredients.isEmpty()) {
                        editor.putInt("heldItemIngredientsCount", ingredients.size());

                        for (int i = 0; i < ingredients.size(); i++) {
                            Ingredient ingredient = ingredients.get(i);
                            editor.putInt("heldItemIngredient_" + i + "_id", ingredient.getId());
                            editor.putString("heldItemIngredient_" + i + "_name", ingredient.getName());
                        }
                    }
                }
            } else {
                editor.putInt("heldItemType", PlayerInventory.EMPTY);
            }

            // Save Table items
            List<Table> tables = game.getTables();
            editor.putInt("tableCount", tables.size());

            for (int i = 0; i < tables.size(); i++) {
                Table table = tables.get(i);
                FoodItem itemOnTable = table.getItemOnTable();

                if (itemOnTable != null) {
                    int itemType = (itemOnTable instanceof Ingredient) ?
                            PlayerInventory.INGREDIENT : PlayerInventory.COOKED;
                    editor.putInt("table_" + i + "_itemType", itemType);
                    editor.putInt("table_" + i + "_itemId", itemOnTable.getId());
                    editor.putString("table_" + i + "_itemName", itemOnTable.getName());

                    // For cooked food, save ingredients
                    if (itemType == PlayerInventory.COOKED && itemOnTable instanceof CookedFood) {
                        CookedFood cookedFood = (CookedFood) itemOnTable;
                        List<Ingredient> ingredients = cookedFood.getMadeWith();

                        if (ingredients != null && !ingredients.isEmpty()) {
                            editor.putInt("table_" + i + "_ingredientsCount", ingredients.size());

                            for (int j = 0; j < ingredients.size(); j++) {
                                Ingredient ingredient = ingredients.get(j);
                                editor.putInt("table_" + i + "_ingredient_" + j + "_id", ingredient.getId());
                                editor.putString("table_" + i + "_ingredient_" + j + "_name", ingredient.getName());
                            }
                        }
                    }
                } else {
                    editor.putInt("table_" + i + "_itemType", -1);  // -1 indicates empty table
                }
            }

            // Save Process information
            List<Process> activeProcesses = gameManager.getActiveProcesses();
            List<Process> relevantProcesses = new ArrayList<>();

            // Only save processes that aren't complete or dead
            for (Process process : activeProcesses) {
                if (!process.isComplete() && !process.isDead()) {
                    relevantProcesses.add(process);
                }
            }

            editor.putInt("processCount", relevantProcesses.size());

            // Save each process
            for (int i = 0; i < relevantProcesses.size(); i++) {
                Process process = relevantProcesses.get(i);
                editor.putString("process_" + i + "_recipe", process.getRecipe().getName());
                editor.putInt("process_" + i + "_remaining", process.getTimeRemaining());
                editor.putInt("process_" + i + "_limit", process.getTimeLimit());
            }

            // Save pots
            List<Pot> pots = game.getPots();
            editor.putInt("potCount", pots.size());

            for (int i = 0; i < pots.size(); i++) {
                Pot pot = pots.get(i);

                // Save common pot info for all pot states
                editor.putString("pot_" + i + "_state", pot.getState());

                List<Ingredient> ingredients = pot.getInPot();
                editor.putInt("pot_" + i + "_ingredientCount", ingredients.size());

                //Save the ingredients
                for (int j = 0; j < ingredients.size(); j++) {
                    Ingredient ingredient = ingredients.get(j);
                    editor.putInt("pot_" + i + "_ingredient_" + j + "_id", ingredient.getId());
                }

                // If pot is DONE state, save cooked food
                if (pot.getState().equals(Pot.State.DONE.name())) {
                    CookedFood food = pot.getFood();
                    editor.putInt("pot_" + i + "_food_id", food.getId());
                    editor.putString("pot_" + i + "_food_name", food.getName());

                    List<Ingredient> madeWith = food.getMadeWith();
                    editor.putInt("pot_" + i + "_food_ingredientCount", madeWith.size());

                    for (int j = 0; j < madeWith.size(); j++) {
                        Ingredient ingredient = madeWith.get(j);
                        editor.putInt("pot_" + i + "_food_ingredient_" + j + "_id", ingredient.getId());
                    }
                }

                // If pot is in COOKING state, save cooking progress and recipe being cooked as well
                if (pot.getState().equals(Pot.State.COOKING.name())) {
                    editor.putInt("pot_" + i + "_cooking_progress", pot.getPotFunctions().getCookProgress());
                    Recipe beingCooked=pot.getPotFunctions().getRecipeCooking();
                    editor.putString("pot_" + i + "_recipe_name", beingCooked.getName());

                    List<Ingredient> ingredientList=beingCooked.getIngredients();

                    editor.putInt("pot_" + i + "_recipe_ingredientCount", ingredientList.size());

                    for (int j = 0; j < ingredientList.size(); j++) {
                        Ingredient ingredient = ingredientList.get(j);
                        editor.putInt("pot_" + i + "_recipe_ingredient_" + j + "_id", ingredient.getId());
                    }
                }
            }

            // Apply all changes
            editor.apply();

            Toast.makeText(this, "Game saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("GameActivity", "Error saving game: " + e.getMessage());
            Toast.makeText(this, "Error saving game", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGameState() {
        try {
            //Get load save
            SharedPreferences prefs = getSharedPreferences("GameSave", MODE_PRIVATE);
            // Load player position
            float playerX = prefs.getFloat("playerX", 0);
            float playerY = prefs.getFloat("playerY", 0);
            game.getPlayer().setPosition(playerX, playerY);

            //load dead process count
            int deadProcessCount = prefs.getInt("deadProcessCount", 0);
            gameManager.setDeadProcessCount(deadProcessCount);

            //Load score
            int score = prefs.getInt("score", 0);
            gameManager.setScore(score);

            //Update UI
            updateScoreDisplay(score);
            updateDeadProcessCountDisplay(deadProcessCount);

            //Clear player inventory
            playerInventory.getAndRemoveItem();

            // Load Player Inventory
            int heldItemType = prefs.getInt("heldItemType", PlayerInventory.EMPTY);
            if (heldItemType != PlayerInventory.EMPTY) {
                int itemId = prefs.getInt("heldItemId", 0);
                String itemName = prefs.getString("heldItemName", "");

                FoodItem itemToHold = null;

                if (heldItemType == PlayerInventory.INGREDIENT) {
                    // Create ingredient using saved id
                    itemToHold = new Ingredient(itemId);
                }
                else if (heldItemType == PlayerInventory.COOKED) {
                    // Get ingredients for cooked food
                    int ingredientsCount = prefs.getInt("heldItemIngredientsCount", 0);
                    List<Ingredient> ingredients = new ArrayList<>();

                    for (int i = 0; i < ingredientsCount; i++) {
                        int ingId = prefs.getInt("heldItemIngredient_" + i + "_id", 0);
                        Ingredient ingredient = new Ingredient(ingId);
                        ingredients.add(ingredient);
                    }

                    // Create cooked food
                    itemToHold = new CookedFood(itemId, itemName, ingredients);
                }

                // If valid item was created, add to inventory
                if (itemToHold != null) {
                    playerInventory.grabItem(itemToHold);
                }
            }

            // Restore items on tables
            int tableCount = prefs.getInt("tableCount", 0);
            List<Table> tables = game.getTables();

            for (int i = 0; i < Math.min(tableCount, tables.size()); i++) {
                Table table = tables.get(i);
                int itemType = prefs.getInt("table_" + i + "_itemType", -1);

                table.clearItem();

                if (itemType != -1) {  // -1 means table was empty
                    int itemId = prefs.getInt("table_" + i + "_itemId", 0);
                    String itemName = prefs.getString("table_" + i + "_itemName", "");

                    FoodItem itemToPlace = null;

                    if (itemType == PlayerInventory.INGREDIENT) {
                        // Create a simple ingredient
                        itemToPlace = new Ingredient(itemId);
                    }
                    else if (itemType == PlayerInventory.COOKED) {
                        // Get ingredients for cooked food
                        int ingredientsCount = prefs.getInt("table_" + i + "_ingredientsCount", 0);
                        List<Ingredient> ingredients = new ArrayList<>();

                        for (int j = 0; j < ingredientsCount; j++) {
                            int ingId = prefs.getInt("table_" + i + "_ingredient_" + j + "_id", 0);
                            Ingredient ingredient = new Ingredient(ingId);
                            ingredients.add(ingredient);
                        }

                        // Create cooked food
                        itemToPlace = new CookedFood(itemId, itemName, ingredients);
                    }

                    // Place item on table
                    if (itemToPlace != null) {
                        table.placeItem(itemToPlace);  // Add this method to Table.java
                    }
                }
            }

            // Make sure the player inventory view is updated in UI
            updatePlayerInventoryView();

            // Load processes
            int processCount = prefs.getInt("processCount", 0);
            List<Recipe> recipes = Recipe.getDefaultRecipes();

            for (int i = 0; i < processCount; i++) {
                String recipeName = prefs.getString("process_" + i + "_recipe", "");
                int timeRemaining = prefs.getInt("process_" + i + "_remaining", 30);
                int timeLimit = prefs.getInt("process_" + i + "_limit", 60);

                // Find matching recipe
                for (Recipe recipe : recipes) {
                    if (recipe.getName().equals(recipeName)) {
                        // Create a new process with this data
                        Process process = Process.generateRandomProcess(recipe, timeLimit, timeRemaining);
                        gameManager.addProcessDirectly(process);
                        break;
                    }
                }
            }

            //Load pots
            int potCount = prefs.getInt("potCount", 0);
            List<Pot> pots = game.getPots();

            for (int i = 0; i < Math.min(potCount, pots.size()); i++) {
                Pot pot = pots.get(i);

                // Load pot state, or get default of EMPTY
                String potState = prefs.getString("pot_" + i + "_state", Pot.State.EMPTY.name());
                Log.d(TAG,"potState:"+potState);
                pot.setState(potState);

                // Load ingredients in the pot
                int ingredientCount = prefs.getInt("pot_" + i + "_ingredientCount", 0);
                List<Ingredient> ingredients = new ArrayList<>();

                for (int j = 0; j < ingredientCount; j++) {
                    int ingId = prefs.getInt("pot_" + i + "_ingredient_" + j + "_id", 0);
                    Ingredient ingredient = new Ingredient(ingId);
                    ingredients.add(ingredient);
                }

                // Add ingredients to pot
                PotFunctions potFunctions = pot.getPotFunctions();
                for (Ingredient ingredient : ingredients) {
                    potFunctions.addIngredient(ingredient);
                }

                // If pot is in DONE state, load the cooked food
                if (Pot.State.valueOf(potState)==Pot.State.DONE) {
                    Log.d(TAG,"Loading DONE pot");
                    //Cooked food is always id of 5
                    int foodId = prefs.getInt("pot_" + i + "_food_id", 5);
                    String foodName = prefs.getString("pot_" + i + "_food_name", "");

                    int foodIngCount = prefs.getInt("pot_" + i + "_food_ingredientCount", 0);
                    List<Ingredient> foodIngredients = new ArrayList<>();

                    for (int j = 0; j < foodIngCount; j++) {
                        int ingId = prefs.getInt("pot_" + i + "_food_ingredient_" + j + "_id", 0);
                        Ingredient ingredient = new Ingredient(ingId);
                        foodIngredients.add(ingredient);
                    }

                    // Create the cooked food and set it in the pot
                    CookedFood cookedFood = new CookedFood(foodId, foodName, foodIngredients);
                    potFunctions.setCookedFood(cookedFood);
                }
                // If pot is in COOKING state, restore cooking progress
                else if (Pot.State.valueOf(potState)==Pot.State.COOKING) {
                    int cookProgress = prefs.getInt("pot_" + i + "_cooking_progress", 0);
                    String recipeName = prefs.getString("pot_" + i + "_recipe_name", "");

                    // Find the recipe that's being cooked from default recipes
                    Recipe cookingRecipe = null;
                    for (Recipe recipe : Recipe.getDefaultRecipes()) {
                        if (recipe.getName().equals(recipeName)) {
                            cookingRecipe = recipe;
                            break;
                        }
                    }
                    if (cookingRecipe==null){
                        cookingRecipe=new Recipe("Waste",new ArrayList<>());
                    }

                    Log.d(TAG,"Restart cooking recipe");
                    // put pot in cooking state
                    pot.setState(Pot.State.COOKING.name());

                    //Set cooking progress and recipe
                    potFunctions.setCookProgress(cookProgress);
                    final Recipe resumeRecipe=cookingRecipe;

                    // Restart cooking using cookRecipe and progress
                    potThreadPool.submit(() -> {
                        potFunctions.restartCooking(resumeRecipe, this);
                        //Set pot state
                        pot.setState(Pot.State.DONE.name());
                        Log.d(TAG,"Finished cooking recipe");
                    });

                }
            }

            if (processAdapter != null) {
                processAdapter.updateProcesses(gameManager.getActiveProcesses());
            }

            updateMediaPlaybackSpeed(deadProcessCount);

            Toast.makeText(this, "Game loaded", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("GameActivity", "Error loading game: " + e.getMessage());
        }
    }
    private void initializeGameComponents() {
        GameView gameView = findViewById(R.id.gameView);

        List<Recipe> recipeList=Recipe.getDefaultRecipes();
        playerInventory = new PlayerInventory(recipeList);
        basketManager = new BasketManager(maxIngredients);
        int maxPots = 2;
        potThreadPool = new PotThreadPool(maxPots);
        game = new Game(gameView, this, playerInventory,potThreadPool,basketManager);
        gameView.init(game);

        setupMovementControls();
        initializeUIComponents();
        initializeInventory();

        gameManager = new GameManager(this, this,recipeList);
        game.setGameManager(gameManager);
        gameManager.startGame();

        updateScoreDisplay(0);
        updateDeadProcessCountDisplay(0);

        boolean shouldLoadSave = getIntent().getBooleanExtra("loadSavedGame", false);
        if (shouldLoadSave) {
            loadGameState();
        }
    }

    private void setupMovementControls() {
        View btnUp = findViewById(R.id.btnUp);
        View btnDown = findViewById(R.id.btnDown);
        View btnLeft = findViewById(R.id.btnLeft);
        View btnRight = findViewById(R.id.btnRight);

        setupHoldMovement(btnUp, () -> game.moveUp());
        setupHoldMovement(btnDown, () -> game.moveDown());
        setupHoldMovement(btnLeft, () -> game.moveLeft());
        setupHoldMovement(btnRight, () -> game.moveRight());
    }

    private void setupHoldMovement(View button, Runnable movementAction) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.performClick();
                    moveRunnable = new Runnable() {
                        @Override
                        public void run() {
                            movementAction.run();
                            moveHandler.postDelayed(this, 150);
                        }
                    };
                    moveRunnable.run();
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    moveHandler.removeCallbacks(moveRunnable);
                    game.getPlayer().stopMovement();
                    return true;
            }
            return false;
        });
    }

    private void initializeUIComponents() {
        try {
            // Initialize statistics text views
            scoreTextView = findViewById(R.id.scoreTextView);
            deadProcessCountTextView = findViewById(R.id.deadProcessCountTextView);

            // Initialize process list
            RecyclerView processRecyclerView = findViewById(R.id.processRecyclerView);
            if (processRecyclerView == null) {
                Log.e(TAG, "processRecyclerView is null");
                return;
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            processRecyclerView.setLayoutManager(layoutManager);
            processAdapter = new ProcessAdapter(this, new ArrayList<>());
            processRecyclerView.setAdapter(processAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UI components: " + e.getMessage(), e);
        }
    }

    private void initializeInventory() {
        try {
            ingredientFetcher = new IngredientFetchWorker(maxIngredients,basketManager);
            initializeViewLists();
            initIngredientViews();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ingredientInventory: " + e.getMessage(), e);
        }
    }

    private void initializeViewLists() {
        try {
            // Initialize ingredientInventory UI slots
            inventoryViews = new ArrayList<>();
            View slot1 = findViewById(R.id.ingredientSlot1);
            View slot2 = findViewById(R.id.ingredientSlot2);
            View slot3 = findViewById(R.id.ingredientSlot3);

            if (slot1 != null) inventoryViews.add((ImageView) slot1);
            if (slot2 != null) inventoryViews.add((ImageView) slot2);
            if (slot3 != null) inventoryViews.add((ImageView) slot3);

            // Initialize available ingredients UI view
            availableIngredientsViews = new ArrayList<>();
            View option1 = findViewById(R.id.swapOption1);
            View option2 = findViewById(R.id.swapOption2);

            if (option1 != null) availableIngredientsViews.add((ImageView) option1);
            if (option2 != null) availableIngredientsViews.add((ImageView) option2);

            // Get other UI elements
            swapOptionsLayout = findViewById(R.id.swapOptionsLayout);
            ingredientBlocker = findViewById(R.id.ingredientBlockerOverlay);
            playerInventoryView = findViewById(R.id.playerInventory);
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
            if (ingredientFetcher == null) {
                Log.e(TAG, "ingredientInventory or ingredientFetcher is null");
                return;
            }

            //Generate initial 3 ingredients randomly
            List<Ingredient> initialList = ingredientFetcher.generateIngredientsRandom(this);
            Log.d(TAG, "Initial ingredients: " + initialList.size());

            if (initialList.isEmpty()) {
                Log.e(TAG, "Initial ingredient list is empty");
                return;
            }

            //Set up the click listeners
            setupInventoryClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ingredient views: " + e.getMessage(), e);
        }
    }

    private void updatePlayerInventoryView(){
        if (playerInventory.checkHeldType()!= PlayerInventory.EMPTY){
            playerInventoryView.setImageResource(playerInventory.getHeld().getIconResourceId());
        }else{
            playerInventoryView.setImageResource(R.drawable.button_secondary);
        }
    }

    private void updateAvailableUI() {
        //Update the available Ingredient views in UI
        try {
            if (ingredientFetcher == null || availableIngredientsViews == null || availableIngredientsViews.isEmpty()) {
                Log.e(TAG, "ingredientFetcher or availableIngredientsViews is null/empty");
                return;
            }

            //Get list of available ingredients
            List<Ingredient> availableIngredients = ingredientFetcher.getAvailableList();
            if (availableIngredients == null || availableIngredients.isEmpty()) {
                Log.e(TAG, "Swappable ingredients list is null/empty");
                return;
            }

            for (int i = 0; i < availableIngredientsViews.size(); i++) {
                if (i >= availableIngredients.size()) {
                    break;
                }

                final int index = i;
                ImageView avIngView = availableIngredientsViews.get(i);
                Ingredient ingredient = availableIngredients.get(i);

                if (ingredient != null) {
                    //Set the image and background resources
                    avIngView.setImageResource(ingredient.getIconResourceId());
                    avIngView.setBackgroundResource(R.drawable.swap_options_normal);

                    //Set the onClickListener for each available view
                    avIngView.setOnClickListener(v -> handleAvailableIngredientClick(index));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating available UI: " + e.getMessage(), e);
        }
    }

    private void handleAvailableIngredientClick(int index) {
        //Onclick handler
        try {
            if (selectedIngredientIndex != -1 && ingredientFetcher != null) {
                //Get available list of swap ingredients
                List<Ingredient> availableList = ingredientFetcher.getAvailableList();

                if (availableList != null && index < availableList.size() && selectedIngredientIndex < basketManager.getBasketCount()) {
                    //map the basket index to selectedIngredientIndex
                    int basketIndex=maxIngredients-1-selectedIngredientIndex;//basket ingredients are reverse of used list ingredients
                    Ingredient dropIngredient = basketManager.getIngredientFromBasket(basketIndex);

                    availableIngredientsViews.get(index).setBackgroundResource(R.drawable.swap_options_selected);

                    selectedSwapIndex = index;

                    //Disable the views until swap is done
                    disableAllViews();

                    //Put up the blocker in case disable fails
                    if (ingredientBlocker != null) {
                        ingredientBlocker.setVisibility(VISIBLE);
                    }

                    try{
                        Log.d(TAG,"exchanging "+dropIngredient.getName()+" for "+availableList.get(index).getName());
                        ingredientFetcher.exchangeIngredient(dropIngredient, availableList.get(index), this);
                    }catch(Exception e){
                        Log.e(TAG,"Failed to fetch ingredient:"+e.getLocalizedMessage());
                    }

                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling available ingredient click: " + e.getMessage(), e);
            resetSelectionState();
        }
    }

    private void disableAllViews() {
        //Disable all ingredient views when swapping
        try {
            disableAll(availableIngredientsViews);
            disableAll(inventoryViews);
        } catch (Exception e) {
            Log.e(TAG, "Error disabling views: " + e.getMessage(), e);
        }
    }

    private void updateInventoryUI(List<Ingredient> invHeld) {
        //Update inventory icons
        try {
            updateInventoryIcons(invHeld);

            Log.d(TAG, "Finished updateInventoryUI()");
        } catch (Exception e) {
            Log.e(TAG, "Error in updateInventoryUI: " + e.getMessage(), e);
        }
    }

    private void updateInventoryIcons(List<Ingredient> invHeld) {
        //Update inventory icons on every basket update to match baskets
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

    private void setupInventoryClickListeners() {
        //Set up the inventory click listeners
        try {
            if (inventoryViews == null) {
                return;
            }

            for (int i = 0; i < inventoryViews.size(); i++) {

                final int index = i;
                ImageView invView = inventoryViews.get(i);

                if (invView == null) continue;

                invView.setOnClickListener(v -> handleInventoryItemClick(index));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up ingredientInventory click listeners: " + e.getMessage(), e);
        }
    }

    private void handleInventoryItemClick(int index) {
        //Inventory click handler, set selectedIngredientIndex and appropriate styling
        try {
            Log.d(TAG, "Clicked ingredientInventory index: " + index);

            if (selectedIngredientIndex == index) {//If click on already selected inventory
                // Deselect the current item
                selectedIngredientIndex = -1;

                //Make available list invisible
                if (swapOptionsLayout != null) {
                    swapOptionsLayout.setVisibility(INVISIBLE);
                }
                disableAll(availableIngredientsViews);

                //Set the background of inventory views to unselected
                if (index < inventoryViews.size()) {
                    inventoryViews.get(index).setBackgroundResource(R.drawable.inventory_slot_normal);
                }


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

                //Make available ingredients visible and enable them
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
    public void fetchIngredientProgressUpdate(int progress){
        //Add progress updater for ingredient fetch
        runOnUiThread(() -> Log.d(TAG,"Received progress update for ingredient:"+progress));
    }

    @Override
    public void finishedBasketFilling(List<Ingredient> fillOrder){
        //Listener to update ui when baskets are changed
        runOnUiThread(() -> {
            try {
                updateAvailableUI();
                updateInventoryUI(fillOrder);

                // Reset selection states
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
                    Log.e(TAG, "Error in finish basket filling: " + e.getMessage(), e);
                }
            });
    }

    @Override
    public void potProgressUpdate(int progress){
        //Add progress updates for pot
        runOnUiThread(() -> Log.d(TAG,"Received progress update for pot:"+progress));
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
            applyJoystickScale(findViewById(android.R.id.content));

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


    private void updateMediaPlaybackSpeed(int deadProcessCount) {
        if (deadProcessCount >= 3) {
            if (mediaPlayer != null) {
                mediaPlayer.stop(); // Stop the music
                mediaPlayer.reset(); // Reset the MediaPlayer to prepare for reuse if needed
                mediaPlayer = MediaPlayer.create(this, R.raw.gameover);
                sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
                int savedVolume = sharedPreferences.getInt("volume", 100);
                float volume = savedVolume / 100f;
                mediaPlayer.setVolume(volume,volume);
                mediaPlayer.start(); // Start playing the audio
            }
            return;
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
                    updatePlayerInventoryView();
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


}
