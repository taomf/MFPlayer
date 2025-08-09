package com.ygzy.finance_elec;

/**
 * @author : taomf
 * Date    : 2024/12/12/9:58
 * Desc    : 常量
 */
public interface Constant {
    int newScaleBaudRate = 9600;
    String newPrintUSBPort = "VID:0x4B43,PID:0x3538";
    /**
     * 2025/08/08/06  添加新打印机pid
     * ww
     * */
    String lastPrintUSBPort = "VID:0x4B43,PID:0x3830";
    String charsetName = "GB2312";
    String oldPrintPort = "/dev/ttyS1";
    int oldPrintBaudRate1 = 115200;
    int oldPrintBaudRate2 = 9600;

    // 秤类型 Build.MODEL
    /**
     * 经费电子秤
     */
    String newScale = "rk3568_r";
    /**
     * 食安电子秤（中科深信）二代
     */
    String oldScale1 = "sx3566";
    /**
     * 食安电子秤（中科深信）一代
     */
    String oldScale2 = "rk3288";
    /**
     * 经费电子秤 二代
     */
    String newScale2 = "GS-3566-A";

}
