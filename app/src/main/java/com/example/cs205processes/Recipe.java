package com.example.cs205processes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recipe {
    private String id;
    private String name;
    private List<Ingredient> ingredients;
    private String instructions;

    public Recipe(String name, List<Ingredient> ingredients, String instructions) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public static List<Recipe> getDefaultRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        List<Ingredient> tomatoSoup = Arrays.asList(
                new Ingredient("Tomato", R.drawable.tomato),
                new Ingredient("Tomato", R.drawable.tomato),
                new Ingredient("Tomato", R.drawable.tomato)
        );
        recipes.add(new Recipe("Tomato Soup", tomatoSoup,
                "Put tomatoes in pot until boiling"));

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

    public String getInstructions() {
        return instructions;
    }
}
