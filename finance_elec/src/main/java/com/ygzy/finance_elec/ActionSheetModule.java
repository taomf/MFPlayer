package com.ygzy.finance_elec;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.LogUtils;

import androidx.recyclerview.widget.RecyclerView;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;


public class ActionSheetModule extends UniModule {
    public static final String TAG = "ActionSheetModule";

    @UniJSMethod()
    public void showActionSheet(JSONObject options, UniJSCallback callback) {
        LogUtils.d(TAG, "调用插件方法,showActionSheet");

        JSONArray itemList = getItemList(options);
        boolean cancelBack = getCancelBack(options);
        Context context =  mUniSDKInstance.getContext();
        Dialog dialog = new Dialog(context,R.style.BaseDialogTheme);
        View bottomView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout, null);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        dialogWindow.setAttributes(lp);
        dialogWindow.setWindowAnimations(R.style.BottomAnimStyle);

        RecyclerView rcvSheet = bottomView.findViewById(R.id.rv_menu_list);
        ActionSheetAdapter actionSheetAdapter = new ActionSheetAdapter(context, itemList, position -> {
            JSONObject data = new JSONObject();
            data.put("tapIndex", position);
            callback.invoke(data);
            dialog.dismiss();
        });
        rcvSheet.setAdapter(actionSheetAdapter);

        dialog.setContentView(bottomView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        bottomView.findViewById(R.id.tv_menu_cancel).setOnClickListener(v -> {
            LogUtils.d(TAG, "dialog 关闭");
            if (cancelBack){
                JSONObject data = new JSONObject();
                data.put("tapIndex", -1);
                callback.invoke(data);
            }
            dialog.dismiss();
        });
    }

    private JSONArray getItemList(JSONObject options) {
        if (options.containsKey("itemList")){
            return options.getJSONArray("itemList");
        }
        return null;
    }

    private boolean getCancelBack(JSONObject options) {
        if (options.containsKey("cancelBack")){
            return options.getBooleanValue("cancelBack");
        }
        return false;
    }
}
