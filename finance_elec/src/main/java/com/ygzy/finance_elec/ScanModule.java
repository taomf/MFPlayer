package com.ygzy.finance_elec;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.LogUtils;
import com.ygzy.finance_elec.service.ScanService;
import com.ygzy.finance_elec.utils.SpDataUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 * @author : taomf
 * Date    : 2024/12/7/14:57
 * Desc    : 扫码枪
 */
public class ScanModule extends UniModule {
    private final String TAG = "ScanModule";
    @UniJSMethod()
    public void init() {
        LogUtils.d(TAG, "init");

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        Context context = mUniSDKInstance.getContext();
        if (!isStartAccessibilityService(context, ScanService.class.getName())) {
            new AlertDialog.Builder(context)
                    .setTitle("扫码服务未开启")
                    .setMessage("为了更好地提供服务，请打开智能云秤服务。")
                    .setPositiveButton("去开启", (dialog, which) -> openAccessibilitySettings())
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    /**
     * 扫码内容
     */
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void scanResult(String result) {
        LogUtils.d(TAG, "扫码结果=" +  result);

        if (!TextUtils.isEmpty(result) && !result.contains(FinanceHooks.photoPath)) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("scanResult", result);
            mUniSDKInstance.fireGlobalEventCallback("scanData", params);
        }
    }

    /**
     * 判断AccessibilityService服务是否已经启动
     */
    public static boolean isStartAccessibilityService(Context context, String name){
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfos = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : serviceInfos) {
            String id = info.getId();
            if (id.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mUniSDKInstance.getContext().startActivity(intent);
    }

    /**
     * 平台地址
     */
    @UniJSMethod()
    public void platformUrl(JSONObject options) {
        SpDataUtils.setDeviceUrl(options.getString("deviceUrl"));
    }

    @UniJSMethod(uiThread = false)
    public void getAccessibilityServiceStatus(UniJSCallback callback){
        Context context = mUniSDKInstance.getContext();
        callback.invoke(isStartAccessibilityService(context, ScanService.class.getName()));
    }

    @UniJSMethod()
    public void showAccessibilityServiceDialog(){
        Context context = mUniSDKInstance.getContext();
        if (!isStartAccessibilityService(context, ScanService.class.getName())) {
            new AlertDialog.Builder(context)
                    .setTitle("扫码服务未开启")
                    .setMessage("为了更好地提供服务，请打开智能云秤服务。")
                    .setPositiveButton("去开启", (dialog, which) -> openAccessibilitySettings())
                    .setNegativeButton("取消", null)
                    .show();
        }
    }
}
