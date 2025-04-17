package com.example.cs205processes;

import java.util.List;

public class CookedFood extends FoodItem {

    private final List<Ingredient> madeWith;

    public CookedFood(int id, String recipeName, int iconResourceId, List<Ingredient> madeWith) {
        super(id);
        this.name = recipeName;
        this.iconResourceId = iconResourceId;
        this.madeWith=madeWith;
    }

    public List<Ingredient> getMadeWith(){
        return madeWith;
    }

    private void getIconResourceIdFromName(String name){

    }
}