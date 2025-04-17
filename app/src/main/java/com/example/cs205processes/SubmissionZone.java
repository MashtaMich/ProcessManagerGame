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
    private Recipe checkCorrectIngredientsForRecipe(String recipeName, List<Ingredient> submittedIngredients) {
        if (recipeName == null || submittedIngredients == null) {
            return null;
        }
        for (Recipe recipe : Recipe.getDefaultRecipes()) {
            if (recipe.getName().equals(recipeName)) {
                boolean result = recipe.canCook(submittedIngredients);
                return recipe;
            }
        }
        return null;
    }
    @Override
    public void onInteract(Player player) {
        if (player == null || player.getInventory() == null) {
            return;
        }
        
        // Get the held item type
        int heldType = player.getInventory().checkHeldType();
        FoodItem heldItem = player.getInventory().getHeld();

        if (heldType == PlayerInventory.COOKED && heldItem != null) {
            try {
                CookedFood food = (CookedFood) heldItem;

                if (food.getMadeWith() == null) {
                    return;
                }

                Recipe getRecipe = checkCorrectIngredientsForRecipe(food.getName(), food.getMadeWith());

                if (getRecipe != null) {
                    GameManager manager = player.getGame().getGameManager();
                    List<Process> active = manager.getActiveProcesses();
                    for (Process process : active) {
                        if (!process.isComplete() && !process.isDead() &&
                                process.getRecipe().getName().equals(food.getName())) {

                            manager.completeProcess(process.getId()); // removes it next tick + updates UI
                            break;
                        }
                    }
                    player.getInventory().getAndRemoveItem();



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
