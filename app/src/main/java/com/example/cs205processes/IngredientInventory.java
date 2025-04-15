package com.example.cs205processes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class IngredientInventory {
    private static final String TAG = "Ingredient Inventory";
    int maxCap=3;
    private List<Ingredient> heldIngredients=new ArrayList<>(maxCap);

    public List<Ingredient> getHeld(){
        return this.heldIngredients;
    }

    public void grabIngredient(Ingredient ingredient){
        if (!isFull()){
            heldIngredients.add(ingredient);
            Log.d(TAG, "put in inventory "+ingredient.getName());
        }else{
            Log.d(TAG, "Failed to add "+ingredient.getName());
        }
    }

    public void clear(){
        heldIngredients.clear();
    }

    public boolean dropIngredient(Ingredient ingredient){
        return heldIngredients.remove(ingredient);
    }

    public Ingredient getIngredientByIndex(int index){
        return heldIngredients.get(index);
    }

    public void setInitialList(List<Ingredient> initial_list){
        heldIngredients.clear();
        heldIngredients=initial_list;
    }

    public Ingredient getByIndex(int index){
        return heldIngredients.get(index);
    }

    public Ingredient dropByIndex(int index){
        Ingredient returnIngredient=heldIngredients.remove(index);
        Log.d(TAG, "Removed and returned "+returnIngredient.getName());
        return returnIngredient;
    }

    public void swapIngredientAtIndex(int index,Ingredient swapIngredient){
        heldIngredients.set(index,swapIngredient);
    }

    public int heldItemCount(){
        return heldIngredients.size();
    }

    public boolean isFull(){
        return heldIngredients.size()>=3;
    }

    public boolean isEmpty(){
        return heldIngredients.isEmpty();
    }

    public boolean isInHeld(List<Ingredient> ingredients){
        for (Ingredient ingredient : ingredients) {
            if (!heldIngredients.contains(ingredient)) {
                return false; // If even one ingredient missing return false
            }
        }
        return true; //Is in held list, have all ingredients for recipe
        }
    }
