package com.example.cs205processes;

import java.util.ArrayList;
import java.util.List;

public class Pot {
    private final List<Ingredient> ingredientsInside;
    private final int maxIngredients;
    private boolean readyToCook;

    public Pot(){
        this.ingredientsInside=new ArrayList<>();
        this.maxIngredients=3;
        this.readyToCook=false;
    }

    public List<Ingredient> getIngredientsInside(){
        return ingredientsInside;
    }

    private void addIngredient(Ingredient ingredient){
        ingredientsInside.add(ingredient);
        readyToCook=ingredientsInside.size()==maxIngredients;
    }

    private boolean isReadyToCook(){
        return readyToCook;
    }

    private CookedFood cookIngredients(Recipe recipe){
        if (readyToCook){
            CookedFood newFood=new CookedFood(5,recipe.getName(),R.drawable.placeholder,new ArrayList<>(ingredientsInside));
            ingredientsInside.clear();
            readyToCook=false;
            return newFood;
        }
        return null;
    }
}
