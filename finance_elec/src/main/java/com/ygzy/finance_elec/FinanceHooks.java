package com.ygzy.finance_elec;

import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.ygzy.finance_elec.utils.SPUtils;
import com.ygzy.finance_elec.utils.ScaleWeighUtil;
import com.ygzy.finance_elec.utils.SerialByteUtil;
import com.ygzy.finance_elec.utils.SpDataUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import android_serialport_api.SerialPortFinder;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.dcloud.feature.uniapp.UniAppHookProxy;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

/**
 * author : taomf
 * Date   : 2021/8/5 17:03
 * Desc   : 初始化
 */
public class FinanceHooks implements UniAppHookProxy {
    WeighData weight = null;
    private SerialHelper mSerialHelper;
    private boolean mIsValid = false;

    public static String photoPath;
    WindowManager windowManager;
    WindowManager.LayoutParams  params;
    private View popView ;
    public boolean isAdd = false;
    public Disposable dis ;
    private Application myApplication =  null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Application application) {
        myApplication = application;
        photoPath = application.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        initBugly(application);

        //当前uni应用进程回调 仅触发一次 多进程不会触发
        //可通过UniSDKEngine注册UniModule或者UniComponent
        SPUtils.init(application.getApplicationContext());
        LogUtils.d(FinanceWeighModule.TAG, "onCreate: " + SpDataUtils.getSerialPath());
        if (!Build.MODEL.equals(Constant.newScale)){
            ScreenUtils.setSleepDuration(Integer.MAX_VALUE);
            LogUtils.d(FinanceWeighModule.TAG, ScreenUtils.getSleepDuration());
        }
        if (TextUtils.isEmpty(SpDataUtils.getSerialPath()) && Build.MODEL.equals(Constant.newScale)) {
            LogUtils.d(FinanceWeighModule.TAG, "onCreate: 开始获取串口");
            mSerialHelper = new SerialHelper("/dev/ttyS4", Constant.newScaleBaudRate) {
                @Override
                protected void onDataReceived(ComBean comBean) {
                    weight = ScaleWeighUtil.getInstance().parseData(SerialByteUtil.ByteArrToHex(comBean.bRec));
                    if (weight != null && !mIsValid) {
                        LogUtils.d(FinanceWeighModule.TAG, "getSerialPort: 找到串口" + this.getPort());
                        SpDataUtils.saveSerialPath(this.getPort());
                        mIsValid = true;
                    }
                }
            };
            mSerialHelper.setDataBits(8);
            mSerialHelper.setStopBits(1);
            mSerialHelper.setParity(0);
            getSerialPort();
        }

        windowManager = (WindowManager)application.getSystemService(Context.WINDOW_SERVICE);
        params = new  WindowManager.LayoutParams();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.TRANSPARENT;
        params.gravity = Gravity.TOP;
//        params.y = 100;
        popView = LayoutInflater.from(application).inflate(R.layout.network_tip, null);
//
//        popView.setOnClickListener(v -> {
//            operateWindowView(false);
//        });

        boolean status = isActiveNetwork(application);
        if (!status){
            operateWindowView(true);
            checkNetwork();
        }
        networkCallBack(application);
    }

    private void initBugly(Application application) {
        //初始话bugly
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(application);
        strategy.setAppVersion(AppUtils.getAppVersionName());
        strategy.setDeviceModel(Build.MODEL);
        //默认10s ，改为20s
        strategy.setAppReportDelay(20000);
        // 设置anr时是否获取系统trace文件，默认为false
        strategy.setEnableCatchAnrTrace(true);
        // 设置是否获取anr过程中的主线程堆栈，默认为true
        strategy.setEnableRecordAnrMainStack(true);

        CrashReport.initCrashReport(application, "1b327aa0f8", true,strategy);
    }

    /**
     * 网络监听
     * @param application 上下文
     */
    private void networkCallBack(Application application) {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        NetworkRequest request = builder.build();
        ConnectivityManager connMgr = (ConnectivityManager) application.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            connMgr.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    operateWindowView(false);
                    checkNetwork();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    operateWindowView(true);
                }
            });
        }

    }

    /**
     * 添加悬浮窗
     * @param addOrRemove true 添加  false 删除
     */
    private synchronized void operateWindowView(boolean addOrRemove) {
        if (addOrRemove){
            if (!isAdd) {
                isAdd = true;
                TextView tvMenuText = popView.findViewById(R.id.tv_menu_text);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                boolean wifiConnected = NetworkUtils.isWifiConnected();
                if(myApplication != null){
                    if (wifiConnected) {
                        tvMenuText.setText(myApplication.getText(R.string.network_error));
                    } else {
                        tvMenuText.setText(myApplication.getText(R.string.no_network));
                    }
                }
//                ToastUtils.showLong("当前线程" + Thread.currentThread().getName() + "===wifiConnected=  " + wifiConnected);
//                popView.measure(
//                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//                );
//                params.width = popView.getMeasuredWidth();
                windowManager.addView(popView,params);
            }
        }else {
            if (isAdd) {
                isAdd = false;
                windowManager.removeView(popView);
            }
        }
    }

    /**
     * 检查网络
     */
    private void checkNetwork() {
        Observable.interval(5,5, TimeUnit.SECONDS).observeOn(Schedulers.io()).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                if (dis != null) dis.dispose();
                dis = d;
            }
            @Override
            public void onNext(Long aLong) {
                String deviceUrl = SpDataUtils.getDeviceUrl();
                boolean isConnected;
                if (deviceUrl.contains("192.168.122.31")){
                    //佛冈
                    isConnected = NetworkUtils.isAvailableByPing("192.168.122.30");
                }else if (deviceUrl.contains("pdm.canantong.com")){
                    //正式环境
                    isConnected = NetworkUtils.isAvailable();
                } else {
                    //正式环境
                    isConnected = NetworkUtils.isAvailable();
                }

                ThreadUtils.runOnUiThread(() -> {
                    LogUtils.d("taomf---",deviceUrl + "---" + SpDataUtils.getDeviceUrl());
                    if (deviceUrl.equals(SpDataUtils.getDeviceUrl())){
                        operateWindowView(!isConnected);
                    }
                });
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });

    }

    @Override
    public void onSubProcessCreate(Application application) {
        //子进程初始化回调
        LogUtils.d(FinanceWeighModule.TAG, "onSubProcessCreate: " + SpDataUtils.getSerialPath());
    }

    /**
     * 获取串口
     */
    public void getSerialPort() {
        new Thread(() -> {
            SerialPortFinder serialPortFinder = new SerialPortFinder();
            String[] allDevices = serialPortFinder.getAllDevicesPath();
            LogUtils.d(FinanceWeighModule.TAG, "getSerialPort: 串口列表" + Arrays.toString(allDevices));
            if (allDevices != null && allDevices.length > 0) {
                for (int index = 0; index < allDevices.length; index++) {
                    try {
                        String port = allDevices[index];
                        LogUtils.d(FinanceWeighModule.TAG, "串口=" + port);
                        mSerialHelper.setPort(port);
                        mSerialHelper.open();
                        Thread.sleep(2000);
                        if (weight == null) {
                            LogUtils.d(FinanceWeighModule.TAG, "weight == null");
                            mSerialHelper.close();
                            if (index == allDevices.length - 1) {
                                ToastUtils.showShort("未找到串口地址");
                            } else {
                                Thread.sleep(1000);
                            }
                        } else {
                            ToastUtils.showShort("找到串口" + port);
                            mSerialHelper.close();
                            break;
                        }
                    } catch (Exception error) {
                        LogUtils.d(FinanceWeighModule.TAG, "getSerialPort: 串口异常" + error);
                        ToastUtils.showShort("串口异常" + error);
                    }
                }
            } else {
                ToastUtils.showShort("未找到设备串口");
            }
        }).start();
    }

    /**
     * 判断当前网络是否可用
     */
    public boolean isActiveNetwork(Application application){
        ConnectivityManager connMgr = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

}
