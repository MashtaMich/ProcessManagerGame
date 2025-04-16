package com.example.cs205processes;

public class Ingredient extends FoodItem {

    public Ingredient(int id) {
        super(id);
        setNameAndImage(id);
    }

    private void setNameAndImage(int id) {
        switch (id) {
            case 0:
                this.name = "carrot";
                this.iconResourceId = R.drawable.carrot;
                break;
            case 1:
                this.name = "potato";
                this.iconResourceId = R.drawable.potato;
                break;
            case 2:
                this.name = "onion";
                this.iconResourceId = R.drawable.onion;
                break;
            case 3:
                this.name = "cabbage";
                this.iconResourceId = R.drawable.cabbage;
                break;
            case 4:
                this.name = "tomato";
                this.iconResourceId = R.drawable.tomato;
                break;
            default:
                this.name = "invalid";
                this.iconResourceId = R.drawable.placeholder;
                break;
        }
    }
}
