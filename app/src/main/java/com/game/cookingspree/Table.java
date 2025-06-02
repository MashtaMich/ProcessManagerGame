package com.game.cookingspree;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import org.json.JSONObject;

public class Table extends Interactable {
    private Bitmap tableSprite,cabbageSprite,tomatoSprite,onionSprite,carrotSprite,potatoSprite,mashedPotatoSprite,veggieStewSprite,trashSprite,tomatoSoupSprite,saladSprite;
    private FoodItem itemOnTable;

    public Table(Context context, float x, float y, JSONObject props) {
        this.x = x;
        this.y = y;
        try {
            tableSprite = loadSprite(context, props.getString("sprite"));
            cabbageSprite = loadSprite(context, props.getString("table_cabbage_sprite"));
            tomatoSprite = loadSprite(context, props.getString("table_tomato_sprite"));
            onionSprite = loadSprite(context, props.getString("table_onion_sprite"));
            carrotSprite = loadSprite(context, props.getString("table_carrot_sprite"));
            potatoSprite = loadSprite(context, props.getString("table_potato_sprite"));
            mashedPotatoSprite = loadSprite(context, props.getString("table_mashed potato_sprite"));
            veggieStewSprite = loadSprite(context, props.getString("table_veggie stew_sprite"));
            tomatoSoupSprite = loadSprite(context, props.getString("table_tomato soup_sprite"));
            saladSprite = loadSprite(context, props.getString("table_salad_sprite"));
            trashSprite = loadSprite(context, props.getString("table_trash_sprite"));
            sprite = tableSprite; // Set the main sprite
        } catch (Exception e) {
            Log.e("Table","Failed to load table sprites:"+e.getLocalizedMessage());
        }
    }

    @Override
    public void onInteract(Player player) {
        if (player == null || player.getInventory() == null) return;

        PlayerInventory inventory = player.getInventory();
        FoodItem heldItem = inventory.getHeld();

        // CASE 1: Player is holding something and table is empty → place item
        if (heldItem != null && itemOnTable == null) {
            itemOnTable = heldItem;
            inventory.getAndRemoveItem();
            Log.d("Table", "Item placed on table: " + itemOnTable.getName());
            updateSprite();
        }
        // CASE 2: Player is empty-handed and table has item → pick up item
        else if (heldItem == null && itemOnTable != null) {
            inventory.grabItem(itemOnTable);
            Log.d("Table", "Item picked up from table: " + itemOnTable.getName());
            itemOnTable = null;
            updateSprite();
        }
    }

    private void updateSprite() {
        if (itemOnTable == null) {
            sprite = tableSprite;  // No item, use default table
        } else {
            // You can check the name or ID of the item
            String itemName = itemOnTable.getName().toLowerCase();
            switch (itemName) {
                case "cabbage":
                    sprite = cabbageSprite;
                    break;
                case "potato":
                    sprite = potatoSprite;
                    break;
                case "onion":
                    sprite = onionSprite;
                    break;
                case "tomato":
                    sprite = tomatoSprite;
                    break;
                case "carrot":
                    sprite = carrotSprite;
                    break;
                case "mashed potato":
                    sprite = mashedPotatoSprite;
                    break;
                case "tomato soup":
                    sprite = tomatoSoupSprite;
                    break;
                case "veggie stew":
                    sprite = veggieStewSprite;
                    break;
                case "salad":
                    sprite = saladSprite;
                    break;
                // Add other items if needed
                default:
                    sprite = trashSprite; // fallback
                    break;
            }
        }
    }

    public FoodItem getItemOnTable() {
        return itemOnTable;
    }

    public void clearItem() {
        itemOnTable = null;
        updateSprite();
    }

    public void placeItem(FoodItem item) {
        itemOnTable = item;
        updateSprite();
    }
}