package com.example.cs205processes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Recipe {

    final static int CARROT = 0;
    final static int POTATO = 1;
    final static int ONION = 2;
    final static int CABBAGE = 3;
    final static int TOMATO = 4;

    private final String id;
    private final String name;
    private final List<Ingredient> ingredients;
    //protected int iconResourceId;

    public Recipe(String name, List<Ingredient> ingredients) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.ingredients = ingredients;
    }

    public static List<Recipe> getDefaultRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        List<Ingredient> tomatoSoupIngredients = Arrays.asList(
                new Ingredient(TOMATO),
                new Ingredient(CARROT),
                new Ingredient(ONION)
        );
        List<Ingredient> veggieStewIngredients = Arrays.asList(
                new Ingredient(CABBAGE),
                new Ingredient(POTATO),
                new Ingredient(CARROT)
        );
        List<Ingredient> mashedPotatoIngredients = Arrays.asList(
                new Ingredient(POTATO),
                new Ingredient(POTATO),
                new Ingredient(ONION)
        );
        List<Ingredient> saladIngredients = Arrays.asList(
                new Ingredient(TOMATO),
                new Ingredient(POTATO),
                new Ingredient(CARROT)
        );

        recipes.add(new Recipe("Tomato Soup", tomatoSoupIngredients));
        recipes.add(new Recipe("Veggie Stew", veggieStewIngredients));
        recipes.add(new Recipe("Mashed Potato", mashedPotatoIngredients));
        recipes.add(new Recipe("Salad", saladIngredients));

        return recipes;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public boolean haveAllIngredients(List<Ingredient> inventoryList){
        return new HashSet<>(this.ingredients).containsAll(inventoryList);
    }

    public boolean canCook(List<Ingredient> potList){
        return haveAllIngredients(potList) && haveSameNumIngredients(potList);
    }

    public boolean haveSameNumIngredients(List<Ingredient> potList){
        Map<Integer,Integer> countMapRecipe=getIngredientCount(ingredients);
        Map<Integer,Integer> countMapList=getIngredientCount(potList);
        return countMapRecipe.equals(countMapList);
    }

    private Map<Integer,Integer> getIngredientCount(List<Ingredient> list){
        Map<Integer,Integer> countMap=new HashMap<>();
        for (Ingredient ing:list){
            int id=ing.getId();
            Integer currentCount = countMap.get(id);
            if (currentCount == null) currentCount = 0;
            countMap.put(id,currentCount+1);
        }
        return countMap;
    }
}
