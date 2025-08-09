package com.ygzy.finance_elec.utils;

/**
 * @author : taomf
 * Date    : 2025/4/28 028/10:52
 * Desc    : 摄像头过滤器
 */
import android.annotation.SuppressLint;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraFilter;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.impl.CameraInfoInternal;
import androidx.core.util.Preconditions;
import java.util.ArrayList;
import java.util.List;

public class MyCameraFilter implements CameraFilter {
    private final String mId;

    public MyCameraFilter(String id) {
        LogUtils.d("MyCameraFilter", "filter:=" + id);
        this.mId = id;
    }

    @NonNull
    @SuppressLint("RestrictedApi")
    @Override
    public List<CameraInfo> filter(List<CameraInfo> cameraInfos) {
        List<CameraInfo> result = new ArrayList<>();

        for (CameraInfo cameraInfo : cameraInfos) {
            Preconditions.checkArgument(
                    cameraInfo instanceof CameraInfoInternal,
                    "The camera info doesn't contain internal implementation."
            );

            CameraInfoInternal cameraInfoInternal = (CameraInfoInternal) cameraInfo;
            String id = cameraInfoInternal.getCameraId();

            LogUtils.d("MyCameraFilter", "id=" + id);
            if (id.contains(mId)) {
                result.add(cameraInfo);
            }
            LogUtils.d("MyCameraFilter", "result" + result);

        }

        return result;
    }
}
