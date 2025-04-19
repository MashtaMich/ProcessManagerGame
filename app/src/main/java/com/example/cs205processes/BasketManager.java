package com.example.cs205processes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BasketManager {
    //To manage baskets, mainly fills the baskets at basketFiller and returns them when requested at UI thread
    //Basket index is 0 at bottom most and last is the vertically highest basket
    private final String TAG = "BasketManager";
    private final int maxCap; //Max num of baskets
    private final List<Basket> baskets;
    private final Object basketsLock = new Object();

    public BasketManager(int maxCap){
        this.maxCap=maxCap;
        this.baskets=new ArrayList<>(maxCap);
    }

    public void addBasket(Basket basket) {
        if (baskets.size() < maxCap) {
            baskets.add(basket);
            Log.d(TAG, "added basket:"+(baskets.size()-1));
        } else {
            Log.d(TAG, "Cannot add any more baskets");
        }
    }

    public void updateBasketContents(int basketIndex, Ingredient ingredient) {
        Log.d(TAG,"Updating basket content");
        synchronized (basketsLock){
            if (basketIndex > -1 && basketIndex < baskets.size()) {
                baskets.get(basketIndex).setIngredient(ingredient.getName());
                Log.d(TAG, "Updated basket " + (basketIndex+1) + " to: " + ingredient.getName());
            }
        }
    }

    public Ingredient getIngredientFromBasket(int index){
        synchronized (basketsLock){
            if (index > -1 && index < baskets.size()) return new Ingredient(baskets.get(index).getIngredient());
        }
        return null;
    }

    public int getBasketCount() {
        return baskets.size();
    }
}