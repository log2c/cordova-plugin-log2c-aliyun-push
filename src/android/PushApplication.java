package com.alipush;

import android.app.Application;
import android.content.pm.PackageManager;


public class PushApplication extends Application {

    public static void init(Application application) {
        try {
            com.alipush.PushUtils.initPushService(application);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
