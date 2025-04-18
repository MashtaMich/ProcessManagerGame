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
    private Recipe recipeCooking;
    private Integer cookProgress;

    public interface PotListener{//To send to UI thread in GameActivity
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
            this.recipeCooking=recipe;

            // Start cooking the ingredients
            this.cookProgress = 0; // Progress will be every 1 second

            try {
                while (cookProgress<cookTime/1000){
                    Thread.sleep(1000);
                    cookProgress++;
                    listener.potProgressUpdate(cookProgress);
                }
            } catch (InterruptedException e) {
                Log.e(TAG,"Error at cooking process Ingredient "+e.getLocalizedMessage());
            }

            newFood = new CookedFood(5, recipe.getName(),new ArrayList<>(recipe.getIngredients()));
            synchronized (ingredientLock) {
                this.ingredientsInside.clear();
            }
            synchronized (foodDoneLock) {
                this.foodDone = newFood;
            }
            this.recipeCooking=null;

            listener.potProgressUpdate(cookProgress+1);
            this.cookProgress=0;
        }
    }

    public void restartCooking(Recipe recipe,PotListener listener){
        // restart cooking the ingredients
        try {
            while (cookProgress<cookTime/1000){
                Thread.sleep(1000);
                this.cookProgress++;
                listener.potProgressUpdate(cookProgress);
            }
        } catch (InterruptedException e) {
                Log.e(TAG,"Error at cooking process Ingredient "+e.getLocalizedMessage());
        }

        CookedFood newFood = new CookedFood(5, recipe.getName(),new ArrayList<>(recipe.getIngredients()));
        synchronized (ingredientLock) {
            this.ingredientsInside.clear();
        }
        synchronized (foodDoneLock) {
            this.foodDone = newFood;
        }
        this.recipeCooking=null;

        listener.potProgressUpdate(cookProgress+1);
        this.cookProgress=0;
        Log.d(TAG,"Restart complete");
    }

    public Recipe getRecipeCooking(){
        return this.recipeCooking;
    }

    public int getCookProgress(){
        return this.cookProgress;
    }

    public void setCookedFood(CookedFood food) {
        //For loading
        synchronized (foodDoneLock) {
            this.foodDone = food;
        }
    }

    public void setCookProgress(int progress){
        this.cookProgress=progress;
    }
}
