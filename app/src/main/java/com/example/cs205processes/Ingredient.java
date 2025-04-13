package com.example.cs205processes;

public class Ingredient {
    private int id;
    private String name;
    private int iconResourceId;

    public Ingredient(int id){
        this.id=id;
        set_name_image(id);
    }

    private void set_name_image(int id){
        switch(id){
            case 0:
                this.name="carrot";
                this.iconResourceId =R.drawable.carrot;
                break;
            case 1:
                this.name="potato";
                this.iconResourceId =R.drawable.potato;
                break;
            case 2:
                this.name="onion";
                this.iconResourceId =R.drawable.onion;
                break;
            case 3:
                this.name="cabbage";
                this.iconResourceId =R.drawable.cabbage;
                break;
            case 4:
                this.name="tomato";
                this.iconResourceId =R.drawable.tomato;
                break;
            default:
                this.name="invalid";
                this.iconResourceId =R.drawable.placeholder;
                break;
        }
    }

    public int getIconResourceId(){
        return this.iconResourceId;
    }

    public String getName(){
        return this.name;
    }

    public int getId(){
        return this.id;
    }

    @Override
    public boolean equals(Object o){
        if (this==o) {//If same object
            return true;
        }

        if (o==null||this.getClass()!=o.getClass()){
            return false;//If null or not an ingredient
        }

        Ingredient object=(Ingredient) o;
        return this.id==object.getId();//If ingredient compare id
    }

    @Override
    public int hashCode() {//For in case someone wants to use hashmaps
        return Integer.hashCode(id);
    }
}