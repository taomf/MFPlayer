package com.ygzy.finance_elec.utils;

import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ygzy.finance_elec.Constant;
import com.ygzy.finance_elec.FinanceWeighModule;
import com.ygzy.finance_elec.WeighData;

import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;


public  class ScaleWeighUtil {
    private SerialHelper serialHelper;
    private WeighCallback callBack;

    private static ScaleWeighUtil instance;

    public interface WeighCallback {
        void onWeighDataReceived(WeighData weighData);
    }

    public void setCallback(WeighCallback callback) {
        this.callBack = callback;
    }

    private ScaleWeighUtil() {}

    public static ScaleWeighUtil getInstance() {
        if (instance == null) {
            instance = new ScaleWeighUtil();
        }
        return instance;
    }

    public  void sendData(byte[] bOutArray){
        serialHelper.send(bOutArray);
    }

    /**
     * 去皮
     */
    public void peeling() {
        if (serialHelper == null || !serialHelper.isOpen()) {
            openSerialPort();
        }
        serialHelper.send(new byte[]{
                (byte) 0xAB,
                0x00,
                0x00,
                0x00,
                0x00,
                (byte) 0x80,
                0x04,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                (byte) 0x12F
        });
    }

    /**
     * 手动去皮
     *
     * @param number 皮重值
     */
    public void peelingWeight(int number) {
        if (serialHelper == null || !serialHelper.isOpen()) {
            openSerialPort();
        }

        if (number > 255) {
            int integerPart = number / 256;
            int remainder = number % 256;
            serialHelper.send(new byte[]{
                    (byte) 0xAB,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    (byte) 0x80,
                    0x05,
                    (byte) integerPart,
                    (byte) remainder,
                    0x00,
                    0x00,
                    0x00,
                    (byte) (304 + integerPart + remainder)
            });
        } else {
            serialHelper.send(new byte[]{
                    (byte) 0xAB,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    (byte) 0x80,
                    0x05,
                    0x00,
                    (byte) number,
                    0x00,
                    0x00,
                    0x00,
                    (byte) (304 + number)
            });
        }
    }

    /**
     * 置零
     */
    public void sendZero() {
        if (serialHelper == null || !serialHelper.isOpen()) {
            openSerialPort();
        }
        serialHelper.send(new byte[]{
                (byte) 0xAB,
                0x00,
                0x00,
                0x00,
                0x00,
                (byte) 0x80,
                0x03,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                (byte) 0x12E
        });
    }

    /**
     * 打开串口
     */
    public void openSerialPort() {
        if (serialHelper == null) {
            if (TextUtils.isEmpty(SpDataUtils.getSerialPath())){
                ToastUtils.showShort( "未获取到设备串口");
                return;
            }
            serialHelper = new SerialHelper(SpDataUtils.getSerialPath(), Constant.newScaleBaudRate) {
                @Override
                public void onDataReceived(ComBean comBean) {
                    WeighData weight = parseData(SerialByteUtil.ByteArrToHex(comBean.bRec));
                    if (callBack != null){
                        ThreadUtils.runOnUiThread(() ->
                                callBack.onWeighDataReceived(weight)
                        );
                    }
                }
            };
        }

        if (!serialHelper.isOpen()) {
            serialHelper.setDataBits(8);
            serialHelper.setStopBits(1);
            serialHelper.setParity(0);
            try {
                serialHelper.open();
            } catch (Exception e) {
                LogUtils.d(FinanceWeighModule.TAG, "打开串口失败");
                ToastUtils.showShort("打开串口失败");
            }
        }
    }


    /**
     * 打开串口
     */
    public void openSerialPort(String path,int port) {
        if (serialHelper == null) {
            serialHelper = new SerialHelper(path, port) {
                @Override
                public void onDataReceived(ComBean comBean) {
                    WeighData weight = parseData(SerialByteUtil.ByteArrToHex(comBean.bRec));
                    if (callBack != null){
                        ThreadUtils.runOnUiThread(() ->
                                callBack.onWeighDataReceived(weight)
                        );
                    }
                }
            };
        }
        if (!serialHelper.isOpen()) {
            try {
                serialHelper.open();
            } catch (Exception e) {
                LogUtils.d(FinanceWeighModule.TAG, "打开串口失败");
                ToastUtils.showShort("打开串口失败");
            }
        }
    }



    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        if (serialHelper != null) {
            serialHelper.close();
        }
    }

    public WeighData parseData(String it) {
        String[] dataList = it.split(" ");
        WeighData weightMessage = null;

        switch (dataList.length) {
            case 31:
                String flag = dataList[13];
                int flagLast = getFlagStatus(Integer.parseInt(flag, 16));
                String netWeight = getWeight(dataList[17], dataList[18] + dataList[19] + dataList[20]);
                String tareWeight = getWeight(dataList[21], dataList[22] + dataList[23] + dataList[24]);
                String grossWeight = getWeight(dataList[25], dataList[26] + dataList[27] + dataList[28]);
                weightMessage = new WeighData(flagLast, netWeight, tareWeight, grossWeight);
                break;
            case 23:
                String flag24 = dataList[13];
                int flagLast24 = getFlagStatus(Integer.parseInt(flag24, 16));
                String netWeight24 = getWeight(dataList[17], dataList[18] + dataList[19] + dataList[20]);
                weightMessage = new WeighData(flagLast24, netWeight24, "0", netWeight24);
                break;
            case 13:
                String back = dataList[6];
                if ("0E".equals(back)){
                    ToastUtils.showShort("成功");
                }else if ("0D".equals(back)){
                    ToastUtils.showShort("失败");
                }
                break;
        }
        return weightMessage;
    }

    /**
     * 获取稳定状态
     * Bit0：0表示重量不稳定		1表示重量稳定
     * Bit1：0表示重量没有溢出		1表示重量溢出
     * Bit2：0表示有开机归零		1表示没有开机归零
     * Bit3：0表示当前重量大于最小称量范围
     * 1表示当前重量小于最小称量范围
     */
    private int getFlagStatus(int data) {
        // 使用按位与操作获取最低位
        return data & 0x1;
    }

    private String getWeight(String isNegative, String weight) {
        long weightValue = Long.parseLong(weight, 16);
        return isNegative.equals("80") ? -weightValue + "" : weightValue + "";
    }

}
