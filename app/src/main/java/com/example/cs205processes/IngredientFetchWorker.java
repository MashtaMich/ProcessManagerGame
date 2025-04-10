package com.example.cs205processes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientFetchWorker {
    private ExecutorService executor= Executors.newSingleThreadExecutor();
    private int total_count=5;
    private List<Ingredient> allIngredientList=new ArrayList<>(total_count);
    private List<Ingredient> availableList=new ArrayList<>(total_count);
    private int gen_time=3000;
    private Random random;

    private final Object availableLock = new Object();//To synch available list


    public interface generator_listener{
        void onGenerated(List<Ingredient> newIngredients);
    }

    public IngredientFetchWorker(){
        generateIngredientList();
        this.random=new Random();
    }

    private void generateIngredientList(){
        for (int i=0;i<total_count;i++){
            Ingredient ingredient=new Ingredient(i);
            this.allIngredientList.add(ingredient);
        }
        synchronized (availableLock) {
            this.availableList=new ArrayList<>(allIngredientList);
        }
    }

    public List<Ingredient> getAvailableList(){
        synchronized (availableLock) {
            return new ArrayList<>(availableList); // Return a copy to prevent external modification
        }
    }

    public void generate_ingredients(List<Ingredient> genList,generator_listener listener){
        executor.submit(()->{
            fetch_ingredients(genList,listener);
        });
    }

    public void generate_ingredients_random(Integer get_number,generator_listener listener){
        executor.submit(()->{
            fetch_ingredients_random(get_number,listener);
        });
    }

    private void fetch_ingredients(List<Ingredient> ingredients,generator_listener listener){
        try{
            Thread.sleep(gen_time);
        }catch(InterruptedException e){
            //handle later
        }

        List<Ingredient> result=new ArrayList<>();
        // Get random ingredients from the available list
        synchronized (availableLock) {
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient=ingredients.get(i);
                if (availableList.contains(ingredient)) {
                    result.add(ingredient);

                    // Remove ingredient to prevent duplicates
                    availableList.remove(ingredient);
                }
            }
        }
        listener.onGenerated(result);
    }
    private void fetch_ingredients_random(Integer get_number,generator_listener listener){
        try{
            Thread.sleep(gen_time);
        }catch(InterruptedException e){
            //handle later
        }

        List<Ingredient> result=new ArrayList<>();
        // Get random ingredients from the available list
        synchronized (availableLock) {
            for (int i = 0; i < get_number; i++) {
                if (availableList.isEmpty()) {
                    break;
                }

                int randomIndex = random.nextInt(availableList.size());
                Ingredient ingredient = availableList.get(randomIndex);
                result.add(ingredient);

                // Remove ingredient to prevent duplicates
                availableList.remove(randomIndex);
            }
        }

        listener.onGenerated(result);
    }

    public void return_ingredients(List<Ingredient> ingredients){
        synchronized (availableLock) {
            for (int i=0;i<ingredients.size();i++){
                Ingredient ingredient=ingredients.get(i);
                if (!availableList.contains(ingredient)){
                    availableList.add(ingredient);
                }
            }
        }
    }

    private void clear_available_list(){
        synchronized (availableLock) {
            this.availableList.clear();
            this.availableList=new ArrayList<>(allIngredientList);
        }
    }
}
