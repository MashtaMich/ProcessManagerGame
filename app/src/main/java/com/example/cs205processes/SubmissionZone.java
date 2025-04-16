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
        for (Recipe recipe : Recipe.getDefaultRecipes()) {
            if (recipe.getName().equals(recipeName)) {
                return recipe.canCook(submittedIngredients);
            }
        }
        return false;
    }
    @Override
    public void onInteract(Player player) {
        // Logic: check if held dish is valid for an order
        Log.d("SubmissionZone", "Attempted to submit dish");
        int heldType = player.getInventory().checkHeldType();

        Log.d("SubmissionZone", "COOKED constant value: " + PlayerInventory.COOKED);
        Log.d("SubmissionZone", "Actual heldType value: " + heldType);
        Log.d("SubmissionZone", "Is held item null? " + (player.getInventory().getHeld() == null));
        Log.d("SubmissionZone", "Condition result: " + (heldType == PlayerInventory.COOKED && player.getInventory().getHeld() != null));

        if (heldType == PlayerInventory.COOKED && player.getInventory().getHeld() != null) {
            CookedFood food = (CookedFood) player.getInventory().getHeld();
            Log.d("SubmissionZone", "Getting food: " + food.getName());

            boolean isCorrect = checkCorrectIngredientsForRecipe(food.getName(), food.getMadeWith());
            Log.d("SubmissionZone", "Correct : " + food.getName());

            // IMPLEMENT MY LOGIC HERE -> Submit the dish, e.g., score++ && remove from process queue
            if ( isCorrect ) {
                player.getInventory().getAndRemoveItem();
                Log.d("SubmissionZone", "Remove : " + food.getName());
            }

        } else {
            // DISPLAY AN ERROR MESSAGE TO SAY WRONG INGREDIENTS FOR THE DISH
            Log.d("SubmissionZone", "Nothing to submit or invalid item.");
        }
    }

}
