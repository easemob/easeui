package com.hyphenate.easeui.manager;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;



import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 作为线程的管理类，可以实现工作线程和主线程的切换
 */
public class EaseThreadManager {
    private static volatile EaseThreadManager instance;
    private Executor mIOThreadExecutor;
    private Handler mMainThreadHandler;

    private EaseThreadManager() { init();}

    public static EaseThreadManager getInstance() {
        if(instance == null) {
            synchronized (EaseThreadManager.class) {
                if(instance == null) {
                    instance = new EaseThreadManager();
                }
            }
        }
        return instance;
    }

    private void init() {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingDeque<>();
        mIOThreadExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES,
                NUMBER_OF_CORES * 2,
                                KEEP_ALIVE_TIME,
                                KEEP_ALIVE_TIME_UNIT,
                                taskQueue,
                                new BackgroundThreadFactory(Process.THREAD_PRIORITY_BACKGROUND));
        mMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 在异步线程执行
     * @param runnable
     */
    public void runOnIOThread(Runnable runnable) {
        mIOThreadExecutor.execute(runnable);
    }

    /**
     * 在UI线程执行
     * @param runnable
     */
    public void runOnMainThread(Runnable runnable) {
        mMainThreadHandler.post(runnable);
    }

    /**
     * 判断是否是主线程
     * @return true is main thread
     */
    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
