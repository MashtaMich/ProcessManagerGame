package com.example.cs205processes;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//not currently in use since resourceloader makes the game load fast enough. Can consider integrating if the game gets larger and loading takes longer
//purpose : separate creation of game and game manager away from UI thread
public class GameLoader {
    public interface Callback {
        void onGameLoaded(Game game);
    }

    public static void loadGameAsync(Context context, GameView gameView, Callback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Setup required parts in background thread
            PlayerInventory inventory = new PlayerInventory(Recipe.getDefaultRecipes());
            BasketManager basketManager = new BasketManager(3);
            PotThreadPool potThreadPool = new PotThreadPool(2);

            Game game = new Game(gameView, context, inventory, potThreadPool, basketManager);

            mainHandler.post(() -> {
                callback.onGameLoaded(game);
            });
        });
    }
}
