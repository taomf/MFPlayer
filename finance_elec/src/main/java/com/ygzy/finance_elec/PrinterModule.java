package com.ygzy.finance_elec;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.caysn.autoreplyprint.AutoReplyPrint;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.ygzy.finance_elec.utils.ScaleWeighUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class PrinterModule extends UniModule {

    String TAG = "PrinterModule";

    Pointer printer = AutoReplyPrint.INSTANCE.CP_Port_OpenUsb(Constant.lastPrintUSBPort, 1);
    /**
     * 打印起始高度
     */
    public int y = 35;
    /**
     * 打印起始距离
     */
    public int x = 15;
    /**
     * 打印间隔
     */
    public int yh = 30;
    /**
     * 打印横向间隔
     */
    public int xw = 200;

    //打印机初始化
    //run JS thread
    @UniJSMethod (uiThread = false)
    public void printerFunc(JSONObject options, UniJSCallback callback){


        if (!Build.MODEL.equals(Constant.newScale) && !Build.MODEL.equals(Constant.newScale2)){
            int baudRate = Constant.oldPrintBaudRate1;
            if (Build.MODEL.equals(Constant.oldScale2)) baudRate = Constant.oldPrintBaudRate2;
            ScaleWeighUtil.getInstance().openSerialPort(Constant.oldPrintPort,baudRate);
            String printText = print22Code(options);
            try {
                ScaleWeighUtil.getInstance().sendData(printText.getBytes(Constant.charsetName));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return;
        }
        //先判断新旧打印机 ，如果是新的打印机连接打开就用新的，如果旧的打印机连接打开，就用旧的，如果旧的打印机连接失败，那就返回弹窗
        printer = AutoReplyPrint.INSTANCE.CP_Port_OpenUsb(Constant.newPrintUSBPort, 1);
        boolean isLastOpen = AutoReplyPrint.INSTANCE.CP_Port_IsOpened(printer);
        if (!isLastOpen){
            printer = AutoReplyPrint.INSTANCE.CP_Port_OpenUsb(Constant.lastPrintUSBPort, 1);
            boolean isNewOpen = AutoReplyPrint.INSTANCE.CP_Port_IsOpened(printer);
            if(!isNewOpen){
                ToastUtils.showShort("电子秤不支持当前型号打印机，请联系管理员");
                return;
            }
        }
        AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteMode(printer);
        AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteEncoding(printer, AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        //句柄 ，页面起始x坐标，页面起始y坐标，页面宽度，页面高度，页面旋转
        AutoReplyPrint.INSTANCE.CP_Label_PageBegin(printer, 0, 0, 480, 300, 0);
        //设置打印浓度 [0,15]
        AutoReplyPrint.INSTANCE.CP_Pos_SetPrintDensity(printer, 4);
        //句柄 ，x坐标，y坐标，矩形宽度，矩形高度，边框颜色
//        AutoReplyPrint.INSTANCE.CP_Label_DrawBox(h, 0, 0, 384, 240, 1, 1)

        String goodsName = options.getString("goodsName");
        String goodsNo = options.getString("goodsNo");
        Log.e(TAG, "goodsName--"+goodsName);
        Log.e(TAG, "goodsNo--"+goodsNo);
        //处理商品名称长度
        if(goodsName.length()>13){
            goodsName = goodsName.substring(0, 11)+"..";
        }
        //句柄 ，x坐标，y坐标，字体，风格，内容
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 0, 10, 22, 1, goodsName +" " +goodsNo);

        String warehouseName = options.getString("warehouseName");
        String productionDate = options.getString("productionDate");
        Log.e(TAG, "warehouseName--"+warehouseName);
        Log.e(TAG, "productionDate--"+productionDate);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 0, 45, 22, 1, "仓库:"+warehouseName);

        String nw = options.getString("nw");
        if(nw == null){
            nw="无";
        }
        String shelfLifeDuration = options.getString("shelfLifeDuration");
        if(shelfLifeDuration == null){
            shelfLifeDuration="无";
        }
        Log.e(TAG, "nw--"+nw);
        Log.e(TAG, "productionDate--"+productionDate);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 0, 80, 22, 1, "净重kg:"+nw);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 175, 80, 22, 1, "生产日期:"+productionDate);

        String unitName = options.getString("unitName");
        String shelfLifeUnitName = options.getString("shelfLifeUnitName");
        if(shelfLifeUnitName == null){
            shelfLifeUnitName="";
        }
        Log.e(TAG, "unitName--"+unitName);
        Log.e(TAG, "shelfLifeDuration--"+shelfLifeDuration);
        Log.e(TAG, "shelfLifeUnitName--"+shelfLifeUnitName);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 0, 115, 22, 1, "计量单位:"+unitName);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 175, 115, 22, 1, "保质期:"+shelfLifeDuration+shelfLifeUnitName);

        String qty = options.getString("qty");
        String expirationDate = options.getString("expirationDate");
        if(expirationDate == null){
            expirationDate="无";
        }
        Log.e(TAG, "qty--"+qty);
        Log.e(TAG, "expirationDate--"+expirationDate);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 0, 150, 22, 1, "数量:"+qty);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 175, 150, 22, 1, "到期日期:"+expirationDate);

        String unitAmt = options.getString("unitAmt");
        String totalAmt = options.getString("totalAmt");
        Log.e(TAG, "unitAmt--"+unitAmt);
        Log.e(TAG, "totalAmt--"+totalAmt);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 0, 185, 22, 1, "单价:"+unitAmt);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 175, 185, 22, 1, "金额:"+totalAmt);

        String batchNo = options.getString("batchNo");
        Log.e(TAG, "batchNo--"+batchNo);
        AutoReplyPrint.INSTANCE.CP_Label_DrawText(printer, 0, 215, 22, 1, "批次号:"+batchNo);

        String goodsid = options.getString("id");
        Log.e(TAG, "goodsid--"+goodsid);
        AutoReplyPrint.INSTANCE.CP_Label_DrawBarcode(printer, 0, 245, AutoReplyPrint.CP_Label_BarcodeType_CODE128, AutoReplyPrint.CP_Label_BarcodeTextPrintPosition_BelowBarcode, 30, 2, 0, goodsid);

        //，风格，内容
        /**
         * 句柄 ，x坐标，y坐标，
         * 4,指定字符版本。取值范围：[0,16]。 当 version 为 0 时，打印机根据字符串长度自动计算版本号。
         * 5,指定纠错等级。取值范围：[1, 4]
         * ECC 纠错等级
         *   1 L：7%，低纠错，数据多。
         *    2 M：15%，中纠错
         *    3 Q：优化纠错
         *   4 H：30%，最高纠错，数据少。
         * 6定义码块单元宽度。取值范围：[1, 4]。
         * 7 rotation 表示旋转角度。取值范围：[0, 3]。
         * 8 打印的内容
         * **/
