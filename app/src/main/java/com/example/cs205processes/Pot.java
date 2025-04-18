package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Pot extends Interactable {
    private String TAG="pot";
    private enum State { EMPTY, COOKING, DONE }
    private PotFunctions potFunctions;
    private State state;
    private Bitmap emptySprite, cookingSprite, doneSprite;
    private HashMap<String,Bitmap> ingredientSprites=new HashMap<>();
    private int cookingDuration = 6000; // ms
    //Shared pot thread pool
    private final PotThreadPool potThreadPool;
    private final Context context;
    private final Object stateLock = new Object();

    public Pot(Context context, float x, float y, JSONObject props,PotThreadPool potThreadPool) {
        this.x = x;
        this.y = y;
        this.potFunctions=new PotFunctions(cookingDuration);
        this.potThreadPool=potThreadPool;
        this.context=context;//Game Activity context

        try {
            this.state = State.valueOf(props.optString("state", "empty").toUpperCase());
            this.cookingDuration = props.optInt("cooking_time", 3000);

            loadSprites(context,props);


            updateSprite();
        } catch (Exception e) {
            Log.e(TAG,"Error at setting up pot:"+e.getLocalizedMessage());
        }
    }

    private void loadSprites(Context context,JSONObject props){
        try{
            this.emptySprite = loadSprite(context, props.getString("empty_sprite"));
            this.cookingSprite = loadSprite(context, props.getString("cooking_sprite"));
            this.doneSprite = loadSprite(context, props.getString("done_sprite"));

            ingredientSprites.put("carrot",loadSprite(context,props.getString("carrot")));
            ingredientSprites.put("potato",loadSprite(context,props.getString("potato")));
            ingredientSprites.put("onion",loadSprite(context,props.getString("onion")));
            ingredientSprites.put("cabbage",loadSprite(context,props.getString("cabbage")));
            ingredientSprites.put("tomato",loadSprite(context,props.getString("tomato")));
        }catch (Exception e){
            Log.e(TAG,"Error at setting up pot sprites:"+e.getLocalizedMessage());
        }

    }

    @Override
    public void onInteract(Player player) {
        PlayerInventory inventory=player.getInventory();
        synchronized (stateLock) {
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

                                updateSprite();
                                final Recipe recipeToCook=cookRecipe;
                                potThreadPool.submit(() -> {
                                    potFunctions.cookIngredients(recipeToCook,(GameActivity)context);
                                    synchronized (stateLock) {
                                        state=State.DONE;
                                    }
                                    updateSprite();
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling pot put in Ingredient: " + e.getMessage(), e);
                    }
                    break;
                case DONE:
                    if (potFunctions.gotFood() && inventory.checkHeldType()== PlayerInventory.EMPTY){
                        inventory.grabItem(potFunctions.getFood());
                        state = State.EMPTY;
                        updateSprite();
                        Log.d(TAG,"got:"+inventory.getHeld().getName());
                    }
                    break;
            }
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int TILE_SIZE) {
        if (sprite == null) {
            Log.e("DrawDebug", "Missing sprite for " + getClass().getSimpleName());
            return;
        }
        canvas.drawBitmap(Bitmap.createScaledBitmap(sprite, TILE_SIZE, TILE_SIZE, true), x, y, paint);
        //Draw all ingredients inside above the pot
        List<Ingredient> ingredients = potFunctions.getIngredientsInside();
        if (!ingredients.isEmpty()) {
            drawIngredientsAbovePot(canvas, paint, TILE_SIZE, ingredients);
        }
    }

    private void drawIngredientsAbovePot(Canvas canvas, Paint paint, int TILE_SIZE, List<Ingredient> ingredients) {
        //3 icons above pot max since capacity is 3, so tile size/3
        int iconSize = TILE_SIZE / 3;
        int padding = 5;//To prevent the ingredients from being too close
        int startX = (int) x;//Same starting x
        int startY = (int) (y - iconSize - padding);

        for (int i = 0; i < ingredients.size(); i++) {
            String ingredientName = ingredients.get(i).getName();
            Bitmap icon = ingredientSprites.get(ingredientName);

            if (icon != null) {
                Bitmap scaledIcon = Bitmap.createScaledBitmap(icon, iconSize, iconSize, true);
                canvas.drawBitmap(scaledIcon, startX + i * (iconSize + padding), startY, paint);
            } else {
                // draw placeholder if missing icon
                paint.setColor(Color.RED);
                canvas.drawRect(startX + i * (iconSize + padding), startY, startX + i * (iconSize + padding) + iconSize, startY + iconSize, paint);
            }
        }
    }


    private void updateSprite() {
        synchronized (stateLock) {
            switch (state) {
                case EMPTY:
                    sprite = emptySprite;
                    break;
                case COOKING:
                    sprite = cookingSprite;
                    break;
                case DONE:
                    sprite = doneSprite;
                    break;
            }
        }
    }

    public List<Ingredient> getInPot(){
        return potFunctions.getIngredientsInside();
    }

    public State getState(){
        return this.state;
    }
}
