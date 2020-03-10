package com.alipush;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class AliyunPush extends CordovaPlugin {
    private static final String TAG = AliyunPush.class.getSimpleName();
    /**
     * JS回调接口对象
     */
    static CallbackContext pushCallbackContext = null;


    private final CloudPushService pushService = PushServiceFactory.getCloudPushService();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        new PushUtils(cordova.getActivity()).isShowNoticeDialog(cordova.getActivity(), null);
    }


    /**
     * 插件主入口
     */
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        LOG.d(TAG, "AliyunPush#execute");

        boolean ret = false;

        if ("onMessage".equalsIgnoreCase(action)) {
            if (pushCallbackContext == null) {
                pushCallbackContext = callbackContext;
                new PushUtils(cordova.getActivity()).getNotice();
            }
            ret = true;
        } else if ("requireNotifyPermission".equalsIgnoreCase(action)) {
            final String msg = args.getString(0);
            this.cordova.getActivity().runOnUiThread(() -> new PushUtils(cordova.getActivity()).isShowNoticeDialog(cordova.getActivity(), msg));
            ret = true;
        } else if ("getRegisterId".equalsIgnoreCase(action)) {
            callbackContext.success(pushService.getDeviceId());
            sendNoResultPluginResult(callbackContext);
            ret = true;
        } else if ("bindAccount".equalsIgnoreCase(action)) {
            final String account = args.getString(0);
            cordova.getThreadPool().execute(() -> {
                LOG.d(TAG, "PushManager#bindAccount");
                pushService.bindAccount(account, new CommonCallback() {
                    @Override
                    public void onSuccess(String s) {
                        callbackContext.success(s);
                    }

                    @Override
                    public void onFailed(String s, String s1) {
                        resError(callbackContext, s, s1);
                    }
                });
            });
            sendNoResultPluginResult(callbackContext);
            ret = true;
        } else if ("unbindAccount".equalsIgnoreCase(action)) {
            cordova.getThreadPool().execute(() -> {
                LOG.d(TAG, "PushManager#unbindAccount");
                pushService.unbindAccount(new CommonCallback() {
                    @Override
                    public void onSuccess(String s) {
                        callbackContext.success(s);
                    }

                    @Override
                    public void onFailed(String s, String s1) {
                        resError(callbackContext, s, s1);
                    }
                });
            });
            sendNoResultPluginResult(callbackContext);
            ret = true;
        } else if ("bindTags".equalsIgnoreCase(action)) {
            final int target = args.getInt(0);
            final String[] tags = toStringArray(args.getJSONArray(1));
            final String alias = args.length() > 2 ? args.getString(2) : null;

            cordova.getThreadPool().execute(() -> {
                LOG.d(TAG, "PushManager#bindTags");

                if (tags != null && tags.length > 0) {
                    pushService.bindTag(target, tags, alias, new CommonCallback() {
                        @Override
                        public void onSuccess(String s) {
                            callbackContext.success(s);
                        }

                        @Override
                        public void onFailed(String s, String s1) {
                            resError(callbackContext, s, s1);
                        }
                    });
                }

            });
            sendNoResultPluginResult(callbackContext);
            ret = true;
        } else if ("unbindTags".equalsIgnoreCase(action)) {
            final int target = args.getInt(0);
            final String[] tags = toStringArray(args.getJSONArray(1));
            final String alias = args.length() > 2 ? args.getString(2) : null;
            cordova.getThreadPool().execute(() -> {
                LOG.d(TAG, "PushManager#unbindTags");

                if (tags != null && tags.length > 0) {

                    pushService.unbindTag(target, tags, alias, new CommonCallback() {
                        @Override
                        public void onFailed(String s, String s1) {
                            resError(callbackContext, s, s1);
                        }

                        @Override
                        public void onSuccess(String s) {
                            LOG.d(TAG, "onSuccess:" + s);
                            callbackContext.success(s);
                        }
                    });
                }

            });
            sendNoResultPluginResult(callbackContext);
            ret = true;
        } else if ("listTags".equalsIgnoreCase(action)) {

            cordova.getThreadPool().execute(() -> {
                LOG.d(TAG, "PushManager#listTags");
                pushService.listTags(pushService.DEVICE_TARGET, new CommonCallback() {
                    @Override
                    public void onFailed(String s, String s1) {
                        resError(callbackContext, s, s1);
                    }

                    @Override
                    public void onSuccess(String s) {
                        LOG.d(TAG, "onSuccess:" + s);
                        callbackContext.success(s);
                    }
                });

            });
            sendNoResultPluginResult(callbackContext);
            ret = true;
        }

        return ret;
    }

    private void resError(CallbackContext callbackContext, String reason, String res) {
        LOG.d(TAG, "onFailed reason:" + reason + "res:" + res);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", res);
            jsonObject.put("reason", reason);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callbackContext.error(jsonObject);
    }

    private void sendNoResultPluginResult(CallbackContext callbackContext) {
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    /**
     * 接收推送内容并返回给前端JS
     *
     * @param data JSON对象
     */
    static void pushData(final JSONObject data) {
        Log.i(TAG, data.toString());
        if (pushCallbackContext == null) {
            return;
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK, data);
        result.setKeepCallback(true);
        pushCallbackContext.sendPluginResult(result);
    }

    private static String[] toStringArray(JSONArray array) {
        if (array == null)
            return null;
        String[] arr = new String[array.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = array.optString(i);
        }
        return arr;
    }
}
