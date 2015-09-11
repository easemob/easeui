package com.easemob.easeuisimpledemo;

import com.easemob.easeui.controller.EaseUI;

import android.app.Application;

public class DemoApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        EaseUI.getInstance().init(this);
    }
    
}
