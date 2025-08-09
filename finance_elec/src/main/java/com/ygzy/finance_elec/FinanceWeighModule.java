package com.ygzy.finance_elec;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.qhscale.QHJNIScale;
import com.qhscale.data.WeightV2;
import com.tencent.bugly.crashreport.CrashReport;
import com.wzx.WeightAPI.WeightDLL;
import com.ygzy.finance_elec.utils.DateUtils;
import com.ygzy.finance_elec.scale.ScaleCallback;
import com.ygzy.finance_elec.scale.ScaleManager;
import com.ygzy.finance_elec.utils.ScaleStabilityChecker;
import com.ygzy.finance_elec.utils.ScaleWeighUtil;
import com.ygzy.finance_elec.utils.SpDataUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;


public class FinanceWeighModule extends UniModule {
    public static final String TAG = "FinanceWeighModule";
    /**
     * 设备型号
     */
    public String model = Build.MODEL;
    /**
     * 毛重
     */
    public static int grossWeight = 0;
    /**
     * 皮重
     */
    public static int tareWeight = 0;
    /**
     * 净重
     */
    public static int netWeight = 0;
    /**
     * 稳定状态
     */
    public boolean  flag = false;
    boolean ifReadWeight = false;
    private WeightDLL weightdll;
    ReadThreadWeight threadWeight;

    private ScaleManager scaleManager = null;
    /**
     * 编辑皮重
     */
    private boolean editTare = false;
    /**
     * 编辑皮重值
     */
    private float editTareWeight = 0f;
    /**
     * 稳定状态
     */
    boolean stable = false;

