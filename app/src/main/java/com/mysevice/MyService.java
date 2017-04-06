package com.mysevice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class MyService extends Service {

    private static final int CORE_TREADS = 3;
    private static final int MAX_THREADS = 4;
    private static final int MAX_QUE_SIZE = 32;

    private static final BlockingQueue<Runnable> POOL_TASKS =
            new LinkedBlockingQueue<>(MAX_QUE_SIZE);

    private ThreadPoolExecutor mExecutor;
    private final IBinder mBinder = new LocalBinder();

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {

        final AtomicInteger nextId = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable,
                    "MyService # " + nextId.incrementAndGet());
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        }
    };

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutor = new ThreadPoolExecutor(CORE_TREADS,
                MAX_THREADS,
                1,
                TimeUnit.SECONDS,
                POOL_TASKS,
                THREAD_FACTORY);
        mExecutor.prestartAllCoreThreads();
    }

    void echoInBackground(final String msg,
                          final WeakReference<ResultCallback<String>> callbackWeakReference) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (callbackWeakReference != null &&
                        callbackWeakReference.get() != null) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // empty
                    }
                    String echo = echo(msg);
                    notifyInUI(echo, callbackWeakReference.get());
                }
            }
        };
        mExecutor.execute(runnable);
    }

    private String echo(String s) {
        return "Echo: ( " + s + " ) "
                + Thread.currentThread().getName()
                + " POOL SIZE: " + POOL_TASKS.size();
    }

    private <T> void notifyInUI(final T result, final ResultCallback<T> callback) {
        Looper mainLooper = Looper.getMainLooper();
        Handler handler = new Handler(mainLooper);
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onResult(result);
            }
        });
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mExecutor.shutdownNow();
        return super.onUnbind(intent);
    }
}
