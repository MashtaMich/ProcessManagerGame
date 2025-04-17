package com.example.cs205processes;

import android.util.Log;

public class PlayerInventory {
    private static final String TAG = "Player Inventory";

    public static final int EMPTY=0;
    public static final int INGREDIENT=1;
    public static final int COOKED=2;
    public static final int INVALID=-1;
    private FoodItem held;

    public FoodItem getHeld(){
        return held;
    }

    public void grabItem(FoodItem item){
        Log.d(TAG,"Added item to player inventory: "+item.name);
        this.held=item;
    }

    public FoodItem getAndRemoveItem(){
        if (held == null) {
            Log.d(TAG,"Cannot remove item from player inventory: nothing is being held");
            return null;
        }
        Log.d(TAG,"removed item from player inventory: "+held.name);
        FoodItem item=this.held;
        this.held=null;
        return item;
    }

    public int checkHeldType() {
        if (held == null) {
            System.out.println("Nothing is being held.");
            Log.d("HELDTYPE", "Player holding nothing");
            return EMPTY;
        } else if (held instanceof Ingredient) {
            System.out.println("Holding an ingredient: " + held.getName());
            Log.d("HELDTYPE", "Player holding Ingredient: " + held.getName());
            return INGREDIENT;
        } else if (held instanceof CookedFood) {
            System.out.println("Holding cooked food: " + held.getName());
            Log.d("HELDTYPE", "Player holding Cooked Food: " + held.getName());
            Log.d("HELDTYPE", "Class: " + held.getClass().getName());
            return COOKED;
        } else {
            System.out.println("Unknown item held.");
            Log.d("HELDTYPE", "Error player holding unknown type: " + held.getClass().getName());
            return INVALID;
        }
    }
}
