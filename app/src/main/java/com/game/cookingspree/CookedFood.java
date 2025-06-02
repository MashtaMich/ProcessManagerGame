package com.game.cookingspree;

import java.util.List;

public class CookedFood extends FoodItem {
    //Class for food created by Pot using a recipe, foodItem id is always 5
    private final List<Ingredient> madeWith;

    public CookedFood(int id, String recipeName, List<Ingredient> madeWith) {
        super(id);
        this.name = recipeName;
        getIconResourceIdFromName(recipeName);
        this.madeWith=madeWith;
    }

    public List<Ingredient> getMadeWith(){
        return madeWith;
    }

    private void getIconResourceIdFromName(String name){
        switch (name){
            case "Tomato Soup":
                this.iconResourceId=R.drawable.table_tomatosoup;
                break;
            case "Veggie Stew":
                this.iconResourceId=R.drawable.table_veggiestew;
                break;
            case "Mashed Potato":
                this.iconResourceId=R.drawable.table_mashedpotato;
                break;
            case "Salad":
                this.iconResourceId=R.drawable.table_salad;
                break;
            default:
                this.iconResourceId=R.drawable.table_trash;
                break;
        }
    }
}