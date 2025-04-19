package com.example.cs205processes;

public class Ingredient extends FoodItem {
    //Class for ingredients gotten from basket, in recipe and in CookedFood

    public Ingredient(int id) { //Create using ingredient id
        super(id);
        setNameAndImage(id);
    }

    public Ingredient(String name){ //Create using ingredient name
        super(name);
        setIdAndImage(name);
    }

    private void setIdAndImage(String name) {
        switch (name) {
            case "carrot":
                this.id = 0;
                this.iconResourceId = R.drawable.carrot;
                break;
            case "potato":
                this.id = 1;
                this.iconResourceId = R.drawable.potato;
                break;
            case "onion":
                this.id = 2;
                this.iconResourceId = R.drawable.onion;
                break;
            case "cabbage":
                this.id = 3;
                this.iconResourceId = R.drawable.cabbage;
                break;
            case "tomato":
                this.id=4;
                this.iconResourceId = R.drawable.tomato;
                break;
            default:
                this.name = "invalid";
                this.id=-1;
                this.iconResourceId = R.drawable.placeholder;
                break;
        }
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
