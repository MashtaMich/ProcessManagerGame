package com.example.cs205processes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientFetchWorker {
    // to simulate a worker maintaining the ingredient storage
    private static final String TAG = "IngredientFetcher";
    private final ExecutorService executor= Executors.newSingleThreadExecutor();
    private final int totalIngredients =5;
    private List<Ingredient> availableList=new ArrayList<>(totalIngredients);
    private final int fetchTime =3000;
    private final Random random;
    // rest of the thread is the ingredient storage

    private final Object availableLock = new Object();// to sync available list


    public interface ingredientFetchListener{
        void receiveNewIngredient(Ingredient newIngredient);
    }

    public IngredientFetchWorker(){
        // fill the available list
        Log.d(TAG, "Initializing ingredient list");
        generateIngredientList();
        this.random=new Random();
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
            fetchIngredient(returnIngredient,getIngredient,listener);
        });
    }

    public List<Ingredient> generateIngredientsRandom(Integer get_number){
        //Done to generate the initial inventory
        //Get number depends on the max_cap in Inventory class
        List<Ingredient> result=new ArrayList<>();
        // get random ingredients from the available list
        synchronized (availableLock) {
            for (int i = 0; i < get_number; i++) {
                if (availableList.isEmpty()) {
                    break;
                }

                int randomIndex = random.nextInt(availableList.size());
                Ingredient ingredient = availableList.get(randomIndex);
                result.add(ingredient);

                // remove ingredient to prevent duplicates
                availableList.remove(randomIndex);
            }
        }
        return result;
    }

    private void fetchIngredient(Ingredient returnIngredient,Ingredient ingredient, ingredientFetchListener listener) {
        try {
            Thread.sleep(fetchTime);
        } catch (InterruptedException e) {
            // handle later
            Log.d(TAG,"Error at fetch Ingredient "+e.getLocalizedMessage());
        }
        Ingredient result = null;
        // get input ingredient from the available list
        synchronized (availableLock) {
            //add returned ingredient from inventory
            if (!availableList.contains(returnIngredient)){
                availableList.add(returnIngredient);
            }
            //remove ingredient to fetch from inventory
            if (availableList.contains(ingredient)) {
                result = ingredient;
                // remove ingredient to prevent duplicates
                availableList.remove(ingredient);
            }
        }
        Log.d(TAG,"Swapped "+returnIngredient.getName()+" for "+ingredient.getName());
        listener.receiveNewIngredient(result);
    }

    public void returnIngredients(List<Ingredient> ingredients){
        synchronized (availableLock) {
            for (int i=0;i<ingredients.size();i++){
                Ingredient ingredient=ingredients.get(i);
                if (!availableList.contains(ingredient)){
                    availableList.add(ingredient);
                }
            }
        }
    }

    public void returnIngredient(Ingredient ingredient){
        synchronized (availableLock) {
            if (!availableList.contains(ingredient)){
                availableList.add(ingredient);
            }
        }
        Log.d(TAG, "Added "+ingredient.getName());
    }

    public Ingredient getIngredient(Ingredient ingredient){
        //For testing
        synchronized (availableLock) {
            availableList.remove(ingredient);
        }
        Log.d(TAG, "Removed and returned "+ingredient.getName());
        return ingredient;
    }

    private void resetAvailableList(){
        synchronized (availableLock) {
            this.availableList.clear();
        }
        generateIngredientList();
    }
}
