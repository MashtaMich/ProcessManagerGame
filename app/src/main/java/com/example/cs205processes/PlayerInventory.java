package com.example.cs205processes;

import android.util.Log;

public class PlayerInventory {
    private static final String TAG = "Player Inventory";

    public final int EMPTY=0;
    public final int INGREDIENT=1;
    public final int COOKED=2;
    public final int INVALID=-1;
    private FoodItem held;

    public FoodItem getHeld(){
        return held;
    }

    public void grabHeld(FoodItem item){
        Log.d(TAG,"Added item to player inventory: "+item.name);
        this.held=item;
    }

    public FoodItem giveItem(){
        Log.d(TAG,"removed item from player inventory: "+held.name);
        FoodItem item=this.held;
        this.held=null;
        return item;
    }

    public int checkHeldType() {
        if (held == null) {
            System.out.println("Nothing is being held.");
            Log.d(TAG,"Player holding nothing");
            return EMPTY;
        } else if (held instanceof Ingredient) {
            System.out.println("Holding an ingredient: " + held.getName());
            Log.d(TAG,"Player holding Ingredient");
            return INGREDIENT;
        } else if (held instanceof CookedFood) {
            System.out.println("Holding cooked food: " + held.getName());
            Log.d(TAG,"Player holding Cooked Food");
            return COOKED;
        } else {
            System.out.println("Unknown item held.");
            Log.d(TAG,"Error player holding unknown");
            return INVALID;
        }
    }
}
