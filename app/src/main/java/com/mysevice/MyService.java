package com.mysevice;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class MyService extends Service {

    public static final String BROADCAST = "broadcast";
    public static final String RESULT = "result";
    public static final String HANDLED = "handled";

    private static final int CORE_TREADS = 3;
    private static final int MAX_THREADS = 4;

    private static final int MAX_QUE_SIZE = 32;
    private static final BlockingQueue<Runnable> POOL_TASKS =
            new LinkedBlockingQueue<>(MAX_QUE_SIZE);

    private ThreadPoolExecutor mExecutor;

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String message = intent.getStringExtra(MainActivity.DATA);
        echoInBackground(message);
        return START_NOT_STICKY;
    }

    void echoInBackground(final String msg) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String echo = echo(msg);
                SystemClock.sleep(3000);
                notifyInUI(echo);
            }
        };
        mExecutor.execute(runnable);
    }

    private void notifyInUI(final String message) {
        Looper mainLooper = Looper.getMainLooper();
        Handler handler = new Handler(mainLooper);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BROADCAST);
                sendLocalBroadcast(intent, message);
                boolean handled = intent.getBooleanExtra(HANDLED, false);
                if (!handled) {
                    sendNotification(message);
                }
            }
        });
    }

    private void sendLocalBroadcast(Intent intent, String message) {
        intent.putExtra(RESULT, message);
        LocalBroadcastManager
                .getInstance(MyService.this)
                .sendBroadcastSync(intent);
    }

    private void sendNotification(String message) {

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_face_black_24dp)
                        .setContentTitle("This is Title")
                        .setContentText(message);

        NotificationManager nm = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);

        nm.notify(message.hashCode(), builder.build());
    }

    private String echo(String s) {
        return "Echo: ( " + s + " ) "
                + Thread.currentThread().getName()
                + " POOL SIZE: " + POOL_TASKS.size();
    }

    @Override
    public boolean stopService(Intent name) {
        mExecutor.shutdownNow();
        return super.stopService(name);
    }
}
