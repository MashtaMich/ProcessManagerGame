// SubmissionZone.java
package com.example.cs205processes;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import org.json.JSONObject;

import java.util.List;

public class SubmissionZone extends Interactable {
    private final String TAG="SubmissionZone";
    private final Context context;
    public SubmissionZone(Context context, float x, float y, JSONObject props) {
        this.context = context;
        this.x = x;
        this.y = y;
        try {
            sprite = loadSprite(context, props.getString("sprite"));
        } catch (Exception e) {
            Log.e(TAG,"Error at loading submission zone sprite:"+e.getLocalizedMessage());
        }
    }

    private Recipe checkCorrectIngredientsForRecipe(String recipeName, List<Ingredient> submittedIngredients) {
        if (recipeName == null || submittedIngredients == null) {
            return null;
        }
        for (Recipe recipe : Recipe.getDefaultRecipes()) {
            if (recipe.getName().equals(recipeName)) {
                //boolean result = recipe.canCook(submittedIngredients);
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
                    playNotificationSound();
                    return;
                }

                Recipe getRecipe = checkCorrectIngredientsForRecipe(food.getName(), food.getMadeWith());

                if (getRecipe != null) {
                    GameManager manager = player.getGame().getGameManager();
                    List<Order> active = manager.getActiveProcesses();
                    boolean submitted = false;
                    for (Order order : active) {
                        if (!order.isComplete() && !order.isDead() &&
                                order.getRecipe().getName().equals(food.getName())) {

                            manager.completeProcess(order.getId()); // removes it next tick + updates UI
                            submitted = true;
                            break;
                        }
                    }
                    if (submitted) {
                        player.getInventory().getAndRemoveItem();
                    } else {
                        playNotificationSound();
                    }


                }else {
                    playNotificationSound();
                }
            } catch (ClassCastException e) {
                Log.e(TAG, "ClassCastException: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Nothing to submit or invalid item. HeldType: " + heldType);
            playNotificationSound();
        }
    }

    private void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            if (r != null) {
                r.play();
            }
        } catch (Exception e) {
            Log.e(TAG,"Exception when playing notification sound:"+e.getLocalizedMessage());
        }
    }
}