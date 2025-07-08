package com.game.cookingspree;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONObject;

public class RubbishBin extends Interactable {
    private Bitmap openSprite, closedSprite;

    private int emptyInteractionCount = 0;

    private final Context context;

    public RubbishBin(Context context, float x, float y, JSONObject props) {
        this.context = context;
        this.x = x;
        this.y = y;

        try {
            closedSprite = loadSprite(context, props.getString("closed_sprite"));
            openSprite = loadSprite(context, props.getString("open_sprite"));
            sprite = closedSprite;
        } catch (Exception e) {
            Log.e("Bin","Error loading sprites for Bin:"+e.getLocalizedMessage());
        }
    }

    @Override
    public void onInteract(Player player) {
        try {
            // Check if the player has an inventory
            if (player == null || player.getInventory() == null) {
                return;
            }

            // Get the player's inventory
            PlayerInventory inventory = player.getInventory();

            // Check if the player is holding something
            int heldType = inventory.checkHeldType();

            if (heldType != PlayerInventory.EMPTY) {
                //isClosed = false;
                sprite = openSprite;

                // Remove the item from inventory
                inventory.getAndRemoveItem();

                // Close the bin after a short delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    sprite = closedSprite;
                }, 500);
            } else {
                emptyInteractionCount++;
                sprite = openSprite;

                if (emptyInteractionCount >= 10) {
                    emptyInteractionCount = 0;
                    Toast.makeText(context, "Stop playing with my feelings, give me some actual food!", Toast.LENGTH_SHORT).show();
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    sprite = closedSprite;
                }, 500);
            }
        } catch (Exception e) {
            sprite = closedSprite;
        }
    }
}