    /**
     * 获取称重数据
     */
    @UniJSMethod()
    public void getWeighData() {
        LogUtils.d(TAG, "调用插件方法，获取称重数据");
        if(model.equals(Constant.newScale)){
            ScaleWeighUtil.getInstance().openSerialPort();

            ScaleWeighUtil.getInstance().setCallback(weightData -> {
                        if (weightData != null) {
//                        LogUtils.d(TAG, "flag" + weightData.getFlag() + "总重" + weightData.getGrossWeight() + " 皮重" + weightData.getTareWeight() + "  净重" + weightData.getNetWeight());
                            HashMap<String, Object> params = new HashMap<>();

                            float tareWeight = Float.parseFloat(weightData.getTareWeight());
                            float netWeight = Float.parseFloat(weightData.getNetWeight());

                            float grossWeight = tareWeight + netWeight;

                            if(editTare){
                                params.put("grossWeight", grossWeight);
                                params.put("tareWeight", editTareWeight);
                                params.put("netWeight",grossWeight - editTareWeight);
                            }else {
                                params.put("grossWeight", grossWeight);
                                params.put("tareWeight", tareWeight);
                                params.put("netWeight",netWeight);
                            }

                            stable = weightData.getFlag() == 1;
                            params.put("flag", stable ? 1 : 0);
                            mUniSDKInstance.fireGlobalEventCallback("weightData", params);
                        }
                    }
            );

        } else if(model.equals(Constant.newScale2)){
            scaleManager = new ScaleManager(mUniSDKInstance.getContext(), "ttyS4");
            scaleManager.setScaleCallback(onWeightUpdate);
            scaleManager.initialize();
        }else{
            try {
                // 实例化重量版
                weightdll = new WeightDLL();
                weightdll.WeightOpen("/dev/ttyS3",115200,0,30,0,1);
                ifReadWeight = true;
                threadWeight = new ReadThreadWeight();
                threadWeight.start();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (scaleManager != null){
            scaleManager.stop();
        }
    }

     ScaleCallback onWeightUpdate = new ScaleCallback() {
         public void onCalibrationSwitchEvent() {

         }
         @Override
         public void onWeightUpdate(@NonNull WeightV2 scaleWeight, @NonNull String weight) {
//            LogUtils.d("Weight 皮重Tare=", scaleWeight.getTare());
//            LogUtils.d("Weight 重量Weight=", scaleWeight.getWeight());
//            LogUtils.d("Weight 稳定状态=", scaleWeight.getStable());

             HashMap<String, Object> params = new HashMap<>();
            if (scaleWeight.getWeight().matches("-?\\d+(\\.\\d+)?")) {
                float tareWeight = Float.parseFloat(scaleWeight.getTare()) * 100;
                float netWeight = Float.parseFloat(scaleWeight.getWeight()) * 100;

                float grossWeight = tareWeight + netWeight;

                if(editTare){
                    params.put("grossWeight", grossWeight);
                    params.put("tareWeight", editTareWeight / 10);
                    params.put("netWeight",grossWeight - editTareWeight / 10);
                }else {
                    params.put("grossWeight", grossWeight);
                    params.put("tareWeight", tareWeight);
                    params.put("netWeight",netWeight);
                }
                stable = scaleWeight.getStable();
                params.put("flag", stable ? 1 : 0);
                mUniSDKInstance.fireGlobalEventCallback("weightData", params);
            }else {
                params.put("grossWeight", "0");
                params.put("tareWeight", "0");
                params.put("netWeight","0");
                mUniSDKInstance.fireGlobalEventCallback("weightData", params);
            }
         }

         @Override
         public void onZeroStateChanged(boolean state) {
         }

         @Override
         public void onTaredStateChanged(boolean state) {
         }

         @Override
         public void onStableStateChanged(boolean state) {

         }
     };

    /**
     * 关闭串口
     */
    @UniJSMethod()
    public void closeWeigh() {
        if (model.equals(Constant.newScale)){
            ScaleWeighUtil.getInstance().closeSerialPort();
        }else{
            ifReadWeight = false;
            threadWeight.interrupt();
            weightdll.WeightClose();
        }
    }

    /**
     * 去皮
     */
    @UniJSMethod()
    public void peeling() {
        if (model.equals(Constant.newScale)){
            if (stable){
                this.editTare = false;
                ScaleWeighUtil.getInstance().peeling();
            }else {
                ToastUtils.showShort("请稳定后再操作");
            }
        } else if(model.equals(Constant.newScale2)){
            if (stable){
                this.editTare = false;
                scaleManager.tare();
            }else {
                ToastUtils.showShort("请稳定后再操作");
            }
        }else{
            if(tareWeight == 0){
                tareWeight = grossWeight;
            }else {
                tareWeight = 0;
            }
        }
    }

    /**
     * @param json 皮重数据
     *             手动去皮
     */
    @UniJSMethod()
    public void peelingWeight(JSONObject json) {
        String peelWeight = optString(json, "peelWeight");
        try {
            if (model.equals(Constant.newScale)) {
                this.editTare = true;
                editTareWeight = Float.parseFloat(peelWeight);
            } else if(model.equals(Constant.newScale2)){
                this.editTare = true;
                editTareWeight = (int) (Float.parseFloat(peelWeight) * 10);
            }else{
                tareWeight = (int) (Float.parseFloat(peelWeight) * 10);
            }
        } catch (Exception e) {
            ToastUtils.showShort("输入的皮重数据异常" + e);
        }
    }

    /**
     * 置零
     */
    @UniJSMethod(uiThread = false)
    public void returnZero() {
        if (model.equals(Constant.newScale)){
            this.editTare = false;
            ScaleWeighUtil.getInstance().sendZero();
        }else if(model.equals(Constant.newScale2)){
            this.editTare = false;
            scaleManager.zero();
        }else{
            int[] P = { 12, 34 };
            weightdll.WeightControl(4, P);
        }
    }

    private static String optString(JSONObject json, String key) {
        if (json.containsKey(key)) {
            String value = json.getString(key);
            return value == null ? "" : value;
        } else {
            return "";
        }
    }

    private class ReadThreadWeight extends Thread {
        @Override
        public void run() {
            while (ifReadWeight) {
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void handleMessage(Message msg) {
            // 整型
            if (msg.what == 0) {
                netWeight = grossWeight - tareWeight;
                flag = ScaleStabilityChecker.checkStability(grossWeight);

                HashMap<String, Object> params = new HashMap<>();
                params.put("grossWeight", grossWeight / 10);
                params.put("tareWeight", tareWeight / 10);
                params.put("netWeight", netWeight / 10);
                params.put("flag", flag ? "1" : "0");
                mUniSDKInstance.fireGlobalEventCallback("weightData", params);
            }
        }
    };
    @UniJSMethod(uiThread = false)
    public void getCurrentWifiName(JSONObject json,UniJSCallback callback) {
        new Thread(()->{
            WifiManager wifiManager = (WifiManager) mUniSDKInstance.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            String wifiName = "";
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
                // 处理双引号及空格
                ssid = ssid.replace("\"", "").trim();
                if(!StringUtils.isEmpty(ssid) && !"<unknown ssid>".equals(ssid)){
                    wifiName = "wifi：" + ssid;
                }
            }
            boolean isConnect = NetworkUtils.isAvailableByPing("192.168.122.30");
            writeLogInfo(json.toString() + "wifi:" + wifiName + "-ping:192.168.122.30= " + isConnect);

            callback.invoke(wifiName + "\r\nping:192.168.122.30=" + isConnect);
        }).start();
    }

    @UniJSMethod(uiThread = false)
    public void getWifiName(JSONObject json,UniJSCallback callback) {
        new Thread(()->{
            WifiManager wifiManager = (WifiManager) mUniSDKInstance.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            String wifiName = "";
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
                // 处理双引号及空格
                ssid = ssid.replace("\"", "").trim();
                if(!StringUtils.isEmpty(ssid) && !"<unknown ssid>".equals(ssid)){
                    wifiName = ssid;
                }
            }
            callback.invoke(wifiName);
        }).start();
    }

    String savePath = Environment.getExternalStorageDirectory().getPath() + "/aalog/";

    /**
     * 写入日志信息
     * @param logInfo 日志信息
     */
    public  void writeLogInfo(String logInfo){
        File file = new File(savePath,  DateUtils.getNowTime("yyyy-MM-dd") +".txt" );
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            FileWriter fileWriter = new FileWriter(file,true);
            fileWriter.write(logInfo + " -> " + DateUtils.getNowTime());
            fileWriter.write("\r\n");
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将信号强度转换为百分比
     * @param rssi 信号强度
     * @return
     */
    public int convertToPercentage(int rssi) {
        // 将dBm值转换为百分比，这是一个简化的示例，实际应用中可能需要更复杂的算法来更准确地映射信号强度到百分比。
        if (rssi <= -100) {
            return 0; // 最弱信号
        } else if (rssi >= -50) {
            return 100; // 最强信号
        } else {
            return 2 * (rssi + 100); // 一个简单的线性映射示例
        }
    }
    @UniJSMethod(uiThread = false)
    public void getWifiInfo(UniJSCallback callback){
        WifiManager wifiManager = (WifiManager) mUniSDKInstance.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String wifiName = "";

        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int rssi = wifiInfo.getRssi();
            int rssiPercentage =convertToPercentage(rssi);

            String ssid = wifiInfo.getSSID();
            // 处理双引号及空格
            ssid = ssid.replace("\"", "").trim();
            if(!StringUtils.isEmpty(ssid) && !"<unknown ssid>".equals(ssid)){
                wifiName = "wifi：" + ssid;
            }else {
                wifiName = "wifi：未连接";
            }

            LogUtils.d("taomf=", "信号值:" + rssi + "  信号强度:" + rssiPercentage);
            callback.invoke(wifiName + "  信号值:" + rssi + "  信号强度:" + rssiPercentage);
        }
    }

    @UniJSMethod(uiThread = false)
    public void pingService(UniJSCallback callback){
        new Thread(() -> {
            WifiManager wifiManager = (WifiManager) mUniSDKInstance.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            String wifiName = "";

            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int rssi = wifiInfo.getRssi();
                int rssiPercentage =convertToPercentage(rssi);

                String ssid = wifiInfo.getSSID();
                // 处理双引号及空格
                ssid = ssid.replace("\"", "").trim();
                if(!StringUtils.isEmpty(ssid) && !"<unknown ssid>".equals(ssid)){
                    wifiName = "wifi：" + ssid;
                }else {
                    wifiName = "wifi：未连接";
                }
                LogUtils.d("taomf=", "信号值:" + rssi + "  信号强度:" + rssiPercentage);
                callback.invoke(wifiName + "  信号值:" + rssi + "  信号强度:" + rssiPercentage);
            }
            boolean isConnect = NetworkUtils.isAvailableByPing("192.168.122.30");
            ToastUtils.showLong("ping:192.168.122.30= " + isConnect);
        }).start();
    }

    /**
     * 获取自启动开关状态
     * */
        @UniJSMethod(uiThread = false)
        public void getBootSwitch(UniJSCallback callback){
       callback.invoke(SpDataUtils.getBootSwitch());
    }

    /**
     * 保存自启动开关状态
     * */
    @UniJSMethod(uiThread = false)
    public void saveBootSwitch(Boolean bootSwitch){
       SpDataUtils.saveBootSwitch(bootSwitch);
    }

    /**
     * 设置bugly设备号
     * @param deviceId 设备号
     */
    @UniJSMethod(uiThread = false)
    public void setDeviceId(String deviceId){
        LogUtils.d("taomf==", "setDeviceId: " + deviceId);
        CrashReport.setDeviceId(mUniSDKInstance.getContext(), deviceId);
    }

    /**
     *
     * 设置系统时间
     * */

    @UniJSMethod(uiThread = false)
    public void setSystemTime(long timeMillis) {
        // 需要确保 timeMillis 是 UTC 毫秒值
        boolean success = SystemClock.setCurrentTimeMillis(timeMillis);
        if (!success) {
            // 有些ROM会拦截或权限不完全
            Log.e("TimeUtils", "设置时间失败，可能无权限");
        }else{
            Log.e("TimeUtils", "设置时间成功");
        }
    }

    /**
     * 隐藏导航栏
     */
    @UniJSMethod
    public void hideNavBar() {
        BarUtils.setNavBarVisibility(((Activity) mUniSDKInstance.getContext()).getWindow(),false);
    }

    /**
     * 显示导航栏
     */
    @UniJSMethod
    public void showNavBar() {
        BarUtils.setNavBarVisibility(((Activity) mUniSDKInstance.getContext()).getWindow(),true);
    }

}
