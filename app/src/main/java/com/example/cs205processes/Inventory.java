package com.example.cs205processes;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    int max_cap=3;
    private final List<Ingredient> heldIngredients=new ArrayList<>(max_cap);

    public List<Ingredient> getHeld(){
        return this.heldIngredients;
    }

    public void grabIngredient(Ingredient ingredient){
        if (!isFull()){
            heldIngredients.add(ingredient);
        }
    }

    public void clear(){
        heldIngredients.clear();
    }

    public void dropIngredient(Ingredient ingredient){
        heldIngredients.remove(ingredient);
    }

    public void drop_by_index(int index){
        heldIngredients.remove(index);
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
}
