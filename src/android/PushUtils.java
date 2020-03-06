package com.alipush;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.register.MiPushRegister;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PushUtils {
    public static final String TAG = PushUtils.class.getSimpleName();
    private SharedPreferences preference;

    public PushUtils(Context context) {
        this.preference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 初始化云推送通道
     *
     * @param application Application
     */
    static void initPushService(final Application application) throws PackageManager.NameNotFoundException {
        PushServiceFactory.init(application);
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
         pushService.setLogLevel(CloudPushService.LOG_DEBUG);
         pushService.register(application, new CommonCallback() {
             @Override
             public void onSuccess(String response) {
                 String deviceId = pushService.getDeviceId();
                 Log.d(TAG, "deviceId: " + deviceId);
//                 pushService.addAlias("test", new CommonCallback() {
//                     @Override
//                     public void onSuccess(String s) {
//                         Log.d(TAG, "onSuccess: " + s);
//                     }
//
//                     @Override
//                     public void onFailed(String s, String s1) {
//                         Log.e(TAG, "onFailed: " + s + ", " + s1);
//                     }
//                 });
             }

             @Override
             public void onFailed(String errorCode, String errorMessage) {
                 Log.d(TAG, "init cloudChannel failed -- errorCode:" + errorCode + " -- errorMessage:" + errorMessage);
             }
         });

        createDefaultChannel(application);

        // 5. 在应用中初始化辅助通道
        // 注册方法会自动判断是否支持小米系统推送，如不支持会跳过注册。
        ApplicationInfo appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
        String miPushAppId = appInfo.metaData.getString("MI_PUSH_APP_ID", "").trim();
        String miPushAppKey = appInfo.metaData.getString("MI_PUSH_APP_KEY", "").trim();
        if (!TextUtils.isEmpty(miPushAppId) && !TextUtils.isEmpty(miPushAppKey)) {
            Log.i(TAG, String.format("MiPush appId:%1$s, appKey:%2$s", miPushAppId, miPushAppKey));
            MiPushRegister.register(application, miPushAppId, miPushAppKey);
        }
        // 注册方法会自动判断是否支持华为系统推送，如不支持会跳过注册。
        HuaWeiRegister.register(application);

        // GCM/FCM辅助通道注册
        // GcmRegister.register(this, sendId, applicationId);
    }

    private static void createDefaultChannel(Application application) {
        // 注册NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 通知渠道的id
            String channelId;
            ApplicationInfo appInfo;
            try {
                appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
                channelId = appInfo.metaData.getString("CHANNEL_ID", "").trim();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "CHANNEL_ID NOT FOUND!");
                return;
            }

            NotificationManager mNotificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);

            // 用户可以看到的通知渠道的名字.
            CharSequence name = "通知";
            // 用户可以看到的通知渠道的描述
            String description = "通知描述";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            // 配置通知渠道的属性
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // 最后在 notificationManager 中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel);
            // 设置8.0系统的通知小图标,必须要纯色的图
            // PushServiceFactory.getCloudPushService().setNotificationSmallIcon(R.drawable.notify);
        }

    }

    /**
     * 解决androidP 第一次打开程序出现莫名弹窗 弹窗内容“detected problems with api ”
     */
    private void closeAndroidPDialog() {
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setNoticeJsonData(String jsonObject) {
        //response为后台返回的json数据
        preference.edit().putString("NoticeJsonData", jsonObject).apply(); //存入json串
    }


    public String getNotice() {
        String jsonData = preference.getString("NoticeJsonData", "");
        //每次取到json数据后，将其清空
        preference.edit().putString("NoticeJsonData", "").apply();
        try {
            JSONObject data = new JSONObject(jsonData);
            AliyunPush.pushData(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonData;
    }


    public void setIsShowNoticePermissions(boolean isShow) {
        preference.edit().putBoolean("ShowNoticePermissions", isShow).apply();
    }

    public boolean getIsShowNoticePermissions() {
        return preference.getBoolean("ShowNoticePermissions", true);
    }

    /**
     * 请求通知权限
     */
    void isShowNoticeDialog(Activity context, String msg) {
        NotificationManagerCompat notification = NotificationManagerCompat.from(context);
        boolean isEnabled = notification.areNotificationsEnabled();
        if (msg == null) {
            msg = "建议你开启通知权限，第一时间收到提醒";
        }
        //未打开通知
        if (!isEnabled) {
            try {
                if (preference.getBoolean("ShowNoticePermissions", true)) {
                    showDialog(context, msg);
                } else {

                    int random = (int) (Math.random() * 10 + 1);
                    //                Log.i("== random", random + "");
                    //随机数1-10，每次启动，如果除以3为0，则显示弹窗请求权限，否则就不弹窗
                    if (random % 3 == 0) {

                        showDialog(context, msg);

                    }
                }
            } catch (Exception ignored) {
            }
        }

    }

    private void showDialog(Activity context, String msg) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("开启推送通知")
                        .setMessage(msg)
                        .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                preference.edit().putBoolean("ShowNoticePermissions", false).apply();
                            }
                        })
                        .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Intent intent = new Intent();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                                    intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //5.0
                                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                                    intent.putExtra("app_package", context.getPackageName());
                                    intent.putExtra("app_uid", context.getApplicationInfo().uid);
                                    context.startActivity(intent);
                                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {  //4.4
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                                } else {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                    intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                                }
                                context.startActivity(intent);

                            }
                        })
                        .create();
                if (context.isFinishing()) return;
                alertDialog.show();
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            }
        }, 5000);

    }
}
