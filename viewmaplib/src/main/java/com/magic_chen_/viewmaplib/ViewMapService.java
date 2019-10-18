package com.magic_chen_.viewmaplib;

import android.app.Application;

/**
 * Created by magic_chen_ on 2019/10/18.
 */
public class ViewMapService implements IViewMapService {

    public Application mApp;

    private static ViewMapService instance = new ViewMapService();
    private ViewMapService(){}
    public static ViewMapService getInstance(){
        return instance;
    }

    public void initService(Application app) {
        this.mApp = app;
    }
}
