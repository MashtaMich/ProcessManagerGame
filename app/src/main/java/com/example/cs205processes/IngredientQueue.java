package com.example.cs205processes;

import android.util.Log;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IngredientQueue {
    //Honestly wrote the whole Ingredient logic before remembering that its supposed to be Producer/Consumer queue is supposed to replace inventory
    private static final String TAG = "Ingredient Queue";
    private final Queue<Ingredient> queue = new LinkedList<>();
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    public IngredientQueue(int capacity) {
        this.capacity = capacity;
    }

    public void put(Ingredient ingredient) throws InterruptedException {
        lock.lock();
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
            lock.unlock();
        }
    }

    public Ingredient take() throws InterruptedException {
        Log.d(TAG,"taking");
        lock.lock();
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
            lock.unlock();
            Log.d(TAG,"Took an ingredient");
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}