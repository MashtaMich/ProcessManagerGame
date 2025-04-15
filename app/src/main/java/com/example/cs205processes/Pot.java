package com.example.cs205processes;

import java.util.List;

public class Pot {
    private List<Ingredient> ingredientsInside;
    private final int maxIngredients=3;
    private boolean readyToCook=false;

    private void addIngredient(Ingredient ingredient){
        ingredientsInside.add(ingredient);
        readyToCook=ingredientsInside.size()==maxIngredients;
    }

    private boolean isReadyToCook(){
        return readyToCook;
    }

    private CookedFood cookIngredients(Recipe recipe){
        CookedFood newFood=new CookedFood(0,recipe.getName(),R.drawable.placeholder);
        ingredientsInside.clear();
        readyToCook=false;
        return newFood;
    }
}
