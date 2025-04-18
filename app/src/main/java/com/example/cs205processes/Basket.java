package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;

import org.json.JSONObject;

import java.util.List;

public class Basket extends Interactable {
    private String ingredient;
    private Bitmap emptySprite, cabbageSprite, carrotSprite,onionSprite,tomatoSprite,potatoSprite;

    public Basket(Context context, float x, float y, JSONObject props) {
        this.x = x;
        this.y = y;

        try {
            ingredient = props.getString("contents").toLowerCase();

            emptySprite = loadSprite(context, props.getString("basket_empty_sprite"));
            cabbageSprite = loadSprite(context, props.getString("basket_cabbage_sprite"));
            carrotSprite = loadSprite(context, props.getString("basket_carrot_sprite"));
            onionSprite = loadSprite(context, props.getString("basket_onion_sprite"));
            tomatoSprite = loadSprite(context, props.getString("basket_tomato_sprite"));
            potatoSprite = loadSprite(context, props.getString("basket_potato_sprite"));
            sprite=emptySprite;
        } catch (Exception e) {
            Log.e("Basket","Error at load content sprite for basket:"+e.getLocalizedMessage());
        }
    }

    public String getIngredient(){
        return this.ingredient;
    }

    public void setIngredient(String ingredient){
        try{
            this.ingredient=ingredient;
        } catch (Exception e) {
            Log.e("Basket","Error at setIngredient for basket:"+e.getLocalizedMessage());
        }
    }

    @Override
    public void onInteract(Player player) {
        System.out.println("Player took a " + ingredient);
        PlayerInventory inventory=player.getInventory();
        try {
            if (inventory.checkHeldType()==PlayerInventory.EMPTY){
                inventory.grabItem(new Ingredient(ingredient));
            }else{
                Log.d("Basket","Failed to take ingredient:inventory full");
            }
        } catch (Exception e) {
            System.out.println("Error when taking "+ingredient);
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int TILE_SIZE) {
        if (sprite == null) {
            Log.e("DrawDebug", "Missing sprite for " + getClass().getSimpleName());
            return;
        }
        updateSprite();
        canvas.drawBitmap(Bitmap.createScaledBitmap(sprite, TILE_SIZE, TILE_SIZE, true), x, y, paint);
    }

    private void updateSprite() {
        switch (ingredient) {
            case "empty": sprite = emptySprite; break;
            case "cabbage": sprite = cabbageSprite; break;
            case "carrot": sprite = carrotSprite; break;
            case "onion": sprite = onionSprite; break;
            case "potato": sprite = potatoSprite; break;
            case "tomato": sprite = tomatoSprite; break;
        }
    }
}
