package com.example.cs205processes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PotFunctions {
    private final String TAG="PotFunctions";
    private final List<Ingredient> ingredientsInside;
    private CookedFood foodDone;
    private final int maxIngredients;
    private final int cookTime;
    private final Object foodDoneLock = new Object();// to sync available list
    private final Object ingredientLock=new Object();//ingredient lock

    public interface PotListener{
        void potProgressUpdate(int progress);
    }

    public PotFunctions(int cookTime){
        this.ingredientsInside=new ArrayList<>();
        this.maxIngredients=3;
        this.foodDone=null;
        this.cookTime=cookTime;
    }

    public boolean isReadyToCook() {
        synchronized (ingredientLock) {
            return ingredientsInside.size() == maxIngredients;
        }
    }

    public List<Ingredient> getIngredientsInside(){
        synchronized (ingredientLock) {
            return new ArrayList<>(ingredientsInside); // return a copy to avoid concurrency issues
        }
    }

    public CookedFood getFood(){
        synchronized (foodDoneLock) {
            if (this.foodDone != null) {
                CookedFood food = this.foodDone;
                this.foodDone = null;
                return food;
            }
            return null;
        }
    }

    public void addIngredient(Ingredient ingredient){
        synchronized (ingredientLock) {
            ingredientsInside.add(ingredient);
        }
    }

    public boolean gotFood(){
        synchronized (foodDoneLock) {
            return foodDone != null;
        }
    }

    public int potContainingNum(){
        synchronized (ingredientLock) {
            return ingredientsInside.size();
        }
    }

    public void cookIngredients(Recipe recipe,PotListener listener){
        boolean canCook;
        synchronized (ingredientLock) {
            canCook=isReadyToCook();
        }

        if (canCook){
            CookedFood newFood;

            // Start cooking the ingredients
            int progress = 0; // Progress will be every 1 second

            try {
                while (progress<cookTime/1000){
                    Thread.sleep(1000);
                    progress++;
                    listener.potProgressUpdate(progress);
                }
            } catch (InterruptedException e) {
                // handle later
                Log.e(TAG,"Error at cooking process Ingredient "+e.getLocalizedMessage());
            }

            synchronized (ingredientLock) {
                newFood = new CookedFood(5, recipe.getName(),new ArrayList<>(recipe.getIngredients()));
                ingredientsInside.clear();
            }
            synchronized (foodDoneLock) {
                this.foodDone = newFood;
            }

            listener.potProgressUpdate(progress+1);
        }
    }
}
