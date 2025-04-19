package com.example.cs205processes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PotThreadPool {
    //Pool for pot threads, should be equal to number of pots
    final ExecutorService pool;
    public PotThreadPool(int threads) {
        pool = Executors.newFixedThreadPool(threads);
    }
    public void submit(final Runnable task) {
        pool.submit(task);
    }
    public void shutdown() {
        pool.shutdown();
    }
}
