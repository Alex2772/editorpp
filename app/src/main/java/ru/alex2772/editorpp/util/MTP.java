package ru.alex2772.editorpp.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MTP {
    private static ExecutorService service = Executors.newFixedThreadPool(4);

    public static void schedule(Runnable r) {
        service.submit(r);
    }

    public static <T> Future<T> schedule(Callable<T> task) {
        return service.submit(task);
    }
}
