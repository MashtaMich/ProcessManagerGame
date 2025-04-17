package com.example.cs205processes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientBasketFiller {
    private static final String TAG = "Basket Filler";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final IngredientQueue queue;
    private final BasketManager basketManager;

    public interface BasketFillListener{
        void finishedBasketFilling(List<Ingredient> fillOrder);
    }

    public IngredientBasketFiller(IngredientQueue queue, BasketManager basketManager) {
        this.queue = queue;
        this.basketManager = basketManager;
    }

    public void startFilling(BasketFillListener listener) {
        Log.d(TAG,"Start filling from queue size:"+queue.size());
            executor.submit(() -> {
                List<Ingredient> fillOrder=new ArrayList<>();
                int fillSize= queue.size();

                     for (int i=fillSize-1;i> -1;i--){
                        // Take ingredient from queue (will block, should never be blocking if it is there is an error)
                         Ingredient ingredient=null;
                         try {
                             ingredient = queue.take();
                         } catch (Exception e) {
                             Log.e(TAG, "Exception during take()", e);
                         }

                         basketManager.updateBasketContents(i, ingredient);

                        Log.d(TAG, "filled basket with: " + ingredient.getName() + " at basket " + i);
                        fillOrder.add(ingredient);

                    }
                listener.finishedBasketFilling(fillOrder);
            });
    }
}
