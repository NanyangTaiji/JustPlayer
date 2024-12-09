package com.nytaiji.nybase.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class RetryUtil {
    public static boolean retry(int times, long delayTime, Callable<Boolean> block) {
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Future<Boolean> result = executor.submit(() -> {
            for (int i = 0; i < times - 1; i++) {
                try {
                    if (block.call()) {
                        return true;
                    }
                    if (delayTime > 0) {
                        Thread.sleep(delayTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                return block.call(); // last attempt
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        } finally {
            executor.shutdown();
            scheduler.shutdown();
        }
    }

}
