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
    private final List<Ingredient> usedList=new ArrayList<>();
    private final Random random;
    private final IngredientQueue queue;
    private final Object availableLock = new Object();// to sync available list and usedList
    private final int maxCap;


    public interface ingredientFetchListener{
        void fetchIngredientProgressUpdate(int progress);
    }

    public IngredientFetchWorker(Integer maxCap,BasketManager basketManager){
        // fill the available list
        Log.d(TAG, "Initializing ingredient list");
        generateIngredientList();
        this.maxCap=maxCap;
        this.queue=new IngredientQueue(maxCap);
        this.random=new Random();
        this.basketFiller=new IngredientBasketFiller(queue,basketManager,maxCap);
    }

    private void generateIngredientList(){
        //generate ingredient list
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
        //Swaps returnIngredient into availableList and getIngredient into usedList
        executor.submit(()->{
            try{
                fetchIngredient(returnIngredient,getIngredient,listener);
            }catch(Exception e){
                Log.e(TAG,"Error at fetch Ingredient thread "+e.getLocalizedMessage());
            }
        });

    }

    public void updateBaskets(ingredientFetchListener listener){
        //Updates baskets using Producer-Consumer pattern with used list
        executor.submit(()->{
            try{
                //Start consumer
                basketFiller.startFilling((IngredientBasketFiller.BasketFillListener) listener);
                //Start producer
                updatingBaskets();
            }catch(Exception e){
                Log.e(TAG,"Error at fetch Ingredient thread "+e.getLocalizedMessage());
            }
        });

    }

    private void updatingBaskets() {
        try{
            synchronized (availableLock){
                //Used list should always be size of maxCap
                if (usedList.size()!=maxCap){
                    Log.e(TAG,"Used list is not correct size");
                    return;
                }
                //Put all ingredients into the queue, will be taken from queue by basket filler
                for (Ingredient ingredient:usedList){
                    queue.put(ingredient);
                }
            }

        }catch(InterruptedException e){
            Log.e(TAG, "updating interrupted: " + e.getMessage());
        }

    }

    public List<Ingredient> generateIngredientsRandom(ingredientFetchListener listener){
        //Generates initial ingredient set up to maxCap
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
        //swaps the ingredient using a timer
        Log.d(TAG,"Starting fetch");
        //In ms, currently 3 seconds
        int fetchTime = 3000;
        try {
            for (int i = 1; i<= fetchTime /1000; i++){
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
                Log.d(TAG,"Swap item is at index:"+swapItemIndex+" is ingredient"+ingredient.getName());
                availableList.set(swapItemIndex,returnIngredient);

                result = ingredient;
                int returnItemIndex=usedList.indexOf(returnIngredient);
                Log.d(TAG,"return item is at index:"+swapItemIndex+" is ingredient"+returnIngredient.getName());
                usedList.set(returnItemIndex,result);
            }
        }
        if (result != null) {
            Log.d(TAG,"Swapped "+returnIngredient.getName()+" for "+ingredient.getName());
        }else{
            Log.d(TAG,"Failed to swap  "+returnIngredient.getName());
        }

        listener.fetchIngredientProgressUpdate(fetchTime /1000+1);
        Log.d(TAG,"Starting update Baskets");
        updateBaskets(listener);
    }
}
