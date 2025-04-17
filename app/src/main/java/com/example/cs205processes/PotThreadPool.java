package com.example.cs205processes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PotThreadPool {
    final ExecutorService pool;

    public PotThreadPool(int threads) {
        pool = Executors.newFixedThreadPool(threads);
    }

    public void submit(final Runnable task) {
        pool.submit(task);
    }

    //To help with game end
    public void shutdown() {
        pool.shutdown();
    }
}
