package com.example.cs205processes;

import androidx.annotation.NonNull;

public class FoodItem {
    protected int id;
    protected String name;
    protected int iconResourceId;

    public FoodItem(String name) {
        this.name = name;
    }

    public FoodItem(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        FoodItem that = (FoodItem) o;
        return this.name.equals(that.getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @NonNull
    @Override
    public String toString(){
        return "id:"+getId()+",name:"+getName();
    }
}
