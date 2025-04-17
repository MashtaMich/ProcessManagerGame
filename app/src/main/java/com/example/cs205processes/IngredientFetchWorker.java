package com.example.cs205processes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientFetchWorker {
    // to simulate a worker maintaining the ingredient storage
    private static final String TAG = "Ingredient Fetcher";
    private final IngredientBasketFiller basketFiller;
    private final ExecutorService executor= Executors.newSingleThreadExecutor();
    private final int totalIngredients =5;
    private List<Ingredient> availableList=new ArrayList<>(totalIngredients);
    private List<Ingredient> usedList=new ArrayList<>();
    private final int fetchTime =3000;
    private final Random random;
    // rest of the thread is the ingredient storage room
    private final IngredientQueue queue;
    private final Object availableLock = new Object();// to sync available list
    private final int maxCap=3;


    public interface ingredientFetchListener{
        void fetchIngredientProgressUpdate(int progress);
    }

    public IngredientFetchWorker(IngredientQueue queue,IngredientBasketFiller basketFiller){
        // fill the available list
        Log.d(TAG, "Initializing ingredient list");
        generateIngredientList();
        this.queue=queue;
        this.random=new Random();
        this.basketFiller=basketFiller;
    }

    private void generateIngredientList(){
        List<Ingredient> ingredientList=new ArrayList<>();
        for (int i = 0; i< totalIngredients; i++){
            Ingredient ingredient=new Ingredient(i);
            ingredientList.add(ingredient);
        }
        synchronized (availableLock) {
            this.availableList=ingredientList;
        }
    }

    public List<Ingredient> getAvailableList(){
        synchronized (availableLock) {
            return new ArrayList<>(availableList); // Return a copy to prevent external modification
        }
    }

    public void exchangeIngredient(Ingredient returnIngredient,Ingredient getIngredient,ingredientFetchListener listener){

            executor.submit(()->{
                try{
                    fetchIngredient(returnIngredient,getIngredient,listener);
                }catch(Exception e){
                    Log.e(TAG,"Error at fetch Ingredient thread "+e.getLocalizedMessage());
                }
            });

    }

    public void updateBaskets(ingredientFetchListener listener){
            executor.submit(()->{
                try{
                    updatingBaskets();
                    basketFiller.startFilling((IngredientBasketFiller.BasketFillListener) listener);
                }catch(Exception e){
                    Log.e(TAG,"Error at fetch Ingredient thread "+e.getLocalizedMessage());
                }
            });

    }

    private void updatingBaskets() {
        try{
            if (usedList.size()!=3){
                Log.e(TAG,"Used list is not correct size");
                return;
            }
            for (Ingredient ingredient:usedList){
                queue.put(ingredient);
            }
        }catch(InterruptedException e){
            Log.e(TAG, "filler interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

    }

    public List<Ingredient> generateIngredientsRandom(ingredientFetchListener listener){
        //Done to generate the initial inventory
        //Get number depends on the max_cap in Inventory class
        // get random ingredients from the available list
        synchronized (availableLock) {
            for (int i = 0; i < maxCap; i++) {
                if (availableList.isEmpty()) {
                    break;
                }

                int randomIndex = random.nextInt(availableList.size());

                // remove ingredient to prevent duplicates
                usedList.add(availableList.remove(randomIndex));
            }
        }
        updateBaskets(listener);
        return usedList;
    }

    private void fetchIngredient(Ingredient returnIngredient,Ingredient ingredient, ingredientFetchListener listener) {
        Log.d(TAG,"Starting fetch");
        try {
            for (int i=1;i<=fetchTime/1000;i++){
                    Thread.sleep(1000);
                    listener.fetchIngredientProgressUpdate(i);
            }
        } catch (InterruptedException e) {
                // handle later
                Log.e(TAG,"Error at fetch Ingredient "+e.getLocalizedMessage());
        }
        Ingredient result = null;
        // get input ingredient from the available list
        synchronized (availableLock) {
            //replace ingredient to fetch from inventory
            if (usedList.contains(returnIngredient) && availableList.contains(ingredient)) {
                int swapItemIndex=availableList.indexOf(ingredient);
                availableList.set(swapItemIndex,returnIngredient);

                result = ingredient;
                swapItemIndex=usedList.indexOf(returnIngredient);
                usedList.set(swapItemIndex,result);
            }
        }
        if (result != null) {
            Log.d(TAG,"Swapped "+returnIngredient.getName()+" for "+ingredient.getName());
        }else{
            Log.d(TAG,"Failed to swap  "+returnIngredient.getName());
        }

        listener.fetchIngredientProgressUpdate(fetchTime/1000+1);
        Log.d(TAG,"Starting update Baskets");
        updateBaskets(listener);
    }
}
