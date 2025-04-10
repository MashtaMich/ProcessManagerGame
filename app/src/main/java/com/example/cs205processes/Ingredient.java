package com.example.cs205processes;

public class Ingredient {
    private String name;
    private int iconResourceId; // Reference to the drawable resource

    public Ingredient(String name, int iconResourceId) {
        this.name = name;
        this.iconResourceId = iconResourceId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }
}