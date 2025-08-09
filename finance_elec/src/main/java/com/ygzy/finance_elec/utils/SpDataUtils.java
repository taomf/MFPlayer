package com.ygzy.finance_elec.utils;

import com.ygzy.finance_elec.SPConstants;

/**
 * @author : taomf
 * Date    : 2023/8/22/14:44
 * Desc    : 获取sp数据
 */
public class SpDataUtils {

    /**
     * @return 串口路径
     */
    public static String getSerialPath() {
        return SPUtils.getInstance(SPConstants.COMMON).readString(SPConstants.SERIAL_PATH);
    }

    /**
     * 保存串口路径
     */
    public static void saveSerialPath(String serialPath) {
        SPUtils.getInstance(SPConstants.COMMON).write(SPConstants.SERIAL_PATH, serialPath);
    }

    /**
     * @return 摄像头类型
     */
    public static int getCameraType() {
        return SPUtils.getInstance(SPConstants.COMMON).readInt(SPConstants.CAMERA_TYPE,-1);
    }

    /**
     * 保存摄像头类型
     */
    public static void saveCameraType(int cameraType) {
        SPUtils.getInstance(SPConstants.COMMON).write(SPConstants.CAMERA_TYPE, cameraType);
    }

    /**
     * @return 自启动开关状态
     */
    public static Boolean getBootSwitch() {
        return SPUtils.getInstance(SPConstants.BOOT_SWITCH).readBoolean(SPConstants.BOOT_SWITCH,true);
    }

    /**
     * 保存自启动开关状态
     */
    public static void saveBootSwitch(Boolean bootSwitch) {
        SPUtils.getInstance(SPConstants.BOOT_SWITCH).write(SPConstants.BOOT_SWITCH, bootSwitch);
    }

    /**
     * @return 平台地址
     */
    public static String getDeviceUrl() {
        return SPUtils.getInstance(SPConstants.COMMON).readString(SPConstants.DEVICE_URL);
    }

    /**
     * 保存平台地址
     */
    public static void setDeviceUrl(String url) {
        SPUtils.getInstance(SPConstants.COMMON).write(SPConstants.DEVICE_URL, url);
    }
}
