package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Pot extends Interactable {
    private enum State { EMPTY, COOKING, DONE }
    PotFunctions potFunctions;
    private State state;
    private Bitmap emptySprite, cookingSprite, doneSprite;
    private long cookingStartTime;
    private int cookingDuration = 3000; // ms
    //Shared pot thread pool
    private final PotThreadPool potThreadPool;
    private final Context context;

    public Pot(Context context, float x, float y, JSONObject props,PotThreadPool potThreadPool) {
        this.x = x;
        this.y = y;
        this.potFunctions=new PotFunctions(cookingDuration);
        this.potThreadPool=potThreadPool;
        this.context=context;

        try {
            this.state = State.valueOf(props.optString("state", "empty").toUpperCase());
            this.cookingDuration = props.optInt("cooking_time", 3000);

            emptySprite = loadSprite(context, props.getString("empty_sprite"));
            cookingSprite = loadSprite(context, props.getString("cooking_sprite"));
            doneSprite = loadSprite(context, props.getString("done_sprite"));


            updateSprite();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInteract(Player player) {
        PlayerInventory inventory=player.getInventory();
        switch (state) {
            case EMPTY:
                try {
                    if (inventory.checkHeldType()== PlayerInventory.INGREDIENT && !potFunctions.isReadyToCook() && !potFunctions.gotFood()){
                        potFunctions.addIngredient((Ingredient) inventory.getAndRemoveItem());
                        Log.d("Pot","potFunctions has "+ potFunctions.getIngredientsInside().size()+" ingredients");

                        if (potFunctions.isReadyToCook()){
                            Log.d("Pot","potFunctions is ready to cook");
                            List<Ingredient> inPot= potFunctions.getIngredientsInside();
                            Recipe cookRecipe=null;
                            for (Recipe recipe:inventory.getRecipeList()){
                                if (recipe.canCook(inPot)){
                                    cookRecipe=recipe;
                                    break;
                                }
                            }
                            if (cookRecipe==null){
                                //Default waste recipe, no actual recipe then cook
                                cookRecipe=new Recipe("Waste",new ArrayList<>());
                            }

                            state = State.COOKING;
                            cookingStartTime = System.currentTimeMillis();

                            updateSprite();
                            final Recipe recipeToCook=cookRecipe;
                            potThreadPool.submit(() -> {
                                potFunctions.cookIngredients(recipeToCook,(GameActivity)context);
                                state=State.DONE;
                                updateSprite();
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e("Pot", "Error handling pot put in Ingredient: " + e.getMessage(), e);
                }
                break;
            case DONE:
                if (potFunctions.gotFood() && inventory.checkHeldType()== PlayerInventory.EMPTY){
                    inventory.grabItem(potFunctions.getFood());
                    state = State.EMPTY;
                    updateSprite();
                    Log.d("Pot","got:"+inventory.getHeld().getName());
                }
                break;
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int TILE_SIZE) {
        if (sprite == null) {
            Log.e("DrawDebug", "Missing sprite for " + getClass().getSimpleName());
            return;
        }
//        if (state == State.COOKING && System.currentTimeMillis() - cookingStartTime >= cookingDuration) {
//            state = State.DONE;
//            updateSprite();
//        }
        canvas.drawBitmap(Bitmap.createScaledBitmap(sprite, TILE_SIZE, TILE_SIZE, true), x, y, paint);
    }

    private void updateSprite() {
        switch (state) {
            case EMPTY: sprite = emptySprite; break;
            case COOKING: sprite = cookingSprite; break;
            case DONE: sprite = doneSprite; break;
        }
    }

}
