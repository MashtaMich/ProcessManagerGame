package com.example.cs205processes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Recipe {

    final static int CARROT = 0;
    final static int POTATO = 1;
    final static int ONION = 2;
    final static int CABBAGE = 3;
    final static int TOMATO = 4;

    private String id;
    private String name;
    private List<Ingredient> ingredients;

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

    public boolean have_all_ingredients(List<Ingredient> inventory_list){
        return new HashSet<>(this.ingredients).containsAll(inventory_list);
    }
}
