package com.ygzy.finance_elec;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;

import java.io.File;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 * @author : taomf
 * Date    : 2024/12/25/9:25
 * Desc    : 拍照
 */
public class TakePhotoModule extends UniModule {
    private final int REQUEST_TAKE = 333;
    private String capturePath = "";
    private UniJSCallback mCallback = null;

    @UniJSMethod(uiThread = true)
    public void goTakePhoto(UniJSCallback callback){
        LogUtils.d("taomf", "goTakePhoto: ");
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            Activity activity = (Activity) mUniSDKInstance.getContext();
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File file = new File(initPhotoPath(), TimeUtils.getNowMills() + ".jpg");
            capturePath =  file.getAbsolutePath();
            Uri photoUri = Uri.fromFile(file);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            activity.startActivityForResult(takePhotoIntent, REQUEST_TAKE);
            mCallback = callback;
        }
    }

    public String initPhotoPath() {
        File directory = Environment.getExternalStorageDirectory();
        String path = directory.getAbsolutePath() + "/PlayCameraT";
        File file = new File(path);
        file.mkdirs();
        return path;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        LogUtils.d("taomf", "requestCode: " + requestCode);
        if (requestCode == REQUEST_TAKE && mCallback != null) {
            mCallback.invoke(new JSONObject(){{
                put("capturePath",capturePath);
            }});
        }
    }
}