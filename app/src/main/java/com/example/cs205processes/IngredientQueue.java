package com.example.cs205processes;

import android.util.Log;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IngredientQueue {
    //Queue replaces Ingredient Inventory, will fetch from Ingredient Fetch Worker to be consumed to fill the baskets at Basket Filler
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
                //Should never be full if full there is an error,
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
        //Standard consumer put function
        Log.d(TAG,"taking");
        queueLock.lock();
        try {
            while (queue.isEmpty()) {
                //Queue shouldn't be empty
                Log.d(TAG, "Queue is empty. Waiting to take an ingredient.");
                notEmpty.await();
            }
            Ingredient ingredient = queue.remove();
            Log.d(TAG, "Removed from queue: " + ingredient.getName());
            notFull.signal();
            return ingredient;
        } finally {
            queueLock.unlock();
            Log.d(TAG,"Take finished");
        }
    }

    public boolean isEmpty() {
        queueLock.lock();
        try {
            return queue.isEmpty();
        } finally {
            queueLock.unlock();
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