//        val retentionDate = mViewModel.sampleRecordResult.value?.find { it.mealId == mViewModel.mealId.value }?.retentionDate.takeUnless { it.isNullOrEmpty() } ?: mViewModel.date.value
//        val mealTime  = getMealTime(mViewModel.retentionTime.value!!)
//        val id  = mViewModel.selectedFood.value?.id
//
//        AutoReplyPrint.INSTANCE.CP_Label_DrawQRCode(h, 0, 50, 0, 4, 4, 0,"$retentionDate,$mealTime,$id")
//        AutoReplyPrint.INSTANCE.CP_Label_DrawBarcode(h, 40, 70, AutoReplyPrint.CP_Label_BarcodeType_CODE128, AutoReplyPrint.CP_Label_BarcodeTextPrintPosition_BelowBarcode, 60, 2, 0, "No.123456")
        boolean status = AutoReplyPrint.INSTANCE.CP_Label_PagePrint(printer, 1);
        LogUtils.d(TAG,"打印状态："+status);

        {
            QueryPrintResult(((Activity)mUniSDKInstance.getContext()), printer);
        }

        AutoReplyPrint.INSTANCE.CP_Port_Close(printer);
    }


    public float lineBreakY(float currentY, Paint paint) {
        return currentY + paint.getFontSpacing() + 3;
    }



    @SuppressLint("SuspiciousIndentation")
    private boolean QueryPrintResult(Activity ctx, Pointer h)
    {
        boolean result = AutoReplyPrint.INSTANCE.CP_Pos_QueryPrintResult(h, 30000);
        Log.i(TAG, result ? "Print Success" : "Print Failed" + result);
        if (!result) {
            LongByReference printer_error_status = new LongByReference();
            LongByReference printer__info_status = new LongByReference();
            LongByReference timestamp_ms_printer_status = new LongByReference();
            if (AutoReplyPrint.INSTANCE.CP_Printer_GetPrinterStatusInfo(h, printer_error_status, printer__info_status, timestamp_ms_printer_status)) {
                AutoReplyPrint.CP_PrinterStatus status = new AutoReplyPrint.CP_PrinterStatus(printer_error_status.getValue(), printer__info_status.getValue());
                String error_status_string = String.format("Printer Error Status: 0x%04X", printer_error_status.getValue() & 0xffff);
                String errmsg = "";
                if (status.ERROR_OCCURED()) {
                    if (status.ERROR_CUTTER())
                        error_status_string += "[ERROR_CUTTER]";
                        errmsg = "ERROR_CUTTER";
                    if (status.ERROR_FLASH())
                        error_status_string += "[ERROR_FLASH]";
                    errmsg = "ERROR_FLASH";
                    if (status.ERROR_NOPAPER()){
                        ToastUtils.showShort("打印机缺纸！");
                        error_status_string += "[ERROR_NOPAPER]";
                        return false;
                    }
                    if (status.ERROR_VOLTAGE())
                        error_status_string += "[ERROR_VOLTAGE]";
                    errmsg = "ERROR_VOLTAGE";
                    if (status.ERROR_MARKER())
                        error_status_string += "[ERROR_MARKER]";
                    errmsg = "ERROR_MARKER";
                    if (status.ERROR_ENGINE())
                        error_status_string += "[ERROR_ENGINE]";
                    errmsg = "ERROR_ENGINE";
                    if (status.ERROR_OVERHEAT())
                        error_status_string += "[ERROR_OVERHEAT]";
                    errmsg = "ERROR_OVERHEAT";
                    if (status.ERROR_COVERUP())
                        error_status_string += "[ERROR_COVERUP]";
                    errmsg = "ERROR_COVERUP";
                    if (status.ERROR_MOTOR())
                        error_status_string += "[ERROR_MOTOR]";
                    errmsg = "ERROR_MOTOR";
                }
                Log.i(TAG,"----->" + errmsg + error_status_string);

                ToastUtils.showShort("请检查打印机状态！"+error_status_string);
            } else {
                Log.i(TAG, "CP_Printer_GetPrinterStatusInfo Failed");
            }
        }
        return result;
    }

    public String printBarTemplate(JSONObject options) {
        y = 10;
        String goodsName = options.getString("goodsName");
        String goodsNo = options.getString("goodsNo");
        //处理商品名称长度
        if(goodsName.length()>13){
            goodsName = goodsName.substring(0, 11)+"..";
        }
        String name = PrintBarText(goodsName +" " +goodsNo, x, y) + "\n";

        String warehouseName = options.getString("warehouseName");
        String warehouse = PrintBarText("仓库:" + warehouseName, x, getY(true)) + "\n";

        String nw = options.getString("nw");
        if(nw == null){
            nw="无";
        }
        String line3L = PrintBarText("净重kg:" + nw , x, getY(true)) +"\n";

        String productionDate = options.getString("productionDate");
        String line3R= PrintBarText("生产日期:" + productionDate , xw, getY(false)) +"\n";

        String unitName = options.getString("unitName");
        String line4L = PrintBarText("计量单位:" + unitName , x , getY(true)) + "\n";
        String shelfLifeDuration = options.getString("shelfLifeDuration");
        if(shelfLifeDuration == null){
            shelfLifeDuration="无";
        }
        String line4R = PrintBarText("保质期:" + shelfLifeDuration , xw , getY(false)) + "\n";

        String qty = options.getString("qty");
        String line5L = PrintBarText("数量:" + qty , x, getY(true)) + "\n";
        String expirationDate = options.getString("expirationDate");
        if(expirationDate == null){
            expirationDate="无";
        }
        String line5R = PrintBarText("到期日期:" + expirationDate , xw, getY(false)) + "\n";

        String unitAmt = options.getString("unitAmt");
        String totalAmt = options.getString("totalAmt");
        String line6L = PrintBarText("单价:" + unitAmt , x, getY(true)) + "\n";
        String line6R = PrintBarText("金额:" + totalAmt, xw, getY(false)) + "\n";

        String batchNo = options.getString("batchNo");
        String batchNoText = PrintBarText("批次号:" + batchNo, x, getY(true)) + "\n";

        String goodsId = options.getString("id");
        String xcode = PrintBarCode(goodsId, x, getY(true)) + "\n";
        return name + warehouse + line3L + line3R +  line4L + line4R +  line5L + line5R + line6L + line6R + batchNoText +  xcode;

    }

    public int getY(boolean isAdd){
        return isAdd ? y += yh : y;
    }

    public String print22Code(JSONObject options) {
        try {
            String tmp = getFromAssets("print.txt");
            return String.format(tmp, printBarTemplate(options), 1);
        } catch (Exception e) {
            // TODO: handle exception
            return "";
        }
    }

    public String PrintBarText(String str, int x, int y) {
        return String.format("TEXT " + x + "," + y + ",\"TSS24.BF2\",0,1,1,\"%s\"", str);
    }

    public String PrintBarCode(String str, int x, int y) {
        return String.format("BARCODE " + x + "," + y + ",\"128\",40,1,0,2,1,\"%s\"", str);
    }

    public String Print2BarCode(String str, int x, int y) {
        str = "=" + str;
        return String.format("QRCODE " + x + "," + y + ",L,3,A,0,\"%s\"", str);
    }

    public String getFromAssets(String fileName) {
        StringBuilder Result = new StringBuilder();
        try {
            InputStreamReader inputReader = new InputStreamReader(mUniSDKInstance.getContext().getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            while ((line = bufReader.readLine()) != null)
                Result.append(line).append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.toString();
    }
}
