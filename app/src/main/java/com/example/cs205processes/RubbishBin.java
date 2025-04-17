package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

public class RubbishBin extends Interactable {
    private Bitmap openSprite, closedSprite;
    private boolean isClosed = true;

    public RubbishBin(Context context, float x, float y, JSONObject props) {
        this.x = x;
        this.y = y;

        try {
            closedSprite = loadSprite(context, props.getString("closed_sprite"));
            openSprite = loadSprite(context, props.getString("open_sprite"));
            sprite = closedSprite;
        } catch (Exception e) {
            e.printStackTrace();
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
                isClosed = false;
                sprite = openSprite;

                // Remove the item from inventory
                inventory.getAndRemoveItem();

                // Close the bin after a short delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    isClosed = true;
                    sprite = closedSprite;
                }, 500);
            } else {
                // Still animate the bin opening and closing for feedback
                isClosed = false;
                sprite = openSprite;

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    isClosed = true;
                    sprite = closedSprite;
                }, 500);
            }
        } catch (Exception e) {
            isClosed = true;
            sprite = closedSprite;
        }
    }
}
