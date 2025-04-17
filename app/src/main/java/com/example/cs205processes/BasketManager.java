package com.example.cs205processes;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasketManager {
    private final String TAG = "BasketManager";
    private final int maxCap;
    private final List<Basket> baskets;

    public BasketManager(int maxCap){
        this.maxCap=maxCap;
        this.baskets=new ArrayList<>(maxCap);
    }

    public void registerBasket(Basket basket) {
        if (baskets.size() < maxCap) {
            baskets.add(basket);
            Log.d(TAG, "Registered basket");
        } else {
            Log.d(TAG, "Cannot add any more baskets");
        }
    }

    public void updateBasketContents(int basketIndex, Ingredient ingredient) {
        Log.d(TAG,"Updating basket content");
        if (basketIndex > -1 && basketIndex < baskets.size()) {
            baskets.get(basketIndex).setIngredient(ingredient.getName().toLowerCase());
            Log.d(TAG, "Updated basket " + basketIndex+1 + " to: " + ingredient.getName());
        }
    }

    public Basket getBasket(int index) {
        if (index > -1 && index < baskets.size()) {
            return baskets.get(index);
        }
        return null;
    }

    public Ingredient getIngredientFromBasket(int index){
        if (index > -1 && index < baskets.size()) {
            return new Ingredient(baskets.get(index).getIngredient());
        }
        return null;
    }

    public int getBasketCount() {
        return baskets.size();
    }
}