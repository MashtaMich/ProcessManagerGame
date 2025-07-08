package com.game.cookingspree;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/** @noinspection BusyWait*/
public class PotFunctions {
    //To handle the pot internal functions
    private final String TAG="PotFunctions";
    private final List<Ingredient> ingredientsInside;
    private CookedFood foodDone;
    private final int progressStep=1000;//in ms, how long each progress tick is
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
        //Ready to cook if ingredient list reaches the maxIngredients
        synchronized (ingredientLock) {
            return ingredientsInside.size() == maxIngredients;
        }
    }

    public List<Ingredient> getIngredientsInside(){
        //Return a copy of ingredients inside, shouldn't be manipulated outside of this class
        synchronized (ingredientLock) {
            return new ArrayList<>(ingredientsInside);
        }
    }

    public CookedFood getFood(){
        //Get finished food from pot and clear the food when passed
        synchronized (foodDoneLock) {
            if (this.foodDone != null) {
                CookedFood food = this.foodDone;
                this.foodDone = null;
                return food;
            }
        }
        return null;
    }

    public void addIngredient(Ingredient ingredient){
        synchronized (ingredientLock) {
            ingredientsInside.add(ingredient);
        }
    }

    public boolean gotFood(){
        //If there is finished food in the pot
        synchronized (foodDoneLock) {
            return foodDone != null;
        }
    }

    public void cookIngredients(Recipe recipe,PotListener listener){
        //Cooking ingredients function

        boolean canCook;
        synchronized (ingredientLock) {
            canCook=isReadyToCook();
        }

        if (canCook){
            CookedFood newFood;
            this.recipeCooking=recipe;//Store recipe for saving

            // Start cooking the ingredients
            this.cookProgress = 0; // Store cooking progress for saving

            try {
                //Simulate cooking
                while (cookProgress<cookTime/progressStep){
                    Thread.sleep(progressStep);
                    cookProgress++;

                    //Send progress update to UI thread
                    listener.potProgressUpdate(cookProgress);
                }
            } catch (InterruptedException e) {
                Log.e(TAG,"Error at cooking process Ingredient "+e.getLocalizedMessage());
            }

            //Create new cooked food using recipe, default id is 5
            newFood = new CookedFood(5, recipe.getName(),new ArrayList<>(recipe.getIngredients()));

            synchronized (ingredientLock) {
                this.ingredientsInside.clear();
            }

            synchronized (foodDoneLock) {
                this.foodDone = newFood;
            }

            this.recipeCooking=null;

            //Send final progressUpdate to UI thread
            listener.potProgressUpdate(cookProgress+1);
            this.cookProgress=0;
        }
    }

    public void restartCooking(Recipe recipe,PotListener listener){
        // restart cooking the ingredients
        try {
            while (cookProgress<cookTime/progressStep){
                Thread.sleep(progressStep);
                this.cookProgress++;
                listener.potProgressUpdate(cookProgress);
            }
        } catch (InterruptedException e) {
                Log.e(TAG,"Error at cooking process Ingredient after restart"+e.getLocalizedMessage());
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
        Log.d(TAG,"Restarted cooking complete");
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