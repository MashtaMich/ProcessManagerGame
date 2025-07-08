package com.game.cookingspree;

import android.util.Log;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IngredientQueue {
    //Queue replaces Ingredient Inventory, will fetch from Ingredient Fetch Worker to be consumed to fill the baskets at Basket Filler
    //Uses FIFO to maintain order between baskets and fetch worker used list
    private static final String TAG = "Ingredient Queue";
    private final Queue<Ingredient> queue = new LinkedList<>();
    private final int capacity;
    private final Lock queueLock = new ReentrantLock();
    private final Condition notEmpty = queueLock.newCondition();
    private final Condition notFull = queueLock.newCondition();

    public IngredientQueue(int capacity) {
        this.capacity = capacity;
    }

    public void put(Ingredient ingredient) throws InterruptedException {
        //Standard producer put method
        queueLock.lock();
        try {
            while (queue.size() == capacity) {
                //Should never be full if full there is an error
                Log.d(TAG, "Queue is full. Waiting to put: " + ingredient.getName());
                notFull.await();
            }

            queue.add(ingredient);
            Log.d(TAG, "Added to queue: " + ingredient.getName());
            notEmpty.signal();
        } finally {
            queueLock.unlock();
        }
    }

    public Ingredient take() throws InterruptedException {
        //Standard consumer take function
        Log.d(TAG,"taking");
        queueLock.lock();
        try {
            while (queue.isEmpty()) {
                //Queue should be empty until fetch worker adds the first ingredient in
                Log.d(TAG, "Queue is empty. Waiting to take an ingredient.");
                notEmpty.await();
            }

            Ingredient ingredient = queue.poll();//Take from front for FIFO

            if (ingredient!=null){
                Log.d(TAG, "Removed from queue: " + ingredient.getName());
            }else{
                Log.e(TAG,"Failed to take an ingredient from the queue");
            }

            notFull.signal();
            return ingredient;
        } finally {
            queueLock.unlock();
            Log.d(TAG,"Take finished");
        }
    }

    public int size() {
        queueLock.lock();
        try {
            return queue.size();
        } finally {
            queueLock.unlock();
        }
    }
}