// SubmissionZone.java
package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import org.json.JSONObject;

import java.util.List;

public class SubmissionZone extends Interactable {
    public SubmissionZone(Context context, float x, float y, JSONObject props) {
        this.x = x;
        this.y = y;
        try {
            sprite = loadSprite(context, props.getString("sprite"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * check correct
     */
    private boolean checkCorrectIngredientsForRecipe(String recipeName, List<Ingredient> submittedIngredients) {
        if (recipeName == null || submittedIngredients == null) {
            Log.e("SubmissionZone", "Recipe name or ingredients list is null");
            return false;
        }
        
        Log.d("SubmissionZone", "Checking recipe: " + recipeName);
        Log.d("SubmissionZone", "Submitted ingredients count: " + submittedIngredients.size());
        
        for (Recipe recipe : Recipe.getDefaultRecipes()) {
            Log.d("SubmissionZone", "Comparing with recipe: " + recipe.getName());
            if (recipe.getName().equals(recipeName)) {
                boolean result = recipe.canCook(submittedIngredients);
                Log.d("SubmissionZone", "Recipe match found, canCook result: " + result);
                return result;
            }
        }
        
        Log.d("SubmissionZone", "No matching recipe found for: " + recipeName);
        return false;
    }
    @Override
    public void onInteract(Player player) {
        // Logic: check if held dish is valid for an order
        Log.d("SubmissionZone", "onInteract called for SubmissionZone");
        
        // Check if player inventory is null
        if (player == null) {
            Log.e("SubmissionZone", "Player is null");
            return;
        }
        
        if (player.getInventory() == null) {
            Log.e("SubmissionZone", "Player inventory is null");
            return;
        }
        
        // Get the held item type
        int heldType = player.getInventory().checkHeldType();
        FoodItem heldItem = player.getInventory().getHeld();
        
        Log.d("SubmissionZone", "Player inventory held item: " + (heldItem != null ? heldItem.getName() : "null"));
        Log.d("SubmissionZone", "Player inventory held item class: " + (heldItem != null ? heldItem.getClass().getSimpleName() : "null"));
        Log.d("SubmissionZone", "COOKED constant value: " + PlayerInventory.COOKED);
        Log.d("SubmissionZone", "Actual heldType value: " + heldType);
        Log.d("SubmissionZone", "Is held item null? " + (heldItem == null));
        Log.d("SubmissionZone", "Condition result: " + (heldType == PlayerInventory.COOKED && heldItem != null));

        if (heldType == PlayerInventory.COOKED && heldItem != null) {
            try {
                CookedFood food = (CookedFood) heldItem;
                Log.d("SubmissionZone", "Getting food: " + food.getName());
                
                if (food.getMadeWith() == null) {
                    Log.e("SubmissionZone", "Food ingredients list is null");
                    return;
                }
                
                Log.d("SubmissionZone", "Food ingredients: " + food.getMadeWith().size());
                
                boolean isCorrect = checkCorrectIngredientsForRecipe(food.getName(), food.getMadeWith());
                Log.d("SubmissionZone", "Recipe check result: " + isCorrect);
                Log.d("SubmissionZone", "Correct: " + food.getName());

                // IMPLEMENT MY LOGIC HERE -> Submit the dish, e.g., score++ && remove from process queue
                if (isCorrect) {
                    FoodItem removedItem = player.getInventory().getAndRemoveItem();
                    Log.d("SubmissionZone", "Removed item: " + (removedItem != null ? removedItem.getName() : "null"));
                    Log.d("SubmissionZone", "Remove: " + food.getName());
                }
            } catch (ClassCastException e) {
                Log.e("SubmissionZone", "ClassCastException: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // DISPLAY AN ERROR MESSAGE TO SAY WRONG INGREDIENTS FOR THE DISH
            Log.d("SubmissionZone", "Nothing to submit or invalid item. HeldType: " + heldType);
        }
    }

}
