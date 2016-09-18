package com.company;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    private static int COUNT = 200000000;
    //    private static final int COUNT = 140000000;
    private static int BUFFER_SIZE = 1;
    static final int[] x = {0};
    static final int[] y = {0};
    static int z = 0;
    static int w = 0;
    static int[] a = new int[COUNT];
    static int[] b = new int[COUNT];

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        for (; BUFFER_SIZE < COUNT / 2; BUFFER_SIZE = BUFFER_SIZE * 10) {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            int[] buffer = new int[BUFFER_SIZE];

            for (int i = 0; i < BUFFER_SIZE; i++) {
                buffer[i] = 0;
            }
            for (int i = 0; i < COUNT; i++) {
                a[i] = i;
                b[i] = 0;
            }
            long startTime = System.currentTimeMillis();
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            executorService.submit(() -> {
//                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                while (x[0] < COUNT) {
                    if (bufferHasFreeSpace(x[0], y[0])) synchronized (buffer) {
                        buffer[x[0] % BUFFER_SIZE] = a[x[0]];
                        x[0]++;
                    }
                    else {
//                        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    }
                }
            });
            Future<Boolean> future = executorService.submit(() -> {
//                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                while (y[0] < COUNT) {
                    if (bufferHasUnreadElements(x[0], y[0])) synchronized (buffer) {
                        b[y[0]] = buffer[y[0] % BUFFER_SIZE];
                        y[0]++;
                    }
                    else {
//                        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    }
                }
                return true;
            });
            future.get();
            long endTime = System.currentTimeMillis();

            executorService.shutdownNow();
            if (compareArrays(a, b)) {
                System.out.println("Buffer size: " + BUFFER_SIZE);
                System.out.println("Array was copied for " + (endTime - startTime) + " milliseconds");
            } else System.err.println("Array was not copied");
        }
    }

    private static boolean compareArrays(int[] a, int[] b) {
        for (int i = 0; i < COUNT; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    private static boolean bufferHasFreeSpace(int x, int y) {
        if (x == y) return true;
        if ((x - y) % (BUFFER_SIZE + 1) == BUFFER_SIZE) return false;
        return true;
    }

    private static boolean bufferHasUnreadElements(int x, int y) {
        if (x <= y) return false;
        return true;
    }
}
