package com.game.cookingspree;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientBasketFiller {
    //Fills baskets off IngredientFetchWorker's input into Ingredient Queue
    //To maintain index order with used List in fetch worker must maintain FIFO principles
    private static final String TAG = "Basket Filler";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final IngredientQueue queue;
    private final BasketManager basketManager;
    private final int fillSize;

    public interface BasketFillListener{
        void finishedBasketFilling(List<Ingredient> fillOrder);
    }

    public IngredientBasketFiller(IngredientQueue queue, BasketManager basketManager,Integer fillSize) {
        this.queue = queue;
        this.basketManager = basketManager;
        this.fillSize=fillSize;
    }

    public void startFilling(BasketFillListener listener) {
            executor.submit(() -> {
                List<Ingredient> fillOrder=new ArrayList<>();

                for (int i=fillSize-1;i> -1;i--){
                        // Take ingredient from queue (blocks if no ingredient inside)
                         Ingredient ingredient=null;
                         try {
                             ingredient = queue.take();
                         } catch (Exception e) {
                             Log.e(TAG, "Exception during take()", e);
                         }

                         if (ingredient!=null){
                             basketManager.updateBasketContents(i, ingredient);

                             Log.d(TAG, "filled basket with: " + ingredient.getName() + " at basket " + i);
                             fillOrder.add(ingredient);
                         }else{
                             Log.e(TAG,"Failed to add an ingredient at basket"+i);
                         }
                }
                listener.finishedBasketFilling(fillOrder);
            });
    }
}
