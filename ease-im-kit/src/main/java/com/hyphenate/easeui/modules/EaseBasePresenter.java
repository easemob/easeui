package com.hyphenate.easeui.modules;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.hyphenate.easeui.manager.EaseThreadManager;

public abstract class EaseBasePresenter implements LifecycleObserver {
    private static final String TAG = EaseBasePresenter.class.getSimpleName();
    private boolean isDestroy;

    public abstract void attachView(ILoadDataView view);

    public abstract void detachView();

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        Log.i(TAG, this.toString() +" onCreate");
        isDestroy = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        Log.i(TAG, this.toString() +" onStart");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        Log.i(TAG, this.toString() +" onResume");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        Log.i(TAG, this.toString() +" onPause");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Log.i(TAG, this.toString() +" onStop");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        Log.i(TAG, this.toString() +" onDestroy");
        isDestroy = true;
    }

    /**
     * 生命周期正在销毁
     * @return
     */
    public boolean isDestroy() {
        return isDestroy;
    }

    /**
     * 生命周期仍在活跃
     * @return
     */
    public boolean isActive() {
        return !isDestroy;
    }

    /**
     * 执行UI线程
     * @param runnable
     */
    public void runOnUI(Runnable runnable) {
        EaseThreadManager.getInstance().runOnMainThread(runnable);
    }

    /**
     * 执行IO异步线程
     * @param runnable
     */
    public void runOnIO(Runnable runnable) {
        EaseThreadManager.getInstance().runOnIOThread(runnable);
    }
}
