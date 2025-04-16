package com.example.cs205processes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PotFunctions {
    private final String TAG="PotFunctions";
    private final List<Ingredient> ingredientsInside;
    private CookedFood foodDone;
    private final int maxIngredients;
    private boolean readyToCook;
    private final int cookTime=3000;
    private final Object foodDoneLock = new Object();// to sync available list
    private final Object ingredientLock=new Object();//ingredient lock

    public interface PotListener{
        void cookIngredientsUpdate(CookedFood foodCooked,int index);
        void potProgressUpdate(int progress);
    }

    public PotFunctions(){
        this.ingredientsInside=new ArrayList<>();
        this.maxIngredients=3;
        this.readyToCook=false;
        this.foodDone=null;
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
            readyToCook = ingredientsInside.size() == maxIngredients;
        }
    }

    public boolean isReadyToCook(){
        synchronized (ingredientLock) {
            return readyToCook;
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

    public void cookIngredients(Recipe recipe,PotListener listener,int index){
        boolean canCook;
        synchronized (ingredientLock) {
            canCook = readyToCook;
            if (canCook) {
                readyToCook = false;
            }
        }

        if (canCook){
            CookedFood newFood;
            try{
                Thread.sleep(cookTime);
            }catch(InterruptedException e){
                Log.e(TAG,"Error at fetch Ingredient "+e.getLocalizedMessage());
            }
            synchronized (ingredientLock) {
                newFood = new CookedFood(5, recipe.getName(), R.drawable.placeholder, new ArrayList<>(recipe.getIngredients()));
                ingredientsInside.clear();
            }
            synchronized (foodDoneLock) {
                this.foodDone = newFood;
            }

            listener.cookIngredientsUpdate(newFood,index);
        }
        listener.cookIngredientsUpdate(null,index);
    }
}
