package com.example.cs205processes;

import android.content.Context;
import android.graphics.*;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pot extends Interactable {
    //Handles pot UI interactions, pot functions handled in class pot functions
    //All pots should share same potThreadPool
    private final String TAG="pot";
    public enum State { EMPTY, COOKING, DONE }
    private final PotFunctions potFunctions;
    private State state;
    private Bitmap emptySprite, cookingSprite, doneSprite;
    private final HashMap<String,Bitmap> ingredientSprites=new HashMap<>();
    //Shared pot thread pool
    private final PotThreadPool potThreadPool; //Should be initialized in GameActivity then passed from Game
    private final Context context; //Should be GameActivity context from Game
    private final Object stateLock = new Object();

    public Pot(Context context, float x, float y, JSONObject props,PotThreadPool potThreadPool) {
        this.x = x;
        this.y = y;
        // preset cookingDuration, ms so 6 seconds
        int cookingDuration = 6000;
        this.potThreadPool=potThreadPool;
        this.context=context; //Game Activity context
        //Use preset cooking duration if props has no cooking_time set
        cookingDuration = props.optInt("cooking_time", cookingDuration);
        this.potFunctions=new PotFunctions(cookingDuration);

        try {
            //Default if not props state is State.EMPTY
            this.state = State.valueOf(props.optString("state", "empty").toUpperCase());

            loadSprites(context,props);//Load all the sprites

            updateSprite();//to update the Interactable sprite
        } catch (Exception e) {
            Log.e(TAG,"Error at setting up pot:"+e.getLocalizedMessage());
        }
    }

    private void loadSprites(Context context,JSONObject props){
        try{
            //Load Pot State sprites
            this.emptySprite = loadSprite(context, props.getString("empty_sprite"));
            this.cookingSprite = loadSprite(context, props.getString("cooking_sprite"));
            this.doneSprite = loadSprite(context, props.getString("done_sprite"));

            //Load ingredients in pot sprites
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
        //Interact handler
        PlayerInventory inventory=player.getInventory();
        synchronized (stateLock) {
            switch (state) {
                case EMPTY://If State.EMPTY
                    try {
                        //Check if player is holding an ingredient
                        if (inventory.checkHeldType()== PlayerInventory.INGREDIENT){
                            if (potFunctions.isReadyToCook() || potFunctions.gotFood()){
                                Log.e(TAG,"Error at pot: State.EMPTY when it should be Done or Cooking");
                                break;
                            }

                            //add ingredient to pot and remove from inventory the ingredient
                            potFunctions.addIngredient((Ingredient) inventory.getAndRemoveItem());
                            Log.d("Pot","potFunctions has "+ potFunctions.getIngredientsInside().size()+" ingredients");

                            if (potFunctions.isReadyToCook()){//Ready when potFunctions hits max cap for ingredients
                                Log.d("Pot","potFunctions is ready to cook");

                                //Use ingredients in pot to check the default recipe list stored in inventory
                                List<Ingredient> inPot= potFunctions.getIngredientsInside();
                                Recipe cookRecipe=null;
                                for (Recipe recipe:inventory.getRecipeList()){
                                    if (recipe.canCook(inPot)){
                                        cookRecipe=recipe;
                                        break;
                                    }
                                }

                                //If no recipe found, is waste food, no actual use must throw at bin
                                if (cookRecipe==null){
                                    //Default waste recipe, no actual recipe then cook
                                    cookRecipe=new Recipe("Waste",new ArrayList<>());
                                }

                                //Set state to cooking
                                state = State.COOKING;

                                updateSprite();//Update pot sprite

                                final Recipe recipeToCook=cookRecipe;
                                //Submit to the thread pool
                                potThreadPool.submit(() -> {
                                    //Send to potFunctions, will update the CookedFood there
                                    potFunctions.cookIngredients(recipeToCook,(GameActivity)context);

                                    //When done set state to done and update sprite so player knows to collect food
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

                case DONE://Case if State.DONE
                    //Check if there player inventory is empty
                    if (inventory.checkHeldType()== PlayerInventory.EMPTY){
                        //Check if pot has food
                        if (!potFunctions.gotFood()){
                            Log.e(TAG,"Error at pot: pot has no food when State.DONE");
                            break;
                        }

                        //Take food from pot, potFunctions removes food to pass to inventory
                        inventory.grabItem(potFunctions.getFood());

                        //Update pot state
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
        int spriteSize = TILE_SIZE / 3;
        int startX = (int) x;//Same starting x as pot
        int startY = (int) (y - spriteSize);//Start above the pot by a third of a tile size

        for (int i = 0; i < ingredients.size(); i++) {
            String ingredientName = ingredients.get(i).getName();
            Bitmap ingredientSprite = ingredientSprites.get(ingredientName);

            if (ingredientSprite != null) {
                Bitmap scaledIcon = Bitmap.createScaledBitmap(ingredientSprite, spriteSize, spriteSize, true);
                canvas.drawBitmap(scaledIcon, startX + i * (spriteSize), startY, paint);
            } else {
                // draw placeholder if missing ingredient sprite
                paint.setColor(Color.RED);
                canvas.drawRect(startX + i * (spriteSize), startY, startX + (i+1) * (spriteSize), startY + spriteSize, paint);
            }
        }
    }

    private void updateSprite() {
        //update pot sprite
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

    public String getState(){
        return this.state.name();
    }

    public PotFunctions getPotFunctions(){
        return this.potFunctions;
    }

    public CookedFood getFood(){
        return potFunctions.getFood();
    }

    public void setState(String newState) {
        //For loading not meant for use elsewhere
        synchronized (stateLock) {
            this.state = State.valueOf(newState);
            updateSprite();
        }
    }
}