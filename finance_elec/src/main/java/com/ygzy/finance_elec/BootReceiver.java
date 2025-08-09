package com.ygzy.finance_elec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ygzy.finance_elec.utils.SpDataUtils;

/**
 * @author : taomf
 * Date    : 2024/5/29/15:48
 * Desc    : 开机启动广播
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            if(SpDataUtils.getBootSwitch()){
                Intent intentMainActivity = new Intent(Intent.ACTION_MAIN);
                intentMainActivity.setClassName(context.getPackageName(), "io.dcloud.PandoraEntry");
                intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentMainActivity);
            }
        }
    }
}
